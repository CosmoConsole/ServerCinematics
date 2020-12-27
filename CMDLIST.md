# Command list

All commands should also work with /cam instead of /camera

- /cam add (speed / n) (yaw / n / c) (pitch / n / c)
  - Adds a new waypoint.
  - The speed parameter is optional: if set to any positive number, the waypoint will change the current movement speed. If set to "n", speed will not change.
  - Yaw and pitch are normally set to the same direction you are facing. If 'n' is set, it sets to not effect yaw/pitch. 'c' in both means "current" and means the current yaw and pitch of yourself. Please note that yaw/pitch only work in tpmode.
- /cam insert id (speed / n) (yaw / n / c) (pitch / n / c)
  - Similar to /cam add, but with an extra parameter.
  - The id parameter specifies the new id of the point. All following points will be shifted to the next id. Yaw and pitch act the same as in add.
- /cam edit id (speed / n / d) (yaw / n / c / d) (pitch / n / c / d)
  - Allows to edit the properties of a waypoint, currently only speed.
  - Specify the id and the new speed. If you do not want to change the speed, enter d (for don't change) into the speed field. If you want to change the speed to be not affected when flying through, set n. Yaw and pitch act the same as in add, except for the d functionality.
- /cam msg id (set msg / setcolored msg / remove)
  - Allows adding a message to waypoint. With only the ID parameter, that current message is shown.
  - set and setcolored allow you to set the message. setcolored will format color codes (such as &4 -> §4) but set will not.
  - remove will remove the message from that waypoint.
- /cam cmd id (add command / get id / list / remove id)
  - Allows adding commands to a waypoint. They can be listed with the list command.
  - Remove and Get both take an ID to either remove or print that specific command.
  - When adding command with add, do not precede them with /. %player% acts as a wildcard, being replaced with the player's name when the command is run.
  - Since v1.14.2, this requires a special permission to be granted since it is an extremely dangerous command in the wrong hands.
- /cam playlist (add path / insert pos path / remove pos / list / clear / play / loop)
  - Allows creating playlists. Adding paths, listing and clearing are self-explanatory.
  - List shows the position of the item in the list, which can be entered into remove or used to enter item into somewhere else than the end of the list by using Insert.
  - Play will play the playlist once, and Loop will play it forever.
  - To stop a playlist, you use /cam stop.
- /cam option (id) (option)
  - See all possible options. Options are similar to flags, but are set on a per-waypoint basis. (Current options are 0 (immediately teleport to next waypoint from this point if tpmode is enabled))
  - If a waypoint ID is given, show all options enabled for that waypoint.
  - If the option number is also given, the given option is toggled for that waypoint.
- /cam delay id (delay)
  - See the delay in a waypoint (in seconds), with precision up to 0.05 seconds, or set it. Delay 0 means no delay.
  - During a delay, the player is kept in place in that waypoint.
- /cam clone player
  - Copies the current path of a player and sets it as your path.
- /cam list
  - Lists the waypoints of the current path.
- /cam goto id
  - Lets you teleport directly to any waypoint. You need to know the waypoint ID (try checking /cam list)
- /cam remove (id)
  - Removes the waypoint with specified ID from the path. If ID is not specified, removes the last waypoint.
- /cam clear
  - Clears your entire current path.
- /cam load (file)
  - Loads a path from a file. If no file is specified, lists all saved paths.
- /cam save file
  - Saves your current path to a file. ALWAYS SAVE YOUR PATH BEFORE LOADING OR PLAYING OTHER PATHS.
- /cam speed (speed)
  - Views the current starting speed of a path or changes it.
- /cam play
  - Plays the path. If no spline is calculated, does so first.
- /cam tplay hours:minutes:seconds.frac
  - Plays the path with the given time: for example, 0:10 means the path should take 10 seconds, 1:00:00 an hour and 0:05:00.500 5 minutes and half a second to play,. The plugin first needs to know the period of time that it takes to play the path normally (which is saved with the save command, but needs to be re-evaluated on every edit).
- /cam fplay player path (tp | notp | pathless)
  - Loads the specified path and plays it to a player. If the fourth parameter is "tp", the path will be played back with "tpmode". (read below), and if the fourth parameter is "notp", the tpmode is disabled for that path.
  - If player is **; starts a global playback mode. All players on the server will be synchronized to play and loop the playlist of the player that issued the command. The path must still be specified, but does not affect the result in any way.
  - If the player you're doing this on has a path that they are editing, warn them beforehand - their path will not be saved automatically.
- /cam ftplay player path hours:minutes:seconds.frac (tp | notp | pathless)
  - Similar to /cam fplay, but also takes the time similar to /cam tplay.
- /cam fstop player
  - Stops any path the player is running. If player is **, stops global playback.
- /cam fclear player
  - Clears the path of any player.
- /cam tpmode
  - Toggles 'tpmode'. When tpmode is on, players will be teleported rather than moved around with velocity. This makes it less smooth (and the freedom of turning is disabled), **but makes it yaw/pitch settings in waypoints function**.
- /cam pathless
  - Toggles pathless mode. Pathless mode has no intermediate spline points and tpmode is force-enabled.
- /cam pause
  - Pauses your current playback of any path.
- /cam resume
  - Unpauses from /cam pause.
- /cam stop
  - Aborts any playback you have running.
- /cam flags
  - See all possible flags. (0: teleport player to the first waypoint when path ends, 1: teleport player back to where they were before playback when the path ends, 2: allow player to turn camera during delay even during tpmode, 3: smoothe the start of a path playback, 4: smoothe the end of a path playback)
- /cam flag flag_id
  - Toggle a flag in the current path.
