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

public class AddPasswordCommand extends Command {

	@Override
	public void execute() {
		ChannelBot bot = ChannelBot.getInstance();
		String newPassword = getArguments();
		
		Channel channel = ChannelBot.getInstance().getChannel(getChannelNumber());
		if (channel != null) {
			if (channel.isHeadModerator(getUsername())) {
				int maximumChannelPasswordLength = Integer.parseInt(ChannelBot.getInstance().getProperties().getProperty("config.channels.maxPasswordLength"));
				if (newPassword.length() <= maximumChannelPasswordLength) {
					channel.addPassword(getUsername(), newPassword);
					bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Channel #" + getChannelNumber() + " Password has been changed to \"" + newPassword + "\".");
				} else {
					bot.getServerConnection().qtell(getUsername(), ChannelBot.getInstance() + ": Channel passwords can only have a maximum length of " + maximumChannelPasswordLength + ".");
				}
			} else {
				bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Insufficient privileges. You must be the Head Moderator to use this command.");
			}
		} else {
			bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
		}
	}

}
