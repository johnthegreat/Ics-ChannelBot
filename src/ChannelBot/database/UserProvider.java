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

import ChannelBot.User;
import ChannelBot.TimeZoneUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserProvider {
    private Connection connection;

    public UserProvider(Connection connection) {
        this.connection = connection;
    }

    public List<User> getUsers() throws SQLException {
        // load list of users from database
        final List<User> userList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement("SELECT username FROM user ORDER BY username ASC")) {
            statement.execute();

            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                User user = getUserByUsername(username);
                userList.add(user);
            }
        }
        return userList;
    }

    public User getUserByUsername(final String username) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT username,v_showTime,v_echo,v_languageFiltered,v_disableToldToString,v_height,v_timeZone FROM user WHERE username = ?")) {
            statement.setString(1, username);
            statement.execute();

            ResultSet resultSet = statement.getResultSet();

            User user = new User();
            user.setName(resultSet.getString("username"));
            user.setShowTime(resultSet.getInt("v_showTime") == 1);
            user.setEcho(resultSet.getInt("v_echo") == 1);
            user.setShowSwearWords(resultSet.getInt("v_languageFiltered") == 1);
            user.setDisableToldToString(resultSet.getInt("v_disableToldToString") == 1);
            user.setHeight(resultSet.getInt("v_height"));

            String timeZone = resultSet.getString("v_timeZone");
            if (timeZone == null || timeZone.isEmpty()) {
                timeZone = "GMT";
            }
            user.setTimeZone(TimeZoneUtils.getTimeZone(timeZone));
            return user;
        }
    }

    public void createOrUpdateUser(User user) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO user (username,v_showTime,v_echo,v_languageFiltered,v_disableToldToString,v_height,v_timeZone) VALUES (?,?,?,?,?,?,?)")) {
            statement.setString(1, user.getName());
            statement.setInt(2, user.isShowTime() ? 1 : 0);
            statement.setInt(3, user.isEcho() ? 1 : 0);
            statement.setInt(4, user.isShowSwearWords() ? 1 : 0);
            statement.setInt(5, user.isDisableToldToString() ? 1 : 0);
            statement.setInt(6, user.getHeight());
            statement.setString(7, TimeZoneUtils.getAbbreviation(user.getTimeZone()));
            statement.execute();
        }
    }

    public void updateUser(User user) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE user SET username = ?, v_showTime = ?, v_echo = ?, v_languageFiltered = ?, v_disableToldToString = ?, v_height = ?, v_timeZone = ? WHERE `username` LIKE ?")) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setInt(2, user.isShowTime() ? 1 : 0);
            preparedStatement.setInt(3, user.isEcho() ? 1 : 0);
            preparedStatement.setInt(4, user.isShowSwearWords() ? 1 : 0);
            preparedStatement.setInt(5, user.isDisableToldToString() ? 1 : 0);
            preparedStatement.setInt(6, user.getHeight());
            preparedStatement.setString(7, TimeZoneUtils.getAbbreviation(user.getTimeZone()));
            preparedStatement.setString(8, user.getName());
        }
    }
}
