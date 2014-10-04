/**
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2012 John Nahlen
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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

public class LRUTimerCache<K> {
	
	private static final Timer timer = new Timer(true);
	
	public static Timer getTimer() {
		return timer;
	}
	
	private int cacheLife = 0;
	private Stack<K> stack;
	
	/**
	 * 
	 * @param time Milliseconds into the future from now.
	 */
	public LRUTimerCache(int time) {
		this.cacheLife = time;
		stack = new Stack<K>();
	}
	
	public List<K> toList() {
		List<K> list = new ArrayList<K>();
		for(int i=0;i<stack.size();i++) {
			list.add(stack.get(i));
		}
		return list;
	}
	
	public void setCacheLife(int life) {
		this.cacheLife = life;
	}
	
	public Stack<K> getStack() {
		return stack;
	}
	
	public void add(K obj) {
		add(obj,0,null);
	}
	
	public void add(K obj,int delay) {
		add(obj,delay,null);
	}
	
	public void add(K obj,int delay,Runnable removedEvent) {
		stack.add(obj);
		startTimer(delay,removedEvent);
	}
	
	protected void startTimer(final int delay,final Runnable removedEvent) {
		int timeToUse;
		if (delay == 0) {
			timeToUse = cacheLife;
		} else {
			timeToUse = delay;
		}
		
		timer.schedule(new TimerTask() {
			public void run() {
				remove();
				if (removedEvent != null) {
					removedEvent.run();
				}
			}
		}, timeToUse);
	}
	
	public K remove() {
		return stack.pop();
	}
}
