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

public class AddNameCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final String newName = context.getArguments();

		final Channel channel = context.getChannelBot().getChannel(context.getChannelNumber());
		if (channel == null) {
			context.getChannelBot().qtell(context.getUsername(), String.format("%s: Could not find that channel number. Please try again.", ChannelBot.getUsername()));
			return;
		}

		if (!channel.isModerator(context.getUsername()) && !ChannelBot.isUserChannelBotAdministrator(context.getUsername())) {
			context.getChannelBot().qtell(context.getUsername(), ChannelBot.getUsername() + ": Insufficient privileges. You must be a channel moderator to use this command.");
			return;
		}

		final int maximumChannelNameLength = Integer.parseInt(context.getChannelBot().getProperties().getProperty("config.channels.maxNameLength"));
		if (newName.length() > maximumChannelNameLength) {
			context.getChannelBot().qtell(context.getUsername(), String.format("%s: Channel names can only have a maximum length of %s.", ChannelBot.getUsername(), maximumChannelNameLength));
			return;
		}

		channel.setName(newName);
		context.getChannelBot().qtell(context.getUsername(), String.format("%s: Channel #%s Name has been changed to \"%s\".", ChannelBot.getUsername(), context.getChannelNumber(), newName));
	}
}
