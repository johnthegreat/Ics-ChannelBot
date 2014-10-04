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

import java.util.Collections;
import java.util.List;

import ChannelBot.Channel;
import ChannelBot.ChannelBot;
import ChannelBot.Command;
import ChannelBot.User;

public class InchannelCommand extends Command {

	@Override
	public void execute() {
		ChannelBot bot = ChannelBot.getInstance();
		
		if (getChannelNumber() > 0) {
			// The user wants to know what users are in a particular channel.
			Channel channel = ChannelBot.getInstance().getChannel(getChannelNumber());
			if (channel != null) {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), buildText(channel, getCommandName().equals("online")));
			} else {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Channel could not be found or does not exist.");
			}
		} else {
			String args = getArguments();
			if (args.equals("")) {
				bot.getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Please provide arguments for that command.");
				return;
			}
			// The user wants to know what channels a specific user is.
			User user = bot.getUser(args);
			if (user != null) {
				StringBuilder b = new StringBuilder(ChannelBot.getUsername() + ":\\n" + user.getName() + " is in the following channels:\\n");
				List<Integer> list = user.getInChannels();
				int size = list.size();
				for(int i=0;i<size;i++) {
					b.append(list.get(i) + " ");
				}
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), b.toString());
			} else {
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": User could not be found.");
			}
		}
	}
	
	public String buildText(Channel channel,boolean onlineOnly) {
		int size = channel.getMembers().size();
		Collections.sort(channel.getMembers(), String.CASE_INSENSITIVE_ORDER);
		StringBuilder qt = new StringBuilder("In channel #" + channel.getID() + ":\\n");
		int listedcount = 0;
		for (int i = 0; i < size; i++) {
			String listener = channel.getMembers().get(i);
			User u = ChannelBot.getInstance().getUser(listener);
			if (u == null) continue;
			char ch = 0;
			Boolean b = u.getOnlineStatus();
			if (b == null) ch = '?'; else {
				if (b.booleanValue() == true) ch = '+';
				if (b.booleanValue() == false) ch = ' ';
			}
			
			boolean isModerator = java.util.Collections.binarySearch(channel.getModeratorsAsList(), listener, String.CASE_INSENSITIVE_ORDER) >= 0;
			
			if (onlineOnly) {
				if (ch == '+') {
					qt.append(ch + " " + listener);
					if (isModerator) {
						qt.append("(m)");
					}
					qt.append("\\n");
					listedcount++;
				}
			} else {
				qt.append(ch + " " + listener);
				if (isModerator) {
					qt.append("(m)");
				}
				qt.append("\\n");
			}
		}
		
		qt.append(size + " member" + (size==1?"":"s") + " in channel #"
			+ channel.getID() + ((onlineOnly)?(", " + listedcount + " members shown."):"."));
		return qt.toString();
	}
}
