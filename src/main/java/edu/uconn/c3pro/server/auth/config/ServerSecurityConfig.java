package edu.uconn.c3pro.server.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class ServerSecurityConfig extends WebSecurityConfigurerAdapter {
 
	
	/**
	 * A quick note here is that the form login configuration isn’t necessary for the Password flow
	 *  – only for the Implicit flow – so you may be able to skip it depending on what OAuth2 flow you’re using.
	 *  (from http://www.baeldung.com/rest-api-spring-oauth2-angularjs)
	 */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) 
      throws Exception {
        auth.inMemoryAuthentication()
          .withUser("john").password("123").roles("USER");
    }
 
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() 
      throws Exception {
        return super.authenticationManagerBean();
    }
 
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/c3pro/register").permitAll()
            .anyRequest().authenticated();
    }
}