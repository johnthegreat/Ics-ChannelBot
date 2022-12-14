# ChannelBot

ChannelBot is a service that can be run on an Internet Chess Server like FICS (http://freechess.org/) which simulates
additional server channels for users to chat.
Users can password protect channels.

This service runs as `ChannelBot(TD)` on FICS.

## Configuration

Copy `src/ChannelBot-template.ini` and configure as desired. Pass the resulting configuration file in as the first
command line argument.

## Dependencies

This project requires the [sqlite-jdbc](https://github.com/xerial/sqlite-jdbc) library. (The current version is 3.34.0
as of this writing.)

## License

Copyright &copy; 2021 John Nahlen.

Licensed under the GNU General Public License (v3). See `LICENSE.txt` for full license text.