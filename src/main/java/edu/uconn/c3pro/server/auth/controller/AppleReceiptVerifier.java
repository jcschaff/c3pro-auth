package edu.uconn.c3pro.server.auth.controller;

public interface AppleReceiptVerifier {
	
	public boolean verifyReceipt(String receipt) throws Exception;

}
