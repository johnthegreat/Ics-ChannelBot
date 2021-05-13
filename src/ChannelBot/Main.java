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

import ChannelBot.config.impl.MainChannelBotConfigurationProvider;

import java.io.*;
import java.sql.Connection;
import java.util.*;

public class Main {
    // Do all the main work here
    public static void main(String[] args) {
        String pid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();

        if (args.length == 0) {
            System.out.println("Required configuration path parameter not provided. Please add the configuration file path and try again.");
            System.exit(1);
        }

        ChannelBotFactory channelBotFactory = new ChannelBotFactory();

        final ChannelBot bot = channelBotFactory.createChannelBot();
        ChannelBot.singletonInstance = bot;

        bot.setUserMap(new HashMap<>());
        bot.botConfigFilePath = args[0];
        if (!new File(bot.botConfigFilePath).exists()) {
            System.out.println("Cannot find configuration path " + bot.botConfigFilePath + ".\n" + "Exiting now.");
            System.exit(1);
        }

        Properties configuration;
        try {
            File configFile = new File(bot.botConfigFilePath);
            configuration = MainChannelBotConfigurationProvider.withConfigurationPath(configFile).getConfiguration();
            bot.digestConfiguration(configuration);
            bot.setProperties(configuration);
        } catch (Exception e) {
            System.err.println("There was a problem loading configuration.");
            ChannelBot.logError(e);
            System.exit(1);
        }

        bot.setChannelFactory(new ChannelFactory());
        bot.setMetrics(new Metrics());

        String username = bot.getProperties().getProperty("server.username");

        System.out.println("Welcome to " + username + " build " + ChannelBot.getPublishDate() + "\n");

        System.out.println(pid);
        System.out.println(TimeZoneUtils.getTime(TimeZone.getDefault(),
                new java.util.Date()));
        System.out.println();

        // load the stuff that we need to know
        try {
            SQLiteConnectionFactory sqLiteConnectionFactory = new SQLiteConnectionFactory();
            Connection databaseConnection = sqLiteConnectionFactory.connect(bot.getProperties().getProperty("config.files.db"));
            bot.setDatabaseConnection(databaseConnection);

            bot.setDatabaseProviderRepository(new DatabaseProviderRepository());

            String pidFilePath = bot.getProperties().getProperty("config.files.pid");
            if (pidFilePath != null && pidFilePath.length() > 0) {
                writePID(pidFilePath, pid);
            }
            bot.loadData();
        } catch (Exception e) {
            ChannelBot.logError(e);
            System.exit(-1);
        }

        LanguageFilterService languageFilterService = new LanguageFilterService();
        String[] badLanguageList = null;
        try {
            badLanguageList = getBadLanguageList(bot.getProperties().getProperty("config.files.language"));
        } catch (Exception e) {
            System.err.println("There was a problem loading the bad language list. No words loaded.");
            ChannelBot.logError(e);
            badLanguageList = new String[0];
        } finally {
            languageFilterService.setFilteredWords(badLanguageList);
        }
        bot.setLanguageFilterService(languageFilterService);

        String address = bot.getProperties().getProperty("server.address");
        String port = bot.getProperties().getProperty("server.port");
        String password = ChannelBot.getPassword();

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

        ServerConnection serverConnection = new ServerConnection();
        serverConnection.setUsername(username);
        serverConnection.setPassword(password);
        serverConnection.connect(address, Integer.parseInt(port));
        serverConnection.writeBatch(new String[] {
                "set Interface ChannelBot(TD) " + ChannelBot.getPublishDate(),
                "set pin 1"
        });
        bot.setServerConnection(serverConnection);

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

        // while(true) keeps the connection to the server alive
        // All actions/reactions take place from right here.
        while (!bot.getServerConnection().getUnderlyingSocket().isClosed()) {
            // try to read from the server
            String in = bot.getServerConnection().read("fics% ");
            in = in.replaceAll("\n\rfics%", "").trim();
            try {
                ChannelBot.parseCommand(in);
            } catch (Exception e) {
                System.err.println("Ignoring line: " + in);
                ChannelBot.logError(e);
            }
        }

        Thread shutdownHook = new Thread(new Runnable() {
            @Override
            public void run() {
                System.err.printf("%s is shutting down now... ",ChannelBot.getUsername());
                try {
                    DataPersistenceService.persist(ChannelBot.getInstance());
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("shutdown complete.");
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private static void writePID(String pidFilePath, String pid) throws IOException {
        System.out.printf("Writing pid to file... %s%n", pidFilePath);
        File pidFile = new File(pidFilePath);
        pidFile.deleteOnExit();
        FileWriter fileWriter = new FileWriter(pidFile);
        fileWriter.write(pid);
        fileWriter.close();
    }

    private static String[] getBadLanguageList(String filePath) throws Exception {
        if (filePath == null || filePath.isEmpty()) {
            System.out.println("Could not load bad language list.");
            return new String[0];
        }

        File f = new File(filePath);
        if (!f.exists()) {
            System.out.println("Could not load bad language list.");
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
     * @throws IOException
     *             If a reading error occurs.
     */
    private static void consoleInput() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String command = reader.readLine();
        ChannelBot.getInstance().getServerConnection().write(command);
    }
}
