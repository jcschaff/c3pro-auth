package edu.uconn.c3pro.server.auth.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jca.endpoint.GenericMessageEndpointManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.uconn.c3pro.server.auth.entities.AuthenticationResponse;
import edu.uconn.c3pro.server.auth.entities.Registration;
import edu.uconn.c3pro.server.auth.entities.RegistrationResponse;

@Controller
public class RegistrationController {
    private static String BASIC_AUTH = "Basic";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    	
	@Autowired
	DataSource dataSource;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
    public static final long ONE_SECOND_IN_MILLIS = 1000;
    public static final int DEFAULT_TOKEN_SIZE= 64;
    public static final int DEFAULT_SECONDS = 3600;

    
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz._";

    protected static final String JSON_TAG_SANDBOX = "sandbox";
    protected static final String JSON_TAG_RECEIPT = "receipt-data";

    protected static final String INSERT_USER = "Insert into Users values ('%s', '%s')";
    protected static final String INSERT_USER_ROLE = "Insert into UserRoles values ('%s', '%s')";
    protected static final String USER_ROLES = "AppUser";

    protected static final String APPLE_JSON_KEY_STATUS = "status";
    protected static final String APPLE_JSON_KEY_RECEIPT = "receipt";
    protected static final String APPLE_JSON_KEY_RECEIPT_BID = "bid";
    protected static final String APPLE_JSON_KEY_BUNDLE = "bundle_id";
	final static ArrayList<Registration> registrations = new ArrayList<Registration>();
	final static ArrayList<RegistrationResponse> registrationResponses = new ArrayList<RegistrationResponse>();
	protected static final String JSON_REQUEST_APPLE =
            "{\n" +
            "  \"" + JSON_TAG_RECEIPT + "\":\"%s\" "+
            "}";

	public static class AppConfig {
		public final static String APP_IOS_ID="dklsdlfkjsd_id_dljsdflkjd";
		public final static String APP_IOS_VERIF_ENDPOINT="endpoint real";
		public final static String APP_IOS_VERIF_TEST_ENDPOINT="endpoint_fake";
		
		public static String getProp(String appIosVerifEndpoint) {
			// TODO Auto-generated method stub
			return null;
		}
	}
 
	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
	
	/**
	 * Registration request:
	 * 
	 *     HTTP/1.1 POST /c3pro/register
	 *     HTTP/1.1 Header Antispam: {{in-app-stored secret}}
	 *     {
	 *     “sandbox”: true/false,
	 *     “receipt-data”: {{your apple-supplied app purchase receipt}}
	 *     }
	 * 
	 * Registration response:
	 * 
	 *     HTTP/1.1 201 Created
	 *     Content-Type: application/json
	 *     {
	 *     "client_id":"{{some opaque client id}}",
	 *     "client_secret": "{{some high-entropy client secret}}",
	 *     "grant_types": ["client_credentials"],
	 *     "token_endpoint_auth_method":"client_secret_basic",
	 *     }
	 * 
	 */
    @RequestMapping(method = RequestMethod.POST, value = "/c3pro/register", produces="application/json")
    public ResponseEntity<RegistrationResponse> register(
    			@RequestHeader(name="Antispam", required=true) String antispam, 
    			@RequestBody Registration registration) {
    	
    		if (antispam == null || !passFilter(antispam)) {
    			logger.error("antispam token included in requestheader either missing or wrong, antispam = "+antispam); 
    		}
        try (Connection conn = dataSource.getConnection();
        		Statement stmt = conn.createStatement();){

            	// At this point the request is authorized. We generate the credentials
            	String clientId = generateClientId();
            	String password = generatePassword();
            	String encPassword = passwordEncoder.encode(password);
            	String insert = String.format(INSERT_USER,clientId, encPassword);
            	String insertRoles = String.format(INSERT_USER_ROLE, clientId, USER_ROLES);

            	stmt.execute(insert);
            stmt.execute(insertRoles);
        }catch (SQLException e) {
        		logger.error("failed to insert new registration", e);
        		throw new RuntimeException("invalid request");
        }
    		RegistrationResponse registrationResponse = new RegistrationResponse(generateClientId(),generatePassword());
    		return new ResponseEntity<RegistrationResponse>(registrationResponse, HttpStatus.CREATED );
    }
    
