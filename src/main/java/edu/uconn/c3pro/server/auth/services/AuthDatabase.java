package edu.uconn.c3pro.server.auth.services;

import java.util.Date;

public interface AuthDatabase {

	void insertUser(String clientId, String password);

	boolean validateClientCredentials(String clientId, String clientSecret);

	void insertUserToken(String clientId, String newToken, Date expirationDate);

}
