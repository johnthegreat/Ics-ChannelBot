/**
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2009,2010 John Nahlen
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
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to verify that the user data has not corrupted.
 * @author John Nahlen
 * @since Thursday, November 11, 2010
 */
@Deprecated
public class UserCorruptionCheck {

	/**
	 * @param args
	 * Takes a single argument containing the path of the file to check.
	 */
	public static void main(String[] args) throws IOException {
		File file = new File(args[0]);
		FileReader frdr = new FileReader(file);
		BufferedReader rdr = new BufferedReader(frdr);
		
		Pattern p = Pattern.compile("\\w{3,17} \\[(true|false) \\w{3} " +
				"(true|false) (true|false) (true|false)\\]");
		while(rdr.ready()) {
			String line = rdr.readLine();
			Matcher m = p.matcher(line);
			if (!m.matches()) {
				System.out.println("Corrupt: " + line);
			}
		}
		rdr.close();
		frdr.close();
	}
}
