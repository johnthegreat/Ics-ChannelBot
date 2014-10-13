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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ChannelBot.commands.AddModeratorCommand;
import ChannelBot.commands.AddNameCommand;
import ChannelBot.commands.AddPasswordCommand;
import ChannelBot.commands.AddUserToListCommand;
import ChannelBot.commands.ChannelCleanerCommand;
import ChannelBot.commands.CreateChannelCommand;
import ChannelBot.commands.DeleteChannelCommand;
import ChannelBot.commands.ExecuteCommand;
import ChannelBot.commands.ExitCommand;
import ChannelBot.commands.HelpCommand;
import ChannelBot.commands.InchannelCommand;
import ChannelBot.commands.InformationCommand;
import ChannelBot.commands.InviteCommand;
import ChannelBot.commands.JoinChannelCommand;
import ChannelBot.commands.LeaveChannelCommand;
import ChannelBot.commands.ListChannelsCommand;
import ChannelBot.commands.MoreCommand;
import ChannelBot.commands.PersistCommand;
import ChannelBot.commands.PoseCommand;
import ChannelBot.commands.ReloadCommand;
import ChannelBot.commands.RemoveModeratorCommand;
import ChannelBot.commands.RemoveUserCommand;
import ChannelBot.commands.RemoveUserFromListCommand;
import ChannelBot.commands.SetVariableCommand;
import ChannelBot.commands.ShowLogCommand;
import ChannelBot.commands.TellChannelCommand;
import ChannelBot.commands.VariablesCommand;
import ChannelBot.commands.VersionCommand;
import ChannelBot.commands.ViewUserListCommand;

public class CommandResolver {
	public static Command resolveCommand(String commandName) {
		PatternService patternService = PatternService.getInstance();
		Pattern pattern;
		Matcher matcher;
		
		if (commandName.equals("set")) {
			return new SetVariableCommand();
		}
		
		// inchannel / who / online command
		pattern = patternService.get("(" + StringUtils.buildCommandRegex("in[channel]") + "|who|online)");
		matcher = pattern.matcher(commandName);
		if (matcher.matches()) {
			return new InchannelCommand();
		}
		
		if (patternService.get(StringUtils.buildCommandRegex("t[ell]")).matcher(commandName).matches()) {
			return new TellChannelCommand();
		}
		
		if (patternService.get(StringUtils.buildCommandRegex("addn[ame]")).matcher(commandName).matches()) {
			return new AddNameCommand();
		}
		
		if (patternService.get(StringUtils.buildCommandRegex("addp[assword]")).matcher(commandName).matches()) {
			return new AddPasswordCommand();
		}
		
		if (patternService.get("(removeuser|kick|nuke)").matcher(commandName).matches()) {
			return new RemoveUserCommand();
		}
		
		if (patternService.get("delete").matcher(commandName).matches()) {
			return new DeleteChannelCommand();
		}
		
		if (patternService.get("create").matcher(commandName).matches()) {
			return new CreateChannelCommand();
		}
		
		if (patternService.get("(lc|" + StringUtils.buildCommandRegex("list[channels]") + ")").matcher(commandName).matches()) {
			return new ListChannelsCommand();
		}
		
		if (patternService.get(StringUtils.buildCommandRegex("info[rmation]")).matcher(commandName).matches()) {
			return new InformationCommand();
		}
		
		if (patternService.get(StringUtils.buildCommandRegex("help")).matcher(commandName).matches()) {
			return new HelpCommand();
		}
		
		if (patternService.get(StringUtils.buildCommandRegex("v[ariables]")).matcher(commandName).matches()) {
			return new VariablesCommand();
		}
		
		if (patternService.get(StringUtils.buildCommandRegex("j[oin]")).matcher(commandName).matches()) {
			return new JoinChannelCommand();
		}
		
		if (patternService.get(StringUtils.buildCommandRegex("showlog")).matcher(commandName).matches()) {
			return new ShowLogCommand();
		}
		
		if (patternService.get(StringUtils.buildCommandRegex("version")).matcher(commandName).matches()) {
			return new VersionCommand();
		}
		
		if (patternService.get(StringUtils.buildCommandRegex("addm[oderator]")).matcher(commandName).matches()) {
			return new AddModeratorCommand();
		}
		
		if (patternService.get(StringUtils.buildCommandRegex("removem[oderator]")).matcher(commandName).matches()) {
			return new RemoveModeratorCommand();
		}
		
		if (patternService.get("invite").matcher(commandName).matches()) {
			return new InviteCommand();
		}
		
		if (patternService.get("(" + StringUtils.buildCommandRegex("n[ext]") + "|" + StringUtils.buildCommandRegex("m[ore]") + ")").matcher(commandName).matches()) {
			return new MoreCommand();
		}
		
		if (patternService.get("(leave|leave-silent)").matcher(commandName).matches()) {
			return new LeaveChannelCommand();
		}
		
		if (patternService.get("exit").matcher(commandName).matches()) {
			return new ExitCommand();
		}
		
		if (patternService.get("reload").matcher(commandName).matches()) {
			return new ReloadCommand();
		}
		
		if (patternService.get("pose").matcher(commandName).matches()) {
			return new PoseCommand();
		}
		
		if (patternService.get(StringUtils.buildCommandRegex("exec[ute]")).matcher(commandName).matches()) {
			return new ExecuteCommand();
		}
		
		if (patternService.get("\\+(?:[a-zA-Z]|ban)").matcher(commandName).matches()) {
			return new AddUserToListCommand();
		}
		
		if (patternService.get("\\-(?:[a-zA-Z]|ban)").matcher(commandName).matches()) {
			return new RemoveUserFromListCommand();
		}
		
		if (patternService.get("=(?:[a-zA-Z]|ban)").matcher(commandName).matches()) {
			return new ViewUserListCommand();
		}
		
		if (patternService.get("clean").matcher(commandName).matches()) {
			return new ChannelCleanerCommand();
		}
		
		if (patternService.get("persist").matcher(commandName).matches()) {
			return new PersistCommand();
		}
		
		return null;
	}
}
