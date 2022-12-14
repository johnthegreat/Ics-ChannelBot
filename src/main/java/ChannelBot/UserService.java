package ChannelBot;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserService {
	private Map<String, User> _userMap;

	public UserService() {
		_userMap = new HashMap<>();
	}

	/**
	 * Returns the user with the given username, or null if not found.
	 * O(1) performance.
	 *
	 * @param username Username to find
	 */
	public User getUser(final String username) {
		if (username == null) {
			throw new IllegalArgumentException("UserService.getUser username cannot be null.");
		}
		return _userMap.get(username.toLowerCase());
	}

	public void addUsers(final Collection<User> users) {
		if (users == null) {
			throw new IllegalArgumentException("UserService.addUsers users cannot be null");
		}
		for (final User user : users) {
			addUser(user);
		}
	}

	public void addUser(final User user) {
		_userMap.put(user.getName().toLowerCase(), user);
	}

	/**
	 * Provides a read-only view of the Users stored in this service.
	 */
	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(_userMap.values());
	}
}
