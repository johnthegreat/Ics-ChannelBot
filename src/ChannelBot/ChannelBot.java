/**
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2009-2020 John Nahlen
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ChannelBot.config.impl.MainChannelBotConfigurationProvider;
import ChannelBot.persist.DatabaseConnection;
import ChannelBot.persist.SQLiteConnection;

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
		
		ChannelBot bot = getInstance();
		
		if (line.startsWith(String.format("**** %s has arrived - you can't both be logged in. ****",ChannelBot.getUsername()))) {
			// We have just been disconnected from the server
			// ChannelBot has logged in somewhere else
			// Do not attempt to reconnect, but do disconnect from our send
			bot.getServerConnection().disconnect();
			return false;
		}
		
		// We can't presume that it's a tell to ChannelBot if we see " tells you: " in a line. For example: 
		// johnthegreat(SR)(50): botchvinik tells you: hehe kewl
		boolean isProbablyTell = line.contains(" tells you: ");
		if (isProbablyTell) {
			Pattern tellPattern = patternService.get("^([a-zA-Z]{3,17})(\\(.*\\))? tells you: (.*)$");
			Matcher tellMatcher = tellPattern.matcher(line);
			if (tellMatcher.matches()) {
				// We now know that this is a personal tell.
				System.out.println(debugRegexMatch(tellMatcher));
				System.out.println(java.util.Arrays.toString(patternService.getKeys()));
				
				String username = tellMatcher.group(1);
				String tags = tellMatcher.group(2);
				String message = tellMatcher.group(3);
				boolean userIsGuest = tags != null && tags.equals("(U)");
				if (userIsGuest) {
					// Functionality for when user is a guest can be built out at a later time
					// Also determine if guests should be allowed to use the program
					System.out.println(String.format("User %s is a guest.",username));
				}
				
				if (!bot.userMap.containsKey(username.toLowerCase())) {
					bot.addToUserBase(username);
				}
				
				if (bot.hasChangedHandle(username)) {
					bot.doHandleChange(bot.getUser(username), username);
				}
				
				boolean isProgrammer = username.equals(ChannelBot.programmer);
				
				if (bot.getBannedUsers().contains(username) && !isProgrammer) {
					bot.getServerConnection().qtell(username, ChannelBot.getUsername() + ": You have been banned from using these services.");
					return false;
				}
				
				if (!isProgrammer && bot.isBotDisabled) {
					getInstance().getServerConnection().qtell(username, 
							getInstance().getServerConnection().getUsername() + " is currently undergoing " +
									"maintainence and is unavailable for use. Apologies for the inconvenience.");
					return false;
				}
				
				Pattern messagePattern = patternService.get("^((?:\\+|\\-|=)?[a-zA-Z]+(?:-[a-zA-Z]+)?)(?: (.*))?$");
				Matcher messageMatcher = messagePattern.matcher(message);
				if (messageMatcher.matches()) {
					System.out.println(debugRegexMatch(messageMatcher));
					String commandName = messageMatcher.group(1);
					String commandArgs = messageMatcher.groupCount() > 1 && messageMatcher.group(2) != null ? messageMatcher.group(2).trim() : "";
					int channelNumber = 0;
					
					// check to see if the channel number is provided as the first argument
					Matcher matcher = patternService.get("(\\d+)(?: (.*))?$").matcher(commandArgs);
					if (matcher.matches()) {
						channelNumber = Integer.parseInt(matcher.group(1));
						commandArgs = matcher.groupCount() > 1 && matcher.group(2) != null ? matcher.group(2).trim() : "";
					}
					
					Command command = CommandResolver.resolveCommand(commandName);
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
		Pattern p = Pattern.compile("\\*qtell ([a-zA-Z]{3,17}) (0|1)\\*");
		Matcher m = p.matcher(in);
		if (m.matches()) {
			String username = m.group(1);
			String isOnline = m.group(2);
			User u = getInstance().getUser(username);
			if (u != null) {
				boolean myIsOnline = isOnline.equals("0");
				u.setOnline(myIsOnline);
			}
		} else {
			System.out.println(in + " didn't match pattern: " + p.pattern());
		}
	}
	
	/**
	 * 
	 * @param m
	 */
	public static void parsePIN(Matcher m) {
		String name = m.group(1);
		String didWhat = m.group(2);	
		if (didWhat.equals("disconnected")) {
			// this code happens when the user disconnects.
			// can be used for removing a user from the channel (to save bandwidth), etc.
			User u = getInstance().getUser(name);
			if (u != null) u.setOnline(false);
		} else if (didWhat.equals("connected")) {
			// this code happens when the user connects.
			// can be used for greeting, reloading channel list, etc.
			User u = getInstance().getUser(name);
			if (u != null) u.setOnline(true);
		}
	}
	
	/**
	 * Removes a user from the user base.
	 * Returns if successful.
	 * @param username
	 * @return
	 */
	public static boolean removeFromUserBase(String username) {
		User user = getInstance().getUser(username);
		boolean b = false;
		if (user != null) {
			b = getInstance().getUserList().remove(user);
		}
		getInstance().allUsernamesCacheStale = true;
		return b;
	}
	
	public static void logError(Exception e) {
		try {
			FileWriter filewriter = new FileWriter(new File(getInstance().getProperties().getProperty("config.files.errorLog")),true);
			PrintWriter s = new PrintWriter(filewriter);
			s.append(new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa z").format(new Date()) + "\r\n");
			e.printStackTrace(s);
			s.append("\r\n\r\n");
			s.flush();
			s.close();
			e.printStackTrace(System.err);
			System.exit(0);
		} catch(Exception myexception) {
			myexception.printStackTrace();
		}
	}

	private final List<User> userList = new ArrayList<User>();
	private HashMap<String,User> userMap;
	private List<String> bannedUsers = new ArrayList<String>();
	/** Configuration option on whether to log channel tells or not. */
	public static boolean logTells = false;
	//public String botStorageDir = null;
	public String botConfigFilePath = null;
	public boolean isBotDisabled;
	private NavigableMap<Integer, Channel> Channels = new TreeMap<Integer, Channel>();
	public ServerConnection serverConnection;
	public String[] allUsernamesCache;
	public boolean allUsernamesCacheStale = false;
	private Properties config;
	private PersistanceProvider persistanceProvider;
	private ChannelFactory channelFactory;
	private Metrics metrics;
	private DatabaseConnection databaseConnection;
	
	protected ChannelBot() {
		
	}

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
		
		bot.setUserMap(new HashMap<String,User>());
		bot.botConfigFilePath = args[0];
		bot.setPersistanceProvider(new PersistanceProvider());
		bot.setChannelFactory(new ChannelFactory());
		bot.setMetrics(new Metrics());
		if (!new File(bot.botConfigFilePath).exists()) {
			System.out.println("Cannot find configuration path " + bot.botConfigFilePath + ".\n" + "Exiting now.");
			System.exit(1);
		}
		
		try {
			File configFile = new File(bot.botConfigFilePath);
			Properties configuration = MainChannelBotConfigurationProvider.withConfigurationPath(configFile).getConfiguration();
			bot.digestConfiguration(configuration);
			bot.config = configuration;
		} catch (Exception e) {
			System.err.println("There was a problem loading configuration.");
			ChannelBot.logError(e);
		}
		
		String username = bot.config.getProperty("server.username");
		
		System.out.println("Welcome to " + username + " build " + getPublishDate() + "\n");
		System.out.println(pid);
		System.out.println(TimeZoneUtils.getTime(TimeZone.getDefault(),
				new java.util.Date()));
		System.out.println();

		// load the stuff that we need to know
		try {
			bot.setDatabaseConnection(new SQLiteConnection());
			bot.getDatabaseConnection().setConnection(bot.getDatabaseConnection().connect(bot.config.getProperty("config.files.db")));
			
			bot.writePID(pid);
			bot.loadData();
		} catch (Exception e) {
			logError(e);
			System.exit(-1);
		}
		
		try {
			Channel.setFilteredWords(getBadLanguageList(bot.config.getProperty("config.files.language")));
		} catch (Exception e) {
			System.err.println("There was a problem loading the bad language list. No words loaded.");
			logError(e);
			Channel.setFilteredWords(new String[0]);
		}
		
		String address = bot.config.getProperty("server.address");
		String port = bot.config.getProperty("server.port");
		String password = getPassword();
		
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
						logError(e);
					}
				}
			}
		});
		t.setDaemon(true);
		t.start();
		
		// the code below is used to flush the channel list to file every 15 minutes to avoid data loss
		/*TimerTask task = new TimerTask() {
			public void run() {
				System.out.println("Flushing data to prevent loss...");
				try {
					getInstance().getPersistanceProvider().saveChannelList(getInstance().getChannelMap());
					getInstance().getPersistanceProvider().dumpBaseList();
				} catch (IOException e) {
					System.err.println("There was a problem saving information.");
					ChannelBot.logError(e);
				}
			}
		};
		Timer timer = new Timer(true);
		timer.schedule(task,900000,900000*4); // 15 * 60000
		*/

		// while(true) keeps the connection to the server alive
		// All actions/reactions take place from right here.
		while (true) {
			if (bot.getServerConnection().getUnderlyingSocket().isClosed()) {
				break;
			}
			
			// try to read from the server
			String in = bot.getServerConnection().read("fics% "); 
			in = in.replaceAll("\n\rfics%", "").trim();
			try {
				parseCommand(in);
			} catch (Exception e) {
				System.err.println("Ignoring line: " + in);
				ChannelBot.logError(e);
				continue;
			}
		}
		
		Thread shutdownHook = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.err.print(String.format("%s is shutting down now... ",getUsername()));
