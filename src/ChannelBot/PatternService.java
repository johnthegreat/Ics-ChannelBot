/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2012, 2014 John Nahlen
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

import java.util.HashMap;
import java.util.regex.Pattern;

public class PatternService {

	private HashMap<String, Pattern> map;
	private static PatternService singletonInstance;
	
	static {
		singletonInstance = new PatternService();
	}
	
	private PatternService() {
		map = new HashMap<>();
	}
	
	protected Pattern add(String key,Pattern pattern) {
		map.put(key, pattern);
		return pattern;
	}
	
	/** value is built and returned if key doesn't exist, and stored in map for future use */
	public Pattern get(String key) {
		Pattern p = map.get(key);
		if (p == null) {
			return add(key,Pattern.compile(key));
		} else {
			return p;
		}
	}
	
	public String[] getKeys() {
		java.util.Set<String> set = map.keySet();
		return set.toArray(new String[0]);
	}

	public static PatternService getInstance() {
		return singletonInstance;
	}
}
