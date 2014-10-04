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
		String topic = getArguments();
		if (topic != null) {
			topic = topic.trim();
		}
		while (topic.contains("./")) {
			topic = topic.replace("./", "");
		}
		
		StringBuilder qtell = new StringBuilder();
		if (!topic.equals("")) {
			try {
				File file = new File(ChannelBot.getInstance().getProperties().getProperty("config.files.helpfiles") + File.separator + topic + ".txt");
				if (file.exists()) {
					qtell.append("Help file for " + topic + ":" + "\\n");
					qtell.append("-----\\n");
					BufferedReader read = new BufferedReader(new FileReader(
							file));
					while (read.ready()) {
						qtell.append(read.readLine() + "\\n");
					}
					read.close();
					qtell.append("------");
				} else {
					qtell.append("No help file for " + topic + " exists.");
				}
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), qtell.toString());
			} catch (IOException ioe) {
				ChannelBot.logError(ioe);
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(), "\\nAn error has occurred showing the " + "\""
						+ topic + "\" help file.\\nPlease report this to "
						+ ChannelBot.programmer + " immediately.");
			}
		} else {
			qtell.append("Available help files: (tell me \"help <topic>\" to read a help file)\\n");
			qtell.append("-----\\n");
			File fileList = new File(ChannelBot.getInstance().getProperties().getProperty("config.files.helpfiles"));
			String[] files = fileList.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".txt");
				}
			});
			if (files == null) {
				System.err.println("Directory not found: " + fileList.getAbsolutePath());
				ChannelBot.getInstance().getServerConnection().qtell(getUsername(),"Help files are missing, please report this to " + ChannelBot.programmer + " immediately.");
				return;
			}
			
			if (files.length > 0) {
				for (int i = 0; i < files.length; i++) {
					String str = files[i].replaceAll(".txt", "");
					if (!str.equals("")) {
						qtell.append(str + "\\n");
					}
				}
				//QTELL.append(files[files.length - 1].replaceAll(".txt", "") + "\\n");
			} else {
				qtell.append("No help files available.\\n");
			}
			qtell.append("-----");
			ChannelBot.getInstance().getServerConnection().qtell(getUsername(), qtell.toString());
		}
	}

}
