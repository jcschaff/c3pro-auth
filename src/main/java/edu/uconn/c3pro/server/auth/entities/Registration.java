package edu.uconn.c3pro.server.auth.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Registration {
	private boolean sandbox;
	private String receipt_data;
	
	public Registration() {
		
	}

	public Registration(boolean sandbox, String receipt_data) {
		this.sandbox = sandbox;
		this.receipt_data = receipt_data;
	}

	@JsonGetter("sandbox")
	public boolean getSandbox() {
		return sandbox;
	}

	@JsonSetter("sandbox")
	public void setSandbox(boolean sandbox) {
		this.sandbox = sandbox;
	}

	@JsonGetter("receipt-data")
	public String getReceiptData() {
		return receipt_data;
	}

	@JsonSetter("receipt-data")
	public void setReceiptData(String receipt_data) {
		this.receipt_data = receipt_data;
	}

}
