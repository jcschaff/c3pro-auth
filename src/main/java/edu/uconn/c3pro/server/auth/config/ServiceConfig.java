package edu.uconn.c3pro.server.auth.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.uconn.c3pro.server.auth.applestore.AppleReceiptVerifierApi;
import edu.uconn.c3pro.server.auth.database.AuthDatabaseDb;
import edu.uconn.c3pro.server.auth.services.AppleReceiptVerifier;
import edu.uconn.c3pro.server.auth.services.AuthDatabase;
import edu.uconn.c3pro.server.auth.services.CredentialGenerator;
import edu.uconn.c3pro.server.auth.services.DefaultCredentialGenerator;

@Configuration
public class ServiceConfig {
	
	@Autowired
	Environment env;
	
	@Bean
	@Profile("default")
	public AuthDatabase authDatabaseDb() {
		AuthDatabase authDatabase = new AuthDatabaseDb();
		return authDatabase;
	}
	
    @Bean
    @Profile("default")
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
        dataSource.setUrl(env.getProperty("jdbc.url"));
        dataSource.setUsername(env.getProperty("jdbc.user"));
        dataSource.setPassword(env.getProperty("jdbc.pass"));
        return dataSource;
    }
    
	@Bean
	@Profile("default")
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
	
	@Bean
	@Profile("default")
	public AppleReceiptVerifier appleReceiptVerifier() {
		AppleReceiptVerifierApi appleReceiptVerifier = new AppleReceiptVerifierApi();
		return appleReceiptVerifier;
	}
    
	@Bean
	@Profile("default")
	public CredentialGenerator credentialGenerator() {
		DefaultCredentialGenerator credentialGenerator = new DefaultCredentialGenerator();
		return credentialGenerator;
	}
    


}
