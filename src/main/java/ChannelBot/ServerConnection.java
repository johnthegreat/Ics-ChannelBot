package ChannelBot;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Arrays;

public class ServerConnection {
	protected final String defaultPrompt;
	protected boolean connectCalled = false;
	protected DataInputStream dataIn;
	protected OutputStream dataOut;
	protected Socket socket;
	protected long totalBytesIn;
	protected long totalBytesOut;
	protected PrintStream outPrintStream;
	protected PrintStream errPrintStream;

	public ServerConnection(final String prompt) {
		defaultPrompt = prompt;
	}

	private static String size(long size) {
		final DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
		if (size <= 0) return "0";
		final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return String.format("%s %s", decimalFormat.format(size / Math.pow(1024, digitGroups)), units[digitGroups]);
	}

	public static String stripUserTags(final String username) {
		int idx = username.indexOf("(");
		if (idx > -1) {
			return username.substring(0, idx);
		}
		return username;
	}

	public boolean connect(final String address, final int port, final String username, final String password) throws IOException, ServerConnectionClosedException {
		if (connectCalled) {
			throw new IllegalStateException("connect() has already been called on this ServerConnection instance.");
		}

		connectCalled = true;

		socket = new Socket(address, port);
		dataIn = new DataInputStream(socket.getInputStream());
		dataOut = socket.getOutputStream();

		String str = this.readFully();
		if (outPrintStream != null) {
			outPrintStream.println(str);
		}

		this.write(username);

		str = this.readFully();
		if (outPrintStream != null) {
			outPrintStream.println(str);
		}

		this.write(password);

		str = this.readFully();
		if (outPrintStream != null) {
			outPrintStream.println(str);
		}

		if (str.contains(String.format("*** Sorry %s is already logged in ***", username))) {
			if (errPrintStream != null) {
				errPrintStream.printf("Connect failed: %s is already logged in.%n", username);
			}
			return false;
		} else if (str.contains(String.format("**** Starting FICS session as %s", username))) {
			return true;
		}

		return true;
	}

	public boolean connect(final String username, final String password) throws IOException, ServerConnectionClosedException {
		return connect("freechess.org", 5000, username, password);
	}

	public void printNetworkStats() {
		if (outPrintStream != null) {
			outPrintStream.println("Total Bytes In: " + size(totalBytesIn));
			outPrintStream.println("Total Bytes Out: " + size(totalBytesOut));
			outPrintStream.println("Total Bytes: " + size(totalBytesIn + totalBytesOut));
		}
	}

	public String readFully() throws IOException, ServerConnectionClosedException {
		byte[] bytes = new byte[4096];
		int bytesRead = dataIn.read(bytes);
		if (bytesRead < 0) {
			throw new ServerConnectionClosedException();
		}
		byte[] strBytes = Arrays.copyOfRange(bytes, 0, bytesRead);
		totalBytesIn += strBytes.length;
		return new String(strBytes);
	}

	public void writeBytes(byte[] bytes) {
		if (bytes.length == 0) throw new IllegalArgumentException();

		totalBytesOut += bytes.length;
		try {
			dataOut.write(bytes, 0, bytes.length);
			dataOut.flush();
		} catch (IOException e) {
			ChannelBot.logError(e);
		}
	}

	public void write(final String message) {
		if (message == null) {
			throw new IllegalArgumentException();
		}
		this.writeBytes((message + "\r\n").getBytes());
	}

	public void writeBatch(final String[] array) {
		for (final String s : array) {
			write(s);
		}
	}

	public void setOutPrintStream(PrintStream outPrintStream) {
		this.outPrintStream = outPrintStream;
	}

	public void setErrPrintStream(PrintStream errPrintStream) {
		this.errPrintStream = errPrintStream;
	}

	public String getDefaultPrompt() {
		return defaultPrompt;
	}
}
