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

}
