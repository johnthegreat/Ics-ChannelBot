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

import java.util.HashMap;

public class StringUtils {
	public static boolean startsWithIgnoreCase(String str, String startWith) {
		if (str.toLowerCase().startsWith(
				startWith.substring(0, startWith.length()).toLowerCase())) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean containsIgnoreCase(String str, String contains) {
		return str.toLowerCase().contains(contains.toLowerCase());
	}

	public static String replaceAllIgnoreCase(String mess, String search,
			String replacement) {
		StringBuilder regex = new StringBuilder();
		for (int i = 0; i < search.length(); i++) {
			char c = search.charAt(i);
			String s = new String(""+c);
			regex.append("[");
			regex.append(s.toUpperCase());
			regex.append(s.toLowerCase());
			regex.append("]");
		}
		mess = mess.replaceAll(regex.toString(), replacement);

		return mess;
	}
	
	private static HashMap<String,String> builtRegexMap = new HashMap<String,String>();
	
	/** Takes a string such as "exec[ute]" and returns a string: exec(?:|u|ut|ute).<br />
	 * For Command Patterns. */
	public static String buildCommandRegex(String str) {
		if (builtRegexMap.containsKey(str)) {
			return builtRegexMap.get(str);
		}
		
		StringBuilder b = new StringBuilder();
		int strlen = str.length();
		StringBuilder newb = new StringBuilder();
		boolean foundOptional = false;
		for(int i=0;i<strlen;i++) {
			char chr = str.charAt(i);
			if (chr == '[') {
				b.append("(?:|");
				foundOptional = true;
			} else if (chr == ']') {
				b.append(")");
			} else {
				if (foundOptional == false) {
					b.append(chr);
				} else {
					newb.append(chr);
					b.append(newb.toString() + "|");
				}
			}
				
		}

		builtRegexMap.put(str, b.toString());
		return b.toString();
	}
	
	public static boolean isNumeric(String str) {
		return PatternService.getInstance().get("[0-9]+").matcher(str).matches();
	}
	
	/**
	 * Method that parses a string for a boolean. Returns 1 if true, 0 if false,
	 * and -1 if it couldn't parse it.
	 * 
	 * @param in
	 * @since Friday, April 30, 2010
	 * @author John
	 */
	public static Boolean parseBoolean(String in) {
		in = in.toLowerCase();
		if (in.matches("1|y|yes|true")) {
			return Boolean.TRUE;
		} else if (in.matches("0|n|no|false")) {
			return Boolean.FALSE;
		}
		
		return null;
	}
}
