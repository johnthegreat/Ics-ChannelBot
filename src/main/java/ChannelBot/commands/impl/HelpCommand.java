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
import ChannelBot.commands.Command;
import ChannelBot.commands.context.CommandContext;

import java.io.*;
import java.util.Arrays;

public class HelpCommand implements Command {

	@Override
	public void execute(final CommandContext context) {
		final ChannelBot channelBot = context.getChannelBot();

		final String helpFileDirectoryPath = channelBot.getProperties().getProperty("config.files.helpfiles");
		final File helpFileDirectory = new File(helpFileDirectoryPath);

		if (!helpFileDirectory.exists() || !helpFileDirectory.isDirectory()) {
			System.err.println("Directory not found: " + helpFileDirectory.getAbsolutePath());
			channelBot.qtell(context.getUsername(), String.format("Help files are missing, please report to %s.", ChannelBot.getChannelBotAdministratorUsername()));
			return;
		}

		final String[] files = helpFileDirectory.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".txt");
			}
		});

		// Handle if there are no help files present in the directory
		if (files == null || files.length == 0) {
			System.err.println("No help files found: " + helpFileDirectory.getAbsolutePath());
			channelBot.qtell(context.getUsername(), String.format("No help files are available, please report to %s.", ChannelBot.getChannelBotAdministratorUsername()));
			return;
		}

		Arrays.sort(files);
		for (int i = 0; i < files.length; i++) {
			files[i] = files[i].replace(".txt", "");
		}

		String topic = context.getArguments();
		if (topic != null) {
			topic = topic.trim();
		}

		final StringBuilder qtell = new StringBuilder();
		if (topic == null || topic.length() == 0) {
			qtell.append("Available help files: (tell me \"help <topic>\" to read a help file)\\n");
			qtell.append("-----\\n");

			for (final String helpTopicName : files) {
				if (helpTopicName.length() > 0) {
					qtell.append(helpTopicName).append("\\n");
				}
			}
			qtell.append("-----");
			channelBot.qtell(context.getUsername(), qtell.toString());
			return;
		}

		boolean helpFileMatched = false;
		for (final String file : files) {
			if (topic.equalsIgnoreCase(file)) {
				helpFileMatched = true;
				break;
			}
		}
		if (!helpFileMatched) {
			channelBot.qtell(context.getUsername(), String.format("Not a valid help file topic: %s", topic));
			return;
		}

		try {
			final File helpFile = new File(String.format("%s%s%s.txt", helpFileDirectoryPath, File.separator, topic));
			if (!helpFile.exists()) {
				qtell.append(String.format("No help file for %s exists.", topic));
				return;
			}

			qtell.append(String.format("Help file for %s:\\n", topic));
			qtell.append("-----").append("\\n");
			try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(helpFile))) {
				while (bufferedReader.ready()) {
					qtell.append(bufferedReader.readLine()).append("\\n");
				}
			}
			qtell.append("------");
			channelBot.qtell(context.getUsername(), qtell.toString());
		} catch (IOException ioe) {
			ChannelBot.logError(ioe);
			channelBot.qtell(context.getUsername(), String.format("\\nAn error has occurred showing the \"%s\" help file.\\nPlease report this to %s immediately.", topic, ChannelBot.getChannelBotAdministratorUsername()));
		}
	}
}
