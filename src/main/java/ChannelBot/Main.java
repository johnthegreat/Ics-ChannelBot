/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2021 John Nahlen
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

import ChannelBot.commands.resolver.impl.LegacyCommandResolver;
import ChannelBot.config.impl.MainChannelBotConfigurationProvider;

import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

public class Main {
	public static void main(final String[] args) {
		final String pid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();

		if (args.length == 0) {
			System.out.println("Required configuration path parameter not provided. Please add the configuration file path and try again.");
			System.exit(1);
		}

		final ChannelBot channelBot = new ChannelBot(new LegacyCommandResolver());
		ChannelBot.singletonInstance = channelBot;

		channelBot.botConfigFilePath = args[0];
		final File botConfigFile = new File(channelBot.botConfigFilePath);
		if (!botConfigFile.exists()) {
			System.err.printf("Cannot find configuration path %s.%nExiting.%n", channelBot.botConfigFilePath);
			System.exit(1);
		}

		Properties configuration;
		try {
			configuration = MainChannelBotConfigurationProvider.withConfigurationPath(botConfigFile).getConfiguration();
			channelBot.digestConfiguration(configuration);
			channelBot.setProperties(configuration);
		} catch (Exception e) {
			System.err.println("There was a problem loading configuration.");
			ChannelBot.logError(e);
			System.exit(1);
		}

		channelBot.setChannelFactory(new ChannelFactory());
		channelBot.setMetrics(new Metrics());

		final String username = channelBot.getProperties().getProperty("server.username");

		System.out.printf("Welcome to %s build %s.%n", username, ChannelBot.getPublishDate());

		System.out.println(pid);
		System.out.println(TimeZoneUtils.getTime(TimeZone.getDefault(),
				new java.util.Date()));
		System.out.println();

		// load the stuff that we need to know
		try {
			final Connection databaseConnection = SQLiteConnectionFactory.createConnection(channelBot.getProperties().getProperty("config.files.db"));
			channelBot.setDatabaseProviderRepository(new DatabaseProviderRepository(databaseConnection));

			final String pidFilePath = channelBot.getProperties().getProperty("config.files.pid");
			if (pidFilePath != null && pidFilePath.length() > 0) {
				writePID(pidFilePath, pid);
			}
			channelBot.loadData();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			ChannelBot.logError(e);
			System.exit(-1);
		}

		final LanguageFilterService languageFilterService = new LanguageFilterService();
		String[] badLanguageList = null;
		try {
			badLanguageList = getBadLanguageList(channelBot.getProperties().getProperty("config.files.language"));
		} catch (Exception e) {
			System.err.println("There was a problem loading the bad language list. No words loaded.");
			ChannelBot.logError(e);
			badLanguageList = new String[0];
		} finally {
			languageFilterService.setFilteredWords(badLanguageList);
		}
		channelBot.setLanguageFilterService(languageFilterService);

		final String address = channelBot.getProperties().getProperty("server.address");
		final String port = channelBot.getProperties().getProperty("server.port");
		final String password = ChannelBot.getPassword();

		// do validation
		if (address == null || port == null || username == null || password == null) {
			System.err.println("Required server params not provided. Shutting down...");
			System.exit(1);
		} else if (address.isEmpty() || port.isEmpty() || username.isEmpty() || password.isEmpty()) {
			System.err.println("Required server params not provided. Shutting down...");
			System.exit(1);
		} else if (!StringUtils.isNumeric(port)) {
			System.err.println("Incorrect server params provided. Port is not numeric. Shutting down...");
			System.exit(1);
		}

		final ServerConnection serverConnection = new ServerConnection("\n\rfics%");
		try {
			serverConnection.setOutPrintStream(System.out);
			serverConnection.setErrPrintStream(System.err);

			serverConnection.connect(address, Integer.parseInt(port), username, password);

			serverConnection.writeBatch(new String[]{
					String.format("set Interface ChannelBot(TD) %s", ChannelBot.getPublishDate()),
					"set pin 1"
			});
			channelBot.setServerConnection(serverConnection);
		} catch (IOException | ServerConnectionClosedException e) {
			ChannelBot.logError(e);
			System.exit(1);
		}

		// Allow console input from System.in
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						consoleInput();
					} catch (IOException e) {
						ChannelBot.logError(e);
					}
				}
			}
		});
		t.setDaemon(true);
		t.start();

		final Thread serverReaderThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						final String fullyRead = channelBot.getServerConnection().readFully().trim();
						final String[] lines = fullyRead.split(channelBot.getServerConnection().getDefaultPrompt());
						for (final String line : lines) {
							ChannelBot.parseCommand(line.trim());
						}
					} catch (IOException | ServerConnectionClosedException e) {
						e.printStackTrace(System.err);
						System.exit(1);
					}
				}
			}
		});
		serverReaderThread.start();

		final Thread shutdownHook = new Thread(new Runnable() {
			@Override
			public void run() {
				System.err.printf("%s is shutting down now... ", ChannelBot.getUsername());
				channelBot.initiateShutdown();
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	private static void writePID(final String pidFilePath, final String pid) throws IOException {
		System.out.printf("Writing pid to file... %s%n", pidFilePath);

		final File pidFile = new File(pidFilePath);
		pidFile.deleteOnExit();
		try (final FileWriter fileWriter = new FileWriter(pidFile)) {
			fileWriter.write(pid);
		}
	}

	private static String[] getBadLanguageList(final String filePath) throws Exception {
		if (filePath == null || filePath.isEmpty()) {
			System.err.println("Could not load bad language list.");
			return new String[0];
		}

		File f = new File(filePath);
		if (!f.exists()) {
			System.err.println("Could not load bad language list.");
			return new String[0];
		}

		BufferedReader reader = new BufferedReader(new FileReader(f));
		List<String> words = new ArrayList<>();
		while (reader.ready()) {
			String word = ((reader.readLine()).replaceAll("\r", "").replaceAll("\n", ""));
			words.add(word);
		}
		reader.close();
		return words.toArray(new String[0]);
	}

	/**
	 * Allows console input directly from the user through <tt>System.in</tt>.
	 * Immediately writes the input to the server.
	 *
	 * @throws IOException If a reading error occurs.
	 */
	private static void consoleInput() throws IOException {
		InputStreamReader inputStreamReader = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(inputStreamReader);
		String command = reader.readLine();
		ChannelBot.getInstance().getServerConnection().write(command);
	}
}
