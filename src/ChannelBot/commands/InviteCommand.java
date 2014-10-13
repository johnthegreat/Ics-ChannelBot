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
import ChannelBot.PatternService;
import ChannelBot.User;

public class InviteCommand extends Command {

	@Override
	public void execute() {
		if (getUsername().equals(ChannelBot.programmer)) {
			String usernameToInvite = getArguments();
			if (usernameToInvite != null && !usernameToInvite.equals("")) {
				if (!PatternService.getInstance().get("^([a-zA-Z]{3,17})$").matcher(usernameToInvite).matches()) {
					ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
							ChannelBot.getUsername() + ": Please provide a valid username.");
					return;
				}
				
				User user = ChannelBot.getInstance().getUser(usernameToInvite);
				if (user != null) {
					usernameToInvite = user.getName();
				}
				
				if (getUsername().equals(usernameToInvite)) {
					ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
						ChannelBot.getUsername() + ": You cannot invite yourself to a channel.");
					return;
				}
				
				Channel channel = ChannelBot.getInstance().getChannel(getChannelNumber());
				if (channel != null) {
					ChannelBot.getInstance().getServerConnection().write(String.format("tell %s %s has invited you to join channel #%s! " +
							"To join, use \"tell ChannelBot join %s\".",usernameToInvite,getUsername(),channel.getID(),channel.getID()));
					ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
							String.format("%s: %s has been invited to channel #%s.", ChannelBot.getUsername(),usernameToInvite, channel.getID()));
				} else {
					ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
							ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
				}
			} else {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
						ChannelBot.getUsername() + ": You must specify a username that you want to invite.");
			}
		} else {
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
					ChannelBot.getUsername() + ": Insufficient privileges. You must be the programmer to issue this command.");
		}
	}

}
