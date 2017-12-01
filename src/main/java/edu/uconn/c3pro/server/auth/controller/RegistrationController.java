package edu.uconn.c3pro.server.auth.controller;

import java.util.ArrayList;

import org.apache.commons.logging.impl.SLF4JLogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    	
	final static ArrayList<Registration> registrations = new ArrayList<Registration>();
	final static ArrayList<RegistrationResponse> registrationResponses = new ArrayList<RegistrationResponse>();
 
	/**
	 * Registration request:
	 * 
	 * HTTP/1.1 POST /c3pro/register
	 * HTTP/1.1 Header Antispam: {{in-app-stored secret}}
	 * {
	 * “sandbox”: true/false,
	 * “receipt-data”: {{your apple-supplied app purchase receipt}}
	 * }
	 * 
	 * Registration response:
	 * 
	 * HTTP/1.1 201 Created
	 * Content-Type: application/json
	 * {
	 * "client_id":"{{some opaque client id}}",
	 * "client_secret": "{{some high-entropy client secret}}",
	 * "grant_types": ["client_credentials"],
	 * "token_endpoint_auth_method":"client_secret_basic",
	 * }
	 * 
	 * 
	 */
    @RequestMapping(method = RequestMethod.POST, value = "/c3pro/register")
    public RegistrationResponse register(@RequestHeader(name="Antispam", required=true) String antispam, @RequestBody Registration registration) {
    		if (antispam == null || !antispam.equals("MY-ANTI-SPAM")) {
    			logger.error("antispam token included in requestheader either missing or wrong, antispam = "+antispam); 
    		}
    		registrations.add(registration);
    		RegistrationResponse registrationResponse = new RegistrationResponse("clientId_from_registration","clientsecret_from_registration");
    		return registrationResponse;
    }
}