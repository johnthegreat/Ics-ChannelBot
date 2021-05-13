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
package ChannelBot.commands;

import ChannelBot.Channel;
import ChannelBot.ChannelBot;
import ChannelBot.Command;
import ChannelBot.Utils;

public class DeleteChannelCommand extends Command {
	@Override
	public void execute() {
		ChannelBot channelBot = ChannelBot.getInstance();
		boolean isProgrammer = getUsername().equals(ChannelBot.programmer);

		if (isProgrammer && getArguments().equals("*")) {
			// delete all channels
			channelBot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": All channels will now be deleted.");
			Channel[] channels = channelBot.getChannels();
			for(Channel channel : channels) {
				channelBot.deleteChannel(channel);
			}
			return;
		}

		Channel channel = channelBot.getChannel(getChannelNumber());
		if (channel == null) {
			channelBot.getServerConnection().qtell(getUsername(),
					ChannelBot.getUsername() + ": Could not find that channel, please try again.");
		} else {
			if (!Utils.listContainsIgnoreCase(channel.getModerators(), getUsername()) && !isProgrammer) {
				channelBot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Insufficient privileges. You must be the Head Moderator to use this command.");
				return;
			}
			
			if (channel.isHeadModerator(getUsername()) || isProgrammer) {
				channel.tell("", "Channel #" + getChannelNumber() + " will now be deleted.");
				channelBot.getServerConnection().qtell(channel.getHeadModerator(), ChannelBot.getUsername() + ": Thank you for using " + ChannelBot.getUsername() + ".");
				channelBot.deleteChannel(channel);
			}
		}
	}
}
