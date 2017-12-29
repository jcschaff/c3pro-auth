package edu.uconn.c3pro.server.auth.database;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Set;
import java.util.zip.CRC32;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.BadCredentialsException;
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
import com.amazonaws.util.DateUtils;

import edu.uconn.c3pro.server.auth.services.AuthDatabase;

@Service
@Profile("default")
public class AuthDatabaseDynamoDB implements AuthDatabase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	AmazonDynamoDB dynamodb;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@DynamoDBTable(tableName="c3pro_user")
	public static class C3pro_user {
	    private Long id;
	    private String clientid;
	    private String clientsecret;
	    private Set<String> roles;
	 
	    // Not encrypted because it is a hash key    
	    @DynamoDBHashKey(attributeName="Id")  
	    public Long getId() { return id;}
	    public void setId(Long id) {this.id = id;}
	 
	    // Encrypted by default
	    @DynamoDBAttribute(attributeName="Clientid")  
	    public String getClientid() {return clientid; }
	    public void setClientid(String clientid) { this.clientid = clientid; }
	 
	    // Encrypted by default
	    @DynamoDBAttribute(attributeName="Clientsecret")  
	    public String getClientsecret() {return clientsecret; }
	    public void setClientsecret(String clientsecret) { this.clientsecret = clientsecret; }
	 
	    // Encrypted by default
	    @DynamoDBAttribute(attributeName = "Roles")
	    public Set<String> getRoles() { return roles; }
	    public void setRoles(Set<String> roles) { this.roles = roles; }
	    
	    static long calculateId(String clientId) {
			CRC32 crc = new CRC32();
			crc.update(clientId.getBytes(Charset.forName("utf-8")));
			long crcValue = crc.getValue();
			return crcValue;
	    }
	}
	
	@DynamoDBTable(tableName="c3pro_token")
	public static class C3pro_token {
	    private Long id;
	    private String clientid;
	    private String token;
	    private String date_ISO8601;
	 
	    // Not encrypted because it is a hash key    
	    @DynamoDBHashKey(attributeName="Id")  
	    public Long getId() { return id;}
	    public void setId(Long id) {this.id = id;}
	 
	    // Encrypted by default
	    @DynamoDBAttribute(attributeName="Clientid")  
	    public String getClientid() {return clientid; }
	    public void setClientid(String clientid) { this.clientid = clientid; }
	 
	    // Encrypted by default
	    @DynamoDBAttribute(attributeName="Token")  
	    public String getToken() {return token; }
	    public void setToken(String token) { this.token = token; }
	 
	    // Encrypted by default
	    @DynamoDBAttribute(attributeName = "Datestring")
	    public String getDate_ISO8601() { return date_ISO8601; }
	    public void setDate_ISO8601(String date_ISO8601) { this.date_ISO8601 = date_ISO8601; }
	    
	    static long calculateId(String clientId) {
			CRC32 crc = new CRC32();
			crc.update(clientId.getBytes(Charset.forName("utf-8")));
			long crcValue = crc.getValue();
			return crcValue;
	    }
	}
	
	private DynamoDBMapper getDynamoDBMapper() {
		AWSKMS kms = AWSKMSClientBuilder.defaultClient();
		String encryptionKeyId = "49f7001f-840a-44c8-8d57-58553ae3c671"; // alias is "c3pro_dynamodb_encrypt"
	    EncryptionMaterialsProvider provider = new DirectKmsMaterialProvider(kms,encryptionKeyId);        
	    DynamoDBMapper mapper = new DynamoDBMapper(dynamodb, DynamoDBMapperConfig.DEFAULT,new AttributeEncryptor(provider));
		return mapper;
	}

	@Override
	public void insertUser(String clientId, String password) {
		C3pro_user user = new C3pro_user();
		long id = C3pro_user.calculateId(clientId);
		user.setId(id);
		user.setClientid(clientId);
		String encPassword = passwordEncoder.encode(password);
		user.setClientsecret(encPassword);
		
		DynamoDBMapper mapper = getDynamoDBMapper();
		mapper.save(user);
	}

	@Override
	public void validateClientCredentials(String clientId, String clientSecret) throws BadCredentialsException {
		DynamoDBMapper mapper = getDynamoDBMapper();
		C3pro_user user = mapper.load(C3pro_user.class, C3pro_user.calculateId(clientId));
		String encPassword = passwordEncoder.encode(clientSecret);
		if (user.getClientsecret().equals(encPassword)){
			logger.info("authentication succeeded");
		} else {			
			logger.info("authenticated failed, clientid/clientsecret not found");
			throw new BadCredentialsException("bad client credentials");
		}
	}

	@Override
	public void insertUserToken(String clientId, String newToken, Date expirationDate) {
		String dateStr = DateUtils.formatISO8601Date(expirationDate);

		C3pro_token token = new C3pro_token();
		token.setId(C3pro_token.calculateId(clientId));
		token.setClientid(clientId);
		token.setToken(newToken);
		token.setDate_ISO8601(dateStr);
		
		DynamoDBMapper mapper = getDynamoDBMapper();
		mapper.save(token);
		logger.info("inserted token into database");
	}

}
