package edu.uconn.c3pro.server.auth.entities;

public class RegistrationResponse {
	private String client_id;
	private String client_secret;
	private final String[] grant_types = { "client_credentials" };
	private final String token_endpoint_auth_method = "client_secret_basic";
	
	public RegistrationResponse(String client_id, String client_secret) {
		super();
		this.client_id = client_id;
		this.client_secret = client_secret;
	}

	public String getClient_id() {
		return client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	public String getClient_secret() {
		return client_secret;
	}

	public void setClient_secret(String client_secret) {
		this.client_secret = client_secret;
	}

	public String[] getGrant_types() {
		return grant_types;
	}

	public String getToken_endpoint_auth_method() {
		return token_endpoint_auth_method;
	}


}
