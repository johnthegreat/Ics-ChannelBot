/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2014-2020 John Nahlen
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
import ChannelBot.TimeZoneUtils;
import ChannelBot.User;
import ChannelBot.commands.Command;
import ChannelBot.commands.context.CommandContext;

import java.sql.SQLException;
import java.util.TimeZone;

public class SetVariableCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final ChannelBot channelBot = context.getChannelBot();
		final User user = channelBot.getUserService().getUser(context.getUsername());
		if (user != null) {
			String args = context.getArguments();
			if (args != null) {
				int pos = args.indexOf(" ");
				if (pos != -1) {
					String variable = args.substring(0, pos);
					String value = args.substring(pos + 1);
					changeUserVariable(context, user, variable, value);
				} else {
					channelBot.qtell(context.getUsername(), ChannelBot.getUsername() + ": Please provide a value for the variable you are trying to set.");
				}
			}
		}
	}

	private void changeUserVariable(final CommandContext context, final User user, final String variable, final String value) {
		final String errorMessage = ChannelBot.getUsername() + ": Unable to parse your input.";

		boolean showTimeCmd = StringUtils.startsWithIgnoreCase(variable, "showt");
		boolean showSwearWordsCmd = StringUtils.startsWithIgnoreCase(variable, "shows");
		boolean showEchoCmd = StringUtils.startsWithIgnoreCase(variable, "echo");
		boolean disableToldToCmd = StringUtils.startsWithIgnoreCase(variable, "disabletoldto");

		if (showTimeCmd || showSwearWordsCmd || showEchoCmd || disableToldToCmd) {
			Boolean status = StringUtils.parseBoolean(value);
			if (status == null) {
				context.getChannelBot().qtell(user.getName(), errorMessage);
				return;
			}

			String variableName = "";
			String newValue = "";
			if (showTimeCmd) {
				variableName = "ShowTime";
				user.setShowTime(status);
				newValue = user.isShowTime() ? "Yes" : "No";
			} else if (showSwearWordsCmd) {
				variableName = "ShowSwearWords";
				user.setShowSwearWords(status);
				newValue = user.isShowSwearWords() ? "Yes" : "No";
			} else if (showEchoCmd) {
				variableName = "Echo";
				user.setEcho(status);
				newValue = user.isEcho() ? "Yes" : "No";
			} else if (disableToldToCmd) {
				variableName = "DisableToldToCount";
				user.setDisableToldToString(status);
				newValue = user.isDisableToldToString() ? "Yes" : "No";
			}

			try {
				context.getChannelBot().getDatabaseProviderRepository().getUserProvider().updateUser(user);
			} catch (SQLException e) {
				ChannelBot.logError(e);
			}
			context.getChannelBot().qtell(context.getUsername(),
					ChannelBot.getUsername() + String.format(": Your variable %s has been set to %s.", variableName, newValue));
			return;
		}

		// set user's timezone
		if (variable.equalsIgnoreCase("tzone") || variable.matches(StringUtils.buildCommandRegex("time[zone]"))) {
			TimeZone tz = TimeZoneUtils.getTimeZone(value);
			if (tz != null) {
				user.setTimeZone(tz);
				try {
					context.getChannelBot().getDatabaseProviderRepository().getUserProvider().updateUser(user);
				} catch (SQLException e) {
					ChannelBot.logError(e);
				}
				context.getChannelBot().qtell(user.getName(),
						ChannelBot.getUsername() + String.format(": Your variable %s has been set to %s.", "TimeZone", TimeZoneUtils.getAbbreviation(tz)));
			} else {
				context.getChannelBot().qtell(user.getName(),
						ChannelBot.getUsername() + ": Couldn't parse abbreviation. Please make sure your input was valid.");
			}
			return;
		}

		if (StringUtils.startsWithIgnoreCase(variable, "height")) {
			if (StringUtils.isNumeric(value)) {
				int height = Integer.parseInt(value);
				if (height < 5 || height > 240) {
					context.getChannelBot().qtell(user.getName(), ChannelBot.getUsername() +
							": Invalid value for variable Height. Please select a value between 5 and 240.");
					return;
				}
				user.setHeight(height);
				try {
					context.getChannelBot().getDatabaseProviderRepository().getUserProvider().updateUser(user);
				} catch (SQLException e) {
					ChannelBot.logError(e);
				}
				context.getChannelBot().qtell(user.getName(),
						ChannelBot.getUsername() + String.format(": Your variable %s has been set to %s.", "Height", user.getHeight()));
			} else {
				context.getChannelBot().qtell(user.getName(), ChannelBot.getUsername() + ": " + errorMessage);
			}
		}
	}
}
