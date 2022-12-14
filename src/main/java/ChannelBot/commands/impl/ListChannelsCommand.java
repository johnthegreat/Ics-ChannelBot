/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2014-2021 John Nahlen
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
import java.util.Set;
import java.util.TreeSet;

public class ListChannelsCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final int ICS_MAX_CHARACTER_LIMIT = 1000;

		final ChannelBot channelBot = context.getChannelBot();
		final User user = channelBot.getUserService().getUser(context.getUsername());
		Channel[] channels = channelBot.getChannels();

		final String newLine = "\\n";
		int numChannels = 0;
		final StringBuilder qtell = new StringBuilder();

		final boolean isProgrammer = ChannelBot.isUserChannelBotAdministrator(context.getUsername());
		if (isProgrammer) {
			// The programmer gets to see additional information about the channel
			qtell.append(String.format("%3s. %-22s %-18s %s" + newLine, "###", "Name", "Moderator", "Last Tell"));
		} else {
			qtell.append(String.format("%3s. %-22s %-18s" + newLine, "###", "Name", "Moderator"));
		}

		String moderator = context.getArguments();
		if (moderator != null && moderator.length() == 0) {
			moderator = null;
		}

		if (moderator != null) {
			// Only show channels that have this moderator
			final Set<Channel> filteredChannels = new TreeSet<>();
			for (Channel channel : channels) {
				if (Utils.collectionContainsIgnoreCase(channel.getModerators(), moderator)) {
					filteredChannels.add(channel);
				}
			}
			channels = filteredChannels.toArray(new Channel[0]);
		}

		for (final Channel channel : channels) {
			final boolean hasPassword = channel.getPassword() != null && !channel.getPassword().equals("");
			if (isProgrammer) {
				String fancyDate = "Never";
				if (channel.getLastTellTime() != 0L) {
					fancyDate = TimeZoneUtils.getTime(user.getTimeZone(), new Date(channel.getLastTellTime()), "MM/dd/yyyy hh:mm:ss a z");
				}
				qtell.append(String.format("%3s. %-22s %-18s %s", channel.getID(), channel.getName() + (hasPassword ? "*" : ""), channel.getHeadModerator(), fancyDate));
			} else {
				qtell.append(String.format("%3s. %-22s %-18s", channel.getID(), channel.getName() + (hasPassword ? "*" : ""), channel.getHeadModerator()));
			}
			qtell.append(newLine);
			numChannels++;
		}

		final boolean onlyOneChannel = numChannels == 1;
		qtell.append(newLine + "An asterisk (\"*\") indicates that the channel is password protected." + newLine);
		qtell.append("There " + (onlyOneChannel ? "is" : "are") + " "
				+ numChannels + " channel" + (onlyOneChannel ? "" : "s")
				+ " listed.");

		// Break the qtell up into multiple qtells if its too long of a message to send to the ICS
		String qtellString = qtell.toString();
		while (qtellString.length() >= ICS_MAX_CHARACTER_LIMIT) {
			final int pos = qtellString.substring(0, ICS_MAX_CHARACTER_LIMIT).lastIndexOf(newLine);
			final String qtellStringSegment = qtellString.substring(0, pos);
			channelBot.qtell(context.getUsername(), qtellStringSegment);
			qtellString = qtellString.substring(pos + newLine.length());
		}
		if (qtellString.trim().length() > 0) {
			channelBot.qtell(context.getUsername(), qtellString);
		}
	}

}
