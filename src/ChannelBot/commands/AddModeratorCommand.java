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

import ChannelBot.Channel;
import ChannelBot.ChannelBot;
import ChannelBot.Command;
import ChannelBot.User;
import ChannelBot.Utils;

public class AddModeratorCommand extends Command {

	@Override
	public void execute() {
		ChannelBot bot = ChannelBot.getInstance();
		
		if (getChannelNumber() > 0) {
			String usernameToAdd = getArguments();
			
			if (usernameToAdd.equals("")) {
				bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Please provide arguments for that command.");
				return;
			}
			
			Channel channel = bot.getChannel(getChannelNumber());
			if (channel != null) {
				// If the user is the channel's head moderator or is the programmer
				if (channel.isHeadModerator(getUsername()) || getUsername().equals(ChannelBot.programmer)) {
					if (!Utils.listContainsIgnoreCase(channel.getMembers(), usernameToAdd)) {
						bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + String.format(": That user is not in channel #%s.",channel.getID()));
						return;
					}
					
					// If the user is already a moderator
					if (!Utils.listContainsIgnoreCase(channel.getModerators(), usernameToAdd)) {
						User user = bot.getUser(usernameToAdd);
						if (user != null) {
							usernameToAdd = user.getName();
						}
						channel.addModerator(usernameToAdd);
						bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": User has been added as a moderator.");
						try {
							bot.getDatabaseProviderRepository().getChannelUserProvider().createOrUpdateChannelUser(channel, user);
						} catch (Exception e) {
							ChannelBot.logError(e);
						}
					} else {
						bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": User is already a moderator.");
					}
				}
			} else {
				// If channel is null, then there is no channel with this channel number
				bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
			}
		}
	}

}
