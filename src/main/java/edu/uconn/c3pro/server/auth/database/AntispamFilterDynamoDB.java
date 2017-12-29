package edu.uconn.c3pro.server.auth.database;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.zip.CRC32;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.AttributeEncryptor;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.encryption.providers.DirectKmsMaterialProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.encryption.providers.EncryptionMaterialsProvider;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;

import edu.uconn.c3pro.server.auth.database.AuthDatabaseDynamoDB.C3pro_user;
import edu.uconn.c3pro.server.auth.services.AntispamFilter;

@Service
@Profile("default")
public class AntispamFilterDynamoDB implements AntispamFilter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	AmazonDynamoDB dynamodb;
	
	@Autowired
	PasswordEncoder passwordEncoder;

	
	@DynamoDBTable(tableName="c3pro_antispam")
	public static class C3pro_antispam {
	    private Long id;
	    private String antispam;
	 
	    // Not encrypted because it is a hash key    
	    @DynamoDBHashKey(attributeName="Id")  
	    public Long getId() { return id;}
	    public void setId(Long id) {this.id = id;}
	 
	    // Encrypted by default
	    @DynamoDBAttribute(attributeName="Antispam")  
	    public String getAntispam() {return antispam; }
	    public void setAntispam(String antispam) { this.antispam = antispam; }
	    
	    static long calculateId() {
			return 0l;
	    }
	}
	
	private DynamoDBMapper getDynamoDBMapper() {
//		AWSKMS kms = AWSKMSClientBuilder.defaultClient();
//		String encryptionKeyId = "49f7001f-840a-44c8-8d57-58553ae3c671"; // alias is "c3pro_dynamodb_encrypt"
//	    EncryptionMaterialsProvider provider = new DirectKmsMaterialProvider(kms,encryptionKeyId);        
//	    DynamoDBMapper mapper = new DynamoDBMapper(dynamodb, DynamoDBMapperConfig.DEFAULT,new AttributeEncryptor(provider));
		DynamoDBMapper mapper = new DynamoDBMapper(dynamodb, DynamoDBMapperConfig.DEFAULT);
		return mapper;
	}

	@Override
	public boolean isValidAntispamToken(String antispamToken) {
		DynamoDBMapper mapper = getDynamoDBMapper();
		C3pro_antispam antispam = mapper.load(C3pro_antispam.class, 0);
		if (antispam != null) {
			if (antispamToken.equals(antispam.antispam)) {
				logger.info("antispam matched");
				return true;
			} else {
				logger.error("antispam filter did not match");
				return false;
			}
		} else {
			logger.error("antispam record not found");
			return false;
		}
	}

}
