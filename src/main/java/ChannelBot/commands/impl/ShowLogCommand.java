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

import ChannelBot.*;
import ChannelBot.commands.Command;
import ChannelBot.commands.context.CommandContext;

import java.util.Date;
import java.util.List;

public class ShowLogCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final ChannelBot channelBot = context.getChannelBot();
		if (context.getChannelNumber() > 0) {
			final Channel channel = channelBot.getChannel(context.getChannelNumber());
			if (channel == null) {
				channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
				return;
			}

			if (Utils.collectionContainsIgnoreCase(channel.getMembers(), context.getUsername()) || ChannelBot.isUserChannelBotAdministrator(context.getUsername())) {
				final User user = channelBot.getUserService().getUser(context.getUsername());
				channelBot.qtell(context.getUsername(), buildTellHistory(user, channel));
			} else {
				channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": You must be in the channel to use this command.");
			}
		}
	}

	private String buildTellHistory(final User user, final Channel channel) {
		final StringBuilder stringBuilder = new StringBuilder(ChannelBot.getUsername() + ": Tell history for channel #" + channel.getID() + ":\\n");
		List<ChannelTell> channelTellHistory = channel.getHistory();
		for (int i = 0; i < channelTellHistory.size(); i++) {
			ChannelTell channelTell = channelTellHistory.get(i);
			stringBuilder.append("[" + TimeZoneUtils.getTime(user.getTimeZone(), new Date(channelTell.getTimestamp())) + "] " + channelTell.getUsername() + "(" + channelTell.getChannelNum() + "): " + channelTell.getMessage() + "\\n");
		}
		return stringBuilder.toString();
	}
}
