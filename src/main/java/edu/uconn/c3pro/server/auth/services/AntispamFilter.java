package edu.uconn.c3pro.server.auth.services;

public interface AntispamFilter {

	public boolean isValidAntispamToken(String antispamToken);
}
