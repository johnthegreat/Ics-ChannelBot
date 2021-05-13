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

import ChannelBot.ChannelBot;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserListProvider {
    public List<String> getUserListNames() throws SQLException {
        try (PreparedStatement statement = ChannelBot.getInstance().getDatabaseConnection().prepareStatement(
                "SELECT DISTINCT listname FROM userlist ORDER BY listname")) {
            statement.execute();
            ResultSet resultSet = statement.getResultSet();

            List<String> listNames = new ArrayList<>();
            while (resultSet.next()) {
                listNames.add(resultSet.getString("listname"));
            }
            return listNames;
        }
    }

    public Map<String,List<String>> getUserLists() throws SQLException {
        List<String> listNames = getUserListNames();
        Map<String,List<String>> userListMap = new HashMap<>();
        for(String listName : listNames) {
            List<String> list = getUserListByName(listName);
            userListMap.put(listName, list);
        }
        return userListMap;
    }

    public List<String> getUserListByName(String listName) throws SQLException {
        try (PreparedStatement statement = ChannelBot.getInstance().getDatabaseConnection().prepareStatement(
                "SELECT username FROM userlist WHERE listname = ?")) {
            statement.setString(1, listName);
            statement.execute();

            ResultSet resultSet = statement.getResultSet();
            List<String> list = new ArrayList<>();
            while(resultSet.next()) {
                list.add(resultSet.getString("username"));
            }
            return list;
        }
    }

    public void addUserToList(String listName,String username) throws SQLException {
        try (PreparedStatement statement = ChannelBot.getInstance().getDatabaseConnection().prepareStatement(
                "INSERT OR REPLACE INTO userlist (listname,username) VALUES (?,?)")) {
            statement.setString(1, listName);
            statement.setString(2, username);
            statement.execute();
        }
    }

    public void removeUserFromList(String listName, String username) throws SQLException {
        try (PreparedStatement statement = ChannelBot.getInstance().getDatabaseConnection().prepareStatement(
                "DELETE FROM userlist WHERE listname = ? AND username = ?")) {
            statement.setString(1, listName);
            statement.setString(2, username);
            statement.execute();
        }
    }
}
