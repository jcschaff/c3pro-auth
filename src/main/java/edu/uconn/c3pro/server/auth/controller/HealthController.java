package edu.uconn.c3pro.server.auth.controller;

import java.security.SecureRandom;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.uconn.c3pro.server.auth.entities.HealthResponse;
import edu.uconn.c3pro.server.auth.services.AuthDatabase;
import edu.uconn.c3pro.server.auth.services.CredentialGenerator;

@Controller
public class HealthController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    	
	@Autowired
	AuthDatabase authDatabase;
	
	@Autowired
	CredentialGenerator credentialGenerator;
	
    public static final long ONE_SECOND_IN_MILLIS = 1000;
    public static final int DEFAULT_SECONDS = 3600;

    
    private static final SecureRandom secureRandom = new SecureRandom();


    /**
     * OAuth2 Authentication request:
     * 
     *     HTTP/1.1 GET /health
     *     
     * OAuth2 Authentication response:
     * 
     *     HTTP/1.1 200 OK
     *     Content-Type: application/json
     *     {
     *     "hello":"world",
     *     } 
     * 
     */
    @RequestMapping(method = RequestMethod.GET, value = "/health", produces="application/json")
    public ResponseEntity<HealthResponse> health() {
    		HealthResponse healthResponse = new HealthResponse("my-status",Calendar.getInstance().getTime().toGMTString());
		return ResponseEntity.ok()
				.cacheControl(CacheControl.noCache())
				.cacheControl(CacheControl.noStore())
				.body(healthResponse);
    }
    
}