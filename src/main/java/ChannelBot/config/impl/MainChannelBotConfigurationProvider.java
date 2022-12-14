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
package ChannelBot.config.impl;

import ChannelBot.config.ConfigurationProvider;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class MainChannelBotConfigurationProvider implements ConfigurationProvider {
	private final File configFile;

	private MainChannelBotConfigurationProvider(File file) {
		this.configFile = file;
	}

	public static MainChannelBotConfigurationProvider withConfigurationPath(File file) {
		return new MainChannelBotConfigurationProvider(file);
	}

	private void expandProperties(final Properties properties) {
		// expand file paths from {botStorageDir}
		final String[] propertiesToExpand = {"config.files.db", "config.files.pid",
				"config.files.helpfiles", "config.files.language",
				"config.files.errorLog", "config.files.base",
				"config.files.ban", "config.files.channels"};

		for (final String property : propertiesToExpand) {
			final String myProperty = properties.getProperty(property);
			properties.setProperty(property, myProperty.replace("{botStorageDir}", properties.getProperty("config.files.botStorageDir")));
		}
	}

	private Properties loadProperties() throws IOException {
		final Properties properties = new Properties();
		properties.load(new FileReader(this.configFile));
		this.expandProperties(properties);

		return properties;
	}

	@Override
	public Properties getConfiguration() throws Exception {
		return loadProperties();
	}
}
