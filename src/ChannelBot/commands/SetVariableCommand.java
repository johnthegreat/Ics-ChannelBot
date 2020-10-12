/**
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
package ChannelBot.commands;

import java.sql.SQLException;
import java.util.TimeZone;

import ChannelBot.ChannelBot;
import ChannelBot.Command;
import ChannelBot.StringUtils;
import ChannelBot.TimeZoneUtils;
import ChannelBot.User;

public class SetVariableCommand extends Command {

	@Override
	public void execute() {
		User user = ChannelBot.getInstance().getUser(getUsername());
		if (user != null) {
			String args = getArguments();
			if (args != null) {
				int pos = args.indexOf(" ");
				if (pos != -1) {
					String variable = args.substring(0, pos);
					String value = args.substring(pos+1);
					changeUserVariable(user, variable, value);
				} else {
					ChannelBot.getInstance().getServerConnection().qtell(getUsername(), ChannelBot.getUsername() + ": Please provide a value for the variable you are trying to set.");
				}
			}
		}
	}
	
	private void changeUserVariable(User user, String variable, String value) {
		final String errmess = ChannelBot.getUsername() + ": Unable to parse your input.";

		boolean showTimeCmd = StringUtils.startsWithIgnoreCase(variable, "showt");
		boolean showSwearWordsCmd = StringUtils.startsWithIgnoreCase(variable, "shows");
		boolean showEchoCmd = StringUtils.startsWithIgnoreCase(variable, "echo");
		boolean disableToldToCmd = StringUtils.startsWithIgnoreCase(variable, "disabletoldto");
		
		if (showTimeCmd || showSwearWordsCmd || showEchoCmd || disableToldToCmd) {
			Boolean status = StringUtils.parseBoolean(value);
			if (status == null) {
				ChannelBot.getInstance().getServerConnection().qtell(user.getName(), errmess);
				return;	
			}
			
			String variableName = "";
			String newValue = "";
			if (showTimeCmd) {
				variableName = "ShowTime";
				user.setShowTime(status.booleanValue());
				newValue = user.isShowTime() ? "Yes" : "No";
			} else if (showSwearWordsCmd) {
				variableName = "ShowSwearWords";
				user.setShowSwearWords(status.booleanValue());
				newValue = user.isShowSwearWords() ? "Yes" : "No";
			} else if (showEchoCmd) {
				variableName = "Echo";
				user.setEcho(status.booleanValue());
				newValue = user.isEcho() ? "Yes" : "No";
			} else if (disableToldToCmd) {
				variableName = "DisableToldToCount";
				user.setDisableToldToString(status.booleanValue());
				newValue = user.isDisableToldToString() ? "Yes" : "No";
			}
			
			try {
				ChannelBot.getInstance().getPersistanceProvider().saveUserToDb(user);
			} catch (SQLException e) {
				ChannelBot.logError(e);
			}
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(),
					ChannelBot.getUsername() + String.format(": Your variable %s has been set to %s.",variableName,newValue));
			return;
		}
		
		// set user's timezone
		if (variable.equalsIgnoreCase("tzone") || variable.matches(StringUtils.buildCommandRegex("time[zone]"))) {
			TimeZone tz = TimeZoneUtils.getTimeZone(value);
			if (tz != null) {
				user.setTimeZone(tz);
				try {
					ChannelBot.getInstance().getPersistanceProvider().saveUserToDb(user);
				} catch (SQLException e) {
					ChannelBot.logError(e);
				}
				ChannelBot.getInstance().getServerConnection().qtell(user.getName(),
						ChannelBot.getUsername() + String.format(": Your variable %s has been set to %s.","TimeZone",TimeZoneUtils.getAbbreviation(tz)));
			} else if (tz == null) {
				ChannelBot.getInstance().getServerConnection().qtell(user.getName(),
						ChannelBot.getUsername() + ": Couldn't parse abbreviation. Please make sure your input was valid.");
			}
			return;
		}
		
		if (StringUtils.startsWithIgnoreCase(variable, "height")) {
			if (StringUtils.isNumeric(value)) {
				int height = Integer.parseInt(value);
				if (height < 5 || height > 240) {
					ChannelBot.getInstance().getServerConnection().qtell(user.getName(), ChannelBot.getUsername() +
							": Invalid value for variable Height. Please select a value between 5 and 240.");
					return;
				}
				user.setHeight(height);
				try {
					ChannelBot.getInstance().getPersistanceProvider().saveUserToDb(user);
				} catch (SQLException e) {
					ChannelBot.logError(e);
				}
				ChannelBot.getInstance().getServerConnection().qtell(user.getName(),
						ChannelBot.getUsername() + String.format(": Your variable %s has been set to %s.","Height",user.getHeight()));
			} else {
				ChannelBot.getInstance().getServerConnection().qtell(user.getName(), ChannelBot.getUsername() + ": " + errmess);
			}
			return;
		}
	}
}
