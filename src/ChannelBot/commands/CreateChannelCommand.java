/**
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
package ChannelBot.commands;

import ChannelBot.Channel;
import ChannelBot.ChannelBot;
import ChannelBot.ChannelChangedEvent;
import ChannelBot.Command;
import ChannelBot.User;

public class CreateChannelCommand extends Command {

	@Override
	public void execute() {
		String[] args = getArguments().split(" ");
		String channelName = "";
		String channelPassword = "";
		if (args.length > 1) {
			channelName = args[0];
		}
		if (args.length > 2) {
			channelName = args[1];
		}

		boolean isProgrammer = getUsername().equals(ChannelBot.programmer);

		User requestingUser = ChannelBot.getInstance().getUser(getUsername());
		
		if (!isProgrammer) {
			int maxChannelsPerUser = Integer.parseInt(ChannelBot.getInstance().getProperties().getProperty("config.channels.maxChannelsPerUser"));
			if (maxChannelsPerUser != 0) {
				int numChannelsForUser = ChannelBot.getInstance().getMetrics().getNumberOfChannelsForUser(ChannelBot.getInstance(), getUsername());
				if (numChannelsForUser >= maxChannelsPerUser) {
					ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + 
							": You have met or exceeded the maximum number of channels allowed. The maximum is " + maxChannelsPerUser + " channels per user.");
					return;
				}
			} else {
				System.out.println("Verification that user does not exceed a maximum number of channels has been disabled.");
			}
		}
		
		Channel channel = ChannelBot.getInstance().getChannelFactory().create(getUsername(), channelName, channelPassword);
		if (isProgrammer && getChannelNumber() > 0) {
			if (ChannelBot.getInstance().isChannelNumberAvailable(getChannelNumber())) {
				channel.setID(getChannelNumber());
			} else {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), String.format("%s: That channel number is in use, and cannot be created.",ChannelBot.getUsername()));
				return;
			}
		}
		ChannelBot.getInstance().addChannel(channel);
		channel.fireChangedEvent(new ChannelChangedEvent());

		if (requestingUser != null) {
			requestingUser.getInChannels().add(channel.getID());
		}
		
		StringBuilder qt = new StringBuilder(ChannelBot.getUsername() + ": Channel #" + channel.getID() + " has been created");
		qt.append(channelPassword.equals("") ? "." : " with password \"" + channelPassword + "\".");
		ChannelBot.getInstance().getServerConnection().qtell(getUsername(), qt.toString());
	}
}
