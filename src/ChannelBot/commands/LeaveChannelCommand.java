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
import ChannelBot.User;
import ChannelBot.Utils;

public class LeaveChannelCommand extends Command {

	@Override
	public void execute() {
		Channel channel = ChannelBot.getInstance().getChannel(getChannelNumber());
		if (channel != null) {
			if (!Utils.listContainsIgnoreCase(channel.getMembers(), getUsername())) {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
						ChannelBot.getUsername() + ": You are not in channel #" + channel.getID() + ".");
				return;
			}
			
			if (channel.isHeadModerator(getUsername())) {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
						ChannelBot.getUsername() + ": You cannot leave a channel that you are moderating.");
				return;
			}
			
			channel.leave(getUsername(),getCommandName().equals("leave-silent"));
			
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
					ChannelBot.getUsername() + ": You have left channel #" + channel.getID() + ".");
			try {
				User user = ChannelBot.getInstance().getUser(getUsername());
				ChannelBot.getInstance().getPersistanceProvider().removeChannelUserFromDb(channel,user);
			} catch (Exception e) {
				ChannelBot.logError(e);
			}
		} else {
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
					ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
		}
	}
}
