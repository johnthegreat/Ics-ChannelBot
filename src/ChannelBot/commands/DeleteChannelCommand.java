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
import ChannelBot.Utils;

public class DeleteChannelCommand extends Command {
	@Override
	public void execute() {
		if (getUsername().equals(ChannelBot.programmer) && getArguments().equals("*")) {
			// delete all channels
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": All channels will now be deleted.");
			Channel[] channels = ChannelBot.getInstance().getChannels();
			for(Channel channel : channels) {
				ChannelBot.getInstance().deleteChannel(channel);
			}
		}
		
		if (getChannelNumber() == 0) {
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
					ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
			return;
		}
		
		Channel channel = ChannelBot.getInstance().getChannel(getChannelNumber());
		if (channel != null) {
			if (!Utils.listContainsIgnoreCase(channel.getModeratorsAsList(), getUsername()) && !getUsername().equals(ChannelBot.programmer)) {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Insufficient privileges. You must be the Head Moderator to use this command.");
				return;
			}
			
			if (channel.isHeadModerator(getUsername()) || getUsername().equals(ChannelBot.programmer)) {
				channel.tell("", "Channel #" + getChannelNumber() + " will now be deleted.");
				ChannelBot.getInstance().getServerConnection().qtell(channel.getHeadModerator(), ChannelBot.getUsername() + ": Thank you for using " + ChannelBot.getUsername() + ".");
				ChannelBot.getInstance().deleteChannel(channel);
			}
		}
	}
}
