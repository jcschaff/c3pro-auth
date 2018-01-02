package edu.uconn.c3pro.server.auth.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import javax.crypto.Cipher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
	
	public static void fixKeyLength() {
	    String errorString = "Failed manually overriding key-length permissions.";
	    int newMaxKeyLength;
	    try {
	        if ((newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES")) < 256) {
	            Class c = Class.forName("javax.crypto.CryptoAllPermissionCollection");
	            Constructor con = c.getDeclaredConstructor();
	            con.setAccessible(true);
	            Object allPermissionCollection = con.newInstance();
	            Field f = c.getDeclaredField("all_allowed");
	            f.setAccessible(true);
	            f.setBoolean(allPermissionCollection, true);

	            c = Class.forName("javax.crypto.CryptoPermissions");
	            con = c.getDeclaredConstructor();
	            con.setAccessible(true);
	            Object allPermissions = con.newInstance();
	            f = c.getDeclaredField("perms");
	            f.setAccessible(true);
	            ((Map) f.get(allPermissions)).put("*", allPermissionCollection);

	            c = Class.forName("javax.crypto.JceSecurityManager");
	            f = c.getDeclaredField("defaultPolicy");
	            f.setAccessible(true);
	            Field mf = Field.class.getDeclaredField("modifiers");
	            mf.setAccessible(true);
	            mf.setInt(f, f.getModifiers() & ~Modifier.FINAL);
	            f.set(null, allPermissions);

	            newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES");
	        }
	    } catch (Exception e) {
	        throw new RuntimeException(errorString, e);
	    }
	    if (newMaxKeyLength < 256)
	        throw new RuntimeException(errorString); // hack failed
	}
	
    @Bean
    @Profile("default")
    public AmazonDynamoDB dynamodb() {
    		fixKeyLength();
        AmazonDynamoDB dynamodb = AmazonDynamoDBClientBuilder
        		.defaultClient();
//        		.standard()
//        		.withRegion("us-east-1")
//        		.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localstack:4569", "us-east-1"))
//        		.build();
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
