package edu.uconn.c3pro.server.auth.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.uconn.c3pro.server.auth.services.AntispamFilter;

@Service
@Profile("default")
public class AntispamFilterDb implements AntispamFilter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DataSource dataSource;

	@Autowired
	private PasswordEncoder passwordEncoder;

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

}
