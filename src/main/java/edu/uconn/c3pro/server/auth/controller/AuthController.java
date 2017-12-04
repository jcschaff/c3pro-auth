package edu.uconn.c3pro.server.auth.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.uconn.c3pro.server.auth.entities.AuthenticationResponse;

@Controller
public class AuthController {
    private static String BASIC_AUTH = "Basic";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    	
	@Autowired
	DataSource dataSource;
	
	@Autowired
	private AntispamFilter antispamFilter;
	
    public static final long ONE_SECOND_IN_MILLIS = 1000;
    public static final int DEFAULT_TOKEN_SIZE= 64;
    public static final int DEFAULT_SECONDS = 3600;

    
    private static final SecureRandom secureRandom = new SecureRandom();


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
    	
		if (antispam == null || !antispamFilter.isValidAntispamToken(antispam)) {
			logger.error("antispam token included in requestheader either missing or wrong, antispam = "+antispam); 
		}
    		if (auth64 == null || !auth64.toLowerCase().startsWith("Basic".toLowerCase())) {
    			logger.error("expecting Basic Authentication in Header, authentication "+auth64); 
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
       
    /**
     * Generate a random token that conforms to RFC 6750 Bearer Token
     * @return a new token that is URL Safe (no '+' or '/' characters). */
    protected String randomToken() {
        final String TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz._";
        byte[] bytes = new byte[DEFAULT_TOKEN_SIZE];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(DEFAULT_TOKEN_SIZE);
        for (byte b : bytes) {
            sb.append(TOKEN_CHARS.charAt(b & 0x3F));
        }
        return sb.toString();
    }


}