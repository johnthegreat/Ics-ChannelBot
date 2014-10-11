package ChannelBot;

import java.util.EventListener;

public abstract class ChannelChangedEventListener implements EventListener, Runnable {
	private ChannelChangedEvent event;
	private Channel channel;
	
	public ChannelChangedEventListener() {
		
	}
	
	public ChannelChangedEventListener(Channel channel) {
		this();
		setChannel(channel);
	}

	public ChannelChangedEvent getEvent() {
		return event;
	}

	public void setEvent(ChannelChangedEvent event) {
		this.event = event;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
}
