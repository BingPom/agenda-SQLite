package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

	private static Connection conn;
	public static String typeDB = null;

	private DB() {
		try {
			conn = DriverManager.getConnection("", "", "");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {
		if (conn == null) {
			new DB();
		}

		return conn;
	}
}
