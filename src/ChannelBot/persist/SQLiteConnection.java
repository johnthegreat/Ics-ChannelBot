/**
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2014-2020 John Nahlen
 *     
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ChannelBot.persist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class SQLiteConnection implements DatabaseConnection {
	
	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace(System.err);
		}
	}
	
	private Connection connection;
	
	public SQLiteConnection() {
		
	}
	
	/* (non-Javadoc)
	 * @see ChannelBot.DatabaseConnection#connect(java.lang.String)
	 */
	@Override
	public Connection connect(String databaseFilePath) throws Exception {
		Connection c = DriverManager.getConnection(String.format("jdbc:sqlite:%s",databaseFilePath));
		return c;
	}

	/* (non-Javadoc)
	 * @see ChannelBot.DatabaseConnection#getConnection()
	 */
	@Override
	public Connection getConnection() {
		return connection;
	}

	/* (non-Javadoc)
	 * @see ChannelBot.DatabaseConnection#setConnection(java.sql.Connection)
	 */
	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	/* (non-Javadoc)
	 * @see ChannelBot.DatabaseConnection#execute(java.lang.String)
	 */
	@Override
	public Statement execute(String query) throws SQLException {
		Statement s = connection.createStatement();
		System.out.println("Executed: " + query);
		s.execute(query);
		return s;
	}
	
	public static String escape(String s) {
		if (s == null) {
			return s;
		}
		
		return s.replace("'","''").replace("\\", "\\\\");
	}
}
