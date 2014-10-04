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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class LegacyContentLoader {
	public static List<String> loadBannedList() throws IOException {
		ChannelBot.getInstance().clearBannedUsers();
		String filePath = ChannelBot.getInstance().getProperties().getProperty("config.files.ban");
		if (filePath == null || filePath.isEmpty()) {
			System.out.println("No file path provided to load ban list. Ignoring...");
			return null;
		}
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		List<String> users = new ArrayList<String>();
		while (reader.ready()) {
			String user = reader.readLine();
			if (user != null && !user.equals("")) {
				users.add(user);
			}
		}
		Collections.sort(ChannelBot.getInstance().getBannedUsers(),String.CASE_INSENSITIVE_ORDER);
		reader.close();
		return users;
	}
	
	// Loads the base list from a file
	public static Map<String,User> loadBaseList() throws IOException {
		String filePath = ChannelBot.getInstance().getProperties().getProperty("config.files.base");
		if (filePath == null || filePath.isEmpty()) {
			System.err.println("Cannot load base list. No file path provided. Shutting down...");
			System.exit(1);
		}
		
		Map<String,User> map = new HashMap<String,User>();
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		while (reader.ready()) {
			String line = reader.readLine();
			if ((line != "") && (line != null)) {
				StringTokenizer userInfoLine = new StringTokenizer(line,"[ ]");
				
				User user = new User();
				user.setName(userInfoLine.nextToken());
				user.setShowTime(Boolean.parseBoolean(userInfoLine.nextToken()));

				user.setTimeZone(TimeZoneUtils.getTimeZone(userInfoLine.nextToken()));
				if (user.getTimeZone() == null) {
					user.setTimeZone(TimeZoneUtils.getTimeZone("GMT"));
				}

				user.setEcho(Boolean.parseBoolean(userInfoLine.nextToken()));
				user.setShowSwearWords(Boolean.parseBoolean(userInfoLine.nextToken()));
				
				user.setDisableToldToString((userInfoLine.hasMoreTokens()?
						Boolean.parseBoolean(userInfoLine.nextToken()):false));
				
				user.setHeight(userInfoLine.hasMoreTokens() ? Integer.parseInt(userInfoLine.nextToken()) : 20);
				
				int pos = Utils.getUserIndexFromListIgnoreCase(ChannelBot.getInstance().getUserList(),user.getName());
				if (pos >= 0) {
					ChannelBot.getInstance().getUserList().set(pos, user);
				} else {
					ChannelBot.getInstance().getUserList().add(user);
				}
				map.put(user.getName().toLowerCase(),user);
			}
		}
		reader.close();
		return map;
	}
	
	public static Map<Integer, Channel> loadChannelList() throws IOException {
		String filePath = ChannelBot.getInstance().getProperties().getProperty("config.files.channels");
		if (filePath == null || filePath.isEmpty()) {
			System.err.println("Cannot load channel list. No file path provided. Shutting down...");
			System.exit(1);
		}
		
		NavigableMap<Integer,Channel> channelMap = new TreeMap<Integer, Channel>();
		FileReader frdr = new FileReader(filePath);
		BufferedReader reader = new BufferedReader(frdr);
		while (reader.ready()) {
			String channel = reader.readLine();
			if (channel != null && !channel.equals("")) {
				Channel ch = new Channel();
				//channel = channel.replace("\"","");
				StringTokenizer str = new StringTokenizer(channel, ",");
									
				ch.setID(Integer.parseInt(str.nextToken().replace("\"","")));
				
				String tok = "";
				
				if (str.hasMoreTokens()) {
					tok = str.nextToken().replace("\"","");
					ch.setName(tok);
				}
				if (str.hasMoreTokens()) {
					tok = str.nextToken();
					tok = tok.equals("null") ? null : tok.replace("\"","");
					ch.setPassword(tok);
				}
				if (str.hasMoreTokens()) {
					tok = str.nextToken().replace("\"","");
					String[] moderators = tok.split("\\|");
					//System.out.println(java.util.Arrays.toString(moderators));
					for(String username : moderators) {
						if (username.endsWith("(h)")) {
							username = username.substring(0,username.indexOf("(h)"));
							System.out.println("Setting Head Moderator (#" + ch.getID() + "): " + username);
							ch.setHeadModerator(username);
						}
						ch.addModerator(username,true);
						//ch.getMembers().add(username);
					}
				}
				
				if (str.hasMoreTokens()) {
					tok = str.nextToken().replace("\"","");
					StringTokenizer memberToks = new StringTokenizer(tok,"|");
					while(memberToks.hasMoreTokens()) {
						ch.getMembers().add(memberToks.nextToken());
					}
				}
				
				if (str.hasMoreTokens()) {
					tok = str.nextToken().replace("\"","");
					ch.setLastTellTime(Long.parseLong(tok));
				}
				
				channelMap.put(ch.getID(), ch);
			}
		}
		frdr.close();
		reader.close();
		return channelMap;
	}
}
