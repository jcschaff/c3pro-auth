package edu.uconn.c3pro.server.auth.database;

import java.io.UnsupportedEncodingException;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;

public class AntispamFilterDynamoDBTest {

  	@Test
 	public void test_antispamFilterDynamoDBTest() throws UnsupportedEncodingException {
  		final String ANTISPAM_GOOD = "myantispam";
  		final String ANTISPAM_BAD = "badantispam";
  		
  		AntispamFilterDynamoDB antispamFilterDynamodb = new AntispamFilterDynamoDB();
  		AmazonDynamoDB dynamodb = AmazonDynamoDBClientBuilder.standard().build();
		antispamFilterDynamodb.dynamodb = dynamodb;
  		antispamFilterDynamodb.passwordEncoder = new BCryptPasswordEncoder();
  		
  		ListTablesResult listTablesResult = dynamodb.listTables();
  		for(String tableName : listTablesResult.getTableNames()){
  			System.out.println("table name = "+tableName);
  		}
  		
  		Assertions.assertThat(antispamFilterDynamodb.isValidAntispamToken(ANTISPAM_GOOD));
  		
  		Assertions.assertThat(!antispamFilterDynamodb.isValidAntispamToken(ANTISPAM_BAD));
  	}
		 	

}
