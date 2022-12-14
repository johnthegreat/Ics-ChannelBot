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
import ChannelBot.commands.Command;
import ChannelBot.commands.context.CommandContext;

import java.sql.SQLException;
import java.util.Collection;

public class RemoveUserFromListCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		if (!ChannelBot.isUserChannelBotAdministrator(context.getUsername())) {
			context.getChannelBot().qtell(context.getUsername(), ChannelBot.getUsername() +
					": Insufficient privileges. You must be the programmer to issue this command.");
			return;
		}

		String usernameToRemove = context.getArguments();
		User user = context.getChannelBot().getUserService().getUser(usernameToRemove);
		if (user != null) {
			usernameToRemove = user.getName();
		}

		Collection<String> userCollection;

		String listName = context.getCommandName().replace("-", "");
		if (listName.equals("ban")) {
			userCollection = context.getChannelBot().getBannedUsers();
		} else {
			System.err.println("No other lists supported at this time. Only 'ban'.");
			return;
		}

		if (userCollection.contains(usernameToRemove)) {
			userCollection.remove(usernameToRemove);
			context.getChannelBot().qtell(context.getUsername(), ChannelBot.getUsername() +
					String.format(": User \"%s\" has been removed from the %s list.", usernameToRemove, listName));
			try {
				context.getChannelBot().getDatabaseProviderRepository().getUserListProvider().removeUserFromList(listName, usernameToRemove);
			} catch (SQLException e) {
				ChannelBot.logError(e);
			}
		} else {
			context.getChannelBot().qtell(context.getUsername(), ChannelBot.getUsername() +
					String.format(": User \"%s\" is not on the %s list.", usernameToRemove, listName));
		}
	}

}
