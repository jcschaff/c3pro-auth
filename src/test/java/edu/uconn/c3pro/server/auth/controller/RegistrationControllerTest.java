package edu.uconn.c3pro.server.auth.controller;

import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import edu.uconn.c3pro.server.auth.entities.Registration;
import edu.uconn.c3pro.server.auth.entities.RegistrationResponse;
import edu.uconn.c3pro.server.auth.services.AntispamFilter;
import edu.uconn.c3pro.server.auth.services.AppleReceiptVerifier;
import edu.uconn.c3pro.server.auth.services.AuthDatabase;
import edu.uconn.c3pro.server.auth.services.CredentialGenerator;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class RegistrationControllerTest {

  	@Test
 	public void test_registration() throws Exception {
  		final String ANTISPAM_BAD = "bad-anti-spam";
  		final String ANTISPAM_GOOD = "good-anti-spam";
  		final String CLIENT_ID = "myclientid";
  		final String CLIENT_SECRET = "myclientsecret";
  		final String[] GRANT_TYPES = { "client_credentials" };
  		final String TOKEN_ENDPOINT_AUTH_METHOD = "client_secret_basic";
  		final Registration REGISTRATION_GOOD = new Registration(true,"good registration data");
  		final Registration REGISTRATION_BAD = new Registration(true,"bad registration data");
  		
  		CredentialGenerator credentialGenerator = Mockito.mock(CredentialGenerator.class);
 		Mockito.when(credentialGenerator.generateClientId()).thenReturn(CLIENT_ID);
 		Mockito.when(credentialGenerator.generatePassword()).thenReturn(CLIENT_SECRET);
 		
  		AntispamFilter antispamFilter = Mockito.mock(AntispamFilter.class);
 		Mockito.when(antispamFilter.isValidAntispamToken(ANTISPAM_BAD)).thenReturn(false);
 		Mockito.when(antispamFilter.isValidAntispamToken(ANTISPAM_GOOD)).thenReturn(true);
 		
 		AppleReceiptVerifier appleReceiptVerifier = Mockito.mock(AppleReceiptVerifier.class);
 		Mockito.when(appleReceiptVerifier.verifyReceipt(REGISTRATION_GOOD.getReceiptData())).thenReturn(true);
 		Mockito.when(appleReceiptVerifier.verifyReceipt(REGISTRATION_BAD.getReceiptData())).thenReturn(false);

		AuthDatabase authDatabase = Mockito.mock(AuthDatabase.class);

 		RegistrationController regController = new RegistrationController();
 		regController.antispamFilter = antispamFilter;
 		regController.appleReceiptVerifier = appleReceiptVerifier;
 		regController.credentialGenerator = credentialGenerator;
 		regController.authDatabase = authDatabase;
 		
 		try {
 			regController.register(ANTISPAM_BAD, REGISTRATION_GOOD);
 			Assertions.failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
 		}catch (IllegalArgumentException e) { 
 		}catch (Throwable e2) { 
 			e2.printStackTrace();
 			Assertions.failBecauseExceptionWasNotThrown(IllegalArgumentException.class); 
 		}
 		
 		try {
 			regController.register(ANTISPAM_BAD, REGISTRATION_BAD);
 			Assertions.failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
 		}catch (IllegalArgumentException e) { 
 		}
 		
 		ResponseEntity<RegistrationResponse> response = regController.register(ANTISPAM_GOOD, REGISTRATION_GOOD);
 		Assertions.assertThat(response.getStatusCode()==HttpStatus.CREATED);
 		Assertions.assertThat(response.getBody().getClient_id().equals(CLIENT_ID));
 		Assertions.assertThat(response.getBody().getClient_secret().equals(CLIENT_SECRET));
 		Assertions.assertThat(response.getBody().getToken_endpoint_auth_method().equals(TOKEN_ENDPOINT_AUTH_METHOD));
 		Assertions.assertThat(Arrays.equals(response.getBody().getGrant_types(), GRANT_TYPES));
 	}
  	
}
