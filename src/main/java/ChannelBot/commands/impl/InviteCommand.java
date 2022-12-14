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

import ChannelBot.Channel;
import ChannelBot.ChannelBot;
import ChannelBot.PatternService;
import ChannelBot.User;
import ChannelBot.commands.Command;
import ChannelBot.commands.context.CommandContext;

public class InviteCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		if (!ChannelBot.isUserChannelBotAdministrator(context.getUsername())) {
			context.getChannelBot().qtell(context.getUsername(),
					ChannelBot.getUsername() + ": Insufficient privileges. You must be the programmer to issue this command.");
		}

		String usernameToInvite = context.getArguments();
		if (usernameToInvite != null && !usernameToInvite.equals("")) {
			if (!PatternService.getInstance().get("^([a-zA-Z]{3,17})$").matcher(usernameToInvite).matches()) {
				context.getChannelBot().qtell(context.getUsername(),
						ChannelBot.getUsername() + ": Please provide a valid username.");
				return;
			}

			final User user = context.getChannelBot().getUserService().getUser(usernameToInvite);
			if (user != null) {
				usernameToInvite = user.getName();
			}

			if (context.getUsername().equals(usernameToInvite)) {
				context.getChannelBot().qtell(context.getUsername(),
						ChannelBot.getUsername() + ": You cannot invite yourself to a channel.");
				return;
			}

			Channel channel = context.getChannelBot().getChannel(context.getChannelNumber());
			if (channel != null) {
				context.getChannelBot().getServerConnection().write(String.format("tell %s %s has invited you to join channel #%s! " +
						"To join, use \"tell ChannelBot join %s\".", usernameToInvite, context.getUsername(), channel.getID(), channel.getID()));
				context.getChannelBot().qtell(context.getUsername(),
						String.format("%s: %s has been invited to channel #%s.", ChannelBot.getUsername(), usernameToInvite, channel.getID()));
			} else {
				context.getChannelBot().qtell(context.getUsername(),
						ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
			}
		} else {
			context.getChannelBot().qtell(context.getUsername(),
					ChannelBot.getUsername() + ": You must specify a username that you want to invite.");
		}
	}

}
