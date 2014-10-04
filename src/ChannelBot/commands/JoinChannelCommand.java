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

public class JoinChannelCommand extends Command {

	@Override
	public void execute() {
		Channel channel = ChannelBot.getInstance().getChannel(getChannelNumber());
		if (channel != null) {
			String channelPassword = channel.getPassword();
			boolean channelHasPassword = channelPassword != null && !channelPassword.equals("");
			String providedPassword = getArguments();
			if (!channelHasPassword || channelPassword.equals(providedPassword)) {
				if (Utils.listContainsIgnoreCase(channel.getMembers(), getUsername())) {
					ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
							ChannelBot.getUsername() + ": You are already a member of channel #" + channel.getID() + ".");
				} else {
					channel.join(getUsername());
					User user = ChannelBot.getInstance().getUser(getUsername());
					try {
						ChannelBot.getInstance().getPersistanceProvider().addChannelUserToDb(channel, user);
					} catch (Exception e) {
						ChannelBot.logError(e);
					}
				}
				return;
			}
			
			if (channelHasPassword && providedPassword.equals("")) {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
						ChannelBot.getUsername() + ": Password required to join channel #" + channel.getID() + ".");
				return;
			}
			
			if (!channelPassword.equals(providedPassword)) {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
						ChannelBot.getUsername() + ": Failed to join the channel. Wrong password provided for channel #" + channel.getID() + ".");
				return;
			}
		} else {
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
		}
	}

}
