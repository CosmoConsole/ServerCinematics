# API descriptio
_(API available from version v1.12.7 onwards)_
Simply get the instance of the plugin, and you'll have access to the following functions:

```java
    /**
     * Gets the list to available paths that can be played.
     *
     * @return List that contains the names of all paths that can be loaded
     */
    List<String> getAvailablePaths();
 
    /**
     * Gets the path the player is playing.
     *
     * @param player The player to check for.
     * @return A path name if playing (MODIFIED_PATH if the player has modified it since) or null if the player is not playing.
     */
    String getPlayingPath(Player player);

    /**
     * Returns whether the player is currently playing a path.
     *
     * @param player The player to check for.
     * @return Whether playing or not.
     */
    public boolean isPlaying(Player player);

    /**
     * Saves the current path used by the player. Designed to only be used around paths played by the plugin if it isn't obvious to the player that they should save first. Needs to be called before playing paths.
     *
     * @param player The player to save the path of.
     * @return A SavedPlayerPath that can be given to restorePlayerPath().
     */
    public SavedPlayerPath savePlayerPath(Player player);
 
    /**
     * Restores a previously saved path by the player. Designed to only be used around paths played by the plugin if it isn't obvious to the player that they should save first.
     *
     * @param player The player to restore the path to.
     * @param path A SavedPlayerPath that is to be restored.
     */
    public void restorePlayerPath(Player player, SavedPlayerPath path);
 
    /**
     * Loads and starts to play a path for a specific player. If the player has a path they are editing, the path will be overwritten.
     * If you want to avoid this, you can store the current player path for later by using savePlayerPath().
     * If already playing a path, throws an exception.
     *
     * @param player
     * @param path
     * @param speed The speed to play at.
     * @param tpmode Whether to enable tpmode for the player.
     * @param pathless Whether to enable pathless mode for the player.
     * @return An ID for this playback instance that is also available on the start/stop events. Always positive; 0 if playback could not start.
     */
    long startPath(Player player, String path, double speed, boolean tpmode, boolean pathless);
 
    /**
     * Starts to play a path for a specific player. If the player has a path they are editing, the path will be overwritten.
     * If you want to avoid this, you can store the current player path for later by using savePlayerPath().
     * If already playing a path, throws an exception.
     *
     * @param player
     * @param path The path as a CinematicPath.
     * @param speed The speed to play at.
     * @param tpmode Whether to enable tpmode for the player.
     * @param pathless Whether to enable pathless mode for the player.
     * @return An ID for this playback instance that is also available on the start/stop events. Always positive; 0 if playback could not start.
     */
    long startPath(Player player, CinematicPath path, double speed, boolean tpmode, boolean pathless);
 
    /**
     * Pauses the playback for a specific player.
     *
     * @param player The player whose playback is to be paused.
     */
    void pausePath(Player player);
 
    /**
     * Resumes the playback for a specific player.
     *
     * @param player The player whose playback is to be resumed from a pause.
     */
    void resumePath(Player player);
 
    /**
     * Stops playing the path for a player.
     *
     * @param player
     */
    void stopPath(Player player);
 
    /**
     * Checks whether a player is in tpmode.
     *
     * @param player The player to check for.
     * @return Whether the given player is in tpmode.
     */
    boolean isTpMode(Player player);
 
    /**
     * Sets whether a player is in tpmode.
     *
     * @param player The player to check for.
     * @param value The new value to set for tpmode for that specific player.
     */
    void setTpMode(Player player, boolean value);
 
    /**
     * Checks whether a player is in pathless mode.
     *
     * @param player The player to check for.
     * @return Whether the given player is in pathless mode.
     */
    boolean isPathless(Player player);
 
    /**
     * Sets whether a player is in pathless mode.
     *
     * @param player
     * @param value
     */
    void setPathless(Player player, boolean value);
```

In addition, you have the events PathPlaybackStartedEvent, PathPlaybackStoppedEvent and WaypointReachedEvent. These events will trigger when a playback starts for a player, stops for a player or when a player reaches a waypoint in the path respectively.

PathPlaybackStartedEvent gives you the player and the name of the path (MODIFIED_PATH if path name cannot be applied).

PathPlaybackStoppedEvent gives the player and the path name similar to PathPlaybackStartedEvent but also the cause for the playback to stop.

WaypointReachedEvent gives the player and the path name similar to PathPlaybackStartedEvent but also the waypoint index (from 0 onwards) and the length of the path in waypoints (waypoint index will never reach the length).

CinematicPath and CinematicWaypoint can be used to create paths from the API. To create a path, create a CinematicPath instance, get `List<CinematicWaypoint>` using getWaypoints() and add CinematicWaypoint instances to that list. You can modify waypoint as well as path properties with methods. Some notes:

CinematicWaypoint location: undefined behavior if the Locations point to different worlds within a single path.
CinematicWaypoint speed: can be null, in which case the point has no speed. Corresponds to speed n in /cam add.
CinematicWaypoint yaw, pitch: can be null, in which case the camera will not turn for this point even in tpmode. Corresponds to yaw n and pitch n in /cam add.
CinematicWaypoint messages: you must do the color code conversion yourself.
CinematicWaypoint commands: same syntax as with /cam cmd: no /, and you can use ~ as a prefix to run a command as the player instead; if not, you can use %player% to get the player user name.
CinematicWaypoint options, CinematicPath flags: configured with special boolean functions for each option/flag.

