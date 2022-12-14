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
import ChannelBot.ChannelChangedEvent;
import ChannelBot.User;
import ChannelBot.commands.Command;
import ChannelBot.commands.context.CommandContext;

public class CreateChannelCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final ChannelBot channelBot = context.getChannelBot();

		String[] args = context.getArguments().split(" ");
		String channelName = "";
		String channelPassword = "";
		if (args.length > 1) {
			channelName = args[0];
		}
		if (args.length > 2) {
			channelPassword = args[1];
		}

		boolean isProgrammer = ChannelBot.isUserChannelBotAdministrator(context.getUsername());

		final User requestingUser = channelBot.getUserService().getUser(context.getUsername());

		if (!isProgrammer) {
			int maxChannelsPerUser = Integer.parseInt(channelBot.getProperties().getProperty("config.channels.maxChannelsPerUser"));
			if (maxChannelsPerUser != 0) {
				int numChannelsForUser = channelBot.getMetrics().getNumberOfChannelsForUser(channelBot, context.getUsername());
				if (numChannelsForUser >= maxChannelsPerUser) {
					channelBot.qtell(context.getUsername(), ChannelBot.getUsername() +
							": You have met or exceeded the maximum number of channels allowed. The maximum is " + maxChannelsPerUser + " channels per user.");
					return;
				}
			} else {
				System.out.println("Verification that user does not exceed a maximum number of channels has been disabled.");
			}
		}

		Channel channel = channelBot.getChannelFactory().create(channelBot, context.getUsername(), channelName, channelPassword);
		if (isProgrammer && context.getChannelNumber() > 0) {
			if (channelBot.isChannelNumberAvailable(context.getChannelNumber())) {
				channel.setID(context.getChannelNumber());
			} else {
				channelBot.qtell(context.getUsername(), String.format("%s: That channel number is in use, and cannot be created.", ChannelBot.getUsername()));
				return;
			}
		}
		channelBot.addChannel(channel);
		channel.fireChangedEvent(new ChannelChangedEvent());

		if (requestingUser != null) {
			requestingUser.getInChannels().add(channel.getID());
		}

		channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Channel #" + channel.getID() + " has been created" + (channelPassword.equals("") ? "." : " with password \"" + channelPassword + "\"."));
	}
}
