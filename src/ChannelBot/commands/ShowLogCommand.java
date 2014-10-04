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

import java.util.Date;
import java.util.List;

import ChannelBot.Channel;
import ChannelBot.ChannelBot;
import ChannelBot.ChannelTell;
import ChannelBot.Command;
import ChannelBot.TimeZoneUtils;
import ChannelBot.User;
import ChannelBot.Utils;

public class ShowLogCommand extends Command {

	@Override
	public void execute() {
		if (getChannelNumber() > 0) {
			Channel channel = ChannelBot.getInstance().getChannel(getChannelNumber());
			if (channel != null) {
				if (Utils.listContainsIgnoreCase(channel.getMembers(), getUsername()) || getUsername().equals(ChannelBot.programmer)) {
					User user = ChannelBot.getInstance().getUser(getUsername());
					ChannelBot.getInstance().getServerConnection().qtell(getUsername(), buildTellHistory(user, channel));
				} else {
					ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": You must be in the channel to use this command.");
					return;
				}
			} else {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
			}
		}
	}
	
	private String buildTellHistory(User user, Channel channel) {
		StringBuilder stringBuilder = new StringBuilder(ChannelBot.getUsername() + ": Tell history for channel #" + channel.getID() + ":\\n");
		List<ChannelTell> channelTellHistory = channel.getHistory();
		for(int i=0;i<channelTellHistory.size();i++) {
			ChannelTell channelTell = channelTellHistory.get(i);
			stringBuilder.append("[" + TimeZoneUtils.getTime(user.getTimeZone(), new Date(channelTell.getTimestamp())) + "] " + channelTell.getUsername() + "(" + channelTell.getChannelNum() + "): " + channelTell.getMessage() + "\\n");
		}
		return stringBuilder.toString();
	}
}
