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

public class ChannelCleaner extends Object {
	private ChannelBot instance;
	
	
	public ChannelCleaner() {
		super();
	}
	
	public ChannelCleaner(ChannelBot instance) {
		this();
		this.instance = instance;
	}
	
	public int cleanChannels() {
		return cleanChannels(120);
	}
	
	public int cleanChannels(int numDays) {
		int numChannelsRemoved = 0;
		
		Channel[] channels = instance.getChannels();
		for(Channel channel : channels) {
			
			long threshold = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * numDays);
			if (channel.getLastTellTime() < threshold) {
				System.out.println(String.format("Cleaning channel #%s",channel.getID()));
				instance.deleteChannel(channel);
				numChannelsRemoved++;
			}
		}
		
		System.out.println(String.format("%s channel(s) have been cleaned.",numChannelsRemoved));
		return numChannelsRemoved;
	}
}
