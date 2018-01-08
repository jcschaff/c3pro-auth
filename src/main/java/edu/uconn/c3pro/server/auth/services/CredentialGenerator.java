package edu.uconn.c3pro.server.auth.services;

import java.util.Date;

public interface CredentialGenerator {
	String generateClientId();
	String generatePassword();
	String generateJwtBearerToken(String clientId);
}
