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
package ChannelBot.commands;

import java.util.List;

import ChannelBot.ChannelBot;
import ChannelBot.Command;

public class ViewUserListCommand extends Command {

	@Override
	public void execute() {
		if (!getUsername().equals(ChannelBot.programmer)) {
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + 
				": Insufficient privileges. You must be the programmer to issue this command.");
			return;
		}
		
		List<String> userList = null;
		
		String listName = getCommandName().replace("=", "");
		if (listName.equals("ban")) {
			userList = ChannelBot.getInstance().getBannedUsers();
		} else {
			System.err.println("No other lists supported at this time. Only 'ban'.");
			return;
		}
		
		ChannelBot.getInstance().getServerConnection().qtell(getUsername(), buildList(listName, userList));
	}
	
	private String buildList(String listName, List<String> userList) {
		StringBuilder b = new StringBuilder();
		b.append(String.format("%s: Users on List '%s':\\n",ChannelBot.getUsername(),listName));
		for(String user : userList) {
			b.append(user);
			b.append("\\n");
		}
		b.append(String.format("%s user%s listed.",userList.size(),userList.size() > 1 ? "s" : ""));
		return b.toString();
	}

}
