package edu.uconn.c3pro.server.auth.services;

public interface CredentialGenerator {
	String generateClientId();
	String generatePassword();
	String generateRandomBearerToken();
}
