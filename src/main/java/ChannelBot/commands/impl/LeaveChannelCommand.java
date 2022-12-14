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

public class LeaveChannelCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		Channel channel = context.getChannelBot().getChannel(context.getChannelNumber());
		if (channel == null) {
			context.getChannelBot().qtell(context.getUsername(),
					ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
			return;
		}

		if (!Utils.collectionContainsIgnoreCase(channel.getMembers(), context.getUsername())) {
			context.getChannelBot().qtell(context.getUsername(),
					ChannelBot.getUsername() + ": You are not in channel #" + channel.getID() + ".");
			return;
		}

		if (channel.isHeadModerator(context.getUsername())) {
			context.getChannelBot().qtell(context.getUsername(),
					ChannelBot.getUsername() + ": You cannot leave a channel that you are moderating.");
			return;
		}

		channel.leave(context.getUsername(), context.getCommandName().equals("leave-silent"));

		try {
			final User user = context.getChannelBot().getUserService().getUser(context.getUsername());
			if (user != null) {
				context.getChannelBot().getDatabaseProviderRepository().getChannelUserProvider().deleteChannelUser(channel, user);
			}
			context.getChannelBot().qtell(context.getUsername(),
					ChannelBot.getUsername() + ": You have left channel #" + channel.getID() + ".");
		} catch (Exception e) {
			ChannelBot.logError(e);
		}
	}
}
