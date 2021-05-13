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

/**
 * @since May 6, 2021
 */
public class LanguageFilterService {
    private static final String filter = "#%&@#%&@#%&@";

    private String[] filteredWords = new String[0];

    public String[] getFilteredWords() {
        return filteredWords;
    }

    public LanguageFilterService() {}

    public void setFilteredWords(String[] filteredWords) {
        this.filteredWords = filteredWords;
    }

    public String filterLanguage(String message) {
        String[] filteredWords = getFilteredWords();
        if (message == null || message.length() == 0 || filteredWords.length == 0) {
            return message;
        }

        for (int i = 0; i < filteredWords.length; i++) {
            if (StringUtils.containsIgnoreCase(message, filteredWords[i])) {
                message = StringUtils.replaceAllIgnoreCase(message, filteredWords[i],
                        filter.substring(0, filteredWords[i].trim().length()));
            }
        }
        return message;
    }
}
