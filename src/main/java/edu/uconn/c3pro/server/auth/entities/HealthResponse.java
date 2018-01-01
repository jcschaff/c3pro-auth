package edu.uconn.c3pro.server.auth.entities;

public class HealthResponse {
	private String status;
	private String timestamp;
	
	public HealthResponse(String status, String timestamp) {
		super();
		this.status = status;
		this.timestamp = timestamp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}
