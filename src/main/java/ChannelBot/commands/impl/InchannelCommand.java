/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2014-2020 John Nahlen
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
import ChannelBot.commands.Command;
import ChannelBot.commands.context.CommandContext;

import java.util.Collections;

public class InchannelCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final ChannelBot channelBot = context.getChannelBot();

		if (context.getChannelNumber() > 0) {
			// The user wants to know what users are in a particular channel.
			Channel channel = channelBot.getChannel(context.getChannelNumber());
			if (channel == null) {
				channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Channel could not be found or does not exist.");
				return;
			}

			channelBot.qtell(context.getUsername(), buildText(context, channel, context.getCommandName().equals("online")));
		} else {
			String args = context.getArguments();
			if (args.equals("")) {
				channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Please provide arguments for that command.");
				return;
			}
			// The user wants to know what channels a specific user is.
			User user = channelBot.getUserService().getUser(args);
			if (user != null) {
				StringBuilder b = new StringBuilder(ChannelBot.getUsername() + ":\\n" + user.getName() + " is in the following channels:\\n");
				Integer[] channelNumberArray = user.getInChannels().toArray(new Integer[0]);
				for (Integer channelNumber : channelNumberArray) {
					b.append(channelNumber).append(" ");
				}
				channelBot.qtell(context.getUsername(), b.toString());
			} else {
				channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": User could not be found.");
			}
		}
	}

	public String buildText(final CommandContext context, final Channel channel, final boolean onlineOnly) {
		int size = channel.getMembers().size();
		Collections.sort(channel.getMembers(), String.CASE_INSENSITIVE_ORDER);
		StringBuilder qt = new StringBuilder("In channel #" + channel.getID() + ":\\n");
		int listedCount = 0;
		for (int i = 0; i < size; i++) {
			String listener = channel.getMembers().get(i);
			User u = context.getChannelBot().getUserService().getUser(listener);
			if (u == null) {
				continue;
			}
			char ch;
			Boolean b = u.getOnlineStatus();
			if (b == null) {
				ch = '?';
			} else {
				ch = b ? '+' : ' ';
			}

			boolean isModerator = Collections.binarySearch(channel.getModerators(), listener, String.CASE_INSENSITIVE_ORDER) >= 0;

			// If we only want to see online users, and this user is not online, skip them.
			if (onlineOnly && ch != '+') {
				continue;
			}

			qt.append(String.format("%s %s", ch, listener));
			if (isModerator) {
				qt.append("(m)");
			}
			qt.append("\\n");
			listedCount++;
		}

		qt.append(size + " member" + (size == 1 ? "" : "s") + " in channel #"
				+ channel.getID() + ((onlineOnly) ? (", " + listedCount + " members shown.") : "."));
		return qt.toString();
	}
}
