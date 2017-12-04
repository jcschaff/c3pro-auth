package edu.uconn.c3pro.server.auth.services;

public interface AppleReceiptVerifier {
	
	public boolean verifyReceipt(String receipt) throws Exception;

}
