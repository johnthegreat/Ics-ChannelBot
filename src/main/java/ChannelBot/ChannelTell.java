/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2012 John Nahlen
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

public final class ChannelTell {
	private String username = null;
	private int channelNum = 0;
	private long timestamp = 0L;
	private String message = null;
	private boolean isFinal = false;

	public ChannelTell() {

	}

	/**
	 * Once markFinal() is called, all fields are no longer externally mutable.<br />
	 * Protects from accidental overwrites.<br />
	 * There is no method to unset the final flag. Use with care.<br />
	 * <br />
	 * Returns true if marked final, false if not.
	 * False will only be returned if some values are omitted.<br />
	 * All fields are required.
	 */
	public boolean markFinal() {
		isFinal = true;

		// if you change the defaults in the class, make sure to change them here as well
		return username != null &&
				channelNum != 0 &&
				timestamp != 0L &&
				message != null;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		if (isFinal) return;
		this.username = username;
	}

	public int getChannelNum() {
		return channelNum;
	}

	public void setChannelNum(int channelNum) {
		if (isFinal) return;
		this.channelNum = channelNum;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		if (isFinal) return;
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		if (isFinal) return;
		this.message = message;
	}
}
