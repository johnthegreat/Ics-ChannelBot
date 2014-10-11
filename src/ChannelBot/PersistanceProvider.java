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
package ChannelBot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistanceProvider {
	
	public PersistanceProvider() {
		
	}
	
	public void flush() throws SQLException {
		// flush list of users to database
		List<User> userList = ChannelBot.getInstance().getUserList();
		for(User user : userList) {
			saveUserToDb(user);
		}
		
		// flush channel list to database
		Channel[] channels = ChannelBot.getInstance().getChannels();
		for(Channel channel : channels) {
			saveChannelToDb(channel);
			List<String> channelMembers = channel.getMembers();
			for(String username : channelMembers) {
				User user = ChannelBot.getInstance().getUser(username);
				addChannelUserToDb(channel, user);
			}
		}
		
		// flush user lists to database
		saveUserListToDb("ban", ChannelBot.getInstance().getBannedUsers());
	}
	
	public List<User> loadUserBase() throws SQLException {
		// load list of users from database
		ResultSet resultSet = ChannelBot.getInstance().getDatabaseConnection().execute("SELECT username FROM user").getResultSet();
		List<User> userList = new ArrayList<User>();
		while(resultSet.next()) {
			User user = loadUserFromDb(resultSet.getString("username"));
			userList.add(user);
		}
		return userList;
	}
	
	public List<Channel> loadChannelListFromDb() throws SQLException {
		// load list of channels from database
		ResultSet resultSet = ChannelBot.getInstance().getDatabaseConnection().execute("SELECT * FROM channel ORDER BY num ASC").getResultSet();
		List<Channel> channelList = new ArrayList<Channel>();
		while(resultSet.next()) {
			Channel channel = loadChannelFromDb(resultSet);
			channelList.add(channel);
		}
		return channelList;
	}
	
	public Map<String,List<String>> loadUserLists() throws SQLException {
		// load user lists from database
		ResultSet resultSet = ChannelBot.getInstance().getDatabaseConnection().execute("SELECT DISTINCT listname FROM userlist").getResultSet();
		Map<String,List<String>> userListMap = new HashMap<String, List<String>>();
		while(resultSet.next()) {
			String listName = resultSet.getString("listname");
			List<String> list = loadUserListFromDb(listName);
			userListMap.put(listName, list);
		}
		return userListMap;
	}
	
	public void saveChannelToDb(Channel channel) throws SQLException {
		ChannelBot.getInstance().getDatabaseConnection().execute(
			String.format("INSERT OR REPLACE INTO channel (num,name,password,lastTellTime) " +
				"VALUES (%s,%s,%s,%s)",
				channel.getID(),
				channel.getName() == null ? null : "'" + SQLiteConnection.escape(channel.getName()) + "'",
				channel.getPassword() == null ? null : "'" + SQLiteConnection.escape(channel.getPassword()) + "'",
				channel.getLastTellTime()));
	}
	
	protected void populateChannelUsersFromDb(Channel channel) throws SQLException {
		ResultSet resultSet = ChannelBot.getInstance().getDatabaseConnection().execute(
			String.format("SELECT username,moderator FROM channel_user WHERE channel = %s",channel.getID())).getResultSet();
		while(resultSet.next()) {
			String username = resultSet.getString("username");
			channel.getMembers().add(username);
			String moderatorLevel = resultSet.getString("moderator");
			if (moderatorLevel != null) {
				if (moderatorLevel.equals("head")) {
					channel.setHeadModerator(username);
					channel.addModerator(username);
				} else if (moderatorLevel.equals("normal")) {
					channel.addModerator(username);
				}
			}
		}
	}
	
	public void addChannelUserToDb(Channel channel,User user) throws SQLException {
		String moderatorLevel = null;
		if (channel.isHeadModerator(user.getName())) {
			moderatorLevel = "head";
		} else if (channel.isModerator(user.getName())) {
			moderatorLevel = "normal";
		}
		ChannelBot.getInstance().getDatabaseConnection().execute(
			String.format("INSERT OR REPLACE INTO channel_user (channel,username,moderator) VALUES (%s,%s,%s)",
				channel.getID(),
				"'" + SQLiteConnection.escape(user.getName()) + "'",
				moderatorLevel == null ? null : "'" + moderatorLevel + "'"));
	}
	
	public void removeChannelUserFromDb(Channel channel,User user) throws SQLException {
		ChannelBot.getInstance().getDatabaseConnection().execute(
				String.format("DELETE FROM channel_user WHERE channel = %s AND username = %s",
					channel.getID(),"'" + SQLiteConnection.escape(user.getName()) + "'"));
	}
	
	public Channel loadChannelFromDb(ResultSet resultSet) throws SQLException {
		Channel channel = new Channel();
		channel.setID(resultSet.getInt("num"));
		channel.setName(resultSet.getString("name"));
		channel.setPassword(resultSet.getString("password"));
		channel.setLastTellTime(resultSet.getLong("lastTellTime"));
		populateChannelUsersFromDb(channel);
		channel.addChannelChangedEventListener(Channel.channelChangedEventListener);
		return channel;
	}
	
	public void deleteChannelFromDb(int id) throws SQLException {
		ChannelBot.getInstance().getDatabaseConnection().execute(String.format("DELETE FROM channel WHERE channel.num = %d",id));
	}
	
	public void saveUserToDb(User user) throws SQLException {
		ChannelBot.getInstance().getDatabaseConnection().execute(
			String.format("INSERT OR REPLACE INTO user " +
				"(username,v_showTime,v_echo,v_languageFiltered,v_disableToldToString,v_height,v_timeZone) " +
				"VALUES (%s,%s,%s,%s,%s,%s,%s)",
				user.getName() == null ? null : "'" + SQLiteConnection.escape(user.getName()) + "'",
				user.isShowTime() ? '1' : '0',
				user.isEcho() ? '1' : '0',
				user.isShowSwearWords() ? '1' : '0',
				user.isDisableToldToString() ? '1' : '0',
				user.getHeight(),
				"'" + TimeZoneUtils.getAbbreviation(user.getTimeZone()) + "'"));
	}
	
	public User loadUserFromDb(String username) throws SQLException {
		Statement statement = ChannelBot.getInstance().getDatabaseConnection().execute(
			String.format("SELECT username,v_showTime,v_echo,v_languageFiltered,v_disableToldToString,v_height,v_timeZone FROM user WHERE username %s",
				username == null ? "IS NULL" : "= '" + SQLiteConnection.escape(username) + "'"));
		ResultSet resultSet = statement.getResultSet();
		User user = new User();
		user.setName(resultSet.getString("username"));
		user.setShowTime(resultSet.getInt("v_showTime") == 1);
		user.setEcho(resultSet.getInt("v_echo") == 1);
		user.setShowSwearWords(resultSet.getInt("v_languageFiltered") == 1);
		user.setDisableToldToString(resultSet.getInt("v_disableToldToString") == 1);
		user.setHeight(resultSet.getInt("v_height"));
		user.setTimeZone(TimeZoneUtils.getTimeZone(resultSet.getString("v_timeZone")));
		return user;
	}
	
	public void addToUserListDb(String listName,String username) throws SQLException {
		ChannelBot.getInstance().getDatabaseConnection().execute(
				String.format("INSERT OR REPLACE INTO userlist (listname,username) VALUES ('%s','%s')",
						SQLiteConnection.escape(listName),SQLiteConnection.escape(username)));
	}
	
	public void removeFromUserListDb(String listName, String username) throws SQLException {
		ChannelBot.getInstance().getDatabaseConnection().execute(
				String.format("DELETE FROM userlist WHERE listname = %s AND username = %s",
					"'" + SQLiteConnection.escape(listName) + "'",
					"'" + SQLiteConnection.escape(username) + "'"));
	}
	
	public void saveUserListToDb(String listName,List<String> list) throws SQLException {
		saveUserListToDb(listName, list.toArray(new String[list.size()]));
	}
	
	public void saveUserListToDb(String listName,String[] list) throws SQLException {
		for(String username : list) {
			addToUserListDb(listName, username);
		}
	}
	
	public List<String> loadUserListFromDb(String listName) throws SQLException {
		Statement statement = ChannelBot.getInstance().getDatabaseConnection().execute(
				String.format("SELECT username FROM userlist WHERE listname %s",
					listName == null ? "IS NULL" : "= '" + SQLiteConnection.escape(listName) + "'"));
		ResultSet resultSet = statement.getResultSet();
		List<String> list = new ArrayList<String>();
		while(resultSet.next()) {
			list.add(resultSet.getString("username"));
		}
		return list;
	}
}
