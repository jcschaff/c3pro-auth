package edu.uconn.c3pro.server.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.uconn.c3pro.server.auth.entities.Registration;
import edu.uconn.c3pro.server.auth.entities.RegistrationResponse;
import edu.uconn.c3pro.server.auth.services.AntispamFilter;
import edu.uconn.c3pro.server.auth.services.AppleReceiptVerifier;
import edu.uconn.c3pro.server.auth.services.AuthDatabase;
import edu.uconn.c3pro.server.auth.services.CredentialGenerator;

@Controller
public class RegistrationController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    	
//	@Autowired
//	DataSource dataSource;
    
    @Autowired
    AuthDatabase authDatabase;
		
	@Autowired
	AppleReceiptVerifier appleReceiptVerifier;
	
	@Autowired 
	AntispamFilter antispamFilter;
	
	@Autowired
	CredentialGenerator credentialGenerator;
	

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
    			logger.error("antispam token included in request header either missing or wrong, antispam = "+antispam); 
    			throw new IllegalArgumentException("antispam token included in request header either missing or wrong");
    		}
    		try {
	    		boolean bReceiptValidated = appleReceiptVerifier.verifyReceipt(registration.getReceiptData());
	    		if (!bReceiptValidated) {
	    			logger.warn("failed to validate apple receipt");
	    			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	    		}
    		}catch (Exception e) {
    			logger.error("failure during apple receipt verification: "+e.getMessage(),e);
    			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    		}
        	// At this point the request is authorized. We generate the credentials
        	String clientId = credentialGenerator.generateClientId();
        	String password = credentialGenerator.generatePassword();
        	authDatabase.insertUser(clientId,password);
        	
    		RegistrationResponse registrationResponse = new RegistrationResponse(clientId,password);
    		return new ResponseEntity<RegistrationResponse>(registrationResponse, HttpStatus.CREATED);
    }
    
            
}