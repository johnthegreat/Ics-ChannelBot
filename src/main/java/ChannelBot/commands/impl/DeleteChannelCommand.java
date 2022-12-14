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
import ChannelBot.Utils;
import ChannelBot.commands.Command;
import ChannelBot.commands.context.CommandContext;

public class DeleteChannelCommand implements Command {
	@Override
	public void execute(final CommandContext context) {
		final ChannelBot channelBot = context.getChannelBot();
		boolean isProgrammer = ChannelBot.isUserChannelBotAdministrator(context.getUsername());

		if (isProgrammer && context.getArguments().equals("*")) {
			// delete all channels
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": All channels will now be deleted.");
			Channel[] channels = channelBot.getChannels();
			for (Channel channel : channels) {
				channelBot.deleteChannel(channel);
			}
			return;
		}

		final Channel channel = channelBot.getChannel(context.getChannelNumber());
		if (channel == null) {
			channelBot.qtell(context.getUsername(),
					ChannelBot.getUsername() + ": Could not find that channel, please try again.");
			return;
		}

		if (!Utils.collectionContainsIgnoreCase(channel.getModerators(), context.getUsername()) && !isProgrammer) {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Insufficient privileges. You must be the Head Moderator to use this command.");
			return;
		}

		if (channel.isHeadModerator(context.getUsername()) || isProgrammer) {
			channel.tell("", "Channel #" + context.getChannelNumber() + " will now be deleted.");
			channelBot.qtell(channel.getHeadModerator(), ChannelBot.getUsername() + ": Thank you for using " + ChannelBot.getUsername() + ".");
			channelBot.deleteChannel(channel);
		}
	}
}
