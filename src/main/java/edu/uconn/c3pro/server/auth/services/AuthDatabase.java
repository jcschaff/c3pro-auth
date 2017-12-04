package edu.uconn.c3pro.server.auth.services;

import java.util.Date;

import org.springframework.security.authentication.BadCredentialsException;

public interface AuthDatabase {

	void insertUser(String clientId, String password);

	void validateClientCredentials(String clientId, String clientSecret) throws BadCredentialsException;

	void insertUserToken(String clientId, String newToken, Date expirationDate);

}
