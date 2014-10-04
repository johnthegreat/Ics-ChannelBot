/**
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
package ChannelBot.commands;

import java.sql.SQLException;
import java.util.List;

import ChannelBot.ChannelBot;
import ChannelBot.Command;
import ChannelBot.User;
import ChannelBot.Utils;

public class AddUserToListCommand extends Command {

	@Override
	public void execute() {
		if (!getUsername().equals(ChannelBot.programmer)) {
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + 
				": Insufficient privileges. You must be the programmer to issue this command.");
			return;
		}
		
		String usernameToAdd = getArguments();
		User user = ChannelBot.getInstance().getUser(usernameToAdd);
		if (user != null) {
			usernameToAdd = user.getName();
		}
		
		List<String> userList = null;
		
		String listName = getCommandName().replace("+", "");
		if (listName.equals("ban")) {
			if (usernameToAdd.equals(ChannelBot.programmer)) {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + 
					": You cannot add the programmer to the banned list.");
				return;
			}
			
			userList = ChannelBot.getInstance().getBannedUsers();
		} else {
			System.err.println("No other lists supported at this time. Only 'ban'.");
			return;
		}
		
		if (Utils.listContainsIgnoreCase(userList, usernameToAdd)) {
			// this user is already on the ban list
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + 
				String.format(": User \"%s\" is already on the %s list.",usernameToAdd,listName));
		} else {
			userList.add(usernameToAdd);
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + 
				String.format(": User \"%s\" has been added to the %s list.",usernameToAdd,listName));
			try {
				ChannelBot.getInstance().getPersistanceProvider().addToUserListDb(listName, usernameToAdd);
			} catch (SQLException e) {
				ChannelBot.logError(e);
			}
		}
	}
}
