// NOT INTENDED TO BE UPLOADED TO SUBVERSION.
package ChannelBot;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Test {

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		SQLiteConnection connection = new SQLiteConnection();
		// "C:\\Users\\John\\Desktop\\consulting\\Bots\\ChannelBot\\info.db"
		Connection c = connection.connect("C:\\Users\\John\\Desktop\\consulting\\Bots\\ChannelBot\\ChannelBot.db");
		connection.setConnection(c);
		Statement statement = connection.execute("SELECT * FROM channel");
		
		int x = 8;
		int y = 8;
		System.out.println((x / (double)y) % 1 == 0.0);
		/*ResultSet rs = statement.getResultSet();
		int channelNumber = rs.getInt(1);
		String channelName = rs.getString(2);
		System.out.println(channelNumber + " " + channelName);*/
		//PersistanceProvider persistanceProvider = new PersistanceProvider();
		//persistanceProvider.setConnection(connection);
		//persistanceProvider.getProperties().put("", value)
		/*LRUTimerCache<Integer> cache = new LRUTimerCache<Integer>(10*1000);
		cache.add(1);
		cache.add(2);
		cache.add(3);
		cache.add(4);
		cache.add(5);
		cache.add(6);
		System.out.println(cache.getList());*/
		
		//System.out.println(StringUtils.buildCommandRegex("exec[ute]")); // exec(?:|u|ut|ute)
		
		/*final SchedulerService schedulerService = SchedulerService.getSingletonInstance();
		List<String> list = new ArrayList<String>();
		list.add("A");
		list.add("B");
		list.add("C");
		schedulerService.scheduleForRemoval(list, "A", 2000, new Runnable() { public void run() { System.out.println("Removed: " + "A"); } });*/
		
		/*LRUTimerCache<String> cache = new LRUTimerCache<String>(1000);
		cache.add("A");
		cache.add("B");
		cache.add("C",3000,new Runnable() { public void run() { System.out.println("Removed: " + "C"); System.exit(0); } });
		System.out.println(cache.toList());
		System.out.println(cache.getStack());*/
		
		
	}

}
