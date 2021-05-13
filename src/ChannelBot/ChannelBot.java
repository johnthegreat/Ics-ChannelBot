/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2009-2021 John Nahlen
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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class for the ChannelBot project.
 * @author John
 */
public class ChannelBot {
	// Ideas for improvement
	// TODO Add more javadoc comments
	// TODO allow channel head moderators to provide their own language filters
	// TODO allow for customized (configurable) standard messages
	// TODO potentially allow multiple bots to run on one application instance
	// TODO begin to use a logger (e.g. log4j) instead of sysout / syserr
	
	public static ChannelBot singletonInstance;
	private static String publishdate;
	public static String programmer;
	public static int MAX_NAME_LENGTH;
	public static int MAX_PASSWORD_LENGTH;
	public static int MAX_CHANNEL_MODERATORS;
	public static final String DATE_FORMAT = "MM/dd/yyyy hh:mm:ss aa z";
	
	/** Gives some debugging information from a pattern match. 
	 * Example output: <pre>5 [Jonacus, 1843, 137, kibitzes, winners]</pre>
	 * Where "5" is the number of groups found, and the information in the brackets are the groups, seperated by commas.
	 * */
	public static String debugRegexMatch(Matcher m) {
		StringBuilder str = new StringBuilder(m.groupCount() + " [");
		for(int i=1;i<=m.groupCount();i++) {
			str.append(m.group(i));
			if (i != m.groupCount()) 
				str.append(", ");
		}
		return str.toString().trim() + "]";
	}
	
	public static String getPublishDate() {
		return publishdate;
	}
	
	public static ChannelBot getInstance() {
		return singletonInstance;
	}

	public int nextAvailableChannelNumber() {
		int i = Integer.parseInt(getProperties().getProperty("config.channels.startingId"));
		while (Channels.containsKey(i)) {
			i++;
		}
		return i;
	}

