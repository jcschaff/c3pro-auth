package edu.uconn.c3pro.server.auth;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import edu.uconn.c3pro.server.auth.controller.AuthController;
import edu.uconn.c3pro.server.auth.controller.RegistrationController;
import edu.uconn.c3pro.server.auth.services.AntispamFilter;
import edu.uconn.c3pro.server.auth.services.AppleReceiptVerifier;
import edu.uconn.c3pro.server.auth.services.AuthDatabase;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class C3ProServerAuthApplicationTests {
	
	@TestConfiguration
	static class C3ProServerAuthApplicationTestsContextConfiguration {
		@Bean
		public AuthDatabase authDatabase() {
			AuthDatabase authDatabase = Mockito.mock(AuthDatabase.class);
			return authDatabase;
		}
		@Bean
		public AppleReceiptVerifier appleReceiptVerifier() {
			AppleReceiptVerifier verifier = Mockito.mock(AppleReceiptVerifier.class);
			return verifier;
		}
	    @Bean
	    public AntispamFilter antispamFilter() {
	    		AntispamFilter antispamFilter = Mockito.mock(AntispamFilter.class);
	    		return antispamFilter;
	    }		
	}
    @Autowired
    private RegistrationController registrationController;

    @Autowired
    private AuthController authController;
    
 	@Test
	public void contextLoads() {
		Assertions.assertThat(registrationController).isNotNull();
		Assertions.assertThat(authController).isNotNull();
	}

}
