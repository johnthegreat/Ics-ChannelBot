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
import ChannelBot.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChannelUserProvider {
	private final Connection connection;

	public ChannelUserProvider(Connection connection) {
		this.connection = connection;
	}

	public void createOrUpdateChannelUser(final Channel channel, final User user) throws SQLException {
		String moderatorLevel = null;
		if (channel.isHeadModerator(user.getName())) {
			moderatorLevel = "head";
		} else if (channel.isModerator(user.getName())) {
			moderatorLevel = "normal";
		}

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"INSERT OR REPLACE INTO channel_user (channel,username,moderator) VALUES (?,?,?)")) {
			preparedStatement.setInt(1, channel.getID());
			preparedStatement.setString(2, user.getName());
			preparedStatement.setString(3, moderatorLevel);
			preparedStatement.execute();
		}
	}

	public void deleteChannelUser(final Channel channel, final User user) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"DELETE FROM channel_user WHERE channel = ? AND username = ?")) {
			preparedStatement.setInt(1, channel.getID());
			preparedStatement.setString(2, user.getName());
			preparedStatement.execute();
		}
	}

	public void deleteChannelUsers(final int channelId) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"DELETE FROM channel_user WHERE channel = ?")) {
			preparedStatement.setInt(1, channelId);
			preparedStatement.execute();
		}
	}
}
