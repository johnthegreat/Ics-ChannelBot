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

import ChannelBot.ChannelBot;
import ChannelBot.Command;
import ChannelBot.User;

public class PoseCommand extends Command {

	@Override
	public void execute() {
		ChannelBot bot = ChannelBot.getInstance();
		
		if (getUsername().equals(ChannelBot.programmer)) {
			System.out.println("The pose command has been issued.");
			
			String args = getArguments();
			if (args != null) {
				int pos = args.indexOf(" ");
				if (pos > -1) {
					String username = args.substring(0, pos);
					String command = args.substring(pos+1);
					User user = bot.getUser(username);
					if (user != null) {
						username = user.getName();
					}
					ChannelBot.parseCommand(username + " tells you: " + command);
				} else {
					bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Please provide arguments for that command.");
				}
			}
			
		} else {
			bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Insufficient privileges. You must be the programmer to issue this command.");
		}
	}

}
