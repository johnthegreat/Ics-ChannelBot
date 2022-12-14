/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2009-2012, 2014 John Nahlen
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

import java.util.Collection;

/**
 * This class contains utility methods.
 *
 * @author John
 * @since Tuesday, June 19, 2012
 */
public class Utils {
	/**
	 * O(n) performance.
	 */
	public static boolean collectionContainsIgnoreCase(final Collection<String> collection, String needle) {
		needle = needle.toLowerCase();
		for (final String s : collection) {
			if (needle.equals(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
