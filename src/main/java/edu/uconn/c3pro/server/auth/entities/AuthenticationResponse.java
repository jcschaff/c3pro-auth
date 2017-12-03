package edu.uconn.c3pro.server.auth.entities;

/**
 * 
 *     HTTP/1.1 201 Created
 *     Content-Type: application/json
 *     {
 *     "access_token":"{{some token}}",
 *     "expires_in": "{{seconds to expiration}}",
 *     "token_type": "bearer"
 *     } 
 *     
 */
public class AuthenticationResponse {
	String access_token;
	long expires_in;
	String token_type;
	
	public AuthenticationResponse(String access_token, long expires_in) {
		this.access_token = access_token;
		this.expires_in = expires_in;
		this.token_type = "bearer";
	}
	
	
	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public long getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(long expires_in) {
		this.expires_in = expires_in;
	}

	public String getToken_type() {
		return token_type;
	}

	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}


}
