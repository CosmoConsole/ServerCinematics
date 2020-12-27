_This release is part of the source code release as outlined in [this announcement](https://www.spigotmc.org/threads/deathmessagesprime.48322/page-53#post-3933244)._

# ServerCinematics

ServerCinematics is a camera plugin for Minecraft servers running the
[Spigot](https://www.spigotmc.org/) server software. This plugin was developed from
mid-late 2014 to late 2020, with first public releases in late 2014. Its original
developer, CosmoConsole, will no longer be supporting the plugin starting from
2021, and its source code is being released under the CC0 license, effectively
releasing the code into the public domain.

The plugin implements a Camera Studio-like waypoint path system that supports
playing back paths of waypoints. It is possible to save paths and customize
them in a variety of ways, and then play them or have another player play them.
The plugin was originally offered for 5 USD, but the price was lowered soon
after release to 3 USD and has been made free ever since support ended in 2021.

The original plugin page was located [here](https://www.spigotmc.org/resources/servercinematics.1504/). It now openly describes that the plugin
is no longer supported. As stated, I wish any developer that seeks to maintain
this code or fork it to massively refactor or possibly even rewrite large portions
or even the entire code. It has never been refactored over its long development
history despite the fact that it should have been, perhaps even more than once.
Bad decisions made years ago still haunt this code base and manifest in truly
awful forms of spaghetti code that borders on unmaintainable.

The original source code of ServerCinematics was lost twice and had to be
decompiled from the compiled binaries. This in part explains why the source
is as messy as it is.

For the second time, I encourage any developer seeking to work on this code
to first dedicate their efforts into making it more maintainable. I would never
write code like this today, and I would have rewritten the plugin myself from
the ground up if I had had more time to actually do it. I apologize in advance.

Issues or pull requests made to this repository will be ignored.

## Original plugin description

_The smooooooothest Camera Studio -like waypoint and path system in town!_

ServerCinematics is an attempt to replicate the Camera Studio Mod server-side, so that its users would not have to install the mod into their clients, or to evade the delays of mod updates when a new Minecraft version comes out.

ServerCinematics has a system to create paths using waypoints. Such paths can then be played back using a configurable speed, which can be dynamically controlled by both the player and any of the waypoints. The player is moved smoothly with velocity, so that the freedom of turning around still stands, except if you enable tpmode.

List of all features:

- Add waypoints and make a path
- Insert waypoints in middle of the path
- Remove any waypoint
- Turns between points are smooth
- Players are moved with velocity, not with teleportation, so it is possible to turn freely (when not in tpmode)
- Path calculated with Catmull-Rom spline (smooth playback)
- List all waypoints, or teleport to one
- Waypoints can change the playing speed dynamically
- Paths can be saved/loaded
- Every player has their own path, which can be shared with either save/load or by cloning from another player
- Pause and resume playing at any time
- Force another player to play a path or stop at any time
- Use tpmode which is less smooth but supports yaw/pitch
- Add messages to waypoints
- Add commands to waypoints
- Create playlists to play and loop over a specified list of paths
- Stop the player temporarily at specific waypoints with delays
- Configure immediate teleportation between two waypoints

Permissions:

`servercinematics.use`: grants permission to use all features of this plugin (except for servercinematics.cmd). only give to trusted players.

`servercinematics.edit`: grants permission to edit paths.

`servercinematics.play`: only grants access to clear, play, pause, resume, stop, load, tpmode, speed and playlist.

`servercinematics.fplay`: grants permission to use fplay, fstop and similar commands.

`servercinematics.cmd`: allows editing path commands. THIS ALLOWS USERS TO CHANGE PATH COMMANDS WHICH CAN BE USED TO RUN ARBITRARY COMMANDS AS OTHER PLAYERS OR THE SERVER. GRANT WITH EXTREME CAUTION.
