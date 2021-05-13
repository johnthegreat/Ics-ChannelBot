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

import java.util.Date;
import java.util.NavigableMap;

import ChannelBot.Channel;
import ChannelBot.ChannelBot;
import ChannelBot.Command;
import ChannelBot.TimeZoneUtils;
import ChannelBot.User;
import ChannelBot.Utils;

public class ListChannelsCommand extends Command {

	@Override
	public void execute() {
		// moderator MAY BE NULL!
		
		// sorts the map into ascending order by keys.
		NavigableMap<Integer,Channel> channels = ChannelBot.getInstance().getChannelMap().descendingMap().descendingMap();
		Channel[] chans = channels.values().toArray(new Channel[channels.size()]);
		
		final String newline = "\\n";
		int numChannels = 0;
		StringBuilder qt = new StringBuilder();
		
		boolean isProgrammer = getUsername().equals(ChannelBot.programmer);
		if (isProgrammer) {
			// The programmer gets to see additional information about the channel
			qt.append(String.format("%3s. %-22s %-18s %s"+newline,"###","Name","Moderator","Last Tell"));
		} else {
			qt.append(String.format("%3s. %-22s %-18s"+newline,"###","Name","Moderator"));
		}
		
		String moderator = getArguments();
		if (moderator != null && moderator.length() == 0) {
			moderator = null;
		}
		User user = ChannelBot.getInstance().getUser(getUsername());
		
		for(int i=0;i<chans.length;i++) {
			Channel c = chans[i];
			if (moderator == null || (moderator != null && Utils.listContainsIgnoreCase(c.getModerators(), moderator))) {
				boolean hasPassword = c.getPassword() != null && !c.getPassword().equals("");
				if (isProgrammer) {
					String fancyDate = null;
					if (c.getLastTellTime() != 0L) {
						fancyDate = TimeZoneUtils.getTime(user.getTimeZone(), new Date(c.getLastTellTime()), "MM/dd/yyyy hh:mm:ss a z");
					} else {
						fancyDate = "Never";
					}
					qt.append(String.format("%3s. %-22s %-18s %s",""+c.getID(),c.getName() + (hasPassword?"*":""),c.getHeadModerator(),fancyDate));
				} else {
					qt.append(String.format("%3s. %-22s %-18s",""+c.getID(),c.getName() + (hasPassword?"*":""),c.getHeadModerator()));
				}
				qt.append(newline);
				numChannels++;
			}
		}

		qt.append(newline + "An asterisk (\"*\") indicates that the channel is password protected." + newline);
		qt.append("There " + (numChannels == 1 ? "is" : "are") + " "
				+ numChannels + " channel" + (numChannels == 1 ? "" : "s")
				+ " listed.");
		
		ChannelBot.getInstance().getServerConnection().qtell(getUsername(), qt.toString());
	}

}
