package edu.uconn.c3pro.server.auth.services;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("default")
public class DefaultCredentialGenerator implements CredentialGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    public static final int DEFAULT_TOKEN_SIZE= 64;

	@Override
	public String generateClientId() {
		/**
		 * Generates a clientId. In the default implementation, it is a randomly generated UUID
		 * @return the new clientId
		 */
        return UUID.randomUUID().toString();
    }

	@Override
	public String generatePassword() {
        Random rnd = new SecureRandom();
        byte[] key = new byte[64];
        rnd.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
	}
	
	@Override
    /**
     * Generate a random token that conforms to RFC 6750 Bearer Token
     * @return a new token that is URL Safe (no '+' or '/' characters). */
    public String generateRandomBearerToken() {
        final String TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz._";
        byte[] bytes = new byte[DEFAULT_TOKEN_SIZE];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(DEFAULT_TOKEN_SIZE);
        for (byte b : bytes) {
            sb.append(TOKEN_CHARS.charAt(b & 0x3F));
        }
        return sb.toString();
    }


}
