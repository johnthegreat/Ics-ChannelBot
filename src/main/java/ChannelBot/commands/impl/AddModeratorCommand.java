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

public class AddModeratorCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final ChannelBot channelBot = context.getChannelBot();

		if (context.getChannelNumber() == 0) {
			return;
		}

		String usernameToAdd = context.getArguments();
		if (usernameToAdd.equals("")) {
			channelBot.qtell(context.getUsername(), String.format("%s: Please provide arguments for that command.", ChannelBot.getUsername()));
			return;
		}

		final Channel channel = channelBot.getChannel(context.getChannelNumber());
		if (channel == null) {
			// If channel is null, then there is no channel with this channel number
			channelBot.qtell(context.getUsername(), String.format("%s: Could not find that channel number. Please try again.", ChannelBot.getUsername()));
			return;
		}

		// If the user is the channel's head moderator or is the programmer
		if (!channel.isHeadModerator(context.getUsername()) && !ChannelBot.isUserChannelBotAdministrator(context.getUsername())) {
			channelBot.qtell(context.getUsername(), String.format("%s: You do not have the correct permissions to perform this command.", ChannelBot.getUsername()));
			return;
		}

		if (!Utils.collectionContainsIgnoreCase(channel.getMembers(), usernameToAdd)) {
			channelBot.qtell(context.getUsername(), String.format("%s: That user is not in channel #%s.", ChannelBot.getUsername(), channel.getID()));
			return;
		}

		// If the user is already a moderator
		if (Utils.collectionContainsIgnoreCase(channel.getModerators(), usernameToAdd)) {
			channelBot.qtell(context.getUsername(), String.format("%s: User is already a moderator.", ChannelBot.getUsername()));
			return;
		}

		final User user = channelBot.getUserService().getUser(usernameToAdd);
		if (user != null) {
			usernameToAdd = user.getName();
			channel.addModerator(usernameToAdd);
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": User has been added as a moderator.");
			try {
				channelBot.getDatabaseProviderRepository().getChannelUserProvider().createOrUpdateChannelUser(channel, user);
			} catch (Exception e) {
				ChannelBot.logError(e);
			}
		}
	}
}
