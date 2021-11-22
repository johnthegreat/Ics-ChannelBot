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

import ChannelBot.Channel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChannelProvider {
    private Connection connection;

    public ChannelProvider(Connection connection) {
        this.connection = connection;
    }

    public List<Channel> getChannels() throws SQLException {
        // load list of channels from database
        List<Channel> channelList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM channel ORDER BY num ASC")) {
            statement.execute();
            ResultSet resultSet = statement.getResultSet();
            while(resultSet.next()) {
                Channel channel = createChannelFromResultSet(resultSet);
                channelList.add(channel);
            }
        }
        return channelList;
    }

    protected Channel createChannelFromResultSet(ResultSet resultSet) throws SQLException {
        Channel channel = new Channel();
        channel.setID(resultSet.getInt("num"));
        channel.setName(resultSet.getString("name"));
        channel.setPassword(resultSet.getString("password"));
        channel.setLastTellTime(resultSet.getLong("lastTellTime"));
        populateChannelUsersFromDb(channel);
        channel.addChannelChangedEventListener(Channel.channelChangedEventListener);
        return channel;
    }

    protected void populateChannelUsersFromDb(Channel channel) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT username,moderator FROM channel_user WHERE channel = ?")) {
            preparedStatement.setInt(1, channel.getID());
            preparedStatement.execute();

            ResultSet resultSet = preparedStatement.getResultSet();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                channel.getMembers().add(username);
                String moderatorLevel = resultSet.getString("moderator");
                if (moderatorLevel != null) {
                    if (moderatorLevel.equals("head")) {
                        channel.setHeadModerator(username);
                        channel.addModerator(username);
                    } else if (moderatorLevel.equals("normal")) {
                        channel.addModerator(username);
                    }
                }
            }
        }
    }

    public void deleteChannel(int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM channel WHERE num = ?")) {
            statement.setInt(1, id);
            statement.execute();
        }
    }

    public void createOrUpdateChannel(Channel channel) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT OR REPLACE INTO channel (num,name,password,lastTellTime) VALUES (?,?,?,?)")) {
            preparedStatement.setInt(1, channel.getID());
            preparedStatement.setString(2, channel.getName());
            preparedStatement.setString(3, channel.getPassword());
            preparedStatement.setLong(4, channel.getLastTellTime());
            preparedStatement.execute();
        }
    }

    public void updateChannel(Channel channel) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE channel SET `name` = ?, `password` = ?, `lastTellTime` = ? WHERE `num` = ?")) {
            preparedStatement.setString(1, channel.getName());
            preparedStatement.setString(2, channel.getPassword());
            preparedStatement.setLong(3, channel.getLastTellTime());
            preparedStatement.setInt(4, channel.getID());
            preparedStatement.execute();
        }
    }
}
