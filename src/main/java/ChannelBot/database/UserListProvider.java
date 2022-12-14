/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2021 John Nahlen
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
package ChannelBot.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UserListProvider {
	private final Connection connection;

	public UserListProvider(Connection connection) {
		this.connection = connection;
	}

	public List<String> getUserListNames() throws SQLException {
		try (final PreparedStatement statement = connection.prepareStatement(
				"SELECT DISTINCT listname FROM userlist ORDER BY listname")) {
			statement.execute();
			final ResultSet resultSet = statement.getResultSet();

			final List<String> listNames = new ArrayList<>();
			while (resultSet.next()) {
				listNames.add(resultSet.getString("listname"));
			}
			return listNames;
		}
	}

	public Map<String, Set<String>> getUserLists() throws SQLException {
		List<String> listNames = getUserListNames();
		Map<String, Set<String>> userListMap = new HashMap<>();
		for (String listName : listNames) {
			Set<String> set = getUserListByName(listName);
			userListMap.put(listName, set);
		}
		return userListMap;
	}

	public Set<String> getUserListByName(final String listName) throws SQLException {
		try (final PreparedStatement statement = connection.prepareStatement(
				"SELECT username FROM userlist WHERE listname = ?")) {
			statement.setString(1, listName);
			statement.execute();

			final ResultSet resultSet = statement.getResultSet();
			final Set<String> list = new TreeSet<>();
			while (resultSet.next()) {
				list.add(resultSet.getString("username"));
			}
			return list;
		}
	}

	public void addUserToList(final String listName, final String username) throws SQLException {
		try (final PreparedStatement statement = connection.prepareStatement(
				"INSERT OR REPLACE INTO userlist (listname,username) VALUES (?,?)")) {
			statement.setString(1, listName);
			statement.setString(2, username);
			statement.execute();
		}
	}

	public void removeUserFromList(final String listName, final String username) throws SQLException {
		try (final PreparedStatement statement = connection.prepareStatement(
				"DELETE FROM userlist WHERE listname = ? AND username = ?")) {
			statement.setString(1, listName);
			statement.setString(2, username);
			statement.execute();
		}
	}
}
