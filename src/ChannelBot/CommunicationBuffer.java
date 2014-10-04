/**
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2009-2011, 2014 John Nahlen
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

public class CommunicationBuffer {
	private User user;
	private String text;
	private int pointer;
	private String[] chunks;
	
	public CommunicationBuffer(User user) {
		setUser(user);
	}
	
	public CommunicationBuffer(User user,String text) {
		this(user);
		setText(text);
	}
	
	/**
	 * 
	 */
	public void parse() {
		pointer = 0;
		parseIntoChunks();
		System.err.println(java.util.Arrays.toString(chunks));
	}
	
	/**
	 * This method parses the data into chunks using 
	 * heights specified by the user's Height variable.
	 */
	private void parseIntoChunks() {
		int maxheight = getUser().getHeight()-2;
		String[] arr = getText().split("\\\\n");
		int size = (arr.length / maxheight);
		/*for(String str : arr) {
			// You can only send 1024 characters in a message, server limit
			if (str.length() > 1024) {
				size++;
			}
		}*/
		if ((arr.length/(double)maxheight) % 1 != 0) {
			size++;
		}
		chunks = new String[size];
		for(int i=0;i<chunks.length;i++) {
			StringBuilder b = new StringBuilder();
			for(int j=0;j<maxheight;j++) {
				boolean isLastLine = i*maxheight+j == arr.length - 1;
				
				String str = arr[(i*maxheight)+j];
				// check for empty strings and remove
				if (!str.trim().equals("")) b.append(str);
				if (arr.length != 1 && j != arr.length - 1 && !isLastLine) {
					b.append("\\n");
				}
				
				if (isLastLine) {
					break;
				}
			}
			
			String chunk = b.toString();
			/*while (chunk.length() > 1024) {
				int pos = chunk.indexOf(" ", 1024);
				chunk.substring
			}*/
			chunks[i] = chunk;
		}
	}
	
	public String readNextChunk() {
		StringBuilder b = new StringBuilder();
		if (pointer+1 > chunks.length) {
			b.append(ChannelBot.getUsername() + ": There is no more to show.");
		} else if (pointer < chunks.length) {
			b.append(chunks[pointer++]);
			if (pointer+1 <= chunks.length) b.append("\\nTo see more, tell me \"next\".");
		}
		return b.toString();
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
