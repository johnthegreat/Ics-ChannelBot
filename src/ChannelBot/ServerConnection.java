/**
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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import ChannelBot.UserCommunicationService.TellMethod;

public class ServerConnection {
	public static String stripTags(String username) {
		if (username.contains("(")) {
			return username.substring(0,username.indexOf("("));
		}
		return username;
	}
	
	private Socket socket;
	private DataInputStream dataIn;
	private OutputStream dataOut;
	private String username;
	private String password;
	
	public boolean connect() {
		return connect("freechess.org",5000);
	}
	
	public boolean connect(String address,int port) {
		try {
			socket = new Socket(address,port);
			dataIn = new DataInputStream(socket.getInputStream());
			dataOut = socket.getOutputStream();

			System.out.println(read("login: ").replace("\n",""));
			write(getUsername() + "\n" + getPassword());
			System.out.println(read("fics% ").replace("\n",""));
			return true;
		} catch (IOException e) {
			ChannelBot.logError(e);
			return false;
		}
	}
	
	public void disconnect() {
		try {
			socket.close();
			dataIn.close();
			dataOut.close();
		} catch(IOException e) {
			ChannelBot.logError(e);
			System.err.println("disconnect() failed.");
		}
	}
	
	public String read(String prompt) {
		StringBuilder result = new StringBuilder();
		try {
			while(result.lastIndexOf(prompt) == -1) {
				int _character = dataIn.read();
				if (dataIn.available() > 0 && _character != -1) {
					char c = (char)_character;
					result.append(c);
				} else {
					break;
				}
			}
		} catch (IOException e) {
			ChannelBot.logError(e);
		}

		return result.toString();
	}
	
	public String getSingleLine() {
		return read("\n\r").replace("\n\r","");
	}

	public void writeBatch(String[] array) {
		for(String s : array) {
			write(s);
		}
	}
	
	public void write(String message) {
		if (message == null) {
			throw new IllegalArgumentException("write(): message cannot be null");
		}
		System.out.println(message);
		try {
			byte[] arr = (message + "\r\n").getBytes();
			dataOut.write(arr);
			dataOut.flush();
		} catch (IOException e) {
			ChannelBot.logError(e);
		}
	}
	
	/**
	 * This is the preferred method of sending long blocks of information to a user.
	 * It breaks up the information given the user's Height variable and allows the 
	 * user to go through multiple pages of information using the "next" or "more" command
	 */
	private void writeWithBuffer(TellMethod tellMethod, User user, String message) {
		UserCommunicationService userCommunicationService = UserCommunicationService.getInstance();
		CommunicationBuffer communicationBuffer = userCommunicationService.get(user);
		if (communicationBuffer == null) {
			userCommunicationService.addOrReplace(new CommunicationBuffer(user));
			communicationBuffer = userCommunicationService.get(user);
		}
		communicationBuffer.setText(message);
		communicationBuffer.parse();
		userCommunicationService.send(user,communicationBuffer.readNextChunk(),tellMethod);
	}
	
	public void qtell(String username, String message) {
		User user = ChannelBot.getInstance().getUser(username);
		if (user != null) {
			writeWithBuffer(TellMethod.QTELL, user, message);
		} else {
			String str = String.format("qtell %s %s",username,message);
			write(str);
		}
	}
	
	public Socket getUnderlyingSocket() {
		return this.socket;
	}
	
	protected DataInputStream getDataInputStream() {
		return this.dataIn;
	}
	
	protected OutputStream getOutputStream() {
		return this.dataOut;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
