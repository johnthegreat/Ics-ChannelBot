/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2009-2014 John Nahlen
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

import java.util.HashMap;
import java.util.Map;

/**
 * The purpose of this class is to handle all user input and store CommunicationBuffer instances. Not yet complete.
 *
 * @author John
 * @since Wednesday, February 23, 2011
 */
public class UserCommunicationService {
	private static UserCommunicationService service = new UserCommunicationService();
	private Map<User, CommunicationBuffer> buffers = new HashMap<>();

	public static UserCommunicationService getInstance() {
		return service;
	}

	public Map<User, CommunicationBuffer> getBuffers() {
		return buffers;
	}

	public CommunicationBuffer get(User user) {
		return buffers.get(user);
	}

	public void addOrReplace(CommunicationBuffer buffer) {
		buffers.put(buffer.getUser(), buffer);
	}

	public void send(final User user, final String message) {
		send(user, message, TellMethod.QTELL);
	}

	public void send(final User user, final String message, final TellMethod tm) {
		String prepend = "";
		switch (tm) {
			case TELL:
				prepend = "tell";
				break;
			case QTELL:
			default:
				prepend = "qtell";
				break;
		}
		String str = String.format("%s %s %s", prepend, user.getName(), message);
		ChannelBot.getInstance().getServerConnection().write(str);
	}

	public enum TellMethod {TELL, QTELL;}
}
