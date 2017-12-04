package edu.uconn.c3pro.server.auth.controller;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.uconn.c3pro.server.auth.entities.Registration;
import edu.uconn.c3pro.server.auth.entities.RegistrationResponse;

@Controller
public class RegistrationController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    	
	@Autowired
	DataSource dataSource;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private AppleReceiptVerifier appleReceiptVerifier;
	
	@Autowired
	private AntispamFilter antispamFilter;
	
    private static final String INSERT_USER = "Insert into Users values ('%s', '%s')";
    private static final String INSERT_USER_ROLE = "Insert into UserRoles values ('%s', '%s')";
    private static final String USER_ROLES = "AppUser";

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
    	
    		if (antispam == null || !antispamFilter.isValidAntispamToken(antispam)) {
    			logger.error("antispam token included in requestheader either missing or wrong, antispam = "+antispam); 
    		}
//    		try {
//	    		boolean bReceiptValidated = appleReceiptVerifier.verifyReceipt(registration.getContent());
//	    		if (!bReceiptValidated) {
//	    			logger.warn("failed to validate apple receipt");
//	    			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//	    		}
//    		}catch (Exception e) {
//    			logger.error("failure during apple receipt verification: "+e.getMessage(),e);
//    			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//    		}
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
    		return new ResponseEntity<RegistrationResponse>(registrationResponse, HttpStatus.CREATED);
    }
    
     /**
     * Generates a clientId. In the default implementation, it is a randomly generated UUID
     * @return the new clientId
     */
    private String generateClientId() {
        return UUID.randomUUID().toString();
    }

    private String generatePassword() {
        Random rnd = new SecureRandom();
        byte[] key = new byte[64];
        rnd.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

            
}