    /**
     * OAuth2 Authentication request:
     * 
     *     HTTP/1.1 POST /c3pro/oauth?grant_type=client_credentials
     *     Authentication: Basic BASE64(ClientId:Secret)
     *     
     * OAuth2 Authentication response:
     * 
     *     HTTP/1.1 201 Created
     *     Content-Type: application/json
     *     {
     *     "access_token":"{{some token}}",
     *     "expires_in": "{{seconds to expiration}}",
     *     "token_type": "bearer"
     *     } 
     * 
     */
    @RequestMapping(method = RequestMethod.POST, value = "/c3pro/auth", produces="application/json")
    public ResponseEntity<AuthenticationResponse> authenticate(
    			@RequestHeader(name="Authorization", required=true) String auth64, 
    			@RequestHeader(name="Antispam", required=true) String antispam, 
    			@RequestParam(name="grant_type", required=true) String grantType) {
    	
		if (antispam == null || !passFilter(antispam)) {
			logger.error("antispam token included in requestheader either missing or wrong, antispam = "+antispam); 
		}
    		if (auth64 == null || !auth64.startsWith("Basic")) {
    			logger.error("expecting Basic Authentication in Header, authentication "+auth64); 
    		}
        if (!isBasicAuth(auth64)) {
            throw new UnauthorizedClientException("expecting Basic Authentication");
        }
        String [] parts = auth64.split(" ");
        if (parts.length!=2) {
            throw new UnauthorizedClientException("expecting 2 parts.");
        }
        final String clientId;
        final String clientSecret;
        try {
            byte [] authBytes = Base64.getDecoder().decode(parts[1]);
            String auth = new String(authBytes, "UTF-8");
            auth = URLDecoder.decode(auth, "UTF-8");
            String []cred = auth.split(":");
            clientId = cred[0];
            clientSecret = cred[1];
        }catch (UnsupportedEncodingException e) {
        		throw new UnauthorizedClientException("bad encoding of credentials");
        }
        //
        // validate client credentials
        //
        try (Connection con = dataSource.getConnection();
        		Statement stmt = con.createStatement();){
        		ResultSet rs = stmt.executeQuery("select count(*) from users where clientid = '"+clientId+"' and clientsecret = '"+clientSecret+"'");
        		if (rs.next()) {
        			logger.info("authentication succeeded");
        		}else {
        			logger.info("authenticated failed, clientid/clientsecret not found");
        			throw new UnauthorizedClientException("bad client credentials");
        		}
        }catch (SQLException e) {
        		logger.error("SQLException while authenticating: "+e.getMessage(),e);
        		throw new UnauthorizedClientException("bad client credentials");
        }
        //
        // create access tokens
        //
        String newToken = this.randomToken();
        Date date = new Date();
        long aux=date.getTime();
        
        int timeToLiveSeconds = DEFAULT_SECONDS;
        AuthenticationResponse authenticationResponse = new AuthenticationResponse(newToken,timeToLiveSeconds);
        logger.info("client " + clientId + " authenticated. Access token has been generated");

        Date expirationDate = new Date(aux + (timeToLiveSeconds*ONE_SECOND_IN_MILLIS));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        String dateStr = dateFormat.format(expirationDate);

        String insertSQL = String.format("Insert into UserTokens values('%s', '%s', to_timestamp('%s', '%s'))",
                clientId, newToken, dateStr, "YYYYMMDD-HH24:MI:SS");

        try (Connection con = dataSource.getConnection();
        		Statement stmt = con.createStatement();){
        		
        		int count = stmt.executeUpdate(insertSQL);
        		if (count!=1) {
        			return new ResponseEntity<AuthenticationResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        		}else {
        			logger.info("inserted token into database");
        		}
        } catch (SQLException e) {
			e.printStackTrace();
			logger.error("failed to insert access token: "+e.getMessage(),e);
			return new ResponseEntity<AuthenticationResponse>(HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.ok()
				.cacheControl(CacheControl.noCache())
				.cacheControl(CacheControl.noStore())
				.body(authenticationResponse);
    }
    
    protected boolean isBasicAuth(String header) {
        if (header == null) return false;

        // if its not a Basic header we deny the access
        if (header.length() < BASIC_AUTH.length()) return false;
        String pre = header.substring(0,BASIC_AUTH.length());
        if (!pre.toLowerCase().equals(BASIC_AUTH.toLowerCase())) return false;
        return true;
    }
    /**
     * Generates a clientId. In the default implementation, it is a randomly generated UUID
     * @return the new clientId
     */
    protected String generateClientId() {
        return UUID.randomUUID().toString();
    }

    protected String generatePassword() {
        Random rnd = new SecureRandom();
        byte[] key = new byte[64];
        rnd.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    protected int validateAppleReceipt(String receipt, String urlStr) throws Exception {
        String jsonReq = String.format(JSON_REQUEST_APPLE, receipt);
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-type", "application/json");
        con.setRequestProperty("Content-Length", Integer.toString(jsonReq.getBytes().length));
        con.getOutputStream().write(jsonReq.getBytes());
        con.getOutputStream().flush();
        con.getOutputStream().close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
        String line=null;
        StringBuilder sb = new StringBuilder();
        while((line = in.readLine())!= null) {
            sb.append(line);
        }
        con.getInputStream().close();

        JSONObject jsonRet = new JSONObject(sb.toString());
        int status = jsonRet.getInt(APPLE_JSON_KEY_STATUS);
        boolean ret = false;
        if (status == 0) {
            JSONObject receiptJSON = jsonRet.getJSONObject(APPLE_JSON_KEY_RECEIPT);
            String bid=null;
            try {
                bid = receiptJSON.getString(APPLE_JSON_KEY_BUNDLE);
                ret = bid.trim().toLowerCase().equals(AppConfig.getProp(AppConfig.APP_IOS_ID).trim().toLowerCase());
                if (ret) status = 0;
            } catch (JSONException e) {
                logger.warn(APPLE_JSON_KEY_BUNDLE + " json field not found");
                ret = AppConfig.getProp(AppConfig.APP_IOS_VERIF_ENDPOINT).contains("sandbox");
                if (ret) status=0;
            }
            if (ret) {
                logger.info("Receipt validated against Apple servers");
            } else {
                logger.warn("Receipt status 0, but iOS app id not valid:" + bid);
            }
        } else {
            logger.info("Apple receipt status:" + status);
        }
        return status;
    }

    /**
     * Performs a validation of the receipt to Apple servers
     * @param receipt
     * @return
     */
    protected boolean validateAppleReceipt(String receipt) throws Exception {
        logger.info("Validating Apple Receipt");
        logger.info(receipt);
        if (receipt.equals("NO-APP-RECEIPT")) return true;
        int status = validateAppleReceipt(receipt, AppConfig.getProp(AppConfig.APP_IOS_VERIF_ENDPOINT));
        System.out.println("Returned code: " + status);
        if (status == 21007) {
            // It means we have a receipt from a test environment
            status = validateAppleReceipt(receipt, AppConfig.getProp(AppConfig.APP_IOS_VERIF_TEST_ENDPOINT));
            System.out.println("Returned code: " + status);
        }
        return (status==0);
    }
    
    /**
     * Performs an antispam filter
     * @param request The request from the servlet
     * @return true if passes the filter. False otherwise
     * @throws Exception
     */
    protected boolean passFilter(String antispamToken) {
        try (Connection con = dataSource.getConnection();
        		Statement stmt = con.createStatement();){
            String query = "SELECT token from antispamtoken";

            try (ResultSet rs = stmt.executeQuery(query);){
            		if (rs.next()) {
            			String previouslyEncodedAntispamToken = rs.getString("token");
            			boolean matches = passwordEncoder.matches(antispamToken, previouslyEncodedAntispamToken);
            			return matches;
            		}
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
       
    /**
     * Generate a random token that conforms to RFC 6750 Bearer Token
     * @return a new token that is URL Safe (no '+' or '/' characters). */
    protected String randomToken() {
        byte[] bytes = new byte[DEFAULT_TOKEN_SIZE];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(DEFAULT_TOKEN_SIZE);
        for (byte b : bytes) {
            sb.append(TOKEN_CHARS.charAt(b & 0x3F));
        }
        return sb.toString();
    }


}