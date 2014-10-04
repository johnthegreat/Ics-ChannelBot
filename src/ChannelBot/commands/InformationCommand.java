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

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import ChannelBot.Channel;
import ChannelBot.ChannelBot;
import ChannelBot.Command;

public class InformationCommand extends Command {

	@Override
	public void execute() {
		if (getChannelNumber() > 0) {
			Channel channel = ChannelBot.getInstance().getChannel(getChannelNumber());
			if (channel != null) {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), getChannelInformation(channel));
			} else {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
			}
		} else {
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Could not find that channel number. Please try again.");
		}
	}
	
	private String getChannelInformation(Channel channel) {
		StringBuilder qt = new StringBuilder("Channel #" + channel.getID() + " Information:\\n");
		qt.append("Name: " + channel.getName() + "\\n");
		qt.append("Head Moderator: " + channel.getHeadModerator() + "\\n");
		qt.append("Moderators: " + java.util.Arrays.toString(channel.getModerators()) + "\\n");
		qt.append(String.format("Member Count: %s",channel.getMembers().size()));
		if (getUsername().equals(ChannelBot.programmer)) {
			// only the programmer can see this information.
			qt.append("\\nPassword: " + channel.getPassword());
			long time = channel.getLastTellTime();
			TimeZone tz = ChannelBot.getInstance().getUser(getUsername()).getTimeZone();
			SimpleDateFormat s = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
			s.setTimeZone(tz);
			qt.append("\\nLast Tell: " + (time == 0 ? "Never" : s.format(time)));
		}
		return qt.toString();
	}
}
