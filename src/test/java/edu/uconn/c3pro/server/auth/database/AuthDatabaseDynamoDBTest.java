package edu.uconn.c3pro.server.auth.database;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;

import edu.uconn.c3pro.server.auth.services.DefaultCredentialGenerator;

public class AuthDatabaseDynamoDBTest {
  	@Test
 	public void test_antispamFilterDynamoDBTest() throws UnsupportedEncodingException {
  		
  		AuthDatabaseDynamoDB authDatabaseDynamoDB = new AuthDatabaseDynamoDB();
  		AmazonDynamoDB dynamodb = AmazonDynamoDBClientBuilder.standard().build();
		authDatabaseDynamoDB.dynamodb = dynamodb;
  		authDatabaseDynamoDB.passwordEncoder = new BCryptPasswordEncoder();
  		
  		ListTablesResult listTablesResult = dynamodb.listTables();
  		for(String tableName : listTablesResult.getTableNames()){
  			System.out.println("table name = "+tableName);
  		}
  		
        DefaultCredentialGenerator credentialGenerator = new DefaultCredentialGenerator();
        
	    	String clientId = credentialGenerator.generateClientId();
	    	String password = credentialGenerator.generatePassword();

        authDatabaseDynamoDB.insertUser(clientId, password);

		String newToken = credentialGenerator.generateJwtBearerToken(clientId);
        Date date = new Date();
        long aux=date.getTime();        
        final long ONE_SECOND_IN_MILLIS = 1000;
        final int DEFAULT_SECONDS = 3600;
        int timeToLiveSeconds = DEFAULT_SECONDS;
        Date expirationDate = new Date(aux + (timeToLiveSeconds*ONE_SECOND_IN_MILLIS));

        authDatabaseDynamoDB.insertUserToken(clientId, newToken, expirationDate);
        
  		authDatabaseDynamoDB.validateClientCredentials(clientId, password);
  	}
		 	

}
