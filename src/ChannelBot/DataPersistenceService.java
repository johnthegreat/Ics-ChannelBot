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
package ChannelBot;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class DataPersistenceService {
	private DataPersistenceService() {}
	
	public static void persist(final ChannelBot channelBot) throws SQLException {
		//
		// Persist users
		//
		final Collection<User> userList = channelBot.getUsers();
		for(User user : userList) {
			channelBot.getDatabaseProviderRepository().getUserProvider().createOrUpdateUser(user);
		}
		
		//
		// Persist channels
		//
		final Channel[] channels = channelBot.getChannels();
		for(Channel channel : channels) {
			channelBot.getDatabaseProviderRepository().getChannelProvider().createOrUpdateChannel(channel);

			//
			// Persist channel users
			//
			final List<String> channelMembers = channel.getMembers();
			for(final String username : channelMembers) {
				User user = channelBot.getUser(username);
				if (user != null) {
					channelBot.getDatabaseProviderRepository().getChannelUserProvider().createOrUpdateChannelUser(channel, user);
				}
			}
		}

		//
		// Persist user lists (only ban list is currently supported)
		//
		for(final String bannedUser : channelBot.getBannedUsers()) {
			channelBot.getDatabaseProviderRepository().getUserListProvider().addUserToList("ban", bannedUser);
		}
	}
}
