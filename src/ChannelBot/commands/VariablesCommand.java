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

import ChannelBot.ChannelBot;
import ChannelBot.Command;
import ChannelBot.TimeZoneUtils;
import ChannelBot.User;

public class VariablesCommand extends Command {

	@Override
	public void execute() {
		String username = getUsername();
		if (getArguments().length() > 0) {
			username = getArguments();
		}
		User user = ChannelBot.getInstance().getUser(username);

		if (user != null) {
			StringBuilder qtell = new StringBuilder("Variables for " + user.getName() + ":\\n");
			qtell.append("Showing Time: " + ((user.isShowTime()) ? "Yes" : "No") + "\\n");
			qtell.append("Time Zone: " + TimeZoneUtils.getAbbreviation(user.getTimeZone())
					+ "\\n");
			qtell.append("Echo Tells: " + ((user.isEcho()) ? "Yes" : "No") + "\\n");
			qtell.append("Showing Swear Words: " + ((user.isShowSwearWords()) ? "Yes" : "No")
					+ "\\n");
			qtell.append("Disabled 'ToldTo...': "
					+ (user.isDisableToldToString() ? "Yes" : "No") + "\\n");
			qtell.append("Height: " + user.getHeight());
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(), qtell.toString());
		} else {
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": User could not be found.");
		}
	}

}
