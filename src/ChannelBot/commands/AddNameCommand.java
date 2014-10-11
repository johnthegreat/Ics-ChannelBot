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
 
import ChannelBot.Channel;
import ChannelBot.ChannelBot;
import ChannelBot.Command;

public class AddNameCommand extends Command {

	@Override
	public void execute() {
		ChannelBot bot = ChannelBot.getInstance();
		String newName = getArguments();
		
		Channel ch = ChannelBot.getInstance().getChannel(getChannelNumber());
		if (ch != null) {
			if (ch.isModerator(getUsername()) || ChannelBot.programmer.equals(getUsername())) {
				int maximumChannelNameLength = Integer.parseInt(ChannelBot.getInstance().getProperties().getProperty("config.channels.maxNameLength"));
				if (newName.length() <= maximumChannelNameLength) {
					ch.setName(newName);
					bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Channel #" + getChannelNumber() + " Name has been changed to \"" + newName + "\".");
				} else {
					bot.getServerConnection().qtell(getUsername(), ChannelBot.getInstance() + ": Channel names can only have a maximum length of " + maximumChannelNameLength + ".");
				}
			} else {
				bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Insufficient privileges. You must be a channel moderator to use this command.");
			}
		} else {
			bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
		}
	}

}
