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
import ChannelBot.commands.Command;
import ChannelBot.commands.context.CommandContext;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class InformationCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final ChannelBot channelBot = context.getChannelBot();

		if (context.getChannelNumber() > 0) {
			Channel channel = channelBot.getChannel(context.getChannelNumber());
			if (channel != null) {
				channelBot.qtell(context.getUsername(), getChannelInformation(context, channel));
			} else {
				channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
			}
		} else {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
		}
	}

	private String getChannelInformation(final CommandContext context, final Channel channel) {
		StringBuilder qt = new StringBuilder("Channel #" + channel.getID() + " Information:\\n");
		qt.append(String.format("Name: %s\\n", channel.getName()));
		qt.append(String.format("Head Moderator: %s\\n", channel.getHeadModerator()));
		qt.append(String.format("Moderators: %s\\n", java.util.Arrays.toString(channel.getModerators().toArray(new String[0]))));
		qt.append(String.format("Member Count: %s", channel.getMembers().size()));

		if (ChannelBot.isUserChannelBotAdministrator(context.getUsername())) {
			// only the programmer can see this information.
			qt.append(String.format("\\nPassword: %s", channel.getPassword()));
			long time = channel.getLastTellTime();
			TimeZone tz = context.getChannelBot().getUserService().getUser(context.getUsername()).getTimeZone();
			SimpleDateFormat s = new SimpleDateFormat(ChannelBot.DATE_FORMAT);
			s.setTimeZone(tz);
			qt.append(String.format("\\nLast Tell: %s", (time == 0 ? "Never" : s.format(time))));
		}
		return qt.toString();
	}
}
