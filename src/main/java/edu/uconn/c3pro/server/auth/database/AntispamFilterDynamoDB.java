package edu.uconn.c3pro.server.auth.database;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

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
	    private String id;
	    private String antispam;
	 
	    // Not encrypted because it is a hash key    
	    @DynamoDBHashKey(attributeName="id")  
	    public String getId() { return id;}
	    public void setId(String id) {this.id = id;}
	 
	    // Encrypted by default
	    @DynamoDBAttribute(attributeName="antispam")  
	    public String getAntispam() {return antispam; }
	    public void setAntispam(String antispam) { this.antispam = antispam; }
	    
	    static long calculateId() {
			return 0l;
	    }
	}
	
	private DynamoDBMapper getDynamoDBMapper() {
//		AWSKMS kms = AWSKMSClientBuilder.standard().build();
//		String encryptionKeyId = "49f7001f-840a-44c8-8d57-58553ae3c671"; // alias is "c3pro_dynamodb_encrypt"
//	    EncryptionMaterialsProvider provider = new DirectKmsMaterialProvider(kms,encryptionKeyId);        
//	    DynamoDBMapper mapper = new DynamoDBMapper(dynamodb, DynamoDBMapperConfig.DEFAULT,new AttributeEncryptor(provider));
		DynamoDBMapper mapper = new DynamoDBMapper(dynamodb, DynamoDBMapperConfig.DEFAULT);
		return mapper;
	}

	@Override
	public boolean isValidAntispamToken(String antispamToken) {
		DynamoDBMapper mapper = getDynamoDBMapper();
		C3pro_antispam antispam_key = new C3pro_antispam();
		antispam_key.setId("0");
		DynamoDBQueryExpression<C3pro_antispam> queryExpression = new DynamoDBQueryExpression<C3pro_antispam>()
				.withHashKeyValues(antispam_key);
		
		List<C3pro_antispam> antispam_list = mapper.query(C3pro_antispam.class, queryExpression);
		if (antispam_list.isEmpty()) {
			logger.error("antispam record not found");
			return false;
		}
		for (C3pro_antispam antispam : antispam_list) {
			if (antispam.getAntispam().equals(antispamToken)) {
				logger.info("antispam token matched");
				return true;
			}
		}
		logger.error("antispam token doesn't match");
		return false;
	}

}
