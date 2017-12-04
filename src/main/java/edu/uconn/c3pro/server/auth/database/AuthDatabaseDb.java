package edu.uconn.c3pro.server.auth.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.stereotype.Service;

import edu.uconn.c3pro.server.auth.entities.AuthenticationResponse;
import edu.uconn.c3pro.server.auth.services.AuthDatabase;

@Service
@Profile("default")
public class AuthDatabaseDb implements AuthDatabase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	DataSource dataSource;
	
	@Autowired
	PasswordEncoder passwordEncoder;

    private static final String INSERT_USER = "Insert into Users values ('%s', '%s')";
    private static final String INSERT_USER_ROLE = "Insert into UserRoles values ('%s', '%s')";
    private static final String USER_ROLES = "AppUser";
	
	@Override
	public void insertUser(String clientId, String password) {
        try (Connection conn = dataSource.getConnection();
        		Statement stmt = conn.createStatement();){

            	// At this point the request is authorized. We generate the credentials
            	String encPassword = passwordEncoder.encode(password);
            	String insert = String.format(INSERT_USER,clientId, encPassword);
            	String insertRoles = String.format(INSERT_USER_ROLE, clientId, USER_ROLES);

            	stmt.execute(insert);
            stmt.execute(insertRoles);
        }catch (SQLException e) {
        		logger.error("failed to insert new registration", e);
        		throw new RuntimeException("invalid request");
        }
	}

	@Override
	public boolean validateClientCredentials(String clientId, String clientSecret) throws UnauthorizedClientException {
        try (Connection con = dataSource.getConnection();
        		Statement stmt = con.createStatement();){
        		ResultSet rs = stmt.executeQuery("select count(*) from users where clientid = '"+clientId+"' and clientsecret = '"+clientSecret+"'");
        		if (rs.next()) {
        			logger.info("authentication succeeded");
        			return true;
        		}else {
        			logger.info("authenticated failed, clientid/clientsecret not found");
        			throw new UnauthorizedClientException("bad client credentials");
        		}
        }catch (SQLException e) {
        		logger.error("SQLException while authenticating: "+e.getMessage(),e);
        		throw new UnauthorizedClientException("bad client credentials");
        }
	}

	@Override
	public void insertUserToken(String clientId, String newToken, Date expirationDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        String dateStr = dateFormat.format(expirationDate);

        String insertSQL = String.format("Insert into UserTokens values('%s', '%s', to_timestamp('%s', '%s'))",
                clientId, newToken, dateStr, "YYYYMMDD-HH24:MI:SS");

        try (Connection con = dataSource.getConnection();
        		Statement stmt = con.createStatement();){
        		
        		int count = stmt.executeUpdate(insertSQL);
        		if (count!=1) {
        			logger.error("failed to save user token");
        			throw new UnauthorizedClientException("failed to save user token");
        		}else {
        			logger.info("inserted token into database");
        		}
        } catch (SQLException e) {
			e.printStackTrace();
			logger.error("failed to insert access token: "+e.getMessage(),e);
			throw new UnauthorizedClientException("failed to insert access token",e);
		}
	}

}
