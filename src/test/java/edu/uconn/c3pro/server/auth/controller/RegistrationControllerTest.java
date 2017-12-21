package edu.uconn.c3pro.server.auth.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import edu.uconn.c3pro.server.auth.entities.AuthenticationResponse;
import edu.uconn.c3pro.server.auth.services.AuthDatabase;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class RegistrationControllerTest {
	 	  	
  	@Test
 	public void test_authController() throws UnsupportedEncodingException {
  		final String CLIENT_ID = "myclientid";
  		final String CLIENT_SECRET_GOOD = "myclientsecret";
  		final String CLIENT_SECRET_BAD = "myclientsecret_BAD";
  		
  		final String urlEncodedClientId_good = URLEncoder.encode(CLIENT_ID,"UTF-8");
		final String urlEncodedClientSecret_good = URLEncoder.encode(CLIENT_SECRET_GOOD,"UTF-8");
		final String clientToken_good = Base64.getEncoder().encodeToString(new String(urlEncodedClientId_good+":"+urlEncodedClientSecret_good).getBytes("UTF-8"));
		
  		final String urlEncodedClientId_bad = URLEncoder.encode(CLIENT_ID,"UTF-8");
		final String urlEncodedClientSecret_bad = URLEncoder.encode(CLIENT_SECRET_BAD,"UTF-8");
		final String clientToken_bad = Base64.getEncoder().encodeToString(new String(urlEncodedClientId_bad+":"+urlEncodedClientSecret_bad).getBytes("UTF-8"));
 
		AuthDatabase authDatabase = Mockito.mock(AuthDatabase.class);
		Mockito.doThrow(BadCredentialsException.class).when(authDatabase).validateClientCredentials(CLIENT_ID, CLIENT_SECRET_BAD);
		
  		AuthController authController = new AuthController();
  		authController.authDatabase = authDatabase;
  		
  		try {
  			ResponseEntity<AuthenticationResponse> response = authController.authenticate("Basic "+clientToken_bad); 	
  			Assertions.failBecauseExceptionWasNotThrown(BadCredentialsException.class);
  		}catch (BadCredentialsException e) {
  		}

 		ResponseEntity<AuthenticationResponse> response = authController.authenticate("Basic "+clientToken_good); 		
 		Assertions.assertThat(response.getStatusCode()==HttpStatus.ACCEPTED);
 		Assertions.assertThat(response.getBody().getAccess_token().equals("abc"));
 	}
}
