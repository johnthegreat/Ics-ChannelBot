/**
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
package ChannelBot.commands;

import ChannelBot.Channel;
import ChannelBot.ChannelBot;
import ChannelBot.Command;

public class CreateChannelCommand extends Command {

	@Override
	public void execute() {
		String[] args = getArguments().split(" ");
		String channelName = "";
		String channelPassword = "";
		if (args.length > 1)
			channelName = args[0];
		if (args.length > 2)
			channelName = args[1];
		
		if (!getUsername().equals(ChannelBot.programmer)) {
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
		ChannelBot.getInstance().addChannel(channel);
		
		StringBuilder qt = new StringBuilder(ChannelBot.getUsername() + ": Channel #" + channel.getID() + " has been created");
		qt.append(channelPassword.equals("") ? "." : " with password \"" + channelPassword + "\".");
		ChannelBot.getInstance().getServerConnection().qtell(getUsername(), qt.toString());
	}
}
