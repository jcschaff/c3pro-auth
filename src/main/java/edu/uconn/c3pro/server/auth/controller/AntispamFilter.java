package edu.uconn.c3pro.server.auth.controller;

import java.sql.SQLException;

public interface AntispamFilter {

	public boolean isValidAntispamToken(String antispamToken);
}
