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
package ChannelBot;

public class ChannelFactory {
	public ChannelFactory() {
		
	}
	
	public Channel create(String moderator,String channelName,String channelPassword) {
		final Channel channel = new Channel();
		channel.addModerator(moderator);
		channel.setHeadModerator(moderator);
		channel.setID(ChannelBot.getInstance().nextAvailableChannelNumber());
		channel.setName(channelName);
		channel.addPassword(moderator,channelPassword);
		channel.getMembers().addAll(channel.getModeratorsAsList());
		channel.addChannelChangedEventListener(Channel.channelChangedEventListener);
		return channel;
	}
}
