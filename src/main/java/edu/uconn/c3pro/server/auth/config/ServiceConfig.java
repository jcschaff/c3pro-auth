package edu.uconn.c3pro.server.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

import edu.uconn.c3pro.server.auth.applestore.AppleReceiptVerifierApi;
import edu.uconn.c3pro.server.auth.services.AppleReceiptVerifier;
import edu.uconn.c3pro.server.auth.services.CredentialGenerator;
import edu.uconn.c3pro.server.auth.services.DefaultCredentialGenerator;

@Configuration
public class ServiceConfig {
	
	@Autowired
	Environment env;
		
    @Bean
    @Profile("default")
    public AmazonDynamoDB dynamodb() {
        AmazonDynamoDB dynamodb = AmazonDynamoDBClientBuilder
        		.standard()
//        		.withRegion("us-east-1")
        		.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localstack:4569", "us-east-1"))
        		.build();
        return dynamodb;
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
