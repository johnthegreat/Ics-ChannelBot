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
package ChannelBot.commands.impl;

import ChannelBot.ChannelBot;
import ChannelBot.TimeZoneUtils;
import ChannelBot.User;
import ChannelBot.commands.Command;
import ChannelBot.commands.context.CommandContext;

public class VariablesCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final ChannelBot channelBot = context.getChannelBot();
		String username = context.getUsername();
		if (context.getArguments().length() > 0) {
			username = context.getArguments();
		}
		User user = channelBot.getUserService().getUser(username);

		if (user == null) {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": User could not be found.");
			return;
		}

		final StringBuilder qtell = new StringBuilder("Variables for " + user.getName() + ":\\n");
		qtell.append("Showing Time: " + ((user.isShowTime()) ? "Yes" : "No") + "\\n");
		qtell.append("Time Zone: " + TimeZoneUtils.getAbbreviation(user.getTimeZone())
				+ "\\n");
		qtell.append("Echo Tells: " + ((user.isEcho()) ? "Yes" : "No") + "\\n");
		qtell.append("Showing Swear Words: " + ((user.isShowSwearWords()) ? "Yes" : "No")
				+ "\\n");
		qtell.append("Disabled 'ToldTo...': "
				+ (user.isDisableToldToString() ? "Yes" : "No") + "\\n");
		qtell.append("Height: " + user.getHeight());
		channelBot.qtell(context.getUsername(), qtell.toString());
	}
}
