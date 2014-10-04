/**
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2010-2012 John Nahlen
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Class containing utility methods for timezone stuff.
 * 
 * @author John
 * 
 */
public class TimeZoneUtils {
	private static Map<String, TimeZone> map;

	static {
		TimeZoneUtils.map = load();
	}

	/**
	 * Returns the formatted time in pattern "hh:mm a z". Ex: 05:45 PM MDT
	 * 
	 * @param tz
	 * @param d
	 * @return
	 */
	public static String getTime(TimeZone tz, Date d) {
		return getTime(tz,d,"hh:mm a z");
	}
	
	public static String getTime(TimeZone tz, Date d, String simpleDateFormatPattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(simpleDateFormatPattern);
		sdf.setTimeZone(tz);
		return sdf.format(d);
	}

	public static String getAbbreviation(TimeZone tz) {
		return getAbbreviation(tz, new Date());
	}

	public static String getAbbreviation(TimeZone tz, Date d) {
		return tz.getDisplayName(tz.inDaylightTime(d), TimeZone.SHORT)
				.toUpperCase();
	}

	public static TimeZone getTimeZone(String abbrev) {
		Map<String, TimeZone> v = TimeZoneUtils.map;
		TimeZone tz = v.get(abbrev);
		return tz;
	}

	private static Map<String, TimeZone> load() {
		final Map<String, TimeZone> map = new HashMap<String, TimeZone>();
		final String[] arr = TimeZone.getAvailableIDs();
		final Date d = new Date();

		for (final String tmp : arr) {
			final TimeZone tz = TimeZone.getTimeZone(tmp);
			final String abbrev = tz.getDisplayName(tz.inDaylightTime(d),
					TimeZone.SHORT);

			if (map.containsKey(abbrev)) {
				continue;
			}

			map.put(abbrev, tz);
		}

		return map;
	}
	
	public static TimeZone getTimeZoneByTime(Date d) {
		Map<String, TimeZone> v = TimeZoneUtils.map;
		java.util.Collection<TimeZone> arr = v.values();
		for(TimeZone tz : arr) {
			System.out.println(TimeZoneUtils.getTime(tz,d));
		}
		
		return null;
	}
}
