package edu.uconn.c3pro.server.auth.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.uconn.c3pro.server.auth.entities.AuthenticationResponse;
import edu.uconn.c3pro.server.auth.services.AntispamFilter;
import edu.uconn.c3pro.server.auth.services.AuthDatabase;

@Controller
public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    	
	@Autowired
	AuthDatabase authDatabase;
	
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
    			@RequestHeader(name="Authorization", required=true) String auth64
//    			@RequestHeader(name="Antispam", required=true) String antispam, 
//    			@RequestHeader(name="grant_type", required=true) String grantType
    			) {
    	
//		if (antispam == null || !antispamFilter.isValidAntispamToken(antispam)) {
//			logger.error("antispam token included in request header either missing or wrong, antispam = "+antispam);
//			throw new IllegalArgumentException("antispam token included in request header either missing or wrong");
//		}
    		if (auth64 == null || !auth64.toLowerCase().startsWith("Basic".toLowerCase())) {
    			logger.error("expecting Basic Authentication in Header, authentication "+auth64); 
    		}
        String [] parts = auth64.split(" ");
        if (parts.length!=2) {
            throw new BadCredentialsException("expecting 2 parts.");
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
        		throw new InternalAuthenticationServiceException("bad encoding of credentials");
        }
        //
        // validate client credentials
        //
        	authDatabase.validateClientCredentials(clientId,clientSecret);
		logger.info("authentication succeeded");
        //
        // create access tokens
        //
        String newToken = this.randomToken();
        Date date = new Date();
        long aux=date.getTime();        
        int timeToLiveSeconds = DEFAULT_SECONDS;
        logger.info("client " + clientId + " authenticated. Access token has been generated");
        Date expirationDate = new Date(aux + (timeToLiveSeconds*ONE_SECOND_IN_MILLIS));
        
        authDatabase.insertUserToken(clientId, newToken, expirationDate);

        AuthenticationResponse authenticationResponse = new AuthenticationResponse(newToken,timeToLiveSeconds);
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