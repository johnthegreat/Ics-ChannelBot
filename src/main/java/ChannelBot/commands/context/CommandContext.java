package ChannelBot.commands.context;

import ChannelBot.ChannelBot;

public class CommandContext {
	private ChannelBot channelBot;
	private String commandName;
	private String username;
	private int channelNumber;
	private String args;

	public CommandContext(final ChannelBot channelBot, final String commandName, final String username, final int channelNumber, final String args) {
		this.setChannelBot(channelBot);
		this.setCommandName(commandName);
		this.setUsername(username);
		this.setChannelNumber(channelNumber);
		this.setArguments(args);
	}

	public ChannelBot getChannelBot() {
		return channelBot;
	}

	protected void setChannelBot(ChannelBot channelBot) {
		this.channelBot = channelBot;
	}

	public String getArguments() {
		return args;
	}

	protected void setArguments(String args) {
		this.args = args;
	}

	public String getUsername() {
		return username;
	}

	protected void setUsername(String username) {
		this.username = username;
	}

	public int getChannelNumber() {
		return channelNumber;
	}

	protected void setChannelNumber(int channelNumber) {
		this.channelNumber = channelNumber;
	}

	public String getCommandName() {
		return commandName;
	}

	protected void setCommandName(String commandName) {
		this.commandName = commandName;
	}
}
