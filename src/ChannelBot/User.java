/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2010-2020 John Nahlen
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

import java.util.TimeZone;
import java.util.TreeSet;

public class User implements Comparable<User> {
	private String name = "";
	private boolean showTime = true;
	private TimeZone timeZone;
	private boolean echo = true;
	private boolean showSwearWords = false;
	private TreeSet<Integer> inChannels;
	private Boolean isOnline = null; // we don't know!
	private boolean disableToldToString = false;
	private int height = 20;

	public User() {
		timeZone = TimeZoneUtils.getTimeZone("PST");
		inChannels = new TreeSet<>();
	}

	public User(String username) {
		this();
		setName(username);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isShowTime() {
		return showTime;
	}

	public void setShowTime(boolean showTime) {
		this.showTime = showTime;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public boolean isEcho() {
		return echo;
	}

	public void setEcho(boolean echo) {
		this.echo = echo;
	}

	public boolean isShowSwearWords() {
		return showSwearWords;
	}

	public void setShowSwearWords(boolean showSwearWords) {
		this.showSwearWords = showSwearWords;
	}

	public TreeSet<Integer> getInChannels() {
		return inChannels;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public Boolean getOnlineStatus() {
		return isOnline;
	}

	public boolean isOnline() {
		if (isOnline == null) {
			return true;
		} else {
			return isOnline;
		}
	}

	public boolean isDisableToldToString() {
		return disableToldToString;
	}

	public void setDisableToldToString(boolean disableToldToString) {
		this.disableToldToString = disableToldToString;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public int compareTo(User o) {
		return getName().compareTo(o.getName());
	}
}
