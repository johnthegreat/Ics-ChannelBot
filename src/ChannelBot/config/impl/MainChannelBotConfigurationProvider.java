/**
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
package ChannelBot.config.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import ChannelBot.config.ConfigurationProvider;

public class MainChannelBotConfigurationProvider implements ConfigurationProvider {
	private File configFile;
	
	private MainChannelBotConfigurationProvider() {
		// TODO Auto-generated constructor stub
	}
	
	private MainChannelBotConfigurationProvider(File file) {
		this.configFile = file;
	}
	
	public static MainChannelBotConfigurationProvider withConfigurationPath(File file) {
		return new MainChannelBotConfigurationProvider(file);
	}
	
	private void expandProperties(Properties properties) {
		// expand file paths from {botStorageDir}
		String[] propertiesToExpand = { "config.files.db", "config.files.pid",
				"config.files.helpfiles", "config.files.language",
				"config.files.errorLog", "config.files.base",
				"config.files.ban", "config.files.channels" };
		
		for(int i=0;i<propertiesToExpand.length;i++) {
			String myProperty = properties.getProperty(propertiesToExpand[i]);
			properties.setProperty(propertiesToExpand[i],myProperty.replace("{botStorageDir}", properties.getProperty("config.files.botStorageDir")));
		}
	}
	
	private Properties loadProperties() throws IOException {
		Properties properties = new Properties();
		properties.load(new FileReader(this.configFile));
		this.expandProperties(properties);
		
		return properties;
	}

	@Override
	public Properties getConfiguration() throws Exception {
		return loadProperties();
	}

}
