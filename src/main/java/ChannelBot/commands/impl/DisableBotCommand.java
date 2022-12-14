/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2020 John Nahlen
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
import ChannelBot.StringUtils;
import ChannelBot.commands.Command;
import ChannelBot.commands.context.CommandContext;

public class DisableBotCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final ChannelBot channelBot = context.getChannelBot();

		if (!ChannelBot.isUserChannelBotAdministrator(context.getUsername())) {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Insufficient privileges. You must be the programmer to issue this command.");
			return;
		}

		System.out.println("The disable bot command has been issued.");

		String args = context.getArguments().trim();
		if (!args.equals("")) {
			Boolean mode = StringUtils.parseBoolean(args);
			if (mode == null) {
				channelBot.qtell(context.getUsername(), String.format("%s: Unable to parse arguments.", ChannelBot.getUsername()));
			} else {
				channelBot.setBotDisabled(mode);
				channelBot.qtell(context.getUsername(), String.format("%s: Command has been successfully executed. %s is now %s.",
						ChannelBot.getUsername(), ChannelBot.getUsername(), channelBot.isBotDisabled() ? "disabled" : "enabled"));
			}
		} else {
			channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Please provide arguments for that command.");
		}
	}
}