	/**
	 * Method that parses user input, and executes the appropriate action.
	 * @param line User input
	 * @return If successful.
	 */
	public static boolean parseCommand(String line) {
		boolean output = true;
		PatternService patternService = PatternService.getInstance();
		
		if (line.startsWith("[")) {
			Matcher m = patternService.get("\\[([a-zA-Z]{3,17})(?: \\(.*\\))? has ((?:dis)?connected).\\]").matcher(line);
			if (m.matches()) {
				parsePIN(m);
				output = false;
			}
		} else if (line.startsWith("*qtell ")) {
			parseEndQTELL(line);
			output = false;
		} else if (line.startsWith("**ANNOUNCEMENT** from")) {
			output = false;
		}
		
		if (output) {
			 // output to the console
			System.out.println(line.replaceAll("\\n\\r","\n"));
		}
		
		ChannelBot channelBot = getInstance();
		
		if (line.startsWith(String.format("**** %s has arrived - you can't both be logged in. ****",ChannelBot.getUsername()))) {
			// We have just been disconnected from the server
			// ChannelBot has logged in somewhere else
			// Do not attempt to reconnect, but do disconnect from our send
			channelBot.getServerConnection().disconnect();
			return false;
		}
		
		// We can't presume that it's a tell to ChannelBot if we see " tells you: " in a line. For example: 
		// johnthegreat(SR)(50): botchvinik tells you: hehe kewl
		boolean isPossiblyTell = line.contains(" tells you: ");
		if (isPossiblyTell) {
			Pattern tellPattern = patternService.get("^([a-zA-Z]{3,17})(\\(.*\\))? tells you: (.*)$");
			Matcher tellMatcher = tellPattern.matcher(line);
			if (tellMatcher.matches()) {
				// We now know that this is a personal tell.
				System.out.println(debugRegexMatch(tellMatcher));
				System.out.println(java.util.Arrays.toString(patternService.getKeys()));
				
				final String username = tellMatcher.group(1);
				final String tags = tellMatcher.group(2);
				final String message = tellMatcher.group(3);
				final boolean userIsGuest = tags != null && tags.equals("(U)");
				if (userIsGuest) {
					// Functionality for when user is a guest can be built out at a later time
					// Also determine if guests should be allowed to use the program
					System.out.printf("User %s is a guest.%n",username);
				}

				User user = channelBot.getUser(username);
				if (user == null) {
					channelBot.addToUserBase(username);
					user = channelBot.getUser(username);
				}

				if (!user.getName().equals(username) && user.getName().equalsIgnoreCase(username)) {
					channelBot.doHandleChange(user, username);
				}
				
				boolean isProgrammer = username.equals(ChannelBot.programmer);
				
				if (channelBot.getBannedUsers().contains(username) && !isProgrammer) {
					channelBot.getServerConnection().qtell(username, ChannelBot.getUsername() + ": You have been banned from using these services.");
					return false;
				}
				
				if (!isProgrammer && channelBot.isBotDisabled) {
					getInstance().getServerConnection().qtell(username, 
							ChannelBot.getUsername() + " is currently undergoing " +
									"maintenance and is unavailable for use. Apologies for the inconvenience.");
					return false;
				}
				
				final Pattern messagePattern = patternService.get("^((?:\\+|\\-|=)?[a-zA-Z]+(?:-[a-zA-Z]+)?)(?: (.*))?$");
				final Matcher messageMatcher = messagePattern.matcher(message);
				if (messageMatcher.matches()) {
					System.out.println(debugRegexMatch(messageMatcher));
					final String commandName = messageMatcher.group(1);
					String commandArgs = messageMatcher.groupCount() > 1 && messageMatcher.group(2) != null ? messageMatcher.group(2).trim() : "";
					int channelNumber = 0;
					
					// check to see if the channel number is provided as the first argument
					final Matcher matcher = patternService.get("(\\d+)(?: (.*))?$").matcher(commandArgs);
					if (matcher.matches()) {
						channelNumber = Integer.parseInt(matcher.group(1));
						commandArgs = matcher.groupCount() > 1 && matcher.group(2) != null ? matcher.group(2).trim() : "";
					}
					
					final Command command = CommandResolver.resolveCommand(commandName);
					if (command != null) {
						command.setCommandName(commandName);
						command.setUsername(username);
						command.setChannelNumber(channelNumber);
						command.setArguments(commandArgs);
						command.execute();
					} else {
						getInstance().getServerConnection().qtell(username, ChannelBot.getUsername() + ": Command not found or not available: " + commandName);
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * A method that was started but never completed. Only thing this is needed
	 * for is determining if a qtell was sent to a user or not.
	 * 
	 * @param in
	 *            Qtell
	 */
	public static void parseEndQTELL(String in) {
		//System.out.println("in = \"" + in + "\"");
		Pattern pattern = PatternService.getInstance().get("\\*qtell ([a-zA-Z]{3,17}) ([0|1])\\*");
		Matcher matcher = pattern.matcher(in);
		if (matcher.matches()) {
			String username = matcher.group(1);
			String isOnline = matcher.group(2);
			User user = getInstance().getUser(username);
			if (user != null) {
				user.setOnline(isOnline.equals("0"));
			}
		} else {
			System.out.println(in + " didn't match pattern: " + pattern.pattern());
		}
	}
	
	public static void parsePIN(Matcher m) {
		String username = m.group(1);
		String didWhat = m.group(2);

		User u = getInstance().getUser(username);
		if (u != null) {
			u.setOnline(didWhat.equals("connected"));
		}
	}

	private static String formatDate(Date date) {
		return (new SimpleDateFormat(ChannelBot.DATE_FORMAT)).format(date);
	}
	
	public static void logError(Exception e) {
		try {
			FileWriter filewriter = new FileWriter(new File(getInstance().getProperties().getProperty("config.files.errorLog")),true);
			PrintWriter s = new PrintWriter(filewriter);
			s.append(formatDate(new Date())).append("\r\n");
			e.printStackTrace(s);
			s.append("\r\n\r\n");
			s.flush();
			s.close();
			e.printStackTrace(System.err);
			System.exit(0);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	private final SortedSet<User> users = new TreeSet<>();
	private HashMap<String,User> userMap;
	private List<String> bannedUsers = new ArrayList<>();
	/** Configuration option on whether to log channel tells or not. */
	public static boolean logTells = false;
	public String botConfigFilePath = null;
	public boolean isBotDisabled;
	private NavigableMap<Integer, Channel> Channels = new TreeMap<>();
	public ServerConnection serverConnection;
	private Properties config;
	private ChannelFactory channelFactory;
	private Metrics metrics;
	private Connection databaseConnection;
	private DatabaseProviderRepository databaseProviderRepository;
	private LanguageFilterService languageFilterService;
	
	protected ChannelBot() {
		
	}
	
	public void loadData() {
		try {
			//
			// Load channels
			//
			List<Channel> channels = getDatabaseProviderRepository().getChannelProvider().getChannels();
			NavigableMap<Integer, Channel> channelMap = new TreeMap<>();
			for(Channel channel : channels) {
				channelMap.put(channel.getID(), channel);
			}
			setChannels(channelMap);

			//
			// Load users
			//
			users.addAll(getDatabaseProviderRepository().getUserProvider().getUsers());
			for(User user : users) {
				userMap.put(user.getName().toLowerCase(), user);
			}

			linkChannelsToUsers();

			//
			// Load ban list
			//
			setBannedUsers(getDatabaseProviderRepository().getUserListProvider().getUserListByName("ban"));
		} catch (SQLException e) {
			ChannelBot.logError(e);
			// Unable to load data is a fatal error
			System.err.println("Unable to load data. System is shutting down...");
			System.exit(-1);
		}
	}

	public Channel[] getChannels() {
		return Channels.values().toArray(new Channel[0]);
	}
	
	public NavigableMap<Integer, Channel> getChannelMap() {
		return Channels;
	}
	
	protected void digestConfiguration(Properties properties) {
		String prop = properties.getProperty("config.files.helpfiles");
		if (prop == null || prop.isEmpty()) {
			System.out.println("Required parameter config.files.helpfiles not provided.\nExiting now.");
			System.exit(1);
		}
		
		prop = properties.getProperty("config.files.errorLog");
		if (prop == null || prop.isEmpty()) {
			System.out.println("Required parameter config.files.errorLog not provided.\nExiting now.");
			System.exit(1);
		}
		prop = properties.getProperty("config.files.base");
		if (prop == null || prop.isEmpty()) {
			System.out.println("Required parameter config.files.base not provided.\nExiting now.");
			System.exit(1);
		}
		prop = properties.getProperty("config.files.channels");
		if (prop == null || prop.isEmpty()) {
			System.out.println("Required parameter config.files.channels not provided.\nExiting now.");
			System.exit(1);
		}
		prop = properties.getProperty("config.channels.maxModCount");
		if (prop == null || prop.isEmpty()) {
			System.out.println("Required parameter config.channels.maxModCount not provided.\nExiting now.");
			System.exit(1);
		}
		MAX_CHANNEL_MODERATORS = Integer.parseInt(prop);
		prop = properties.getProperty("config.channels.maxNameLength");
		if (prop == null || prop.isEmpty()) {
			System.out.println("Required parameter config.channels.maxNameLength not provided.\nExiting now.");
			System.exit(1);
		}
		MAX_NAME_LENGTH = Integer.parseInt(prop);
		prop = properties.getProperty("config.channels.maxPasswordLength");
		if (prop == null || prop.isEmpty()) {
			System.out.println("Required parameter config.channels.maxPasswordLength not provided.\nExiting now.");
			System.exit(1);
		}
		MAX_PASSWORD_LENGTH = Integer.parseInt(prop);
		programmer = properties.getProperty("config.bot.programmer");
		if (programmer == null || programmer.isEmpty()) {
			System.out.println("Required parameter config.bot.programmer not provided.\nExiting now.");
			System.exit(1);
		}
		
		publishdate = properties.getProperty("config.bot.publishDate");
		if (publishdate == null || publishdate.isEmpty()) {
			System.out.println("Required parameter config.bot.publishDate not provided.\nExiting now.");
			System.exit(1);
		}
	}
	
	/**
	 * Returns the user with the given username, or null if not found.<br />
	 * O(1) performance.
	 * 
	 * @param username Username to find
	 * @return
	 */
	public User getUser(String username) {
		return userMap.get(username.toLowerCase());
	}
	
	private void linkChannelsToUsers() {
		Channel[] channels = getChannels();
		for(Channel channel : channels) {
			for(String username : channel.getMembers()) {
				User user = getUser(username);
				if (user != null) {
					user.getInChannels().add(channel.getID());
				}
			}
		}
	}
	
	public void setProperties(Properties properties) {
		this.config = properties;
		
		this.digestConfiguration(properties);
	}
	
	public Properties getProperties() {
		return config;
	}
	
	public void doHandleChange(final User user,final String username) {
		final String oldName = user.getName();
		System.out.println("Changing Handle: " + oldName + " to " + username);
		user.setName(username);
//		int mypos = getUserList().indexOf(user);
//		if (mypos >= 0) {
//			getUserList().set(mypos,user);
//		}
		userMap.put(username.toLowerCase(),user);
		
		Channel[] channels = getChannels();
		for(Channel channel : channels) {
			int memberIndex = channel.getMembers().indexOf(oldName);
			if (memberIndex >= 0) {
				channel.getMembers().set(memberIndex, username);
				
				int moderatorsIndex = channel.getModerators().indexOf(oldName);
				if (moderatorsIndex >= 0) {
					channel.getModerators().set(moderatorsIndex, username);

					if (channel.getHeadModerator().equals(oldName)) {
						System.out.println("Updating head moderator from " + oldName + " to " + username);
						channel.setHeadModerator(username);
					}
				}
			}
		}
		
		int bannedIndex = getBannedUsers().indexOf(oldName);
		if (bannedIndex >= 0) {
			getBannedUsers().set(bannedIndex, username);
		}
		
		try {
			getDatabaseProviderRepository().getUserProvider().updateUser(user);
		} catch (Exception e) {
			System.err.println("There was a problem saving the change.");
			ChannelBot.logError(e);
		}
	}
	
	public void addChannel(Channel channel) {
		Channels.put(channel.getID(), channel);
	}
	
	public void deleteChannel(Channel channel) {
		try {
			getDatabaseProviderRepository().getChannelProvider().deleteChannel(channel.getID());
			getDatabaseProviderRepository().getChannelUserProvider().deleteChannelUsers(channel.getID());

			for(String memberUsername : channel.getMembers()) {
				User memberUser = getUser(memberUsername);
				if (memberUser != null) {
					memberUser.getInChannels().remove(channel.getID());
				}
			}
		} catch (Exception e) {
			System.err.println("There was a problem saving information.");
			ChannelBot.logError(e);
		}

		Channels.remove(channel.getID());
	}
	
	public Channel getChannel(int channelNumber) {
		return Channels.get(channelNumber);
	}

	/**
	 * Adds a user to the user base.
	 * @param username Username
	 * @return Whether the user was added. (returns false if the user already existed)
	 */
	public boolean addToUserBase(String username) {
		// we dont want to insert if it already exists.
		if (getUser(username) != null) {
			return false;
		}
		System.out.println("Adding to user base: " + username);
		
		User user = new User();
		user.setName(username);
		// set defaults
		user.setShowTime(true);
		user.setEcho(true);
		user.setOnline(true);
		user.setHeight(20);
		user.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		getUsers().add(user);
		userMap.put(username.toLowerCase(), user);
		try {
			getDatabaseProviderRepository().getUserProvider().createOrUpdateUser(user);
		} catch (SQLException e) {
			ChannelBot.logError(e);
		}
		return true;
	}

	public List<String> getBannedUsers() {
		return bannedUsers;
	}
	
	public void setBannedUsers(List<String> bannedUsers) {
		this.bannedUsers = bannedUsers;
	}

	public ServerConnection getServerConnection() {
		return serverConnection;
	}
	
	public Set<User> getUsers() {
		return users;
	}

	public static String getUsername() {
		return getInstance().getServerConnection().getUsername();
	}
	
	public static String getPassword() {
		return getInstance().getProperties().getProperty("server.password");
	}
	
	public void setServerConnection(ServerConnection serverConnection) {
		this.serverConnection = serverConnection;
	}

	public ChannelFactory getChannelFactory() {
		return channelFactory;
	}

	public void setChannelFactory(ChannelFactory channelFactory) {
		this.channelFactory = channelFactory;
	}
	
	public void setChannels(NavigableMap<Integer, Channel> channelMap) {
		this.Channels = channelMap;
	}
	
	public void setUserMap(HashMap<String,User> map) {
		this.userMap = map;
	}
	
	public Metrics getMetrics() {
		return metrics;
	}
	
	public void setMetrics(Metrics metrics) {
		this.metrics = metrics;
	}

	public Connection getDatabaseConnection() {
		return databaseConnection;
	}

	public void setDatabaseConnection(Connection databaseConnection) {
		this.databaseConnection = databaseConnection;
	}

	public boolean isChannelNumberAvailable(int channelNumber) {
		return !Channels.containsKey(channelNumber);
	}

	public LanguageFilterService getLanguageFilterService() {
		return languageFilterService;
	}

	public void setLanguageFilterService(LanguageFilterService languageFilterService) {
		this.languageFilterService = languageFilterService;
	}

	public DatabaseProviderRepository getDatabaseProviderRepository() {
		return databaseProviderRepository;
	}

	public void setDatabaseProviderRepository(DatabaseProviderRepository databaseProviderRepository) {
		this.databaseProviderRepository = databaseProviderRepository;
	}
}
