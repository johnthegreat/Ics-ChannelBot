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

public class RemoveModeratorCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final ChannelBot channelBot = context.getChannelBot();

		if (context.getChannelNumber() > 0) {
			final String usernameToRemove = context.getArguments();

			final Channel channel = channelBot.getChannel(context.getChannelNumber());
			if (channel == null) {
				// If channel is null, then there is no channel with this channel number
				channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
				return;
			}

			// If the user is the channel's head moderator or is the programmer
			if (channel.isHeadModerator(context.getUsername()) || ChannelBot.isUserChannelBotAdministrator(context.getUsername())) {
				// If the user is already a moderator
				if (Utils.collectionContainsIgnoreCase(channel.getModerators(), usernameToRemove)) {
					if (!usernameToRemove.equals(channel.getHeadModerator())) {
						channel.removeModerator(usernameToRemove);
						channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": User has been removed as a moderator.");
						try {
							User user = channelBot.getUserService().getUser(usernameToRemove);
							if (user != null) {
								channelBot.getDatabaseProviderRepository().getChannelUserProvider().createOrUpdateChannelUser(channel, user);
							}
						} catch (Exception e) {
							ChannelBot.logError(e);
						}
					} else {
						channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": You cannot remove the Head Moderator.");
					}
				} else {
					channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": User is not a moderator.");
				}
			}
		}
	}

}
