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

public class JoinChannelCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final Channel channel = context.getChannelBot().getChannel(context.getChannelNumber());
		if (channel == null) {
			context.getChannelBot().qtell(context.getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
			return;
		}

		String channelPassword = channel.getPassword();
		boolean channelHasPassword = channelPassword != null && !channelPassword.equals("");
		String providedPassword = context.getArguments();
		if (!channelHasPassword || channelPassword.equals(providedPassword)) {
			if (Utils.collectionContainsIgnoreCase(channel.getMembers(), context.getUsername())) {
				context.getChannelBot().qtell(context.getUsername(),
						ChannelBot.getUsername() + ": You are already a member of channel #" + channel.getID() + ".");
			} else {
				channel.join(context.getUsername());
				User user = context.getChannelBot().getUserService().getUser(context.getUsername());
				if (user != null) {
					try {
						context.getChannelBot().getDatabaseProviderRepository().getChannelUserProvider().createOrUpdateChannelUser(channel, user);
					} catch (Exception e) {
						ChannelBot.logError(e);
					}
				}
			}
			return;
		}

		if (channelHasPassword && providedPassword.equals("")) {
			context.getChannelBot().qtell(context.getUsername(),
					ChannelBot.getUsername() + ": Password required to join channel #" + channel.getID() + ".");
			return;
		}

		if (!channelPassword.equals(providedPassword)) {
			context.getChannelBot().qtell(context.getUsername(),
					ChannelBot.getUsername() + ": Failed to join the channel. Wrong password provided for channel #" + channel.getID() + ".");
		}
	}

}
