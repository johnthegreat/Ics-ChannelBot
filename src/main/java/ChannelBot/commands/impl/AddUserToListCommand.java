/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2014 John Nahlen
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
package ChannelBot.commands.impl;

import ChannelBot.ChannelBot;
import ChannelBot.User;
import ChannelBot.Utils;
import ChannelBot.commands.Command;
import ChannelBot.commands.context.CommandContext;

import java.sql.SQLException;
import java.util.Collection;

public class AddUserToListCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		ChannelBot channelBot = context.getChannelBot();

		if (!ChannelBot.isUserChannelBotAdministrator(context.getUsername())) {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() +
					": Insufficient privileges. You must be the programmer to issue this command.");
			return;
		}

		String usernameToAdd = context.getArguments();
		User user = channelBot.getUserService().getUser(usernameToAdd);
		if (user != null) {
			usernameToAdd = user.getName();
		}

		Collection<String> userCollection;

		final String listName = context.getCommandName().replace("+", "");
		if (listName.equals("ban")) {
			if (ChannelBot.isUserChannelBotAdministrator(usernameToAdd)) {
				channelBot.qtell(context.getUsername(), String.format("%s: You cannot add the programmer to the banned list.", ChannelBot.getUsername()));
				return;
			}

			userCollection = channelBot.getBannedUsers();
		} else {
			System.err.println("No other lists supported at this time. Only 'ban'.");
			return;
		}

		if (Utils.collectionContainsIgnoreCase(userCollection, usernameToAdd)) {
			// this user is already on the ban list
			channelBot.qtell(context.getUsername(), String.format("%s: User \"%s\" is already on the %s list.", ChannelBot.getUsername(), usernameToAdd, listName));
		} else {
			userCollection.add(usernameToAdd);
			channelBot.qtell(context.getUsername(), String.format("%s: User \"%s\" has been added to the %s list.", ChannelBot.getUsername(), usernameToAdd, listName));
			try {
				channelBot.getDatabaseProviderRepository().getUserListProvider().addUserToList(listName, usernameToAdd);
			} catch (SQLException e) {
				ChannelBot.logError(e);
			}
		}
	}
}
