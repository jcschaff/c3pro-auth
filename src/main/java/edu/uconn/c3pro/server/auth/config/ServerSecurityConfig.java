package edu.uconn.c3pro.server.auth.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.uconn.c3pro.server.auth.controller.AntispamFilter;
import edu.uconn.c3pro.server.auth.controller.AppleReceiptVerifier;
import edu.uconn.c3pro.server.auth.controller.AppleReceiptVerifierApi;

@Configuration
public class ServerSecurityConfig extends WebSecurityConfigurerAdapter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Environment env;

	@Autowired
	DataSource dataSource;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	AuthenticationProvider authenticationProvider;
	
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }
 
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() 
      throws Exception {
        return super.authenticationManagerBean();
    }
 
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    		http.csrf().disable();
        http.authorizeRequests()
        .antMatchers("/c3pro/register").permitAll()
        .antMatchers("/c3pro/auth").permitAll()
//        .antMatchers("/c3pro/error").permitAll()
//        .antMatchers("/health").permitAll()
//        .antMatchers("/metrics").permitAll()
//        .antMatchers("/beans").permitAll()
//        .antMatchers("/info").permitAll()
//        .antMatchers("/trace").permitAll()
        .anyRequest().denyAll();
    }
    
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
        dataSource.setUrl(env.getProperty("jdbc.url"));
        dataSource.setUsername(env.getProperty("jdbc.user"));
        dataSource.setPassword(env.getProperty("jdbc.pass"));
        return dataSource;
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
    		AuthenticationProvider provider = new AuthenticationProvider() {
				@Override
				public Authentication authenticate(Authentication authentication) throws AuthenticationException {
					Object credentials = authentication.getCredentials();
					logger.debug("AuthenticationProvider credentials: "+credentials);
					boolean bAuthenticated = true;
					authentication.setAuthenticated(bAuthenticated);
					return authentication;
				}
				@Override
				public boolean supports(Class<?> authentication) {
					logger.debug("do we support authentication type: "+authentication);
					// TODO Auto-generated method stub
					return true;
				}
    		};
    		return provider;
    }
    
	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
	
	@Bean
	public AppleReceiptVerifier appleReceiptVerifier() {
		AppleReceiptVerifierApi appleReceiptVerifier = new AppleReceiptVerifierApi();
		return appleReceiptVerifier;
	}
    
    @Bean
    public AntispamFilter antispamFilter() {
    	   AntispamFilter antispamFilter = new AntispamFilter() {
 
			@Override
			public boolean isValidAntispamToken(String antispamToken) {
	    	        try (Connection con = dataSource.getConnection();
	    	        		Statement stmt = con.createStatement();){
	    	            String query = "SELECT token from antispamtoken";
	
	    	            try (ResultSet rs = stmt.executeQuery(query);){
	    	            		if (rs.next()) {
	    	            			String previouslyEncodedAntispamToken = rs.getString("token");
	    	            			boolean matches = passwordEncoder.matches(antispamToken, previouslyEncodedAntispamToken);
	    	            			return matches;
	    	            		}
	    	            }
	    	            if (logger.isDebugEnabled()) {
	    		    			String typicalEncoding = passwordEncoder.encode(antispamToken);
	    		    			logger.debug("Antispam Token: '"+antispamToken+"' encoding: '"+typicalEncoding+"'");
	    		    			logger.debug("HINT: insert into antispamtoken values ('"+typicalEncoding+"')");
	    	            }
	    	            return false;
				} catch (SQLException e) {
					logger.error("failed to query antispamtoken: "+e.getMessage(), e);
					return false;
				}
			}
    	   };
    	   return antispamFilter;
    }

}