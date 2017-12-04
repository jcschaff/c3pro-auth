package edu.uconn.c3pro.server.auth.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@Configuration
public class ServerSecurityConfig extends WebSecurityConfigurerAdapter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	AuthenticationProvider authenticationProvider;
	
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }
 
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    		http.csrf().disable();
        http.authorizeRequests()
        .antMatchers("/c3pro/register").permitAll()
        .antMatchers("/c3pro/auth").permitAll()
        .anyRequest().denyAll();
    }
    
    /**
     * for the Auth server, the built-in AuthenticationProvider is disabled, rather the Auth and Registration Controllers 
     * use the Antispam filter to discard some invalid requests, and explicitly perform the client authentication 
     * in the RegistrationController and the client authorization in the AuthController
     * @return
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
    		AuthenticationProvider provider = new AuthenticationProvider() {
				@Override
				public Authentication authenticate(Authentication authentication) throws AuthenticationException {
					Object credentials = authentication.getCredentials();
					logger.debug("AuthenticationProvider credentials: "+credentials);
					boolean bAuthenticated = false;
					authentication.setAuthenticated(bAuthenticated);
					return authentication;
				}
				@Override
				public boolean supports(Class<?> authentication) {
					logger.debug("do we support authentication type: "+authentication);
					return false;
				}
    		};
    		return provider;
    }
    
 
}