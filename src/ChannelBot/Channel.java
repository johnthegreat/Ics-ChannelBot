/**
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2009-2014 John Nahlen
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

import java.io.FileWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

public class Channel {
	private static String[] filteredWords = new String[0];
	private static String filter = "#%&@#%&@#%&@";
	
	public static ChannelChangedEventListener channelChangedEventListener = new ChannelChangedEventListener() {
		@Override
		public void run() {
			try {
				ChannelBot.getInstance().getPersistanceProvider().saveChannelToDb(getChannel());
				List<String> channelMembers = getChannel().getMembers();
				for(String username : channelMembers) {
					User user = ChannelBot.getInstance().getUser(username);
					if (user != null) {
						ChannelBot.getInstance().getPersistanceProvider().addChannelUserToDb(getChannel(), user);
					} else {
						System.err.println(String.format("User could not be found (so was not persisted): %s",username));
					}
				}
			} catch (SQLException e) {
				ChannelBot.logError(e);
			}
		}
	};

	public static String[] getFilteredWords() {
		return filteredWords;
	}

	public static void setFilteredWords(String[] filteredWords) {
		Channel.filteredWords = filteredWords;
	}

	public static String filterBadLanguage(String mess) {
		String[] filteredWords = getFilteredWords();
		if (filteredWords.length == 0) {
			return mess;
		}
		for (int i = 0; i < filteredWords.length; i++) {
			if (StringUtils.containsIgnoreCase(mess, filteredWords[i])) {
				mess = StringUtils.replaceAllIgnoreCase(mess, filteredWords[i],
						filter.substring(0, filteredWords[i].trim().length()));
			}
		}
		return mess;
	}

	private int chid;
	private long lastTellTime = 0L;

	// Will be changed to List<User> in the future
	private List<String> members;
	private List<String> moderators;
	private String headModerator;
	private String name;
	private String password;
	private LRUTimerCache<ChannelTell> history;
	private int historyTime;
	private List<ChannelChangedEventListener> channelChangedEventListeners;

	public Channel() {
		setMembers(new ArrayList<String>());
		moderators = new ArrayList<String>();
		historyTime = 1000 * 60;
		historyTime *= Integer.parseInt(ChannelBot.getInstance()
				.getProperties().getProperty("config.channels.history"));
		history = new LRUTimerCache<ChannelTell>(historyTime);
		channelChangedEventListeners = new ArrayList<ChannelChangedEventListener>();
	}

	public boolean isModerator(String username) {
		return Collections.binarySearch(moderators, username) >= 0;
	}

	public boolean isHeadModerator(String username) {
		if (username == null || headModerator == null)
			return false;
		return headModerator.equals(username);
	}

	public void addModerator(String moderator) {
		addModerator(moderator, false);
	}

	protected void addModerator(String moderator, boolean silent) {
		moderators.add(moderator);
		/*if (!silent) {
			tell("", moderator + " has become a channel moderator.");
		}*/
		Collections.sort(moderators, String.CASE_INSENSITIVE_ORDER);
		fireChangedEvent(new ChannelChangedEvent());
	}

	public void removeModerator(String moderator) {
		moderators.remove(moderator);
		//tell("", moderator + " is no longer a channel moderator.");
		fireChangedEvent(new ChannelChangedEvent());
	}

	public boolean addPassword(String moderator, String pass) {
		if (pass.equals("")) {
			removePassword(moderator);
			return true;
		}

		if (pass.length() <= ChannelBot.MAX_PASSWORD_LENGTH) {
			setPassword(pass);
			return true;
		} else {
			return false;
		}
	}

	public void delete(String moderator) {
		tell("", "Channel #" + getID() + " will now be deleted.");
	}

	public int getID() {
		return chid;
	}

	public long getLastTellTime() {
		return lastTellTime;
	}

	public List<String> getMembers() {
		return members;
	}

	public String[] getModerators() {
		return moderators.toArray(new String[moderators.size()]);
	}

	public List<String> getModeratorsAsList() {
		return moderators;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public boolean join(String username) {
		getMembers().add(username);
		tell("", username + " has joined channel #" + getID() + ".");
		User permUser = ChannelBot.getInstance().getUser(username);
		if (permUser != null) {
			permUser.getInChannels().add(getID());
		}
		fireChangedEvent(new ChannelChangedEvent());
		return true;
	}

	/**
	 * Removes the person with the given username from the channel.
	 * 
	 * @param username
	 * @return
	 */
	public boolean kick(String moderator, String username) {
		if (moderator.equals(username) || !getMembers().contains(username)) {
			return false;
		}

		getMembers().remove(username);
		if (isModerator(username)) {
			removeModerator(username);
		}
		User user = ChannelBot.getInstance().getUser(username);
		if (user != null && user.getInChannels().contains(getID())) {
			user.getInChannels().remove(Integer.valueOf(getID()));
			try {
				ChannelBot.getInstance().getPersistanceProvider().removeChannelUserFromDb(this, user);
			} catch (SQLException e) {
				ChannelBot.logError(e);
			}
		}
		tell("", username + " has been removed from the channel by " + moderator + ".");
		ChannelBot.getInstance().getServerConnection().qtell(username,
			ChannelBot.getUsername() + ": You have been kicked out of channel #" + getID() + "!");
		return true;
	}

	/**
	 * Leaves a channel.
	 * 
	 * @param username
	 *            Username who is leaving the channel
	 * @param silent
	 *            When silent=true, the channel is not notified when a player
	 *            leaves the channel.
	 */
	public void leave(String username, boolean silent) {
		// O(n)
		for (String member : getMembers()) {
			if (member.equalsIgnoreCase(username)) {
				username = member;
				break;
			}
		}

		getMembers().remove(username);
		if (isModerator(username)) {
			removeModerator(username);
		}
		
		User permUser = ChannelBot.getInstance().getUser(username);
		if (permUser != null) {
			int pos = permUser.getInChannels().indexOf(getID());
			permUser.getInChannels().remove(pos);
			
			try {
				ChannelBot.getInstance().getPersistanceProvider().removeChannelUserFromDb(this, permUser);
			} catch (SQLException e) {
				ChannelBot.logError(e);
			}
		}
		if (!silent) {
			tell("", username + " has left channel #" + getID() + ".");
		}
		return;
	}

	public void logTell(String mess) {
		Properties props = ChannelBot.getInstance().getProperties();
		boolean logTells = Boolean.parseBoolean(props
				.getProperty("config.channels.log"));
		if (!logTells) {
			return;
		}
		String directoryPath = props.getProperty("config.files.logs");
		try {
			FileWriter writer = new FileWriter(
					directoryPath + getID() + ".log", true);
			writer.append(TimeZoneUtils.getTime(TimeZone.getDefault(),
					new Date()) + " " + mess + "\r\n");
			writer.close();
		} catch (Exception e) {
			ChannelBot.logError(e);
			ChannelBot.getInstance().getServerConnection()
					.qtell(ChannelBot.programmer, "logTell() Failed.");
		}
	}

	public void removePassword(String moderator) {
		setPassword("");
	}

	public void setID(int id) {
		chid = id;
	}

	public void setLastTellTime(long lastTellTime) {
		this.lastTellTime = lastTellTime;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}

	public void setName(String name) {
		this.name = name;
		fireChangedEvent(new ChannelChangedEvent());
	}

	public void setPassword(String password) {
		this.password = password;
		fireChangedEvent(new ChannelChangedEvent());
	}

	/**
	 * Sends a tell to this channel.
	 * 
	 * @param username
	 *            Username of the player sending the tell.
	 * @param message
	 *            Message that the username is sending.
	 */
	public void tell(String username, String message) {
		if (message == null)
			return;

		if (username.equals("")) {
			username = "SYSTEM";
		}
		String origMess = message;
		message = (username + "(" + getID() + "): " + message);

		int sentTo = 0;

		ChannelBot bot = ChannelBot.getInstance();
		String usernameTagsRemoved = ServerConnection.stripTags(username);

		// send the message to each of the users
		for (int i = 0; i < members.size(); i++) {
			String mess = message;
			String name = members.get(i);
			User user = bot.getUser(name);
			if (user == null || (getLastTellTime() != 0 && !user.isOnline()))
				continue;

			if (user.getOnlineStatus() != null)
				sentTo++;
			// add time stamp if the user wants it
			if (user.isShowTime()) {
				String date = TimeZoneUtils.getTime(user.getTimeZone(),
						new Date());
				mess = "[" + date + "]: " + mess;
			}

			//
			if ((user.getName().equals(usernameTagsRemoved) && user.isEcho())
					|| !user.getName().equals(usernameTagsRemoved)) {
				// if user wants swear words blocked
				if (user.isShowSwearWords() == false) {
					mess = filterBadLanguage(mess);
				}
				ChannelBot.getInstance().getServerConnection()
						.qtell(user.getName(), mess);
			}
		}

		User usr = bot.getUser(usernameTagsRemoved);
		if (usr != null && !usr.isDisableToldToString()) {
			ChannelBot
					.getInstance()
					.getServerConnection()
					.qtell(usr.getName(),
							ChannelBot.getUsername() + ": (tell sent to "
									+ sentTo + " players in channel #" + chid
									+ ")");
		}

		long timestamp = System.currentTimeMillis();

		if (!username.equals("SYSTEM")) {
			setLastTellTime(timestamp);
		}

		if (historyTime > 0) {
			final ChannelTell tell = new ChannelTell();
			tell.setUsername(username);
			tell.setChannelNum(getID());
			tell.setMessage(origMess);
			tell.setTimestamp(timestamp);
			tell.markFinal();
			final Channel ch = this;
			history.add(tell, 0, new Runnable() {
				public void run() {
					System.out.println("Removed (ch. " + ch.getID() + "): "
							+ tell.toString());
				}
			});
			System.out.println(history);
		}

	}

	public String buildTellHistory(User user) {
		StringBuilder b = new StringBuilder();
		List<ChannelTell> list = getHistory();
		int size = list.size();
		if (size == 0) {
			b.append("No recent channel history to show.");
		} else {
			for (int i = 0; i < size; i++) {
				ChannelTell tell = list.get(i);
				String date = TimeZoneUtils.getTime(user.getTimeZone(),
						new Date(tell.getTimestamp()), "hh:mm:ss.S a z");
				String message = tell.getMessage();
				if (!user.isShowSwearWords()) {
					message = filterBadLanguage(message);
				}
				b.append("[" + date + "]: " + tell.getUsername() + "("
						+ tell.getChannelNum() + "): " + message + "\\n");
			}
		}

		return b.toString();
	}

	public String getHeadModerator() {
		return headModerator;
	}

	public void setHeadModerator(String headModerator) {
		this.headModerator = headModerator;
	}

	public List<ChannelTell> getHistory() {
		return history.toList();
	}

	public void setHistoryTime(int newTime) {
		int max = 1000 * 60 * 60 * 2; /* two hours */
		if (newTime > max) {
			newTime = max;
		}
		historyTime = newTime;
	}
	
	public void addChannelChangedEventListener(ChannelChangedEventListener listener) {
		if (listener != null) {
			channelChangedEventListeners.add(listener);
		}
	}
	
	public void fireChangedEvent(ChannelChangedEvent event) {
		for(ChannelChangedEventListener listener : channelChangedEventListeners) {
			if (listener != null) {
				listener.setChannel(this);
				listener.setEvent(event);
				listener.run();
			}
		}
	}
}
