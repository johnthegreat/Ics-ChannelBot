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
import ChannelBot.User;
import ChannelBot.Utils;
import ChannelBot.commands.Command;
import ChannelBot.commands.context.CommandContext;

public class RemoveUserCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final ChannelBot channelBot = context.getChannelBot();
		String usernameToRemove = context.getArguments();

		final Channel channel = channelBot.getChannel(context.getChannelNumber());
		if (channel == null) {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
			return;
		}

		if (usernameToRemove.equals("")) {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Please provide arguments for that command.");
			return;
		}

		final User user = channelBot.getUserService().getUser(usernameToRemove);
		if (user != null) {
			usernameToRemove = user.getName();
		}

		if (ChannelBot.isUserChannelBotAdministrator(usernameToRemove)) {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": You can't boot the programmer out of a channel!");
			return;
		}

		if (!Utils.collectionContainsIgnoreCase(channel.getModerators(), context.getUsername())) {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Insufficient privileges. You must be a channel moderator to use this command.");
			return;
		}

		if (!Utils.collectionContainsIgnoreCase(channel.getMembers(), usernameToRemove)) {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": That person is not in channel #" + channel.getID() + ".");
			return;
		}

		if (channel.isModerator(context.getUsername()) && !channel.isHeadModerator(context.getUsername()) && channel.isModerator(usernameToRemove)) {
			// This is a moderator trying to remove another moderator. This cannot be allowed.
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Insufficient privileges. You must be the head moderator to remove another channel moderator.");
		}

		channel.kick(context.getUsername(), usernameToRemove);
	}

}
