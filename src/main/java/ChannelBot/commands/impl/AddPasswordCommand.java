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

public class AddPasswordCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final ChannelBot channelBot = context.getChannelBot();
		final String newPassword = context.getArguments();

		final Channel channel = channelBot.getChannel(context.getChannelNumber());
		if (channel == null) {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
			return;
		}

		if (!channel.isHeadModerator(context.getUsername())) {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Insufficient privileges. You must be the Head Moderator to use this command.");
			return;
		}

		final int maximumChannelPasswordLength = Integer.parseInt(channelBot.getProperties().getProperty("config.channels.maxPasswordLength"));
		if (newPassword.length() > maximumChannelPasswordLength) {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Channel passwords can only have a maximum length of " + maximumChannelPasswordLength + ".");
			return;
		}

		channel.addPassword(context.getUsername(), newPassword);
		channelBot.qtell(context.getUsername(), String.format("%s: Channel #%s Password has been changed to \"%s\".", ChannelBot.getUsername(), context.getChannelNumber(), newPassword));
	}
}