//					getInstance().getPersistanceProvider().dumpBaseList();
//					getInstance().getPersistanceProvider().saveChannelList(getInstance().getChannelMap());
//					getInstance().getPersistanceProvider().dumpBannedList();
					System.err.println("shutdown complete.");
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}
	
	public void loadData() {
		Thread thread = new Thread(new Runnable() { public void run() {
			try {
				/*try {
					java.util.Map<Integer,Channel> map = LegacyContentLoader.loadChannelList();
					for(Integer chNum : map.keySet()) {
						Channel channel = map.get(chNum);
						List<String> members = channel.getMembers();
						for (String member : members) {
							User user = getUser(member);
							if (user != null)
								getPersistanceProvider().addChannelUserToDb(channel, user);
						}
					}
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}*/
				
				// load channels
				List<Channel> channels = getPersistanceProvider().loadChannelListFromDb();
				NavigableMap<Integer, Channel> channelMap = new TreeMap<Integer, Channel>();
				for(Channel channel : channels) {
					channelMap.put(channel.getID(), channel);
				}
				clearChannelList();
				setChannels(channelMap);
				
				// load user base list
				userList.addAll(getPersistanceProvider().loadUserBase());
				for(User user : userList) {
					userMap.put(user.getName().toLowerCase(), user);
				}
				
				linkChannelsToUsers();
				
				// load banned list
				setBannedUsers(getPersistanceProvider().loadUserListFromDb("ban"));
			} catch (SQLException e) {
				ChannelBot.logError(e);
				// Unable to load data is a fatal error
				System.err.println("Unable to load data. System is shutting down...");
				System.exit(-1);
			}
		} });
		thread.start();
	}

	public Channel[] getChannels() {
		java.util.Set<Integer> set = Channels.keySet();
		Integer[] s = set.toArray(new Integer[set.size()]);
		Channel[] chan = new Channel[s.length];
		for (int i = 0; i < s.length; i++) {
			chan[i] = Channels.get(s[i]);
		}
		return chan;
	}
	
	public NavigableMap<Integer, Channel> getChannelMap() {
		return Channels;
	}
	
	public void watchForConfigurationChanges() {
		
	}
	
	private void digestConfiguration(Properties properties) {
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
	
	private void writePID(String pid) throws IOException {
		String pidFilePath = getProperties().getProperty("config.files.pid");
		if (pidFilePath != null && !pidFilePath.equals("null")) {
			System.out.println(String.format("Writing pid to file... %s",pidFilePath));
			File pidFile = new File(pidFilePath);
			pidFile.deleteOnExit();
			FileWriter fileWriter = new FileWriter(pidFile);
			fileWriter.write(pid);
			fileWriter.close();
		} else {
			System.out.println("pid will not be written to file");
		}
	}
	
	private boolean hasChangedHandle(String username) {
		String[] usernames = getAllUsernames();
		// do a case-insensitive search
		int pos = java.util.Arrays.binarySearch(usernames, username,String.CASE_INSENSITIVE_ORDER);
		if (pos >= 0) {
			String myusername = usernames[pos];
			// if the handle does not match (case sensitive), the handle has changed.
			if (!myusername.equals(username) && myusername.equalsIgnoreCase(username)) {
				System.err.println("handleChanged " + myusername + " " + username);
				return true;
			}
		}
		return false;
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
		Channel[] chList = getChannels();
		for(Channel c : chList) {
			for(String member : c.getMembers()) {
				User user = getUser(member);
				if (user == null) {
					continue;
				}
				TreeSet<Integer> list = user.getInChannels();
				list.add(c.getID());
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
	
	public void doHandleChange(User user,String username) {
		String oldName = user.getName();
		System.out.println("Changing Handle: " + oldName + " to " + username);
		int mypos = getUserList().indexOf(user);
		user.setName(username);
		if (mypos >= 0) {
			getUserList().set(mypos,user);
		}
		allUsernamesCacheStale = true;
		userMap.put(username.toLowerCase(),user);
		
		Channel[] channels = getChannels();
		for(int i=0;i<channels.length;i++) {
			Channel channel = channels[i];
			int pos = channel.getMembers().indexOf(oldName);
			if (pos >= 0) {
				channel.getMembers().set(pos, username);
				
				mypos = channel.getModeratorsAsList().indexOf(oldName);
				if (mypos >= 0) {
					channel.getModeratorsAsList().set(mypos, username);
					if (channel.getHeadModerator().equals(oldName)) {
						System.out.println("Updating head moderator from " + oldName + " to " + username);
						channel.setHeadModerator(username);
					}
				}
			}
		}
		
		int pos = getBannedUsers().indexOf(oldName);
		if (pos >= 0) {
			getBannedUsers().set(pos, username);
		}
		
		try {
			getPersistanceProvider().saveUserToDb(user);
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
			getPersistanceProvider().deleteChannelFromDb(channel.getID());

			for(String memberUsername : channel.getMembers()) {
				User memberUser = getUser(memberUsername);
				if (memberUser != null) {
					memberUser.getInChannels().remove(channel.getID());
					getPersistanceProvider().saveUserToDb(memberUser);
				}
			}
		} catch (Exception e) {
			System.err.println("There was a problem saving information.");
			ChannelBot.logError(e);
		}

		Channels.remove(channel.getID());
	}
	
	public Channel getChannel(int channelNumber) {
		return Channels.get(Integer.valueOf(channelNumber));
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
		
		getUserList().add(user);
		userMap.put(username.toLowerCase(), user);
		try {
			this.getPersistanceProvider().saveUserToDb(user);
		} catch (SQLException e) {
			ChannelBot.logError(e);
		}
		allUsernamesCacheStale = true;
		return true;
	}
	
	public void clearBannedUsers() {
		bannedUsers.clear();
	}

	public void clearChannelList() {
		Channels.clear();
	}

	public String[] getAllUsernames() {
		List<User> userList = getUserList();
		int count = userList.size();
		
		if (allUsernamesCache != null && count == allUsernamesCache.length && !allUsernamesCacheStale) {
			return allUsernamesCache;
		}
		
		String[] arr = new String[count];
		for (int i = 0; i < count; i++) {
			arr[i] = userList.get(i).getName();
		}
		java.util.Arrays.sort(arr,0,arr.length,String.CASE_INSENSITIVE_ORDER);

		allUsernamesCache = arr;
		allUsernamesCacheStale = false;
		System.out.println("Updating allUsernamesCache: " + java.util.Arrays.toString(allUsernamesCache));
		return arr;
	}

	public String getAllUsersAndAllChannels(User u) {
		StringBuilder b = new StringBuilder();
		TreeSet<Integer> inList = u.getInChannels();
		
		final String newline = "\\n";
		int numChannels = 0;
		int numUsers = 0;
		for(Integer i : inList) {
			Channel c = Channels.get(i);
			numChannels++;
			String chName = c.getName();
			b.append("--" + (chName.equals("")?"[No Name]":chName) + "--" + newline);
			List<String> members = c.getMembers();
			for(String s : members) {
				numUsers++;
				b.append(" " + s + newline);
			}
		}
		b.append(newline + numChannels + " channels, " + numUsers + " users listed.");
		return b.toString();
	}
	
	public static String[] getBadLanguageList(String filePath) throws Exception {
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
		List<String> words = new ArrayList<String>();
		while (reader.ready()) {
			String word = ((reader.readLine()).replaceAll("\r", "").replaceAll("\n", ""));
			words.add(word);
		}
		reader.close();
		return words.toArray(new String[words.size()]);
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
	
	public List<User> getUserList() {
		return userList;
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

	public PersistanceProvider getPersistanceProvider() {
		return persistanceProvider;
	}

	public void setPersistanceProvider(PersistanceProvider persistanceProvider) {
		this.persistanceProvider = persistanceProvider;
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

	public DatabaseConnection getDatabaseConnection() {
		return databaseConnection;
	}

	public void setDatabaseConnection(DatabaseConnection databaseConnection) {
		this.databaseConnection = databaseConnection;
	}

	public boolean isChannelNumberAvailable(int channelNumber) {
		return !Channels.containsKey(channelNumber);
	}
}
