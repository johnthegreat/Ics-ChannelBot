/*
 *     ChannelBot is a program used to provide additional channels on ICS servers, such as FICS and BICS.
 *     Copyright (C) 2021 John Nahlen
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

import ChannelBot.database.ChannelProvider;
import ChannelBot.database.ChannelUserProvider;
import ChannelBot.database.UserListProvider;
import ChannelBot.database.UserProvider;

public class DatabaseProviderRepository {
    private ChannelProvider channelProvider;
    private ChannelUserProvider channelUserProvider;
    private UserListProvider userListProvider;
    private UserProvider userProvider;

    public DatabaseProviderRepository() {
        setChannelProvider(new ChannelProvider());
        setChannelUserProvider(new ChannelUserProvider());
        setUserListProvider(new UserListProvider());
        setUserProvider(new UserProvider());
    }

    public ChannelProvider getChannelProvider() {
        return channelProvider;
    }

    public void setChannelProvider(ChannelProvider channelProvider) {
        this.channelProvider = channelProvider;
    }

    public ChannelUserProvider getChannelUserProvider() {
        return channelUserProvider;
    }

    public void setChannelUserProvider(ChannelUserProvider channelUserProvider) {
        this.channelUserProvider = channelUserProvider;
    }

    public UserListProvider getUserListProvider() {
        return userListProvider;
    }

    public void setUserListProvider(UserListProvider userListProvider) {
        this.userListProvider = userListProvider;
    }

    public UserProvider getUserProvider() {
        return userProvider;
    }

    public void setUserProvider(UserProvider userProvider) {
        this.userProvider = userProvider;
    }
}
