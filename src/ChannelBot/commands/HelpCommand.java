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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import ChannelBot.ChannelBot;
import ChannelBot.Command;

public class HelpCommand extends Command {

	@Override
	public void execute() {
		File fileList = new File(ChannelBot.getInstance().getProperties().getProperty("config.files.helpfiles"));
		String[] files = fileList.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".txt");
			}
		});
		if (files == null) {
			System.err.println("Directory not found: " + fileList.getAbsolutePath());
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(),String.format("Help files are missing, please report to %s.",ChannelBot.programmer));
			return;
		}
		if (files.length == 0) {
			System.err.println("No help files found: " + fileList.getAbsolutePath());
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(),String.format("No help files are available, please report to %s.",ChannelBot.programmer));
			return;
		}

		for (int i = 0; i < files.length; i++) {
			files[i] = files[i].replaceAll(".txt", "");
		}

		String topic = getArguments();
		if (topic != null) {
			topic = topic.trim();
		}
		
		StringBuilder qtell = new StringBuilder();
		if (topic != null && !topic.equals("")) {
			if (!topic.matches("^[a-zA-Z]+$")) {
				System.err.println(String.format("Not a valid help file topic: %s",topic));
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(),String.format("Not a valid help file topic: %s",topic));
				return;
			}

			while (topic.contains("./")) {
				topic = topic.replace("./", "");
			}

			for(String file : files) {
				if (topic.equalsIgnoreCase(file)) {
					topic = file;
					break;
				}
			}

			try {
				File file = new File(ChannelBot.getInstance().getProperties().getProperty("config.files.helpfiles") + File.separator + topic + ".txt");
				if (file.exists()) {
					qtell.append(String.format("Help file for %s:\\n",topic));
					qtell.append("-----\\n");
					BufferedReader read = new BufferedReader(new FileReader(file));
					while (read.ready()) {
						qtell.append(read.readLine()).append("\\n");
					}
					read.close();
					qtell.append("------");
				} else {
					qtell.append(String.format("No help file for %s exists.",topic));
				}
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), qtell.toString());
			} catch (IOException ioe) {
				ChannelBot.logError(ioe);
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), "\\nAn error has occurred showing the " + "\""
						+ topic + "\" help file.\\nPlease report this to " + ChannelBot.programmer + " immediately.");
			}
		} else {
			qtell.append("Available help files: (tell me \"help <topic>\" to read a help file)\\n");
			qtell.append("-----\\n");

			for(String str : files) {
				if (str.length() > 0) {
					qtell.append(str).append("\\n");
				}
			}
			qtell.append("-----");
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(), qtell.toString());
		}
	}

}
