package email.com.gmail.cosmoconsole.bukkit.camera;

import org.bukkit.plugin.java.*;
import org.bukkit.plugin.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.nio.file.*;
import java.nio.charset.*;
import org.bukkit.util.Vector;

import email.com.gmail.cosmoconsole.bukkit.camera.PathPlaybackStartedEvent.StartCause;
import email.com.gmail.cosmoconsole.bukkit.camera.PathPlaybackStoppedEvent.StopCause;

import java.util.*;
import java.util.regex.Pattern;

import org.bukkit.scheduler.*;
import org.bukkit.*;
import java.io.*;

/*
 * 
 * Disclaimers to anyone reading this source code:
 * 
 * 1. This code is ugly. It started development in 2014 and has never gone
 *    under a major code clean-up or refactoring process.
 *    Don't be under the false assumption that I think this is clean
 *    or well laid out code.
 *    
 * 2. Anyone who tries to maintain this plugin should first and foremost
 *    think of a plan to clean up, refactor or perhaps even completely
 *    rewrite this code, in whole or in part, particularly the
 *    DeathListener class which is much of the core of this plugin's
 *    functionality.
 *    
 *    ~~ Refactoring the code should be the UTMOST priority of anyone
 *       willing to maintain this plugin long-term.
 * 
 * 3. Despite my best efforts, the plugin suffers from extensive feature
 *    creep, which will greatly complicate any such plans to refactor
 *    this code. Notwithstanding that, it should remain the top priority
 *    to anyone willing to maintain this code.
 * 
 * This source code was released publicly on 2020-31-12. If any subsequent
 * modifications have been made to it by its original developer, those
 * changes have last been made on 2020-31-12.
 * 
 */

/**
 * ServerCinematics main class.
 */
public class ServerCinematics extends JavaPlugin implements Listener, TabCompleter
{
    /**
     * A player's path is MODIFIED_PATH if the player has modified it and it hasn't been saved since.
     */
    public final String MODIFIED_PATH = "";
    final double SPDLIMIT = 0.5;
    final double CAN_TURN = 2.5;
    final double DEFSPEED = 5.0;
    final int CALCPOINTS = 20;
    static ServerCinematics instance;
    static long pbid = 0;
    File paths;
    // player info
    HashMap<UUID, Boolean> teleport;
    HashMap<UUID, Boolean> pathless;
    HashMap<UUID, Double> speed;
    HashMap<UUID, String> pathnames;
    // path info
    HashMap<UUID, ArrayList<Location>> waypoints; // location
    HashMap<UUID, ArrayList<Double>> waypoints_s; // speed
    HashMap<UUID, ArrayList<Double>> waypoints_y; // yaw
    HashMap<UUID, ArrayList<Double>> waypoints_p; // pitch
    HashMap<UUID, ArrayList<String>> waypoints_m; // message
    HashMap<UUID, ArrayList<List<String>>> waypoints_c; // commands
    HashMap<UUID, ArrayList<Integer>> waypoints_l; // waypoint flags
    HashMap<UUID, ArrayList<Double>> waypoints_d; // delay
    HashMap<UUID, ArrayList<Boolean>> waypoints_i; // instant?
    HashMap<UUID, Integer> waypoints_f; // path flags
    HashMap<UUID, Double> waypoints_t; // time to play path, or -1 if not yet determined
    // playlist info
    HashMap<UUID, ArrayList<String>> pl_paths;
    HashMap<UUID, Boolean> pl_playing;
    HashMap<UUID, Boolean> pl_looping;
    HashMap<UUID, Integer> pl_index;
    HashMap<UUID, Double> multipl;
    UUID globalMode;
    int timer_id;
    // temp info
    HashMap<UUID, Double> speed_a;
    HashMap<UUID, Boolean> old_af;
    HashMap<UUID, Boolean> old_f;
    HashMap<UUID, GameMode> old_gm;
    HashMap<UUID, Float> old_fsp;
    HashMap<UUID, Boolean> playing;
    HashMap<UUID, Boolean> paused;
    // interpolated path info (temp)
    HashMap<UUID, ArrayList<Location>> wx; // location
    HashMap<UUID, ArrayList<Double>> wxs; // speed
    HashMap<UUID, ArrayList<Double>> wxy; // yaw
    HashMap<UUID, ArrayList<Double>> wxp; // pitch
    HashMap<UUID, ArrayList<String>> wxm; // message
    HashMap<UUID, ArrayList<List<String>>> wxc; // commands
    HashMap<UUID, ArrayList<Integer>> wxl; // waypoint flags
    HashMap<UUID, ArrayList<Double>> wxd; // delay
    HashMap<UUID, ArrayList<Boolean>> wxi; // instant?
    HashMap<UUID, ArrayList<Boolean>> wxtemp; // is point interpolated?
    HashMap<UUID, ArrayList<Integer>> wxindx; // actual index of original point
    HashMap<UUID, Integer> wxf;
    HashMap<UUID, Boolean> wm;
    HashMap<UUID, Location> old_loc;
    HashMap<UUID, Integer> timer_ids;
    HashMap<UUID, Long> pbids;
    ArrayList<Player> tempJoins;
    
    static {
        ServerCinematics.instance = null;
    }
    
    public ServerCinematics() {
        this.timer_id = 0;
        this.tempJoins = null;
        this.globalMode = null;
        this.old_af = null;
        this.old_f = null;
        this.old_gm = null;
        this.old_fsp = null;
        this.playing = null;
        this.paused = null;
        this.teleport = null;
        this.pathless = null;
        this.speed = null;
        this.speed_a = null;
        this.waypoints = null;
        this.waypoints_s = null;
        this.waypoints_y = null;
        this.waypoints_p = null;
        this.waypoints_m = null;
        this.waypoints_c = null;
        this.waypoints_f = null;
        this.waypoints_t = null;
        this.waypoints_l = null;
        this.waypoints_d = null;
        this.waypoints_i = null;
        this.pathnames = null;
        this.wx = null;
        this.wxs = null;
        this.wxy = null;
        this.wxp = null;
        this.wxm = null;
        this.wxc = null;
        this.wxf = null;
        this.wxl = null;
        this.wxd = null;
        this.wxi = null;
        this.wxtemp = null;
        this.wxindx = null;
        this.wm = null;
        this.paths = null;
        this.pl_paths = null;
        this.pl_playing = null;
        this.pl_looping = null;
        this.pl_index = null;
        this.multipl = null;
        this.old_loc = null;
        this.timer_ids = null;
        this.pbids = null;
    }

    public void onDisable() {
    }
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.waypoints = new HashMap<UUID, ArrayList<Location>>();
        this.waypoints_s = new HashMap<UUID, ArrayList<Double>>();
        this.waypoints_y = new HashMap<UUID, ArrayList<Double>>();
        this.waypoints_p = new HashMap<UUID, ArrayList<Double>>();
        this.waypoints_m = new HashMap<UUID, ArrayList<String>>();
        this.waypoints_c = new HashMap<UUID, ArrayList<List<String>>>();
        this.waypoints_f = new HashMap<UUID, Integer>();
        this.waypoints_t = new HashMap<UUID, Double>();
        this.waypoints_d = new HashMap<UUID, ArrayList<Double>>();
        this.waypoints_l = new HashMap<UUID, ArrayList<Integer>>();
        this.waypoints_i = new HashMap<UUID, ArrayList<Boolean>>();
        this.pathnames = new HashMap<UUID, String>();
        this.wx = new HashMap<UUID, ArrayList<Location>>();
        this.wxs = new HashMap<UUID, ArrayList<Double>>();
        this.wxy = new HashMap<UUID, ArrayList<Double>>();
        this.wxp = new HashMap<UUID, ArrayList<Double>>();
        this.wxm = new HashMap<UUID, ArrayList<String>>();
        this.wxc = new HashMap<UUID, ArrayList<List<String>>>();
        this.wxf = new HashMap<UUID, Integer>();
        this.wxd = new HashMap<UUID, ArrayList<Double>>();
        this.wxl = new HashMap<UUID, ArrayList<Integer>>();
        this.wxi = new HashMap<UUID, ArrayList<Boolean>>();
        this.wxtemp = new HashMap<UUID, ArrayList<Boolean>>();
        this.wxindx = new HashMap<UUID, ArrayList<Integer>>();
        this.wm = new HashMap<UUID, Boolean>();
        this.old_af = new HashMap<UUID, Boolean>();
        this.old_f = new HashMap<UUID, Boolean>();
        this.old_gm = new HashMap<UUID, GameMode>();
        this.old_fsp = new HashMap<UUID, Float>();
        this.playing = new HashMap<UUID, Boolean>();
        this.paused = new HashMap<UUID, Boolean>();
        this.teleport = new HashMap<UUID, Boolean>();
        this.pathless = new HashMap<UUID, Boolean>();
        this.speed = new HashMap<UUID, Double>();
        this.speed_a = new HashMap<UUID, Double>();
        this.old_loc = new HashMap<UUID, Location>();
        this.timer_ids = new HashMap<UUID, Integer>();
        this.pl_paths = new HashMap<UUID, ArrayList<String>>();
        this.pl_playing = new HashMap<UUID, Boolean>();
        this.pl_looping = new HashMap<UUID, Boolean>();
        this.pl_index = new HashMap<UUID, Integer>();
        this.multipl = new HashMap<UUID, Double>();
        this.tempJoins = new ArrayList<Player>();
        this.pbids = new HashMap<UUID, Long>();
        ServerCinematics.instance = this;
        if (!this.getDataFolder().isDirectory()) {
            this.getDataFolder().mkdir();
        }
        final File paths = new File(this.getDataFolder(), "paths");
        if (!paths.isDirectory()) {
            paths.mkdir();
        }
        this.short_prefix = this.getConfig().getBoolean("short-prefix", false);
        this.final_tp = this.getConfig().getBoolean("final-waypoint-teleport", true);
        this.paths = paths;
        this.getCommand("camera").setTabCompleter(this);
    }
    boolean short_prefix = false;
    boolean final_tp = true;
    
    @EventHandler(priority = EventPriority.HIGH)
    public void disableFlyOff(final PlayerToggleFlightEvent playerToggleFlightEvent) {
        try {
            if (this.playing.get(playerToggleFlightEvent.getPlayer().getUniqueId())) {
                playerToggleFlightEvent.getPlayer().setAllowFlight(true);
                playerToggleFlightEvent.getPlayer().setFlying(true);
                playerToggleFlightEvent.setCancelled(true);
            } else if (globalMode != null) {
                playerToggleFlightEvent.getPlayer().setGameMode(GameMode.SPECTATOR);
                playerToggleFlightEvent.getPlayer().setAllowFlight(true);
                playerToggleFlightEvent.getPlayer().setFlying(true);
                playerToggleFlightEvent.setCancelled(true);
            }
        }
        catch (Exception ex) {}
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void globalMode_join(final PlayerJoinEvent e) {
        if (globalMode != null) {
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
            e.getPlayer().setAllowFlight(true);
            e.getPlayer().setFlying(true);
            tempJoins.add(e.getPlayer());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void leave_0day_gm3(final PlayerQuitEvent e) {
        if (globalMode != null) {
            Location l = getServer().getWorlds().get(0).getSpawnLocation();
            e.getPlayer().setVelocity(new Vector(0, 0, 0));
            e.getPlayer().teleport(l);
            GameMode gm = getServer().getDefaultGameMode();
            e.getPlayer().setGameMode(gm);
            e.getPlayer().setAllowFlight(gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR);
            e.getPlayer().setFlying(gm == GameMode.SPECTATOR);
            return;
        }
        if (this.isTrue(this.playing.get(e.getPlayer().getUniqueId()))) {
            this.stop(e.getPlayer(), StopCause.LEFT);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void no_teleport(final PlayerTeleportEvent e) {
        try {
            if (this.playing.get(e.getPlayer().getUniqueId()) && e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
                e.setCancelled(true);
            }
        }
        catch (Exception ex) {}
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        Player p = event.getPlayer();
        if (!playing.containsKey(p.getUniqueId()))
            return;
        if (!playing.get(p.getUniqueId()))
            return;
        List<String> cmds = this.getConfig().getStringList("blacklist-command-list");
        for (String command : cmds) {
            if (command.startsWith(":")) {
                String acommand = command.substring(1);
                if (event.getMessage().equalsIgnoreCase("/" + acommand) || event.getMessage().toLowerCase().startsWith("/" + acommand + " ")) {
                    event.setCancelled(true);
                }
                if (event.getMessage().split(" ")[0].split(":").length > 1) {
                    if (event.getMessage().split(" ")[0].split(":")[1].equalsIgnoreCase(acommand)) {
                        event.setCancelled(true);
                    }
                }
            }
            if (event.getMessage().equalsIgnoreCase("/" + command) || event.getMessage().toLowerCase().startsWith("/" + command + " ")) {
                event.setCancelled(true);
            }
        }
    }

    
    /**
     * Gets the list to available paths that can be played.
     * 
     * @return List that contains the names of all paths that can be loaded
     */
    public List<String> getAvailablePaths() {
        ArrayList<String> list = new ArrayList<>();
        File[] listFiles;
        for (int length3 = (listFiles = this.paths.listFiles()).length, n10 = 0; n10 < length3; ++n10) {
            final File file2 = listFiles[n10];
            if (file2.isFile()) {
                list.add(file2.getName());
            }
        }
        return list;
    }
    
    /**
     * Gets the path the player is playing.
     * 
     * @param player The player to check for.
     * @return A path name if playing (MODIFIED_PATH if the player has modified it since or the plugin supplied a custom path) or null if the player is not playing.
     */
    public String getPlayingPath(Player player) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        if (isTrue(this.playing.get(player.getUniqueId()))) {
            return this.pathnames.get(player.getUniqueId());
        }
        return null;
    }
    
    /**
     * Returns whether the player is currently playing a path.
     * 
     * @param player The player to check for.
     * @return Whether playing or not.
     */
    public boolean isPlaying(Player player) {
        return isTrue(this.playing.get(player.getUniqueId()));
    }
    
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
    public long startPath(Player player, String path, double speed, boolean tpmode, boolean pathless) throws Exception {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        if (path == null) throw new IllegalArgumentException("path cannot be null");
        if (isPlaying(player)) {
            throw new IllegalArgumentException("player already playing");
        }
        int j = loadForPlaylist(player, path, false);
        if (j == -2) {
            throw new IllegalArgumentException("No path called '" + path + "' exists");
        } else if (j == -1) {
            throw new Exception("An unknown exception occurred while loading path: it has been printed to server console");
        } else if (j > 0) {
            this.pl_playing.put(player.getUniqueId(), false);
            this.pl_looping.put(player.getUniqueId(), false);
            this.speed.put(player.getUniqueId(), speed);
            this.teleport.put(player.getUniqueId(), tpmode);
            this.pathless.put(player.getUniqueId(), pathless);
            return play(player, null, true, StartCause.PLUGIN);
        }
        return 0;
    }
    
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
    public long startPath(Player player, CinematicPath path, double speed, boolean tpmode, boolean pathless) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        if (path == null) throw new IllegalArgumentException("path cannot be null");
        if (isPlaying(player)) {
            throw new IllegalArgumentException("player already playing");
        }
        deserializePath(player, path);
        if (this.waypoints.get(player.getUniqueId()).size() < 1) {
            return 0;
        }
        this.teleport.put(player.getUniqueId(), tpmode);
        this.pathless.put(player.getUniqueId(), pathless);
        this.speed.put(player.getUniqueId(), speed);
        this.pl_playing.put(player.getUniqueId(), false);
        this.pl_looping.put(player.getUniqueId(), false);
        return play(player, null, true, StartCause.PLUGIN);
    }
    
    @Deprecated
    public long startPath(Player player, String path, boolean tpmode, boolean pathless) throws Exception {
        return startPath(player, path, speed.get(player.getUniqueId()), tpmode, pathless);
    }

    @Deprecated
    public long startPath(Player player, CinematicPath path, boolean tpmode, boolean pathless) {
        return startPath(player, path, speed.get(player.getUniqueId()), tpmode, pathless);
    }
    
    /**
     * Saves the current path used by the player. Designed to only be used around paths played by the plugin if it isn't obvious to the player that they should save first. Needs to be called before playing paths.
     * 
     * @param player The player to save the path of.
     * @return A SavedPlayerPath that can be given to restorePlayerPath().
     */
    public SavedPlayerPath savePlayerPath(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("player cannot be null");
        }
        
        UUID u = player.getUniqueId();
        return new SavedPlayerPath(u,
                                   teleport.get(u),
                                   pathless.get(u),
                                   speed.get(u),
                                   pathnames.get(u),
                                   waypoints.get(u),
                                   waypoints_s.get(u),
                                   waypoints_y.get(u),
                                   waypoints_p.get(u),
                                   waypoints_m.get(u),
                                   waypoints_c.get(u),
                                   waypoints_l.get(u),
                                   waypoints_d.get(u),
                                   waypoints_i.get(u),
                                   waypoints_f.get(u),
                                   waypoints_t.get(u));
    }
    
    /**
     * Restores a previously saved path by the player. Designed to only be used around paths played by the plugin if it isn't obvious to the player that they should save first.
     * 
     * @param player The player to restore the path to.
     * @param path A SavedPlayerPath that is to be restored.
     */
    public void restorePlayerPath(Player player, SavedPlayerPath path) {
        if (player == null) {
            throw new IllegalArgumentException("player cannot be null");
        }
        
        UUID u = path.owner;
        if (player.getUniqueId().equals(u)) {
            putIfNotNull(teleport, u, path.teleport);
            putIfNotNull(pathless, u, path.pathless);
            putIfNotNull(speed, u, path.speed);
            putIfNotNull(pathnames, u, path.pathnames);
            putIfNotNull(waypoints, u, path.waypoints);
            putIfNotNull(waypoints_s, u, path.waypoints_s);
            putIfNotNull(waypoints_y, u, path.waypoints_y);
            putIfNotNull(waypoints_p, u, path.waypoints_p);
            putIfNotNull(waypoints_m, u, path.waypoints_m);
            putIfNotNull(waypoints_c, u, path.waypoints_c);
            putIfNotNull(waypoints_l, u, path.waypoints_l);
            putIfNotNull(waypoints_d, u, path.waypoints_d);
            putIfNotNull(waypoints_i, u, path.waypoints_i);
            putIfNotNull(waypoints_f, u, path.waypoints_f);
            putIfNotNull(waypoints_t, u, path.waypoints_t);
            clearCache(player);
        } else {
            throw new IllegalArgumentException("trying to restoring path to different player");
        }
    }

    /**
     * Pauses the playback for a specific player.
     * 
     * @param player The player whose playback is to be paused.
     */
    public void pausePath(Player player) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        if (isTrue(this.playing.get(player.getUniqueId())) && isFalse(this.paused.get(player.getUniqueId()))) {
            this.paused.put(player.getUniqueId(), true);
        }
    }
    
    /**
     * Resumes the playback for a specific player.
     * 
     * @param player The player whose playback is to be resumed from a pause.
     */
    public void resumePath(Player player) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        if (isTrue(this.playing.get(player.getUniqueId())) && isTrue(this.paused.get(player.getUniqueId()))) {
            this.paused.put(player.getUniqueId(), false);
        }
    }
    
    /**
     * Stops playing the path for a player.
     * 
     * @param player
     */
    public void stopPath(Player player) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        stop(player, PathPlaybackStoppedEvent.StopCause.PLUGIN);
    }
    
    /**
     * Checks whether a player is in tpmode.
     * 
     * @param player The player to check for.
     * @return Whether the given player is in tpmode.
     */
    public boolean isTpMode(Player player) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        if (teleport.containsKey(player.getUniqueId())) {
            return teleport.get(player.getUniqueId());
        }
        return false;
    }
    
    /**
     * Sets whether a player is in tpmode.
     * 
     * @param player The player to check for.
     * @param value The new value to set for tpmode for that specific player.
     */
    public void setTpMode(Player player, boolean value) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        teleport.put(player.getUniqueId(), value);
    }
    
    /**
     * Checks whether a player is in pathless mode.
     * 
     * @param player The player to check for.
     * @return Whether the given player is in pathless mode.
     */
    public boolean isPathless(Player player) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        if (pathless.containsKey(player.getUniqueId())) {
            return pathless.get(player.getUniqueId());
        }
        return false;
    }
    
    /**
     * Sets whether a player is in pathless mode.
     * 
     * @param player
     * @param value
     */
    public void setPathless(Player player, boolean value) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        pathless.put(player.getUniqueId(), value);
    }

    private <K, V> void putIfNotNull(HashMap<K, V> map, K key, V value) {
        if (value != null) {
            map.put(key, value);
        } else {
            map.remove(key);
        }
    }
    private static final String helpString = "" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics " + make_color('f') + "by " + make_color('f') + "CosmoConsole, Kisko, Pietu1998\n/### help - Display this message\n/### add (speed) (yaw/N/C) (pitch/N/C) - Add waypoint to your present location, as last waypoint to your path (and possibly make it change speed)\n/### insert id (speed) (yaw/N/C) (pitch/N/C) - Add waypoint to your present location, to your path at a specific position (and possibly make it change speed)\n/### edit id <speed / d(on't change)> [yaw/N/C/d] [pitch/N/C/d] - Edit properties of a waypoint\n/### clone player - Clone someone elses path\n/### msg id {set msg | setcolored msg | remove} - Set/remove a message to a waypoint\n/### cmd id {add | list | get | remove} - See/add/remove commands of a waypoint\n/### option (id) (option) - See all possible options for a waypoint or toggle one of them\n/### delay id delay - See, add or remove a waypoint delay\n/### flags - See all possible flags.\n/### flag id - Toggles a flag for the path.\n/### list - List waypoints in your path\n/### playlist {add | list | insert | remove | clear | play | loop} - See/add/remove commands of a waypoint\n/### goto id - Teleports to a waypoint\n/### remove (id) - Remove a waypoint from your path (default: last one)\n/### clear - Clear your path\n/### load (file) - List saved paths or load one\n/### save file - Save the current path to a file\n/### speed (speed) - Get / set flying speed\n/### play - Play your path\n/### tplay ((hours:)minutes:)seconds - Play your path with specific duration\n/### tpmode - Toggle tpmode (tpmode has pitch & yaw support but is less smooth)\n/### pathless - Toggle pathless mode (automatic tpmode + teleports to waypoints only)\n/### fplay player path (tp | notp | pathless) - Force the player to load and play a path (possibly in tpmode)\n/### ftplay player ((hh:)mm:)ss path (tp | notp | pathless) - Force the player to load and play a path with specific timespan (possibly in tpmode)\n/### fstop player - Force the player to stop the current path\n/### fclear player - Clear the path of another player\n/### pause - Pause the current path\n/### resume - Resume from last pause\n/### stop - Stop playing\n/### reload - Reload configuration";
    
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] array) {
        if (!command.getName().equalsIgnoreCase("camera")) {
            return false;
        }
        if (array.length < 1) {
            commandSender.sendMessage("" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics " + make_color('f') + "by " + make_color('f') + "CosmoConsole, Kisko, Pietu1998, " + make_color('a') + "version " + this.getDescription().getVersion());
            commandSender.sendMessage("" + make_color('7') + "See /cam help for help.");
            return true;
        }
        if ((!array[0].startsWith("f") || array[0].equalsIgnoreCase("flag")) && !(commandSender instanceof Player)) {
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Only players can use.");
            return true;
        }
        if (commandSender instanceof Player) {
            this.getSafeWaypointDelays(commandSender);
            this.getSafeWaypointOptions(commandSender);
            this.getSafeWaypointInstants(commandSender);
        }
        final String s2 = array[0];
        if (s2.equalsIgnoreCase("help")) {
            this.sendMultilineMessage(commandSender, helpString.replace("###", s), "" + make_color('7') + "");
        }
        else if (s2.equalsIgnoreCase("list")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "List of waypoints in your path:");
            int n = 0;
            final ArrayList<Double> safeWaypointYaw = this.getSafeWaypointYaw(commandSender);
            final ArrayList<Double> safeWaypointPitch = this.getSafeWaypointPitch(commandSender);
            for (final Location location : this.getSafeWaypoints(commandSender)) {
                final double doubleValue = safeWaypointYaw.get(n);
                final double doubleValue2 = safeWaypointPitch.get(n);
                commandSender.sendMessage("" + make_color('e') + "" + n + "" + make_color('7') + ": " + make_color('f') + "" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + ":" + ((doubleValue > 400.0) ? "-" : String.format(Locale.ENGLISH, "%.1f", doubleValue)) + "," + ((doubleValue2 > 400.0) ? "-" : String.format(Locale.ENGLISH, "%.1f", doubleValue2)));
                ++n;
            }
            if (n == 0) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "None at all!");
            }
        }
        else if (s2.equalsIgnoreCase("delay")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " delay id delaylength");
                return true;
            }
            if (array.length < 3) {
                int b = 0;
                final Player p = (Player)commandSender;
                try {
                    b = Integer.parseInt(array[1]);
                    if (b < 0) throw new ArrayIndexOutOfBoundsException();
                    if (b >= this.waypoints_l.get(p.getUniqueId()).size()) throw new ArrayIndexOutOfBoundsException();
                } catch (Exception ex) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Invalid path ID!");
                    return true;
                }
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "Current delay: " + String.format("%.2f", this.waypoints_d.get(p.getUniqueId()).get(b)));
                return true;
            }
            try {
                final double int7 = Double.parseDouble(array[2]);
                final Player p = (Player)commandSender;
                int b = 0;
                try {
                    b = Integer.parseInt(array[1]);
                    if (b < 0) throw new ArrayIndexOutOfBoundsException();
                    if (b >= this.waypoints_l.get(p.getUniqueId()).size()) throw new ArrayIndexOutOfBoundsException();
                } catch (Exception ex) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Invalid path ID!");
                    return true;
                }
                if (int7 < 0) {
                    throw new IndexOutOfBoundsException();
                }
                this.clearCache((Player)commandSender);
                this.clearPathName((Player)commandSender);
                this.waypoints_d.get(p.getUniqueId()).set(b, int7);
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "Delay set.");
            }
            catch (NumberFormatException ex3) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Please type an integer.");
            }
            catch (IndexOutOfBoundsException ex4) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Invalid delay!");
            }
            catch (Exception ex5) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Could not toggle option!");
            }
        }
        /*else if (s2.equalsIgnoreCase("instant")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " instant id");
                return true;
            }
            try {
                final Player p = (Player)commandSender;
                int b = 0;
                try {
                    b = Integer.parseInt(array[1]);
                    if (b < 0) throw new ArrayIndexOutOfBoundsException();
                    if (b >= this.waypoints_l.get(p.getUniqueId()).size()) throw new ArrayIndexOutOfBoundsException();
                } catch (Exception ex) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Invalid path ID!");
                    return true;
                }
                this.clearCache((Player)commandSender);
                boolean newFlag = !this.waypoints_i.get(p.getUniqueId()).get(b);
                this.waypoints_i.get(p.getUniqueId()).set(b, newFlag);
                if (newFlag)
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Point is now instant. (Only works in tpmode)");
                else
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "Point is no longer instant.");
            }
            catch (NumberFormatException ex3) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Please type an integer.");
            }
            catch (IndexOutOfBoundsException ex4) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Invalid point!");
            }
            catch (Exception ex5) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Could not toggle option!");
            }
        }*/
        else if (s2.equalsIgnoreCase("option")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " option id option_id");
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "0 " + make_color('6') + "- " + make_color('f') + "Teleport player immediately to the next point from this waypoint after the delay (if tpmode enabled)");
            }
            if (array.length < 3) {
                if (commandSender instanceof Player) {
                    int b = 0;
                    final Player p = (Player)commandSender;
                    try {
                        b = Integer.parseInt(array[1]);
                        if (b < 0) throw new ArrayIndexOutOfBoundsException();
                        if (b >= this.waypoints_l.get(p.getUniqueId()).size()) throw new ArrayIndexOutOfBoundsException();
                    } catch (Exception ex) {
                        commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Invalid path ID!");
                        return true;
                    }
                    int o = this.waypoints_l.get(p.getUniqueId()).get(b);
                    StringBuilder sb = new StringBuilder();
                    int n = 0;
                    for (int a = 1; a >= 0; a <<= 1) {
                        if ((o&a)>0) {
                            sb.append(n);
                            sb.append(" ");
                        }
                        n++;
                    }
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "Currently enabled options: " + sb.toString());
                }
                return true;
            }
            try {
                final int int7 = Integer.parseInt(array[2]);
                final Player p = (Player)commandSender;
                int b = 0;
                try {
                    b = Integer.parseInt(array[1]);
                    if (b < 0) throw new ArrayIndexOutOfBoundsException();
                    if (b >= this.waypoints_l.get(p.getUniqueId()).size()) throw new ArrayIndexOutOfBoundsException();
                } catch (Exception ex) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Invalid path ID!");
                    return true;
                }
                if (int7 < 0 || int7 > 0) {
                    throw new IndexOutOfBoundsException();
                }
                final int o = this.waypoints_l.get(p.getUniqueId()).get(b) & 1 << int7;
                final boolean oldState = o != 0;
                this.clearCache((Player)commandSender);
                this.clearPathName((Player)commandSender);
                this.waypoints_l.get(p.getUniqueId()).set(b, o ^ 1 << int7);
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "Option " + int7 + " is now " + (oldState ? "" + make_color('c') + "OFF" : "" + make_color('a') + "ON") + "" + make_color('e') + ".");
            }
            catch (NumberFormatException ex3) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Please type an integer.");
            }
            catch (IndexOutOfBoundsException ex4) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Option not found.");
            }
            catch (Exception ex5) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Could not toggle option!");
            }
        }
        else if (s2.equalsIgnoreCase("flags")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "0 " + make_color('6') + "- " + make_color('f') + "Teleport player to first waypoint after path finishes");
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "1 " + make_color('6') + "- " + make_color('f') + "Teleport player to original location after path finishes");
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "2 " + make_color('6') + "- " + make_color('f') + "Allow player to turn during delay (automatically if not tpmode)");
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "3 " + make_color('6') + "- " + make_color('f') + "Smooth velocity from standstill when starting path");
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "4 " + make_color('6') + "- " + make_color('f') + "Smooth velocity from standstill when ending path");
        }
        else if (s2.equalsIgnoreCase("flag")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " flag flag_id");
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "See all possible flags with " + make_color('c') + "/" + s + " flags");
                if (commandSender instanceof Player) {
                    final Player p = (Player)commandSender;
                    int o = this.getSafeWaypointFlags(p);
                    StringBuilder sb = new StringBuilder();
                    int n = 0;
                    for (int a = 1; a >= 0; a <<= 1) {
                        if ((o&a)>0) {
                            sb.append(n);
                            sb.append(" ");
                        }
                        n++;
                    }
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "Currently enabled flags: " + sb.toString());
                }
                return true;
            }
            try {
                final int int7 = Integer.parseInt(array[1]);
                final Player p = (Player)commandSender;
                if (int7 < 0 || int7 > 4) {
                    throw new IndexOutOfBoundsException();
                }
                final int o = this.getSafeWaypointFlags(p) & (1 << int7);
                final boolean oldState = o != 0;
                this.waypoints_t.put(p.getUniqueId(), -1.0);
                this.waypoints_f.put(p.getUniqueId(), this.getSafeWaypointFlags(p) ^ (1 << int7));
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "Flag " + int7 + " is now " + (oldState ? "" + make_color('c') + "OFF" : "" + make_color('a') + "ON") + "" + make_color('e') + ".");
            }
            catch (NumberFormatException ex3) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Please type an integer.");
            }
            catch (IndexOutOfBoundsException ex4) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Flag not found.");
            }
            catch (Exception ex5) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Could not toggle flag!");
            }
        }
        else if (s2.equalsIgnoreCase("reload")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            this.reloadConfig();
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Reloaded.");
        }
        else if (s2.equalsIgnoreCase("add")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            final Location location2 = ((Player)commandSender).getLocation();
            final ArrayList<Location> safeWaypoints = this.getSafeWaypoints(commandSender);
            if (safeWaypoints.size() > 0 && !safeWaypoints.get(0).getWorld().getName().equalsIgnoreCase(location2.getWorld().getName())) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Cannot add waypoints to another world!");
                return true;
            }
            this.clearCache((Player)commandSender);
            this.clearPathName((Player)commandSender);
            safeWaypoints.add(location2);
            this.waypoints.put(((Player)commandSender).getUniqueId(), safeWaypoints);
            final ArrayList<Double> safeWaypointSpeeds = this.getSafeWaypointSpeeds(commandSender);
            final ArrayList<Double> safeWaypointYaw2 = this.getSafeWaypointYaw(commandSender);
            final ArrayList<Double> safeWaypointPitch2 = this.getSafeWaypointPitch(commandSender);
            double n2 = -1.0;
            if (array.length > 1) {
                try {
                    n2 = Double.parseDouble(array[1]);
                    n2 = Math.abs(n2);
                }
                catch (Exception ex16) {}
            }
            double double1 = 444.0;
            if (array.length > 2) {
                if (array[2].equalsIgnoreCase("c")) {
                    double1 = ((Player)commandSender).getLocation().getYaw();
                }
                else if (array[2].equalsIgnoreCase("n")) {
                    double1 = 444.0;
                }
                else {
                    try {
                        double1 = Double.parseDouble(array[2]);
                    }
                    catch (Exception ex17) {}
                }
            }
            else {
                double1 = ((Player)commandSender).getLocation().getYaw();
            }
            double double2 = 444.0;
            if (array.length > 3) {
                if (array[3].equalsIgnoreCase("c")) {
                    double2 = ((Player)commandSender).getLocation().getPitch();
                }
                else if (array[3].equalsIgnoreCase("n")) {
                    double2 = 444.0;
                }
                else {
                    try {
                        double2 = Double.parseDouble(array[3]);
                    }
                    catch (Exception ex18) {}
                }
            }
            else {
                double2 = ((Player)commandSender).getLocation().getPitch();
            }
            double1 = formatAngleYaw(double1);
            double2 = formatAnglePitch(double2);
            safeWaypointSpeeds.add(n2);
            safeWaypointYaw2.add(double1);
            safeWaypointPitch2.add(double2);
            UUID u = ((Player)commandSender).getUniqueId();
            this.waypoints_s.put(u, safeWaypointSpeeds);
            this.waypoints_y.put(u, safeWaypointYaw2);
            this.waypoints_p.put(u, safeWaypointPitch2);
            if (this.waypoints_m.get(u) == null) {
                this.waypoints_m.put(u, new ArrayList<String>());
            }
            this.waypoints_m.get(u).add("");
            if (this.waypoints_c.get(u) == null) {
                this.waypoints_c.put(u, new ArrayList<List<String>>());
            }
            this.waypoints_c.get(u).add(new ArrayList<String>());
            this.getSafeWaypointDelays(commandSender);
            this.getSafeWaypointOptions(commandSender);
            this.getSafeWaypointInstants(commandSender);
            this.waypoints_l.get(u).add(0);
            this.waypoints_d.get(u).add(0.0);
            this.waypoints_i.get(u).add(false);
            if (n2 >= 0.0) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Added waypoint #" + (safeWaypoints.size() - 1) + ", setting the speed to " + n2);
            }
            else {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Added waypoint #" + (safeWaypoints.size() - 1));
            }
        }
        else {
            return onCommand_2(commandSender, command, s, array);
        }
        return true;
    }
    
    private boolean onCommand_2(final CommandSender commandSender, final Command command, final String s, final String[] array)
    {
        final String s2 = array[0];
        if (s2.equalsIgnoreCase("tpmode")) {
            if (!commandSender.hasPermission("servercinematics.play")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            } 
            final UUID uniqueId = ((Player)commandSender).getUniqueId();
            if (this.pathless.containsKey(uniqueId) && this.pathless.get(uniqueId)) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Please disable pathless mode first.");
                return true;
            }
            final Boolean b = this.teleport.get(uniqueId);
            this.clearCache(uniqueId);
            if (this.teleport.containsKey(uniqueId) && b) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Teleport mode switched off for next path (yaw/pitch won't work, more smooth)");
                this.teleport.put(uniqueId, false);
            }
            else {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Teleport mode switched on for next path (yaw/pitch will work, less smooth)");
                this.teleport.put(uniqueId, true);
            }
            return true;
        } else if (s2.equalsIgnoreCase("pathless")) {
                if (!commandSender.hasPermission("servercinematics.play")) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                    return true;
                } 
                if (globalMode != null) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                    return true;
                }
                final UUID uniqueId = ((Player)commandSender).getUniqueId();
                final Boolean b = this.pathless.get(uniqueId);
                this.clearCache(((Player)commandSender));
                if (this.pathless.containsKey(uniqueId) && b) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Pathless switched off (tpmode configurable)");
                    this.pathless.put(uniqueId, false);
                }
                else {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Pathless switched on (automatic tpmode)");
                    this.pathless.put(uniqueId, true);
                    this.teleport.put(uniqueId, true);
                }
                return true;
        } else if (s2.equalsIgnoreCase("cmd")) {
            if (!commandSender.hasPermission("servercinematics.edit") || !commandSender.hasPermission("servercinematics.cmd")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 3) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " cmd id { list | add commandwithout/ | get index | remove index }");
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "%player% will be replaced with the player's name");
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "command starting with a ~ will be run exactly as if the player");
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "    him- or herself had typed it, without the starting ~ of course.");
                return true;
            }
            int int8;
            try {
                int8 = Integer.parseInt(array[1]);
            }
            catch (Exception ex6) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Improper number");
                return true;
            }
            final ArrayList<List<String>> safeWaypointCommands = this.getSafeWaypointCommands(commandSender);
            if (int8 < 0 || int8 >= safeWaypointCommands.size()) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Waypoint not found.");
                return true;
            }
            if (array.length <= 2) {
                return true;
            }
            if (array[2].equalsIgnoreCase("list")) {
                int n3 = 0;
                final Iterator<String> iterator2 = safeWaypointCommands.get(int8).iterator();
                while (iterator2.hasNext()) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "" + n3++ + " " + make_color('f') + "-" + make_color('a') + " /" + iterator2.next());
                }
            }
            else if (array[2].equalsIgnoreCase("add") && array.length > 3) {
                final StringBuilder sb = new StringBuilder();
                for (int i = 3; i < array.length; ++i) {
                    sb.append(" ");
                    sb.append(array[i]);
                }
                final String substring = sb.toString().substring(1);
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Added command #" + safeWaypointCommands.get(int8).size());
                this.getSafeWaypointCommands(commandSender).get(int8).add(substring);
                this.clearCache((Player)commandSender);
                this.clearPathName((Player)commandSender);
            }
            else if (array[2].equalsIgnoreCase("get") && array.length > 3) {
                int int9;
                try {
                    int9 = Integer.parseInt(array[3]);
                }
                catch (Exception ex7) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Improper number");
                    return true;
                }
                if (int9 < 0 || int9 >= safeWaypointCommands.get(int8).size()) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Command not found");
                    return true;
                }
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "/" + safeWaypointCommands.get(int8).get(int9));
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('b') + "The actual command is stored without the preceding slash but still works.");
            }
            else if (array[2].equalsIgnoreCase("remove") && array.length > 3) {
                int int10;
                try {
                    int10 = Integer.parseInt(array[3]);
                }
                catch (Exception ex8) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Improper number");
                    return true;
                }
                if (int10 < 0 || int10 >= safeWaypointCommands.get(int8).size()) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Command not found");
                    return true;
                }
                this.getSafeWaypointCommands(commandSender).get(int8).remove(int10);
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "The command has been removed.");
                this.clearCache((Player)commandSender);
                this.clearPathName((Player)commandSender);
            }
        }
        else if (s2.equalsIgnoreCase("msg")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " msg id { set msg | setcolored msg | remove }");
                return true;
            }
            int int11;
            try {
                int11 = Integer.parseInt(array[1]);
            } catch (NumberFormatException ex) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " msg id { set msg | setcolored msg | remove }");
                return true;
            }
            final ArrayList<String> safeWaypointMessages = this.getSafeWaypointMessages(commandSender);
            if (int11 < 0 || int11 >= safeWaypointMessages.size()) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Waypoint not found.");
                return true;
            }
            if (array.length <= 2) {
                if (safeWaypointMessages.get(int11).length() < 1) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "No message has been set for this waypoint.");
                }
                else {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Message: §r" + safeWaypointMessages.get(int11));
                }
                return true;
            }
            if (array[2].equalsIgnoreCase("remove")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "The message of this waypoint has been removed.");
                this.getSafeWaypointMessages(commandSender).set(int11, "");
                this.clearCache((Player)commandSender);
                this.clearPathName((Player)commandSender);
            }
            else if (array[2].equalsIgnoreCase("set")) {
                if (array.length > 3) {
                    final StringBuilder sb2 = new StringBuilder();
                    for (int j = 3; j < array.length; ++j) {
                        sb2.append(" ");
                        sb2.append(array[j]);
                    }
                    final String substring2 = sb2.toString().substring(1);
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "The message of this waypoint has been set.");
                    this.getSafeWaypointMessages(commandSender).set(int11, substring2);
                    this.clearCache((Player)commandSender);
                    this.clearPathName((Player)commandSender);
                }
                else {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " msg id { set msg | setcolored msg | remove }");
                }
            }
            else if (array[2].equalsIgnoreCase("setcolored")) {
                if (array.length > 3) {
                    final StringBuilder sb3 = new StringBuilder();
                    for (int k = 3; k < array.length; ++k) {
                        sb3.append(" ");
                        sb3.append(array[k]);
                    }
                    final String translateAlternateColorCodes = ChatColor.translateAlternateColorCodes('&', sb3.toString().substring(1));
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "The message of this waypoint has been set.");
                    this.getSafeWaypointMessages(commandSender).set(int11, translateAlternateColorCodes);
                    this.clearCache((Player)commandSender);
                    this.clearPathName((Player)commandSender);
                }
                else {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " msg id { set msg | setcolored msg | remove }");
                }
            }
        }
        else if (s2.equalsIgnoreCase("playlist")) {
            if (!commandSender.hasPermission("servercinematics.play")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " playlist { add path | insert pos path | remove path | list | clear | play | loop }");
                return true;
            }
            String subcmd = array[1];
            Player up = ((Player)commandSender);
            UUID u = up.getUniqueId();
            if (subcmd.equalsIgnoreCase("add")) {
                if (array.length < 2) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " playlist add path");
                    return true;
                }
                if (!pl_paths.containsKey(u)) {
                    pl_paths.put(u, new ArrayList<String>());
                }
                pl_paths.get(u).add(array[2]);
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Added path.");
                return true;
            } else if (subcmd.equalsIgnoreCase("remove")) {
                if (array.length < 2) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " playlist remove index");
                    return true;
                }
                if (!pl_paths.containsKey(u)) {
                    pl_paths.put(u, new ArrayList<String>());
                }
                int k = 0;
                try {
                    k = Integer.parseInt(array[2]);
                    if (k < 0) throw new IllegalArgumentException();
                    if (k >= pl_paths.get(u).size()) throw new ArrayIndexOutOfBoundsException();
                } catch (ArrayIndexOutOfBoundsException ex) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Path not found in that index.");
                    return true;
                } catch (Exception ex) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "The parameter must be a non-negative integer.");
                    return true;
                }
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Removed path.");
                return true;
            } else if (subcmd.equalsIgnoreCase("insert")) {
                if (array.length < 2) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " playlist insert index path");
                    return true;
                }
                if (!pl_paths.containsKey(u)) {
                    pl_paths.put(u, new ArrayList<String>());
                }
                int k = 0;
                try {
                    k = Integer.parseInt(array[2]);
                    if (k < 0) throw new IllegalArgumentException();
                    if (k > pl_paths.get(u).size()) k = pl_paths.get(u).size();
                } catch (IllegalArgumentException ex) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "The parameter must be a non-negative integer.");
                    return true;
                }
                pl_paths.get(u).add(k, array[3]);
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Inserted path.");
                return true;
            } else if (subcmd.equalsIgnoreCase("list")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "List of paths in your playlist:");
                int n = 0;
                if (!pl_paths.containsKey(u)) {
                    pl_paths.put(u, new ArrayList<String>());
                }
                for (final String path: pl_paths.get(u)) {
                    commandSender.sendMessage("" + make_color('e') + "" + n + "" + make_color('7') + ": " + make_color('f') + "" + path);
                    ++n;
                }
                if (n == 0) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "None at all!");
                }
                return true;
            } else if (subcmd.equalsIgnoreCase("play")) {
                if (!pl_paths.containsKey(u)) {
                    pl_paths.put(u, new ArrayList<String>());
                }
                if (pl_paths.get(u).size() < 1) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Your playlist is empty.");
                    return true;
                }
                pl_index.put(u, -1);
                if (!this.findNextSuitablePath(up)) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "The playlist has no playable paths!");
                    return true;
                }
                pl_playing.put(u, true);
                pl_looping.put(u, false);
                up.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Playing...");
                this.play(up, commandSender, false, StartCause.PLAYLIST);
            
            } else if (subcmd.equalsIgnoreCase("clear")) {
                pl_paths.put(u, new ArrayList<String>());
                up.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Cleared.");
                return true;
            } else if (subcmd.equalsIgnoreCase("loop")) {
                if (!pl_paths.containsKey(u)) {
                    pl_paths.put(u, new ArrayList<String>());
                }
                if (pl_paths.get(u).size() < 1) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Your playlist is empty.");
                    return true;
                }
                pl_index.put(u, -1);
                if (!this.findNextSuitablePath(up)) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "The playlist has no playable paths!");
                    return true;
                }
                pl_playing.put(u, true);
                pl_looping.put(u, true);
                up.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Playing...");
                this.play(up, commandSender, false, StartCause.PLAYLIST);
                return true;
            }
        }
        else if (s2.equalsIgnoreCase("insert")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " insert id (speed / n(o effect)) (yaw / c(urrent) / n(o effect)) (pitch / c(urrent) / n(o effect))");
                return true;
            }
            int int12 = Integer.parseInt(array[1]);
            final Location location3 = ((Player)commandSender).getLocation();
            final ArrayList<Location> safeWaypoints2 = this.getSafeWaypoints(commandSender);
            if (int12 < 0 || int12 >= safeWaypoints2.size()) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Position must be a non-negative integer.");
                return true;
            }
            if (int12 > safeWaypoints2.size()) {
                int12 = safeWaypoints2.size();
            }
            if (safeWaypoints2.size() > 0 && !safeWaypoints2.get(0).getWorld().getName().equalsIgnoreCase(location3.getWorld().getName())) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Cannot add waypoints to another world!");
                return true;
            }
            this.clearCache((Player)commandSender);
            this.clearPathName((Player)commandSender);
            safeWaypoints2.add(int12, location3);
            this.waypoints.put(((Player)commandSender).getUniqueId(), safeWaypoints2);
            final ArrayList<Double> safeWaypointSpeeds2 = this.getSafeWaypointSpeeds(commandSender);
            final ArrayList<Double> safeWaypointYaw3 = this.getSafeWaypointYaw(commandSender);
            final ArrayList<Double> safeWaypointPitch3 = this.getSafeWaypointPitch(commandSender);
            double n4 = -1.0;
            if (array.length > 2) {
                try {
                    n4 = Double.parseDouble(array[2]);
                    n4 = Math.abs(n4);
                }
                catch (Exception ex19) {}
            }
            double double3 = -1.0;
            if (array.length > 3) {
                if (array[3].equalsIgnoreCase("c")) {
                    double3 = ((Player)commandSender).getLocation().getYaw();
                }
                else if (array[3].equalsIgnoreCase("n")) {
                    double3 = 444.0;
                }
                else {
                    try {
                        double3 = Double.parseDouble(array[3]);
                    }
                    catch (Exception ex20) {}
                }
            }
            double double4 = -1.0;
            if (array.length > 4) {
                if (array[4].equalsIgnoreCase("c")) {
                    double4 = ((Player)commandSender).getLocation().getPitch();
                }
                else if (array[4].equalsIgnoreCase("n")) {
                    double4 = 444.0;
                }
                else {
                    try {
                        double4 = Double.parseDouble(array[4]);
                    }
                    catch (Exception ex21) {}
                }
            }
            double3 = formatAngleYaw(double3);
            double4 = formatAnglePitch(double4);
            safeWaypointSpeeds2.add(int12, n4);
            safeWaypointYaw3.add(int12, double3);
            safeWaypointPitch3.add(int12, double4);
            UUID u = ((Player)commandSender).getUniqueId();
            this.waypoints_s.put(u, safeWaypointSpeeds2);
            this.waypoints_y.put(u, safeWaypointYaw3);
            this.waypoints_p.put(u, safeWaypointPitch3);
            if (this.waypoints_m.get(u) == null) {
                this.waypoints_m.put(u, new ArrayList<String>());
            }
            this.waypoints_m.get(u).add(int12, "");
            if (this.waypoints_c.get(u) == null) {
                this.waypoints_c.put(u, new ArrayList<List<String>>());
            }
            this.waypoints_c.get(u).add(int12, new ArrayList<String>());
            this.getSafeWaypointDelays(commandSender);
            this.getSafeWaypointOptions(commandSender);
            this.getSafeWaypointInstants(commandSender);
            this.waypoints_l.get(u).add(int12, 0);
            this.waypoints_d.get(u).add(int12, 0.0);
            this.waypoints_i.get(u).add(int12, false);
            if (n4 >= 0.0) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Inserted waypoint at #" + int12 + ", setting the speed to " + n4);
            }
            else {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Inserted waypoint at #" + int12);
            }
        }
        else if (s2.equalsIgnoreCase("edit")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 5) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " edit <id> <speed / n(o effect)> <yaw / c(urrent) / n(o effect)> <pitch / c(urrent) / n(o effect)>");
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " (D(o not change) as any parameter will not change it)");
                return true;
            }
            final int int13 = Integer.parseInt(array[1]);
            final ArrayList<Location> safeWaypoints3 = this.getSafeWaypoints(commandSender);
            if (int13 < 0 && int13 >= safeWaypoints3.size()) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Waypoint not found.");
                return true;
            }
            this.clearCache((Player)commandSender);
            this.clearPathName((Player)commandSender);
            final ArrayList<Double> safeWaypointSpeeds3 = this.getSafeWaypointSpeeds(commandSender);
            final ArrayList<Double> safeWaypointYaw4 = this.getSafeWaypointYaw(commandSender);
            final ArrayList<Double> safeWaypointPitch4 = this.getSafeWaypointPitch(commandSender);
            double n5 = -1.0;
            if (!array[2].equalsIgnoreCase("D")) {
                try {
                    n5 = Double.parseDouble(array[2]);
                    n5 = Math.abs(n5);
                }
                catch (Exception ex22) {}
            }
            double double5 = -1.0;
            if (!array[3].equalsIgnoreCase("D")) {
                if (array[3].equalsIgnoreCase("c")) {
                    double5 = ((Player)commandSender).getLocation().getYaw();
                }
                else if (array[3].equalsIgnoreCase("n")) {
                    double5 = 444.0;
                }
                else {
                    try {
                        double5 = Double.parseDouble(array[3]);
                    }
                    catch (Exception ex23) {}
                }
            }
            double double6 = -1.0;
            if (!array[4].equalsIgnoreCase("D")) {
                if (array[4].equalsIgnoreCase("c")) {
                    double6 = ((Player)commandSender).getLocation().getPitch();
                }
                else if (array[4].equalsIgnoreCase("n")) {
                    double6 = 444.0;
                }
                else {
                    try {
                        double6 = Double.parseDouble(array[4]);
                    }
                    catch (Exception ex24) {}
                }
            }
            if (!array[2].equalsIgnoreCase("D")) {
                safeWaypointSpeeds3.set(int13, n5);
            }
            if (!array[3].equalsIgnoreCase("D")) {
                safeWaypointYaw4.set(int13, double5);
            }
            if (!array[4].equalsIgnoreCase("D")) {
                safeWaypointPitch4.set(int13, double6);
            }
            this.waypoints_s.put(((Player)commandSender).getUniqueId(), safeWaypointSpeeds3);
            this.waypoints_y.put(((Player)commandSender).getUniqueId(), safeWaypointYaw4);
            this.waypoints_p.put(((Player)commandSender).getUniqueId(), safeWaypointPitch4);
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Edited the properties of waypoint #" + int13);
        }
        else if (s2.equalsIgnoreCase("speed")) {
            if (!commandSender.hasPermission("servercinematics.play")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                double doubleValue3 = 5.0;
                try {
                    doubleValue3 = this.speed.get(((Player)commandSender).getUniqueId());
                }
                catch (Exception ex25) {}
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('f') + "Starting speed is currently " + make_color('a') + "" + doubleValue3);
                return true;
            }
            double double7;
            try {
                double7 = Double.parseDouble(array[1]);
            }
            catch (Exception ex9) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "That is not a number!");
                return true;
            }
            final double abs = Math.abs(double7);
            this.speed.put(((Player)commandSender).getUniqueId(), abs);
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('f') + "Starting speed set to " + make_color('a') + "" + abs);
        }
        else if (s2.equalsIgnoreCase("fclear")) {
            if (!commandSender.hasPermission("servercinematics.fplay")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " fclear player");
                return true;
            }
            final Player player = this.getServer().getPlayer(array[1]);
            if (player == null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Cannot find player!");
                return true;
            }
            this.clearCache(player);
            this.clearPathName((Player)commandSender);
            this.waypoints.put(player.getUniqueId(), new ArrayList<Location>());
            this.waypoints_s.put(player.getUniqueId(), new ArrayList<Double>());
            this.waypoints_y.put(player.getUniqueId(), new ArrayList<Double>());
            this.waypoints_p.put(player.getUniqueId(), new ArrayList<Double>());
            this.waypoints_m.put(player.getUniqueId(), new ArrayList<String>());
            this.waypoints_c.put(player.getUniqueId(), new ArrayList<List<String>>());
            this.getSafeWaypointFlags(player);
            this.waypoints_f.put(player.getUniqueId(), 0);
            this.waypoints_t.put(player.getUniqueId(), -1.0);
            this.waypoints_d.put(player.getUniqueId(), new ArrayList<Double>());
            this.waypoints_l.put(player.getUniqueId(), new ArrayList<Integer>());
            this.waypoints_i.put(player.getUniqueId(), new ArrayList<Boolean>());
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Path cleared.");
        }
        else if (s2.equalsIgnoreCase("fstop")) {
            if (!commandSender.hasPermission("servercinematics.fplay")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " fstop player");
                return true;
            }
            if (globalMode != null && array[1].equalsIgnoreCase("**")) {
                stopGlobal();
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "OK");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this. If you want to stop global playback: /" + s + " fstop **");
                return true;
            }
            final Player player = this.getServer().getPlayer(array[1]);
            if (player == null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Cannot find player!");
                return true;
            }
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Stopped.");
            this.stop(player, PathPlaybackStoppedEvent.StopCause.FSTOP);
        }
        else if (s2.equalsIgnoreCase("fload") || s2.equalsIgnoreCase("fplay")) {
            if (!commandSender.hasPermission("servercinematics.fplay")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 3) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " " + s2.toLowerCase() + " player path (tp|notp|pathless)");
                return true;
            }
            if (s2.equalsIgnoreCase("fplay") && array[1].equalsIgnoreCase("**")) {
                if (getServer().getOnlinePlayers().size() < 1) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "No players are online!");
                    return true;
                }
                Player up = commandSender instanceof Player ? ((Player)commandSender) : getServer().getOnlinePlayers().iterator().next();
                UUID u = up.getUniqueId();
                if (!pl_paths.containsKey(u)) {
                    pl_paths.put(u, new ArrayList<String>());
                }
                if (pl_paths.get(u).size() < 1) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Your playlist is empty.");
                    return true;
                }
                pl_index.put(u, -1);
                if (!this.findNextSuitablePath(up)) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "The playlist has no playable paths!");
                    return true;
                }
                globalMode = u;
                pl_playing.put(u, true);
                pl_looping.put(u, true);
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Playing...");
                this.play(up, commandSender, false, StartCause.FPLAY);
                return true;
            }
            final Player player2 = this.getServer().getPlayer(array[1]);
            if (player2 == null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Cannot find player!");
                return true;
            }
            if (this.isTrue(this.playing.get(player2.getUniqueId()))) {
                this.stop(player2, PathPlaybackStoppedEvent.StopCause.FSTOP);
            }
            if (array.length > 3) {
                if (array[3].equalsIgnoreCase("tp")) {
                    this.teleport.put(player2.getUniqueId(), true);
                    this.pathless.put(player2.getUniqueId(), false);
                }
                else if (array[3].equalsIgnoreCase("notp")) {
                    this.teleport.put(player2.getUniqueId(), false);
                    this.pathless.put(player2.getUniqueId(), false);
                }
                else if (array[3].equalsIgnoreCase("pathless")) {
                    this.teleport.put(player2.getUniqueId(), true);
                    this.pathless.put(player2.getUniqueId(), true);
                }
                else {
                    this.teleport.put(player2.getUniqueId(), false);
                    this.pathless.put(player2.getUniqueId(), false);
                }
            }
            try {
                final File file = new File(this.paths, array[2]);
                if (!file.isFile()) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Path not found");
                    return true;
                }
                final String s3 = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                this.clearCache(player2);
                UUID u2 = player2.getUniqueId();
                this.waypoints.put(u2, new ArrayList<Location>());
                this.waypoints_s.put(u2, new ArrayList<Double>());
                this.waypoints_y.put(u2, new ArrayList<Double>());
                this.waypoints_p.put(u2, new ArrayList<Double>());
                this.waypoints_m.put(u2, new ArrayList<String>());
                this.waypoints_c.put(u2, new ArrayList<List<String>>());
                this.waypoints_f.put(u2, 0);
                this.waypoints_t.put(u2, -1.0);
                this.waypoints_d.put(u2, new ArrayList<Double>());
                this.waypoints_l.put(u2, new ArrayList<Integer>());
                this.waypoints_i.put(u2, new ArrayList<Boolean>());
                final ArrayList<Location> safeWaypoints4 = this.getSafeWaypoints(player2);
                final ArrayList<Double> safeWaypointSpeeds4 = this.getSafeWaypointSpeeds(player2);
                final ArrayList<Double> safeWaypointYaw5 = this.getSafeWaypointYaw(player2);
                final ArrayList<Double> safeWaypointPitch5 = this.getSafeWaypointPitch(player2);
                final ArrayList<String> safeWaypointMessages2 = this.getSafeWaypointMessages(player2);
                final ArrayList<List<String>> safeWaypointCommands2 = this.getSafeWaypointCommands(player2);
                final World world = player2.getWorld();
                final String s4 = s3.split("#")[0];
                final String s5 = s3.split("#")[1];
                final float float1 = Float.parseFloat(s4.split(",")[2]);
                final float float2 = Float.parseFloat(s4.split(",")[3]);
                this.pathnames.put(u2, array[2]);
                int safeFlags = 0;
                if (s4.split(",").length > 4) {
                    safeFlags = Integer.parseInt(s4.split(",")[4]);
                }
                double safeTime = -1;
                if (s4.split(",").length > 5) {
                    safeTime = Double.parseDouble(s4.split(",")[5]);
                }
                int n6 = 0;
                String[] split;
                for (int length = (split = s5.split(Pattern.quote("|"))).length, l = 0; l < length; ++l) {
                    final String s6 = split[l];
                    try {
                        final String[] split2 = s6.split(",");
                        final Location location4 = new Location(world, Double.parseDouble(split2[0]), Double.parseDouble(split2[1]), Double.parseDouble(split2[2].split(";")[0]));
                        if (n6 == 0) {
                            location4.setYaw(float1);
                            location4.setPitch(float2);
                        }
                        safeWaypoints4.add(location4);
                        safeWaypointCommands2.add(new ArrayList<String>());
                        if (split2[3].indexOf(10) >= 0) {
                            safeWaypointSpeeds4.add(Double.parseDouble(split2[3].split("\n")[0]));
                            int n7 = 0;
                            String[] split3;
                            for (int length2 = (split3 = split2[3].split("\n")).length, n8 = 0; n8 < length2; ++n8) {
                                final String s7 = split3[n8];
                                if (n7++ >= 1) {
                                    safeWaypointCommands2.get(n6).add(s7.replace("\uf555", ","));
                                }
                            }
                        }
                        else {
                            safeWaypointSpeeds4.add(Double.parseDouble(split2[3]));
                        }
                        try {
                            if (split2[2].split(";").length > 3) {
                                this.waypoints_i.get(u2).add(!split2[2].split(";")[3].equalsIgnoreCase("0"));
                            } else {
                                this.waypoints_i.get(u2).add(false);
                            }
                            if (split2[2].split(";").length < 2) {
                                throw new ArrayIndexOutOfBoundsException();
                            }
                            double d = Double.parseDouble(split2[2].split(";")[1]);
                            int lf = Integer.parseInt(split2[2].split(";")[2]);
                            this.waypoints_d.get(u2).add(d);
                            this.waypoints_l.get(u2).add(lf);
                        } catch (Exception ex) {
                            this.waypoints_d.get(u2).add(0.0);
                            this.waypoints_l.get(u2).add(0);
                        }
                        if (split2.length > 4) {
                            final String[] split4 = split2[4].split(":");
                            final String[] split5 = split4[1].split("\\$", 2);
                            if (split5.length > 1) {
                                safeWaypointMessages2.add(split5[1].replace("\uf555", ","));
                            }
                            else {
                                safeWaypointMessages2.add("");
                            }
                            safeWaypointYaw5.add(this.formatAngleYaw(Double.parseDouble(split4[0])));
                            safeWaypointPitch5.add(this.formatAnglePitch(Double.parseDouble(split5[0])));
                        }
                        else {
                            safeWaypointYaw5.add(444.0);
                            safeWaypointPitch5.add(444.0);
                        }
                        ++n6;
                    }
                    catch (Exception ex10) {
                        if (safeWaypointYaw5.size() > safeWaypointPitch5.size()) {
                            safeWaypointYaw5.remove(safeWaypointYaw5.size() - 1);
                        }
                        if (safeWaypointMessages2.size() > safeWaypointYaw5.size()) {
                            safeWaypointMessages2.remove(safeWaypointMessages2.size() - 1);
                        }
                        if (safeWaypointSpeeds4.size() > safeWaypointYaw5.size()) {
                            safeWaypointSpeeds4.remove(safeWaypointSpeeds4.size() - 1);
                        }
                        if (safeWaypoints4.size() > safeWaypointSpeeds4.size()) {
                            safeWaypoints4.remove(safeWaypoints4.size() - 1);
                        }
                        if (safeWaypointCommands2.size() > safeWaypoints4.size()) {
                            safeWaypointCommands2.remove(safeWaypointCommands2.size() - 1);
                        }
                    }
                }
                this.waypoints_y.put(player2.getUniqueId(), safeWaypointYaw5);
                this.waypoints_p.put(player2.getUniqueId(), safeWaypointPitch5);
                this.waypoints_m.put(player2.getUniqueId(), safeWaypointMessages2);
                this.waypoints_c.put(player2.getUniqueId(), safeWaypointCommands2);
                this.waypoints_f.put(player2.getUniqueId(), safeFlags);
                this.waypoints_t.put(player2.getUniqueId(), safeTime);
                this.speed.put(player2.getUniqueId(), Double.parseDouble(s4.split(",")[1]));
                if (!world.getName().equalsIgnoreCase(s4.split(",")[0])) {
                    final World world2 = this.getServer().getWorld(s4.split(",")[0]);
                    if (world2 != null) {
                        final Iterator<Location> iterator3 = safeWaypoints4.iterator();
                        while (iterator3.hasNext()) {
                            iterator3.next().setWorld(world2);
                        }
                        player2.teleport(world2.getSpawnLocation());
                    }
                    else {
                        commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "Warning: World name does not match with saved name! Proceed with caution.");
                        commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "World name of saved path was '" + s4.split(",")[0] + "'.");
                    }
                }
                this.waypoints.put(player2.getUniqueId(), safeWaypoints4);
                this.waypoints_s.put(player2.getUniqueId(), safeWaypointSpeeds4);
            }
            catch (Exception ex11) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Malformed file, loading failed / was not finished");
                return true;
            }
            //commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Playing!");
            pl_playing.put(player2.getUniqueId(), false);
            pl_looping.put(player2.getUniqueId(), false);
            if (s2.equalsIgnoreCase("fplay"))
                this.play(player2, commandSender, true, StartCause.FPLAY);
        }
        else if (s2.equalsIgnoreCase("ftplay")) {
            if (!commandSender.hasPermission("servercinematics.fplay")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 4) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " ftplay player path hours:minutes:seconds (tp|notp|pathless)");
                return true;
            }
            final Player player2 = this.getServer().getPlayer(array[1]);
            if (player2 == null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Cannot find player!");
                return true;
            }
            if (this.isTrue(this.playing.get(player2.getUniqueId()))) {
                this.stop(player2, PathPlaybackStoppedEvent.StopCause.FSTOP);
            }
            if (array.length > 4) {
                if (array[4].equalsIgnoreCase("tp")) {
                    this.teleport.put(player2.getUniqueId(), true);
                    this.pathless.put(player2.getUniqueId(), false);
                }
                else if (array[4].equalsIgnoreCase("notp")) {
                    this.teleport.put(player2.getUniqueId(), false);
                    this.pathless.put(player2.getUniqueId(), false);
                }
                else if (array[4].equalsIgnoreCase("pathless")) {
                    this.teleport.put(player2.getUniqueId(), true);
                    this.pathless.put(player2.getUniqueId(), true);
                }
                else {
                    this.teleport.put(player2.getUniqueId(), false);
                    this.pathless.put(player2.getUniqueId(), false);
                }
            }

            try {
                final File file = new File(this.paths, array[2]);
                if (!file.isFile()) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Path not found");
                    return true;
                }
                final String s3 = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                this.clearCache(player2);
                UUID u2 = player2.getUniqueId();
                this.waypoints.put(u2, new ArrayList<Location>());
                this.waypoints_s.put(u2, new ArrayList<Double>());
                this.waypoints_y.put(u2, new ArrayList<Double>());
                this.waypoints_p.put(u2, new ArrayList<Double>());
                this.waypoints_m.put(u2, new ArrayList<String>());
                this.waypoints_c.put(u2, new ArrayList<List<String>>());
                this.waypoints_f.put(u2, 0);
                this.waypoints_t.put(u2, -1.0);
                this.waypoints_d.put(u2, new ArrayList<Double>());
                this.waypoints_l.put(u2, new ArrayList<Integer>());
                this.waypoints_i.put(u2, new ArrayList<Boolean>());
                final ArrayList<Location> safeWaypoints4 = this.getSafeWaypoints(player2);
                final ArrayList<Double> safeWaypointSpeeds4 = this.getSafeWaypointSpeeds(player2);
                final ArrayList<Double> safeWaypointYaw5 = this.getSafeWaypointYaw(player2);
                final ArrayList<Double> safeWaypointPitch5 = this.getSafeWaypointPitch(player2);
                final ArrayList<String> safeWaypointMessages2 = this.getSafeWaypointMessages(player2);
                final ArrayList<List<String>> safeWaypointCommands2 = this.getSafeWaypointCommands(player2);
                final World world = player2.getWorld();
                final String s4 = s3.split("#")[0];
                final String s5 = s3.split("#")[1];
                final float float1 = Float.parseFloat(s4.split(",")[2]);
                final float float2 = Float.parseFloat(s4.split(",")[3]);
                this.pathnames.put(u2, array[2]);
                int safeFlags = 0;
                if (s4.split(",").length > 4) {
                    safeFlags = Integer.parseInt(s4.split(",")[4]);
                }
                double safeTime = -1;
                if (s4.split(",").length > 5) {
                    safeTime = Double.parseDouble(s4.split(",")[5]);
                }
                int n6 = 0;
                String[] split;
                for (int length = (split = s5.split(Pattern.quote("|"))).length, l = 0; l < length; ++l) {
                    final String s6 = split[l];
                    try {
                        final String[] split2 = s6.split(",");
                        final Location location4 = new Location(world, Double.parseDouble(split2[0]), Double.parseDouble(split2[1]), Double.parseDouble(split2[2].split(";")[0]));
                        if (n6 == 0) {
                            location4.setYaw(float1);
                            location4.setPitch(float2);
                        }
                        safeWaypoints4.add(location4);
                        safeWaypointCommands2.add(new ArrayList<String>());
                        if (split2[3].indexOf(10) >= 0) {
                            safeWaypointSpeeds4.add(Double.parseDouble(split2[3].split("\n")[0]));
                            int n7 = 0;
                            String[] split3;
                            for (int length2 = (split3 = split2[3].split("\n")).length, n8 = 0; n8 < length2; ++n8) {
                                final String s7 = split3[n8];
                                if (n7++ >= 1) {
                                    safeWaypointCommands2.get(n6).add(s7.replace("\uf555", ","));
                                }
                            }
                        }
                        else {
                            safeWaypointSpeeds4.add(Double.parseDouble(split2[3]));
                        }
                        try {
                            if (split2[2].split(";").length > 3) {
                                this.waypoints_i.get(u2).add(!split2[2].split(";")[3].equalsIgnoreCase("0"));
                            } else {
                                this.waypoints_i.get(u2).add(false);
                            }
                            if (split2[2].split(";").length < 2) {
                                throw new ArrayIndexOutOfBoundsException();
                            }
                            double d = Double.parseDouble(split2[2].split(";")[1]);
                            int lf = Integer.parseInt(split2[2].split(";")[2]);
                            this.waypoints_d.get(u2).add(d);
                            this.waypoints_l.get(u2).add(lf);
                        } catch (Exception ex) {
                            this.waypoints_d.get(u2).add(0.0);
                            this.waypoints_l.get(u2).add(0);
                        }
                        if (split2.length > 4) {
                            final String[] split4 = split2[4].split(":");
                            final String[] split5 = split4[1].split("\\$", 2);
                            if (split5.length > 1) {
                                safeWaypointMessages2.add(split5[1].replace("\uf555", ","));
                            }
                            else {
                                safeWaypointMessages2.add("");
                            }
                            safeWaypointYaw5.add(this.formatAngleYaw(Double.parseDouble(split4[0])));
                            safeWaypointPitch5.add(this.formatAnglePitch(Double.parseDouble(split5[0])));
                        }
                        else {
                            safeWaypointYaw5.add(444.0);
                            safeWaypointPitch5.add(444.0);
                        }
                        ++n6;
                    }
                    catch (Exception ex10) {
                        if (safeWaypointYaw5.size() > safeWaypointPitch5.size()) {
                            safeWaypointYaw5.remove(safeWaypointYaw5.size() - 1);
                        }
                        if (safeWaypointMessages2.size() > safeWaypointYaw5.size()) {
                            safeWaypointMessages2.remove(safeWaypointMessages2.size() - 1);
                        }
                        if (safeWaypointSpeeds4.size() > safeWaypointYaw5.size()) {
                            safeWaypointSpeeds4.remove(safeWaypointSpeeds4.size() - 1);
                        }
                        if (safeWaypoints4.size() > safeWaypointSpeeds4.size()) {
                            safeWaypoints4.remove(safeWaypoints4.size() - 1);
                        }
                        if (safeWaypointCommands2.size() > safeWaypoints4.size()) {
                            safeWaypointCommands2.remove(safeWaypointCommands2.size() - 1);
                        }
                    }
                }
                this.waypoints_y.put(player2.getUniqueId(), safeWaypointYaw5);
                this.waypoints_p.put(player2.getUniqueId(), safeWaypointPitch5);
                this.waypoints_m.put(player2.getUniqueId(), safeWaypointMessages2);
                this.waypoints_c.put(player2.getUniqueId(), safeWaypointCommands2);
                this.waypoints_f.put(player2.getUniqueId(), safeFlags);
                this.waypoints_t.put(player2.getUniqueId(), safeTime);
                this.speed.put(player2.getUniqueId(), Double.parseDouble(s4.split(",")[1]));
                if (!world.getName().equalsIgnoreCase(s4.split(",")[0])) {
                    final World world2 = this.getServer().getWorld(s4.split(",")[0]);
                    if (world2 != null) {
                        final Iterator<Location> iterator3 = safeWaypoints4.iterator();
                        while (iterator3.hasNext()) {
                            iterator3.next().setWorld(world2);
                        }
                        player2.teleport(world2.getSpawnLocation());
                    }
                    else {
                        commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "Warning: World name does not match with saved name! Proceed with caution.");
                        commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "World name of saved path was '" + s4.split(",")[0] + "'.");
                    }
                }
                this.waypoints.put(player2.getUniqueId(), safeWaypoints4);
                this.waypoints_s.put(player2.getUniqueId(), safeWaypointSpeeds4);
            }
            catch (Exception ex11) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Malformed file, loading failed / was not finished");
                return true;
            }
            //commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Playing!");
            String time = array[3];
            if (!waypoints_t.containsKey(player2.getUniqueId())) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "I don't know the playback time for this path yet, please play it once normally (and save if you want to cache it).");
                return true;
            } else if (waypoints_t.get(player2.getUniqueId()) < 0) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "I don't know the playback time for this path yet, please play it once normally (and save if you want to cache it).");
                return true;
            }
            double sec = 0;
            try {
                String[] tok = time.split(":");
                if (tok.length > 3 || tok.length < 1) {
                    throw new IllegalArgumentException();
                }
                int x = 1;
                for (int i = tok.length - 1; i >= 0; i--) {
                    sec += (x == 1 ? Double.parseDouble(tok[i]) : Integer.parseInt(tok[i])) * x;
                    x *= 60;
                }
            } catch (Exception ex) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Cannot parse the given time.");
                return true;
            }
            if (waypoints_t.get(player2.getUniqueId()) > 0) {
                // speed multiplier
                multipl.put(player2.getUniqueId(), waypoints_t.get(player2.getUniqueId()) / sec);
            }
            pl_playing.put(player2.getUniqueId(), false);
            pl_looping.put(player2.getUniqueId(), false);
            this.play(player2, commandSender, true, StartCause.FPLAY);
        }
        else if (s2.equalsIgnoreCase("goto")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " goto id");
                return true;
            }
            try {
                final ArrayList<Double> safeWaypointYaw6 = this.getSafeWaypointYaw(commandSender);
                final ArrayList<Double> safeWaypointPitch6 = this.getSafeWaypointPitch(commandSender);
                final int int14 = Integer.parseInt(array[1]);
                final Location location5 = this.getSafeWaypoints(commandSender).get(int14);
                if (!this.improper(safeWaypointYaw6.get(int14))) {
                    location5.setYaw((float)(double)safeWaypointYaw6.get(int14));
                }
                if (!this.improper(safeWaypointPitch6.get(int14))) {
                    location5.setPitch((float)(double)safeWaypointPitch6.get(int14));
                }
                ((Player)commandSender).teleport(location5);
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Teleported.");
            }
            catch (NumberFormatException ex3) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Please type an integer.");
            }
            catch (IndexOutOfBoundsException ex4) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Waypoint not found.");
            }
            catch (Exception ex5) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Could not teleport!");
            }
        }
        else if (s2.equalsIgnoreCase("remove")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                final ArrayList<Location> safeWaypoints5 = this.getSafeWaypoints(commandSender);
                final int n9 = safeWaypoints5.size() - 1;
                if (n9 < 0) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Your path is already devoid of waypoints!");
                    return true;
                }
                safeWaypoints5.remove(n9);
                final ArrayList<Double> safeWaypointSpeeds5 = this.getSafeWaypointSpeeds(commandSender);
                safeWaypointSpeeds5.remove(n9);
                final ArrayList<Double> safeWaypointYaw7 = this.getSafeWaypointYaw(commandSender);
                safeWaypointYaw7.remove(n9);
                final ArrayList<Double> safeWaypointPitch7 = this.getSafeWaypointPitch(commandSender);
                safeWaypointPitch7.remove(n9);
                this.waypoints.put(((Player)commandSender).getUniqueId(), safeWaypoints5);
                this.waypoints_s.put(((Player)commandSender).getUniqueId(), safeWaypointSpeeds5);
                this.waypoints_y.put(((Player)commandSender).getUniqueId(), safeWaypointYaw7);
                this.waypoints_p.put(((Player)commandSender).getUniqueId(), safeWaypointPitch7);
                if (this.waypoints_m.get(((Player)commandSender).getUniqueId()) == null) {
                    this.waypoints_m.put(((Player)commandSender).getUniqueId(), new ArrayList<String>());
                }
                this.waypoints_m.get(((Player)commandSender).getUniqueId()).remove(n9);
                if (this.waypoints_c.get(((Player)commandSender).getUniqueId()) == null) {
                    this.waypoints_c.put(((Player)commandSender).getUniqueId(), new ArrayList<List<String>>());
                }
                this.waypoints_c.get(((Player)commandSender).getUniqueId()).remove(n9);
                this.waypoints_d.get(((Player)commandSender).getUniqueId()).remove(n9);
                this.waypoints_l.get(((Player)commandSender).getUniqueId()).remove(n9);
                this.waypoints_i.get(((Player)commandSender).getUniqueId()).remove(n9);
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Removed last point!");
            }
            try {
                final int int15 = Integer.parseInt(array[1]);
                final ArrayList<Location> safeWaypoints6 = this.getSafeWaypoints(commandSender);
                safeWaypoints6.remove(int15);
                this.clearCache((Player)commandSender);
                this.clearPathName((Player)commandSender);
                final ArrayList<Double> safeWaypointSpeeds6 = this.getSafeWaypointSpeeds(commandSender);
                safeWaypointSpeeds6.remove(int15);
                final ArrayList<Double> safeWaypointYaw8 = this.getSafeWaypointYaw(commandSender);
                safeWaypointYaw8.remove(int15);
                final ArrayList<Double> safeWaypointPitch8 = this.getSafeWaypointPitch(commandSender);
                safeWaypointPitch8.remove(int15);
                this.waypoints.put(((Player)commandSender).getUniqueId(), safeWaypoints6);
                this.waypoints_s.put(((Player)commandSender).getUniqueId(), safeWaypointSpeeds6);
                this.waypoints_y.put(((Player)commandSender).getUniqueId(), safeWaypointYaw8);
                this.waypoints_p.put(((Player)commandSender).getUniqueId(), safeWaypointPitch8);
                if (this.waypoints_m.get(((Player)commandSender).getUniqueId()) == null) {
                    this.waypoints_m.put(((Player)commandSender).getUniqueId(), new ArrayList<String>());
                }
                this.waypoints_m.get(((Player)commandSender).getUniqueId()).remove(int15);
                if (this.waypoints_c.get(((Player)commandSender).getUniqueId()) == null) {
                    this.waypoints_c.put(((Player)commandSender).getUniqueId(), new ArrayList<List<String>>());
                }
                this.waypoints_c.get(((Player)commandSender).getUniqueId()).remove(int15);
                this.waypoints_d.get(((Player)commandSender).getUniqueId()).remove(int15);
                this.waypoints_l.get(((Player)commandSender).getUniqueId()).remove(int15);
                this.waypoints_i.get(((Player)commandSender).getUniqueId()).remove(int15);
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Removed point #" + int15 + "!");
            }
            catch (NumberFormatException ex12) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Please type an integer.");
            }
            catch (IndexOutOfBoundsException ex13) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Waypoint not found.");
            }
            catch (Exception ex14) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Could not remove!");
            }
        }
        else if (s2.equalsIgnoreCase("clone")) {
            
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " clone player");
                return true;
            }
            final Player player3 = this.getServer().getPlayer(array[1]);
            if (player3 == null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Cannot find player!");
                return true;
            }
            if (this.speed.containsKey(player3.getUniqueId())) {
                this.speed.put(((Player)commandSender).getUniqueId(), this.speed.get(player3.getUniqueId()));
            }
            else {
                this.speed.remove(((Player)commandSender).getUniqueId());
            }
            this.clearCache((Player)commandSender);
            this.pathnames.put(((Player)commandSender).getUniqueId(), this.pathnames.get((((Player)commandSender).getUniqueId())));
            if (this.pathnames.get(((Player)commandSender).getUniqueId()) != null)
                this.clearPathName((Player)commandSender);
            this.waypoints.put(((Player)commandSender).getUniqueId(), this.getSafeWaypoints(player3));
            this.waypoints_s.put(((Player)commandSender).getUniqueId(), this.getSafeWaypointSpeeds(player3));
            this.waypoints_y.put(((Player)commandSender).getUniqueId(), this.getSafeWaypointYaw(player3));
            this.waypoints_p.put(((Player)commandSender).getUniqueId(), this.getSafeWaypointPitch(player3));
            this.waypoints_m.put(((Player)commandSender).getUniqueId(), this.getSafeWaypointMessages(player3));
            this.waypoints_c.put(((Player)commandSender).getUniqueId(), this.getSafeWaypointCommands(player3));
            this.waypoints_d.put(((Player)commandSender).getUniqueId(), this.getSafeWaypointDelays(player3));
            this.waypoints_l.put(((Player)commandSender).getUniqueId(), this.getSafeWaypointOptions(player3));
            this.waypoints_i.put(((Player)commandSender).getUniqueId(), this.getSafeWaypointInstants(player3));
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Path cloned.");
        }
        else if (s2.equalsIgnoreCase("load")) {
            if (!commandSender.hasPermission("servercinematics.play")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                final StringBuilder sb4 = new StringBuilder();
                File[] listFiles;
                for (int length3 = (listFiles = this.paths.listFiles()).length, n10 = 0; n10 < length3; ++n10) {
                    final File file2 = listFiles[n10];
                    if (file2.isFile()) {
                        sb4.append(", ");
                        sb4.append(file2.getName());
                    }
                }
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "List of saved paths:");
                if (sb4.length() < 1) {
                    commandSender.sendMessage("" + make_color('c') + "No paths were found");
                    return true;
                }
                commandSender.sendMessage(sb4.toString().substring(2));
                return true;
            }
            else {
                try {
                    final File file3 = new File(this.paths, array[1]);
                    if (!file3.isFile()) {
                        commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Not found");
                        return true;
                    }
                    final String s8 = new String(Files.readAllBytes(file3.toPath()), StandardCharsets.UTF_8);
                    final Player player4 = (Player)commandSender;
                    final UUID u4 = player4.getUniqueId();
                    int n11 = 0;
                    this.clearCache((Player)commandSender);
                    this.waypoints.put(player4.getUniqueId(), new ArrayList<Location>());
                    this.waypoints_s.put(player4.getUniqueId(), new ArrayList<Double>());
                    this.waypoints_y.put(player4.getUniqueId(), new ArrayList<Double>());
                    this.waypoints_p.put(player4.getUniqueId(), new ArrayList<Double>());
                    this.waypoints_m.put(player4.getUniqueId(), new ArrayList<String>());
                    this.waypoints_c.put(player4.getUniqueId(), new ArrayList<List<String>>());
                    this.waypoints_f.put(player4.getUniqueId(), 0);
                    this.waypoints_t.put(player4.getUniqueId(), -1.0);
                    this.waypoints_d.put(player4.getUniqueId(), new ArrayList<Double>());
                    this.waypoints_l.put(player4.getUniqueId(), new ArrayList<Integer>());
                    this.waypoints_i.put(player4.getUniqueId(), new ArrayList<Boolean>());
                    final ArrayList<Location> safeWaypoints7 = this.getSafeWaypoints(player4);
                    final ArrayList<Double> safeWaypointSpeeds7 = this.getSafeWaypointSpeeds(player4);
                    final ArrayList<Double> safeWaypointYaw9 = this.getSafeWaypointYaw(player4);
                    final ArrayList<Double> safeWaypointPitch9 = this.getSafeWaypointPitch(player4);
                    final ArrayList<String> safeWaypointMessages3 = this.getSafeWaypointMessages(player4);
                    final ArrayList<List<String>> safeWaypointCommands3 = this.getSafeWaypointCommands(player4);
                    final World world3 = player4.getWorld();
                    final String s9 = s8.split("#")[0];
                    final String s10 = s8.split("#")[1];
                    final float float3 = Float.parseFloat(s9.split(",")[2]);
                    final float float4 = Float.parseFloat(s9.split(",")[3]);
                    this.pathnames.put(u4, array[1]);
                    int n12 = 0;
                    int safeFlags2 = 0;
                    if (s9.split(",").length > 4) {
                        safeFlags2 = Integer.parseInt(s9.split(",")[4]);
                    }
                    double safeTime = -1.0;
                    if (s9.split(",").length > 5) {
                        safeTime = Double.parseDouble(s9.split(",")[5]);
                    }
                    String[] split6;
                    for (int length4 = (split6 = s10.split(Pattern.quote("|"))).length, n13 = 0; n13 < length4; ++n13) {
                        final String s11 = split6[n13];
                        try {
                            final String[] split7 = s11.split(",");
                            final Location location6 = new Location(world3, Double.parseDouble(split7[0]), Double.parseDouble(split7[1]), Double.parseDouble(split7[2].split(";")[0]));
                            if (n12 == 0) {
                                location6.setYaw(float3);
                                location6.setPitch(float4);
                            }
                            safeWaypoints7.add(location6);
                            safeWaypointCommands3.add(new ArrayList<String>());
                            if (split7[3].indexOf(10) >= 0) {
                                safeWaypointSpeeds7.add(Double.parseDouble(split7[3].split("\n")[0]));
                                int n14 = 0;
                                String[] split8;
                                for (int length5 = (split8 = split7[3].split("\n")).length, n15 = 0; n15 < length5; ++n15) {
                                    final String s12 = split8[n15];
                                    if (n14++ >= 1) {
                                        safeWaypointCommands3.get(n12).add(s12.replace("\uf555", ","));
                                    }
                                }
                            }
                            else {
                                safeWaypointSpeeds7.add(Double.parseDouble(split7[3]));
                            }

                            try {
                                if (split7[2].split(";").length > 3) {
                                    this.waypoints_i.get(u4).add(!split7[2].split(";")[3].equalsIgnoreCase("0"));
                                } else {
                                    this.waypoints_i.get(u4).add(false);
                                }
                                if (split7[2].split(";").length < 2) {
                                    throw new ArrayIndexOutOfBoundsException();
                                }
                                double d = Double.parseDouble(split7[2].split(";")[1]);
                                int lf = Integer.parseInt(split7[2].split(";")[2]);
                                this.waypoints_d.get(u4).add(d);
                                this.waypoints_l.get(u4).add(lf);
                            } catch (Exception ex) {
                                this.waypoints_d.get(u4).add(0.0);
                                this.waypoints_l.get(u4).add(0);
                            }
                            if (split7.length > 4) {
                                final String[] split9 = split7[4].split(":");
                                final String[] split10 = split9[1].split("\\$", 2);
                                if (split10.length > 1) {
                                    safeWaypointMessages3.add(split10[1].replace("\uf555", ","));
                                }
                                else {
                                    safeWaypointMessages3.add("");
                                }
                                safeWaypointYaw9.add(this.formatAngleYaw(Double.parseDouble(split9[0])));
                                safeWaypointPitch9.add(this.formatAnglePitch(Double.parseDouble(split10[0])));
                            }
                            else {
                                safeWaypointYaw9.add(444.0);
                                safeWaypointPitch9.add(444.0);
                            }
                            ++n12;
                        }
                        catch (Exception ex15) {
                            if (safeWaypointYaw9.size() > safeWaypointPitch9.size()) {
                                safeWaypointYaw9.remove(safeWaypointYaw9.size() - 1);
                            }
                            if (safeWaypointMessages3.size() > safeWaypointYaw9.size()) {
                                safeWaypointMessages3.remove(safeWaypointMessages3.size() - 1);
                            }
                            if (safeWaypointSpeeds7.size() > safeWaypointYaw9.size()) {
                                safeWaypointSpeeds7.remove(safeWaypointSpeeds7.size() - 1);
                            }
                            if (safeWaypoints7.size() > safeWaypointSpeeds7.size()) {
                                safeWaypoints7.remove(safeWaypoints7.size() - 1);
                            }
                            if (safeWaypointCommands3.size() > safeWaypoints7.size()) {
                                safeWaypointCommands3.remove(safeWaypointCommands3.size() - 1);
                            }
                            ++n11;
                        }
                    }
                    this.waypoints.put(player4.getUniqueId(), safeWaypoints7);
                    this.waypoints_s.put(player4.getUniqueId(), safeWaypointSpeeds7);
                    this.waypoints_y.put(player4.getUniqueId(), safeWaypointYaw9);
                    this.waypoints_p.put(player4.getUniqueId(), safeWaypointPitch9);
                    this.waypoints_m.put(player4.getUniqueId(), safeWaypointMessages3);
                    this.waypoints_c.put(player4.getUniqueId(), safeWaypointCommands3);
                    this.waypoints_f.put(player4.getUniqueId(), safeFlags2);
                    this.waypoints_t.put(player4.getUniqueId(), safeTime);
                    this.speed.put(player4.getUniqueId(), Double.parseDouble(s9.split(",")[1]));
                    if (!world3.getName().equalsIgnoreCase(s9.split(",")[0])) {
                        commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "Warning: World name does not match with saved name! Proceed with caution.");
                        commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "World name of saved path was '" + s9.split(",")[0] + "'.");
                    }
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Successfully loaded!");
                    if (n11 > 0)
                        commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "Skipped " + n11 + " malformed entries");
                }
                catch (Exception ex) {
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Malformed file, loading failed / was not finished");
                    commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('f') + "" + ex.toString());
                }
            }
        }
        else if (s2.equalsIgnoreCase("save")) {
            if (!commandSender.hasPermission("servercinematics.edit")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " save filename");
                return true;
            }
            final String s13 = array[1];
            if (s13.contains(".")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Could not save");
                return true;
            }
            final StringBuilder sb5 = new StringBuilder();
            boolean b2 = true;
            final Player player5 = (Player)commandSender;
            final ArrayList<Location> safeWaypoints8 = this.getSafeWaypoints(player5);
            final ArrayList<Double> safeWaypointSpeeds8 = this.getSafeWaypointSpeeds(player5);
            final ArrayList<Double> safeWaypointYaw10 = this.getSafeWaypointYaw(player5);
            final ArrayList<Double> safeWaypointPitch10 = this.getSafeWaypointPitch(player5);
            final ArrayList<String> safeWaypointMessages4 = this.getSafeWaypointMessages(player5);
            final ArrayList<List<String>> safeWaypointCommands4 = this.getSafeWaypointCommands(player5);
            sb5.append(player5.getWorld().getName());
            sb5.append(",");
            Double value = this.speed.get(player5.getUniqueId());
            if (value == null) {
                value = 5.0;
            }
            sb5.append(value);
            sb5.append(",");
            float yaw = 0.0f;
            float pitch = 0.0f;
            if (safeWaypoints8.size() > 0) {
                yaw = safeWaypoints8.get(0).getYaw();
                pitch = safeWaypoints8.get(0).getPitch();
            }
            sb5.append(yaw);
            sb5.append(",");
            sb5.append(pitch);
            sb5.append(",");
            sb5.append(this.getSafeWaypointFlags(player5));
            sb5.append(",");
            UUID u4 = player5.getUniqueId();
            sb5.append(waypoints_t.containsKey(u4) ? waypoints_t.get(u4) : -1.0);
            sb5.append("#");
            for (int n16 = 0; n16 < safeWaypoints8.size(); ++n16) {
                if (b2) {
                    b2 = !b2;
                }
                else {
                    sb5.append("|");
                }
                final Location location7 = safeWaypoints8.get(n16);
                sb5.append(location7.getX());
                sb5.append(",");
                sb5.append(location7.getY());
                sb5.append(",");
                sb5.append(location7.getZ());
                sb5.append(";");
                sb5.append(waypoints_d.get(u4).get(n16));
                sb5.append(";");
                sb5.append(waypoints_l.get(u4).get(n16));
                sb5.append(";");
                sb5.append(waypoints_i.get(u4).get(n16) ? "1" : "0");
                sb5.append(",");
                sb5.append(safeWaypointSpeeds8.get(n16));
                if (safeWaypointCommands4.get(n16).size() > 0) {
                    for (final String s14 : safeWaypointCommands4.get(n16)) {
                        sb5.append("\n");
                        sb5.append(s14.replace(",", "\uf555"));
                    }
                }
                sb5.append(",");
                sb5.append(safeWaypointYaw10.get(n16) + ":" + safeWaypointPitch10.get(n16));
                sb5.append("$" + safeWaypointMessages4.get(n16).replace(",", "\uf555"));
            }
            try {
                final PrintWriter printWriter = new PrintWriter(new File(this.paths, s13), "UTF-8");
                printWriter.print(sb5.toString());
                printWriter.close();
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Saved: open with /" + s + " load " + s13);
                this.pathnames.put(u4, s13);
            }
            catch (Exception ex2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "" + ex2.toString());
            }
        }
        else if (s2.equalsIgnoreCase("clear")) {
            if (!commandSender.hasPermission("servercinematics.play")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            this.clearCache((Player)commandSender);
            this.clearPathName((Player)commandSender);
            this.waypoints.put(((Player)commandSender).getUniqueId(), new ArrayList<Location>());
            this.waypoints_s.put(((Player)commandSender).getUniqueId(), new ArrayList<Double>());
            this.waypoints_y.put(((Player)commandSender).getUniqueId(), new ArrayList<Double>());
            this.waypoints_p.put(((Player)commandSender).getUniqueId(), new ArrayList<Double>());
            this.waypoints_m.put(((Player)commandSender).getUniqueId(), new ArrayList<String>());
            this.waypoints_c.put(((Player)commandSender).getUniqueId(), new ArrayList<List<String>>());
            this.getSafeWaypointFlags((Player)commandSender);
            this.waypoints_f.put(((Player)commandSender).getUniqueId(), 0);
            this.waypoints_t.put(((Player)commandSender).getUniqueId(), -1.0);
            this.waypoints_d.put(((Player)commandSender).getUniqueId(), new ArrayList<Double>());
            this.waypoints_l.put(((Player)commandSender).getUniqueId(), new ArrayList<Integer>());
            this.waypoints_i.put(((Player)commandSender).getUniqueId(), new ArrayList<Boolean>());
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Path cleared.");
        }
        else if (s2.equalsIgnoreCase("resume")) {
            if (!commandSender.hasPermission("servercinematics.play")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            final UUID uniqueId2 = ((Player)commandSender).getUniqueId();
            if (!this.playing.containsKey(uniqueId2)) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You're not playing!");
                return true;
            }
            if (!this.playing.get(uniqueId2)) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You're not playing!");
                return true;
            }
            if (!this.paused.containsKey(uniqueId2)) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You're not paused!");
                return true;
            }
            if (!this.paused.get(uniqueId2)) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You're not paused!");
                return true;
            }
            this.paused.put(uniqueId2, false);
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Resuming...");
        }
        else if (s2.equalsIgnoreCase("pause")) {
            if (!commandSender.hasPermission("servercinematics.play")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            final Player player6 = (Player)commandSender;
            final UUID uniqueId3 = player6.getUniqueId();
            if (!this.playing.containsKey(uniqueId3)) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You're not playing!");
                return true;
            }
            if (!this.playing.get(uniqueId3)) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You're not playing!");
                return true;
            }
            if (this.paused.containsKey(uniqueId3) && this.paused.get(uniqueId3)) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You're already paused!");
                return true;
            }
            player6.setVelocity(new Vector(0.0, 0.0, 0.0));
            this.paused.put(uniqueId3, true);
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "Paused: continue with /" + s + " resume");
        }
        else if (s2.equalsIgnoreCase("play")) {
            if (!commandSender.hasPermission("servercinematics.play")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Only players can run this.");
                return true;
            }
            if (this.isTrue(this.playing.get(((Player)commandSender).getUniqueId()))) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Already playing!");
                return true;
            }
            final Player player7 = (Player)commandSender;
            pl_playing.put(player7.getUniqueId(), false);
            pl_looping.put(player7.getUniqueId(), false);
            player7.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Playing...");
            this.play(player7, commandSender, false, StartCause.MANUAL);
        }
        else if (s2.equalsIgnoreCase("tplay")) {
            if (!commandSender.hasPermission("servercinematics.play")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (array.length < 2) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "/" + s + " tplay ((hours:)minutes:)seconds");
                return true;
            }
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Only players can run this.");
                return true;
            }
            if (this.isTrue(this.playing.get(((Player)commandSender).getUniqueId()))) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Already playing!");
                return true;
            }
            String time = array[1];
            final Player player7 = (Player)commandSender;
            UUID u = player7.getUniqueId();
            if (!waypoints_t.containsKey(u)) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "I don't know the playback time for this path yet, please play it once normally (and save if you want to cache it).");
                return true;
            } else if (waypoints_t.get(u) < 0) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "I don't know the playback time for this path yet, please play it once normally (and save if you want to cache it).");
                return true;
            }
            double sec = 0;
            try {
                String[] tok = time.split(":");
                if (tok.length > 3 || tok.length < 1) {
                    throw new IllegalArgumentException();
                }
                int x = 1;
                for (int i = tok.length - 1; i >= 0; i--) {
                    sec += (x == 1 ? Double.parseDouble(tok[i]) : Integer.parseInt(tok[i])) * x;
                    x *= 60;
                }
            } catch (Exception ex) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "Cannot parse the given time.");
                return true;
            }
            if (waypoints_t.get(u) > 0) {
                multipl.put(u, waypoints_t.get(u) / sec);
            }
            pl_playing.put(player7.getUniqueId(), false);
            pl_looping.put(player7.getUniqueId(), false);
            player7.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Playing...");
            this.play(player7, commandSender, false, StartCause.MANUAL);
        }
        else if (s2.equalsIgnoreCase("stop")) {
            if (!commandSender.hasPermission("servercinematics.play")) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this.");
                return true;
            }
            if (globalMode != null) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You can't use this." + (commandSender.hasPermission("servercinematics.edit") ? " If you want to stop global playback: /" + s + " fstop **" : ""));
                return true;
            }
            if (this.isFalse(this.playing.get(((Player)commandSender).getUniqueId()))) {
                commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "You are not playing!");
                return true;
            }
            this.stop((Player)commandSender, PathPlaybackStoppedEvent.StopCause.MANUAL);
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('a') + "Stopped.");
        }
        else {
            this.sendMultilineMessage(commandSender, helpString.replace("###", s), "" + make_color('7') + "");
        }
        return true;
    }

    private void stopGlobal() {
        if (globalMode == null) return;
        globalMode = null;
        for (Player ps: getServer().getOnlinePlayers()) {
            Location l = getServer().getWorlds().get(0).getSpawnLocation();
            ps.teleport(l);
            GameMode gm = getServer().getDefaultGameMode();
            ps.setGameMode(gm);
            ps.setAllowFlight(gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR);
            ps.setFlying(gm == GameMode.SPECTATOR);
            ps.setFlySpeed(1.0f);
        }
    }

    private long play(final Player player, final CommandSender commandSender, final boolean is_fplay, final PathPlaybackStartedEvent.StartCause cause) {
        final UUID uniqueId = player.getUniqueId();
        return playWithFakeUUID(player, commandSender, uniqueId, is_fplay, cause);
    }
    
    // not necessarily fake UUID despite the misleading name
    private long playWithFakeUUID(final Player player, final CommandSender commandSender, final UUID uniqueId, final boolean is_fplay, final PathPlaybackStartedEvent.StartCause cause) {
        if (this.getSafeWaypoints(uniqueId).size() <= 0 && commandSender != null) {
            commandSender.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "The path seems to have no waypoints!");
            return 0;
        }
        long id = ++pbid;
        if (id <= 0) {
            id = 0;
        }
        pbids.put(uniqueId, id);
        if (globalMode == null) {
            this.old_af.put(uniqueId, player.getAllowFlight());
            this.old_f.put(uniqueId, player.isFlying());
            this.old_gm.put(uniqueId, player.getGameMode());
            this.old_loc.put(uniqueId, player.getLocation());
            this.old_fsp.put(uniqueId, player.getFlySpeed());
            player.setFlySpeed(0.0f);
        }
        this.playing.put(uniqueId, true);
        final int tid = this.timer_id++;
        this.timer_ids.put(uniqueId, tid);
        if (!this.speed.containsKey(uniqueId)) {
            this.speed.put(uniqueId, 5.0);
        }
        this.speed_a.put(uniqueId, this.speed.get(uniqueId));
        PathPlaybackStartedEvent evt = new PathPlaybackStartedEvent(player, getPlayingPath(player), cause, id);
        getServer().getPluginManager().callEvent(evt);
        new BukkitRunnable() {
            CommandSender owner = commandSender;
            Player p = player;
            UUID u = uniqueId;
            // path info: locations, speeds, yaws, pitches, messages, flags, delays, instant toggles
            ArrayList<Location> w = new ArrayList<Location>(ServerCinematics.this.getSafeWaypoints(this.u));
            ArrayList<Double> ws = new ArrayList<Double>(ServerCinematics.this.getSafeWaypointSpeeds(this.u));
            ArrayList<Double> wy = new ArrayList<Double>(ServerCinematics.this.getSafeWaypointYaw(this.u));
            ArrayList<Double> wp = new ArrayList<Double>(ServerCinematics.this.getSafeWaypointPitch(this.u));
            ArrayList<String> wmsg = new ArrayList<String>(ServerCinematics.this.getSafeWaypointMessages(this.u));
            ArrayList<Integer> wl = new ArrayList<Integer>(ServerCinematics.this.waypoints_l.get(this.u));
            ArrayList<Double> wd = new ArrayList<Double>(ServerCinematics.this.waypoints_d.get(this.u));
            ArrayList<Boolean> wi = new ArrayList<Boolean>(ServerCinematics.this.waypoints_i.get(this.u));
            ArrayList<Integer> windx = new ArrayList<Integer>();
            ArrayList<Boolean> wtemp = new ArrayList<Boolean>();
            // path flags
            int wf = ServerCinematics.this.getSafeWaypointFlags(this.u);
            // waypoint commands
            ArrayList<List<String>> wcmd = new ArrayList<List<String>>(ServerCinematics.this.getSafeWaypointCommands(this.u));
            // current waypoint index
            int i = 0;
            // the ID of this path task
            int id = tid;
            // distance to plane formed by next waypoint
            double ls = 0.0;
            //double ld = 0.0;
            double xm = 0.0;
            double ym = 0.0;
            double zm = 0.0;
            double op = 0.0;
            double oy = 0.0;
            double fp = 0.0;
            double fy = 0.0;
            // distance to next waypoint from last waypoint
            double dist = 0.0;
            // whether waypoint has a proper yaw value
            boolean yaw = false;
            // whether waypoint has a proper pitch value
            boolean pitch = false;
            // should end playback instantly
            boolean end = false;
            // teleport mode?
            boolean tm = false;
            // pathless mode?
            boolean nopath = false;
            // running for the first time
            boolean first = true;
            boolean fplay = is_fplay;
            // should this waypoint be shown in instant mode?
            boolean npoint = false;
            // usually [0, 1], approaches 1 as we approach next waypoint
            double q = 0.0;
            // speed multiplier for (f)tplay
            double M = 1.0;
            long ticks_moved = 0;
            // effective position, if not in tpmode
            Location effpos = null;
            // array of waypoints to use in instant mode
            ArrayList<Integer> apoints = new ArrayList<Integer>();
            int totaldelay = 0;
            boolean initAsGlobal = globalMode != null;
            
            private int getCurrentId() {
                return ServerCinematics.this.timer_ids.get(this.u);
            }
            
            private boolean isCurrent() {
                return this.id == this.getCurrentId();
            }
/*
            private String debugFmt(Location l) {
                return String.format("(%+7.3f,%+7.3f,%+7.3f)", l.getX(), l.getY(), l.getZ());
            }
            private String debugFmt(Vector l) {
                return String.format("(%+7.3f,%+7.3f,%+7.3f)", l.getX(), l.getY(), l.getZ());
            }
            */
            public void run() {
                if (this.w.size() <= 0) {
                    if (globalMode == null) this.p.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "The path seems to have no waypoints!");
                    this.cancel();
                    if (globalMode == null) ServerCinematics.this.stop(this.p, PathPlaybackStoppedEvent.StopCause.FINISHED);
                    else ServerCinematics.this.stopGlobalUUID(u);
                    return;
                }
                if (this.first) {
                    this.p.setGameMode(GameMode.SPECTATOR);
                    this.p.setAllowFlight(true);
                    this.p.setFlying(true);
                    if (globalMode == null) this.u = this.p.getUniqueId();
                    else {
                        for (Player ps: getServer().getOnlinePlayers()) {
                            ps.setGameMode(GameMode.SPECTATOR);
                            ps.setAllowFlight(true);
                            ps.setFlying(true);
                            ps.setFlySpeed(0.0f);
                        }
                    }
                    if (multipl.containsKey(u)) {
                        M = multipl.remove(u);
                    }
                    this.tm = (ServerCinematics.instance.teleport.containsKey(this.u) && ServerCinematics.instance.teleport.get(this.u));
                    this.nopath = (ServerCinematics.instance.pathless.containsKey(this.u) && ServerCinematics.instance.pathless.get(this.u));
                    waypointEvent(0);
                    
                    // interpolate paths
                    // creates a whole bunch of intermediate points that are never visible to the player
                    
                    if (globalMode != null || !ServerCinematics.this.hasCache(this.p) || nopath) {
                        if (ServerCinematics.this.pl_playing.get(u) && owner != null)
                            this.owner.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('e') + "Calculating spline, please wait...");
                        new BukkitRunnable() {
                            ArrayList<Location> ow = new ArrayList<Location>(w);
                            ArrayList<Double> ows = new ArrayList<Double>(ws);
                            ArrayList<Double> owy = new ArrayList<Double>(wy);
                            ArrayList<Double> owp = new ArrayList<Double>(wp);

                            ArrayList<Location> v = new ArrayList<Location>(ow);
                            ArrayList<Double> vs = new ArrayList<Double>(ows);
                            ArrayList<Double> vy = new ArrayList<Double>(owy);
                            ArrayList<Double> vp = new ArrayList<Double>(owp);

                            ArrayList<String> vm = new ArrayList<String>(wmsg);
                            ArrayList<List<String>> vc = new ArrayList<List<String>>(wcmd);
                            ArrayList<Double> vd = new ArrayList<Double>(wd);
                            ArrayList<Integer> vl = new ArrayList<Integer>(wl);
                            ArrayList<Boolean> vi = new ArrayList<Boolean>(wi);

                            ArrayList<Integer> vindx = new ArrayList<Integer>();
                            ArrayList<Boolean> vtemp = new ArrayList<Boolean>();
                            int size = this.ow.size() - 1;
                            int[] lsz = new int[this.size];
                            public void run() {
                                for (int i = 0; i <= size; ++i) {
                                    this.vindx.add(i);
                                }
                                apoints.add(0);
                                for (int i = 0; i < this.ow.size(); i++) {
                                    vtemp.add(false);
                                }
                                for (int n = 0; n < this.size; ++n) {
                                    if (!isCurrent()) {
                                        this.cancel();
                                        return;
                                    }
                                    final int min = Math.min(20, (int)((this.wget(n).distance(this.wget(n + 1)) - 1.0) * 2.0));
                                    final boolean isInstant = tm && this.vi.get(n); 
                                    this.lsz[n] = min;
                                    
                                    final double prevSpeed = this.ows.get(n);
                                    double prevYaw = this.owy.get(n);
                                    double prevPitch = this.owp.get(n);
                                    
                                    final double nextSpeed = this.ows.get(n + 1);
                                    final double nextYaw = this.owy.get(n + 1);
                                    final double nextPitch = this.owp.get(n + 1);
                                    
                                    if (ServerCinematics.this.improper(prevYaw) && n == 0) {
                                        prevYaw = this.ow.get(0).getYaw();
                                    }
                                    if (ServerCinematics.this.improper(prevPitch) && n == 0) {
                                        prevPitch = this.ow.get(0).getPitch();
                                    }
                                    int max = -1;
                                    for (int j = 1; j < min; ++j) {
                                        final double n2 = Double.valueOf(j) / min;
                                        final int n3 = this.maxIndex(n) + j;
                                        // interpolated location
                                        Location loc = ServerCinematics.catmull_rom_3d(n2, this.wget(n - 1), this.wget(n), this.wget(n + 1), this.wget(n + 2));
                                        this.v.add(n3, loc);
                                        // interpolated speed
                                        this.vs.add(n3, (prevSpeed == -1.0 || nextSpeed == -1.0) ? -1.0 : this.linear(prevSpeed, nextSpeed, n2));
                                        // interpolated yaw
                                        this.vy.add(n3, (ServerCinematics.this.improper(prevYaw) || ServerCinematics.this.improper(nextYaw)) ? 444.0 
                                                : formatAngleYaw(this.linearO(ServerCinematics.this.properYaw(prevYaw), ServerCinematics.this.properYaw(nextYaw), n2)));
                                        // interpolated pitch
                                        this.vp.add(n3, (ServerCinematics.this.improper(prevPitch) || ServerCinematics.this.improper(nextPitch)) ? 444.0 
                                                : this.linear(prevPitch, nextPitch, n2));
                                        // other info is always default
                                        this.vm.add(n3, "");
                                        this.vc.add(n3, new ArrayList<String>());
                                        this.vd.add(n3, 0.0);
                                        this.vl.add(n3, 0);
                                        this.vi.add(n3, isInstant);
                                        // point is indeed interpolated
                                        this.vtemp.add(n3, true);
                                        // and does not correspond to any original point
                                        this.vindx.add(n3, -1);
                                        max = n3 + 1;
                                    }
                                    if (max >= 0)
                                        apoints.add(max);
                                }
                                
                                // add placeholder blank points
                                if (this.vm.get(0).length() > 0 || this.vc.get(0).size() > 0 || this.vd.get(0) > 0 || (this.vl.get(0)&1)>0) {
                                    for (int i = 0; i < 2; i++) {
                                        this.v.add(0, this.v.get(0));
                                        this.vs.add(0, this.vs.get(0));
                                        this.vy.add(0, this.vy.get(0));
                                        this.vp.add(0, this.vp.get(0));
                                        this.vm.add(0, "");
                                        this.vc.add(0, new ArrayList<String>());
                                        this.vd.add(0, 0.0);
                                        this.vl.add(0, 0);
                                        this.vtemp.add(0, true);
                                        this.vi.add(0, false);
                                        this.vindx.add(0, -1);
                                    }
                                }
                                /*int ti = 0;
                                for (Location l: this.v) {
                                    System.out.println(String.format("%7d ", this.vindx.get(ti)) + debugFmt(l));
                                    ++ti;
                                }
                                for (int i = 0; i < vy.size(); ++i) {
                                    getServer().broadcastMessage("I=" + i + ", O=" + (vindx.get(i) >= 0 ? "Y" : "N") + ", Y=" + String.format("%.4f", vy.get(i)) + ", P=" + String.format("%.4f", vp.get(i)));
                                }*/
                                ServerCinematics.this.wx.put(u, this.v);
                                ServerCinematics.this.wxs.put(u, this.vs);
                                ServerCinematics.this.wxy.put(u, this.vy);
                                ServerCinematics.this.wxp.put(u, this.vp);
                                ServerCinematics.this.wxm.put(u, this.vm);
                                ServerCinematics.this.wxc.put(u, this.vc);
                                ServerCinematics.this.wxl.put(u, this.vl);
                                ServerCinematics.this.wxd.put(u, this.vd);
                                ServerCinematics.this.wxi.put(u, this.vi);
                                ServerCinematics.this.wxindx.put(u, this.vindx);
                                ServerCinematics.this.wxtemp.put(u, this.vtemp);
                                w = this.v;
                                ws = this.ows;
                                wy = this.owy;
                                wp = this.owp;
                                wmsg = this.vm;
                                wcmd = this.vc;
                                wd = this.vd;
                                wl = this.vl;
                                wtemp = vtemp;
                                ServerCinematics.this.wm.put(u, true);
                            }
                            // linear interpolation between two degree angles
                            private double linearO(double a, double b, double c) {
                                return a+optimal(a,b)*c;
                            }/*
                            private double optimal(final double n, final double n2) {
                                return optimalRaw(n + 180, n2 + 180);
                            }
                            private double optimalRaw(final double n, final double n2) {
                                final double n3 = 180.0 - Math.abs(Math.abs(n - n2 + 360.0) - 180.0);
                                if (180.0 - Math.abs(Math.abs(n - n2 + 361.0) - 180.0) > n3) {
                                    return -n3;
                                }
                                return n3;
                            }
                            */
                            private int maxIndex(final int n) {
                                int n2 = 0;
                                for (int i = 0; i < n; ++i) {
                                    n2 += this.lsz[i];
                                }
                                return n2;
                            }
                            
                            // linear interpolation
                            private double linear(final double n, final double n2, final double n3) {
                                return (1.0 - n3) * n + n3 * n2;
                            }
                            
                            private Location wget(final int n) {
                                if (n < 0) {
                                    return this.getNegativePoint(-n);
                                }
                                try {
                                    return this.ow.get(n);
                                }
                                catch (IndexOutOfBoundsException ex) {
                                    return this.ow.get(this.ow.size() - 1);
                                }
                            }
                            
                            // extrapolate a point beyond beginning of path
                            private Location getNegativePoint(final double n) {
                                Location result = cloneLocation(this.ow.get(0)).add(cloneLocation(this.ow.get(1)).subtract(cloneLocation(this.ow.get(0))).getDirection().multiply(n));
                                //System.out.println("neg" + n + " => " + debugFmt(result));
                                return cloneLocation(result);
                            }
                        }.runTaskAsynchronously((Plugin)ServerCinematics.instance);
                    }
                    else {
                        this.w = ServerCinematics.this.wx.get(this.u);
                        this.ws = ServerCinematics.this.wxs.get(this.u);
                        this.wy = ServerCinematics.this.wxy.get(this.u);
                        this.wp = ServerCinematics.this.wxp.get(this.u);
                        this.wmsg = ServerCinematics.this.wxm.get(this.u);
                        this.wcmd = ServerCinematics.this.wxc.get(this.u);
                        this.wl = ServerCinematics.this.wxl.get(this.u);
                        this.wd = ServerCinematics.this.wxd.get(this.u);
                        this.wi = ServerCinematics.this.wxi.get(this.u);
                        this.windx = ServerCinematics.this.wxindx.get(this.u);
                        this.wtemp = ServerCinematics.this.wxtemp.get(this.u);
                    }
                    this.first = false;
                    return;
                }
                if (!this.isCurrent()) {
                    // do not let background task go haywire if it is no longer relevant
                    this.cancel();
                    return;
                }
                if (globalMode == null && !ServerCinematics.this.hasCache(this.p)) {
                    return;
                }
                if (globalMode != null || ServerCinematics.this.hasCache(this.p)) {
                    this.w = ServerCinematics.this.wx.get(this.u);
                    this.ws = ServerCinematics.this.wxs.get(this.u);
                    this.wy = ServerCinematics.this.wxy.get(this.u);
                    this.wp = ServerCinematics.this.wxp.get(this.u);
                    this.wmsg = ServerCinematics.this.wxm.get(this.u);
                    this.wcmd = ServerCinematics.this.wxc.get(this.u);
                    this.wl = ServerCinematics.this.wxl.get(this.u);
                    this.wd = ServerCinematics.this.wxd.get(this.u);
                    this.wi = ServerCinematics.this.wxi.get(this.u);
                    this.windx = ServerCinematics.this.wxindx.get(this.u);
                    this.wtemp = ServerCinematics.this.wxtemp.get(this.u);
                }
                if (initAsGlobal && globalMode == null) {
                    this.cancel();
                    return;
                }
                if (ServerCinematics.this.paused.containsKey(this.u) && ServerCinematics.this.paused.get(this.u)) {
                    return;
                }
                if (this.i >= this.w.size() || this.end || this.w.size() < 1) {
                    // reached the end of the path!
                    if (!ServerCinematics.this.pl_playing.get(u) && !fplay && owner != null)
                        this.owner.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "End of path reached.");
                    // record new duration of this path
                    waypoints_t.put(this.u, this.ticks_moved * (0.05D * M));
                    // teleport player to final waypoint, to stop them from moving
                    if (this.w.size() > 0 && final_tp) {
                        Location loc = cloneLocation(this.w.get(this.w.size() - 1));
                        if (!this.tm) {
                            Location ploc = this.p.getLocation();
                            loc.setYaw(ploc.getYaw());
                            loc.setPitch(ploc.getPitch());
                        }
                        this.p.teleport(loc);
                    }
                    if (globalMode == null)
                        ServerCinematics.this.stop(this.p, false, PathPlaybackStoppedEvent.StopCause.FINISHED);
                    else
                        ServerCinematics.this.stopGlobalUUID(this.u);
                    this.cancel();
                    return;
                }
                if (globalMode == null && !ServerCinematics.this.hasCache(this.p)) {
                    if (globalMode == null)
                        ServerCinematics.this.stop(this.p, PathPlaybackStoppedEvent.StopCause.FINISHED);
                    else
                        ServerCinematics.this.stopGlobalUUID(this.u);
                    this.cancel();
                    return;
                }
                if (ServerCinematics.this.isFalse(ServerCinematics.this.playing.get(this.u))) {
                    this.cancel();
                    return;
                }
                this.npoint = false;
                if (globalMode != null && !this.p.isOnline()) {
                    if (getServer().getOnlinePlayers().isEmpty()) return;
                    this.p = getServer().getOnlinePlayers().iterator().next();
                }
                // global mode; all players to gm3
                if (globalMode != null)
                    for (Player ps : getServer().getOnlinePlayers()) {
                        ps.setGameMode(GameMode.SPECTATOR);
                    }
                if (!this.tm)
                    effpos = this.p.getLocation();
                if (this.i == 0) { // at first waypoint?
                    final Location location = this.w.get(0);
                    if (!ServerCinematics.this.improper(this.wy.get(0))) {
                        location.setYaw((float)(double)this.wy.get(0));
                    }
                    if (!ServerCinematics.this.improper(this.wp.get(0))) {
                        location.setPitch((float)(double)this.wp.get(0));
                    }
                    if (globalMode != null)
                        for (Player ps : getServer().getOnlinePlayers()) {
                            ps.teleport(location);
                        }
                    else
                        this.p.teleport(location);
                    if (this.w.size() > 1)
                        this.dist = this.w.get(0).distance((Location)this.w.get(1));
                    else
                        this.dist = 0;
                    ++this.i;
                    //this.ld = -1.0;
                    this.ls = -1.0;
                    this.npoint = true;
                    this.effpos = this.p.getLocation();
                }
                else {
                    try {
                        Location location2 = this.w.get(this.i);
                        Location prevLoc = this.w.get(this.i - 1);
                        Vector targetVec = cloneLocation(location2).subtract(cloneLocation(prevLoc)).toVector();
                        if (this.ws.get(this.i) >= 0.0) {
                            ServerCinematics.this.speed_a.put(this.u, this.ws.get(this.i));
                        }
                        // check radius, depends on speed
                        double n = ServerCinematics.this.speed_a.get(this.u) / 10.0 * M;
                        final Location location3 = effpos;
                        double ld;// = location3.distance(location2);
                        ld = smartDistance(location3, targetVec, location2);
                        //System.out.println("L=" + String.format("%7.3f", ld) + "  P=" + debugFmt(location3) + "  N=" + debugFmt(targetVec) + "  D=" + debugFmt(location2));
                        if (this.ls == -1.0) {
                            this.ls = n;
                        }
                        if (Math.abs(this.ls - n) > 0.5) {
                            n = this.ls + 0.5 * Math.signum(n - this.ls);
                        }
                        /*if (this.ld >= 0.0) {
                            if (this.dc && ld > this.ld) {
                                ld = 0.0;
                            }
                            if (ld < this.ld) {
                                this.dc = true;
                            }
                        }*/
                        if (ld <= n * (i >= w.size() - 1 ? ((wf & 0x10) != 0x0 ? 0.125 : 1) : 2) && totaldelay < 1) {
                            if ((this.wl.get(this.i)&1)>0) {
                                // insta-teleport to next point
                                this.i++;
                                while (this.i < this.wtemp.size() && wtemp.get(this.i)) {
                                    waypointEvent(this.windx.get(i));
                                    this.i++;
                                }
                                if (this.i >= this.wtemp.size()) {
                                    if (!ServerCinematics.this.pl_playing.get(u) && !fplay && owner != null)
                                        this.owner.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "End of path reached.");
                                    waypoints_t.put(this.u, this.ticks_moved * (0.05D * M));
                                    if (this.w.size() > 0)
                                        this.p.teleport(this.w.get(this.w.size() - 1));
                                    if (globalMode == null)
                                        ServerCinematics.this.stop(this.p, false, PathPlaybackStoppedEvent.StopCause.FINISHED);
                                    else
                                        ServerCinematics.this.stopGlobalUUID(this.u);
                                    this.cancel();
                                    return;
                                }
                                Location location5 = this.w.get(this.i);
                                location5.setPitch((float)(double)this.wp.get(this.i));
                                location5.setYaw((float)(double)this.wy.get(this.i));
                                if (globalMode == null) {
                                    this.p.teleport(location5);
                                    // show waypoint message
                                    if (this.wmsg.get(this.i).length() > 0) {
                                        this.p.sendMessage((String)this.wmsg.get(this.i));
                                    }
                                    // run waypoint commands
                                    for (final String wc : this.wcmd.get(this.i)) {
                                        if (wc.startsWith("~")) {
                                            this.p.chat("/" + wc.substring(1));
                                        }
                                        else {
                                            Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), wc.replace("%player%", this.p.getName()));
                                        }
                                    }
                                } else {
                                    for (Player ps: getServer().getOnlinePlayers()) {
                                        // show waypoint message
                                        if (this.wmsg.get(this.i).length() > 0) {
                                            ps.sendMessage((String)this.wmsg.get(this.i));
                                        }
                                        ps.teleport(location5);
                                        // run waypoint commands
                                        for (final String wc : this.wcmd.get(this.i)) {
                                            if (wc.startsWith("~")) {
                                                ps.chat("/" + wc.substring(1));
                                            }
                                            else {
                                                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), wc.replace("%player%", this.p.getName()));
                                            }
                                        }
                                    }
                                }
                                // total delay spent on waypoint
                                this.totaldelay += (int)(this.wd.get(this.i) * 20.0);
                                this.ticks_moved++;
                                return;
                            }
                        }
                        // check margin
                        double margin = n * (i >= w.size() - 1 ? ((wf & 0x10) != 0x0 ? 0.125 : 1) : 2);
                        while (ld <= margin && totaldelay < 1) {
                            ++this.i;
                            if (!this.isCurrent()) {
                                this.cancel();
                                return;
                            }
                            if (this.i >= this.w.size()) {
                                this.end = true;
                                return;
                            }
                            waypointEvent(this.windx.get(i));
                            prevLoc = location2;
                            location2 = this.w.get(this.i);
                            // direction to target
                            targetVec = cloneLocation(location2).subtract(cloneLocation(prevLoc)).toVector();
                            //ld = location3.distance(location2);
                            // new distance
                            ld = smartDistance(location3, targetVec, location2);
                            this.dist = this.w.get(this.i - 1).distance(location2);
                            //this.dc = false;
                            if (this.apoints.contains(this.i))
                                this.npoint = true;
                            this.oy = this.p.getLocation().getYaw();
                            this.op = this.p.getLocation().getPitch();
                            this.yaw = !ServerCinematics.this.improper(this.wy.get(this.i));
                            if (this.yaw) {
                                this.fy = this.optimal(this.oy, this.wy.get(this.i));
                                //System.out.println("Optimal yaw: " + this.fy);
                                //Bukkit.broadcastMessage("Y: " + this.oy + " -> " + this.fy);
                            }
                            this.pitch = !ServerCinematics.this.improper(this.wp.get(this.i));
                            if (this.pitch) {
                                this.fp = this.optimal(this.op, this.wp.get(this.i));
                            }
                            // show message
                            if (this.wmsg.get(this.i).length() > 0) {
                                if (globalMode == null)
                                    this.p.sendMessage((String)this.wmsg.get(this.i));
                                else
                                    for (Player ps: getServer().getOnlinePlayers()) {
                                        ps.sendMessage((String)this.wmsg.get(this.i));
                                    }
                            }
                            // run commands
                            if (globalMode == null)
                                for (final String wc : this.wcmd.get(this.i)) {
                                    if (wc.startsWith("~")) {
                                        this.p.chat("/" + wc.substring(1));
                                    }
                                    else {
                                        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), wc.replace("%player%", this.p.getName()));
                                    }
                                }
                            else
                                for (Player ps: getServer().getOnlinePlayers()) {
                                    for (final String wc : this.wcmd.get(this.i)) {
                                        if (wc.startsWith("~")) {
                                            ps.chat("/" + wc.substring(1));
                                        }
                                        else {
                                            Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), wc.replace("%player%", ps.getName()));
                                        }
                                    }
                                }
                            // total delay on waypoint
                            this.totaldelay += (int)(this.wd.get(this.i) * 20.0);
                        }
                        double min = Math.min(ld, n);
                        if ((wf & 0x8) != 0x0) {
                            if (ticks_moved < 20) {
                                // smoothe starting velocity
                                min *= Math.min(1, ticks_moved / 20.0 * M);
                            }
                        }
                        if ((wf & 0x10) != 0x0) {
                            // try to estimate number of ticks remaining
                            int remainingWaypoints = this.w.size() - this.i;
                            if (remainingWaypoints < 15) {
                                double totalDist = 0;
                                for (int j = i; j < w.size() - 1; ++j) {
                                    totalDist += w.get(j).distance(w.get(j + 1)); 
                                }
                                totalDist += effpos.distance(w.get(i));
                                int ticksLeft = (int)Math.ceil(totalDist / n);
                                if (ticksLeft < 8) {
                                    // smoothe starting velocity
                                    min *= Math.min(1, Math.max(0.25, ticksLeft / 8.0 * M));
                                }
                            }
                        }
                        this.xm = min * ((location2.getX() - location3.getX()) / ld);
                        this.ym = min * ((location2.getY() - location3.getY()) / ld);
                        this.zm = min * ((location2.getZ() - location3.getZ()) / ld);
                        // magic number that was decided through trial-and-error
                        // trying to adjust for vertical momentum being less than horizontal momentum
                        this.ym *= 1.4678;
                        if (this.totaldelay > 0) {
                            --this.totaldelay;
                            this.ticks_moved++;
                            //this.ld = ld;
                            this.ls = n;
                            if (this.tm && (this.wf & 0x4) == 0) {
                                if (globalMode == null)
                                    this.p.teleport(this.p.getLocation());
                                else
                                    for (Player ps: getServer().getOnlinePlayers()) {
                                        ps.teleport(this.p.getLocation());
                                    }
                            }
                            return;
                        }
                        if (!this.isCurrent()) {
                            this.cancel();
                            return;
                        }
                        if (globalMode != null) {
                            for (final Player ps: tempJoins) {
                                ps.teleport(p);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (ps.getLocation().distanceSquared(p.getLocation()) < 0.01) {
                                            this.cancel();
                                            return;
                                        }
                                        try { 
                                            ps.teleport(p); 
                                        } catch (Exception ex) { 
                                            this.cancel();
                                            return;
                                        }
                                    }
                                }.runTaskTimer(ServerCinematics.this, 20L, 20L);
                            }
                            tempJoins.clear();
                        }
                        if (this.tm) { // tpmode
                            final Location location4 = this.p.getLocation();
                            this.q = (-(location4.distance((Location)this.w.get(this.i)) - margin) + this.dist) / this.dist;
                            //this.q = (this.dist - location4.distance(location2)) / this.dist;
                            this.q = Math.min(1, Math.max(0, this.q));
                            if (this.yaw) {
                                location4.setYaw((float)ServerCinematics.this.formatAngleYaw(this.oy + this.fy * this.q));
                            }
                            if (this.pitch) {
                                location4.setPitch((float)ServerCinematics.this.formatAnglePitch(this.op + this.fp * this.q));
                            }
                            location4.add(this.xm, this.ym, this.zm);
                            effpos = location4;
                            if (this.nopath) {
                                if (this.npoint) {
                                    int ind = Math.max(0, this.i);
                                    Location loc5 = cloneLocation(this.w.get(ind));
                                    if (this.yaw) loc5.setYaw((float)(double)this.wy.get(ind));
                                    else loc5.setYaw(effpos.getYaw());
                                    if (this.pitch) loc5.setPitch((float)(double)this.wp.get(ind));
                                    else loc5.setPitch(effpos.getPitch());
                                    if (globalMode == null)
                                        this.p.teleport(loc5);
                                    else
                                        for (Player ps: getServer().getOnlinePlayers()) {
                                            ps.teleport(loc5);
                                        }
                                    this.npoint = false;
                                }
                            } else {
                                if (globalMode == null)
                                    this.p.teleport(location4);
                                else
                                    for (Player ps: getServer().getOnlinePlayers()) {
                                        ps.teleport(location4);
                                    }
                            }
                        }
                        else { // use velocity instead of teleport
                            if (globalMode == null)
                                this.p.setVelocity(new Vector(this.xm, this.ym, this.zm));
                            else
                                for (Player ps: getServer().getOnlinePlayers()) {
                                    ps.setVelocity(new Vector(this.xm, this.ym, this.zm));
                                }
                        }
                        this.ticks_moved++;
                        //this.ld = ld;
                        this.ls = n;
                    }
                    catch (Exception ex) {
                        if (!this.isCurrent()) {
                            this.cancel();
                            return;
                        }
                        if (owner != null)
                            this.owner.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "An error occurred during play. See the console.");
                        this.cancel();
                        if (globalMode == null) ServerCinematics.this.stop(this.p, PathPlaybackStoppedEvent.StopCause.FINISHED);
                        else ServerCinematics.this.stopGlobalUUID(this.u);
                        ex.printStackTrace();
                    }
                }
            }

            private double smartDistance(Location p, Vector v, Location dest) {
                if (v.lengthSquared() < 0.0001) {
                    return p.distance(dest);
                }
                // distance of p from a plane on which dest is and that v is a normal to
                // use Hessian plane formula to compute distance
                Vector vm = v.normalize();
                return vm.dot(dest.toVector()) - vm.dot(p.toVector());
            }
            
            // do waypoint reached event
            private void waypointEvent(int i) {
                if (i >= 0) {
                    WaypointReachedEvent evt = new WaypointReachedEvent(p, getPlayingPath(p), i, waypoints.get(p.getUniqueId()).size());
                    getServer().getPluginManager().callEvent(evt);
                }
            }

            // really unoptimized way to get the best direction to turn between two degree angles
            private double optimal(final double n, final double n2) {
                double o = n2 - n;
                while (o < -180) {
                    o += 360;
                }
                while (o > 180) {
                    o -= 360;
                }
                return o;
            }
            
/*
            private double optimal(final double n, final double n2) {
                return optimalRaw(n + 180, n2 + 180);
            }

            private double optimalRaw(final double n, final double n2) {
                final double n3 = 180.0 - Math.abs(Math.abs(n - n2 + 360.0) - 180.0);
                if (180.0 - Math.abs(Math.abs(n - n2 + 361.0) - 180.0) > n3) {
                    return -n3;
                }
                return n3;
            }*/
        }.runTaskTimer((Plugin)this, 1L, 1L);
        return id;
    }
    
    protected Location cloneLocation(Location location) {
        return new Location(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    double formatAngleYaw(double n) {
        double v = (n + 180.0) % 360.0;
        if (v < 0.0) {
            v += 360.0;
        }
        return v - 180.0;
    }
    double formatAnglePitch(double n) {
        double v = (n + 90.0) % 180.0;
        if (v < 0.0) {
            v += 180.0;
        }
        return v - 90.0;
    }
    
    protected String fts(final double n) {
        return Double.toString(Math.floor(n * 1000.0) / 1000.0);
    }
    
    private boolean isFalse(final Boolean b) {
        return !this.isTrue(b);
    }
    
    private boolean isTrue(final Boolean b) {
        return b != null && b;
    }
    
    private ArrayList<Location> getSafeWaypoints(final Player player) {
        if (this.waypoints.get(player.getUniqueId()) == null) {
            this.waypoints.put(player.getUniqueId(), new ArrayList<Location>());
        }
        return this.waypoints.get(player.getUniqueId());
    }
    
    private ArrayList<Location> getSafeWaypoints(final CommandSender commandSender) {
        return this.getSafeWaypoints((Player)commandSender);
    }
    
    private ArrayList<Double> getSafeWaypointSpeeds(final Player player) {
        if (this.waypoints_s.get(player.getUniqueId()) == null) {
            this.waypoints_s.put(player.getUniqueId(), new ArrayList<Double>());
        }
        return this.waypoints_s.get(player.getUniqueId());
    }
    
    private ArrayList<Double> getSafeWaypointPitch(final Player player) {
        if (this.waypoints_p.get(player.getUniqueId()) == null) {
            this.waypoints_p.put(player.getUniqueId(), new ArrayList<Double>());
        }
        return this.waypoints_p.get(player.getUniqueId());
    }
    
    private ArrayList<Double> getSafeWaypointYaw(final Player player) {
        if (this.waypoints_y.get(player.getUniqueId()) == null) {
            this.waypoints_y.put(player.getUniqueId(), new ArrayList<Double>());
        }
        return this.waypoints_y.get(player.getUniqueId());
    }
    
    private ArrayList<String> getSafeWaypointMessages(final Player player) {
        if (this.waypoints_m.get(player.getUniqueId()) == null) {
            this.waypoints_m.put(player.getUniqueId(), new ArrayList<String>());
        }
        return this.waypoints_m.get(player.getUniqueId());
    }
    
    private ArrayList<List<String>> getSafeWaypointCommands(final Player player) {
        if (this.waypoints_c.get(player.getUniqueId()) == null) {
            this.waypoints_c.put(player.getUniqueId(), new ArrayList<List<String>>());
        }
        return this.waypoints_c.get(player.getUniqueId());
    }
    
    private ArrayList<Double> getSafeWaypointDelays(final Player player) {
        if (this.waypoints_d.get(player.getUniqueId()) == null) {
            this.waypoints_d.put(player.getUniqueId(), new ArrayList<Double>());
        }
        return this.waypoints_d.get(player.getUniqueId());
    }
    
    private ArrayList<Integer> getSafeWaypointOptions(final Player player) {
        if (this.waypoints_l.get(player.getUniqueId()) == null) {
            this.waypoints_l.put(player.getUniqueId(), new ArrayList<Integer>());
        }
        return this.waypoints_l.get(player.getUniqueId());
    }
    
    private ArrayList<Boolean> getSafeWaypointInstants(final Player player) {
        if (this.waypoints_i.get(player.getUniqueId()) == null) {
            this.waypoints_i.put(player.getUniqueId(), new ArrayList<Boolean>());
        }
        return this.waypoints_i.get(player.getUniqueId());
    }
    
    private int getSafeWaypointFlags(final Player player) {
        if (!this.waypoints_f.containsKey(player.getUniqueId())) {
            this.waypoints_f.put(player.getUniqueId(), 0);
        }
        return this.waypoints_f.get(player.getUniqueId());
    }

    
    private ArrayList<Location> getSafeWaypoints(final UUID u) {
        if (this.waypoints.get(u) == null) {
            this.waypoints.put(u, new ArrayList<Location>());
        }
        return this.waypoints.get(u);
    }
    
    private ArrayList<Double> getSafeWaypointSpeeds(final UUID u) {
        if (this.waypoints_s.get(u) == null) {
            this.waypoints_s.put(u, new ArrayList<Double>());
        }
        return this.waypoints_s.get(u);
    }
    
    private ArrayList<Double> getSafeWaypointPitch(final UUID u) {
        if (this.waypoints_p.get(u) == null) {
            this.waypoints_p.put(u, new ArrayList<Double>());
        }
        return this.waypoints_p.get(u);
    }
    
    private ArrayList<Double> getSafeWaypointYaw(final UUID u) {
        if (this.waypoints_y.get(u) == null) {
            this.waypoints_y.put(u, new ArrayList<Double>());
        }
        return this.waypoints_y.get(u);
    }
    
    private ArrayList<String> getSafeWaypointMessages(final UUID u) {
        if (this.waypoints_m.get(u) == null) {
            this.waypoints_m.put(u, new ArrayList<String>());
        }
        return this.waypoints_m.get(u);
    }
    
    private ArrayList<List<String>> getSafeWaypointCommands(final UUID u) {
        if (this.waypoints_c.get(u) == null) {
            this.waypoints_c.put(u, new ArrayList<List<String>>());
        }
        return this.waypoints_c.get(u);
    }
    
    /*private ArrayList<Double> getSafeWaypointDelays(final UUID u) {
        if (this.waypoints_d.get(u) == null) {
            this.waypoints_d.put(u, new ArrayList<Double>());
        }
        return this.waypoints_d.get(u);
    }
    
    private ArrayList<Integer> getSafeWaypointOptions(final UUID u) {
        if (this.waypoints_l.get(u) == null) {
            this.waypoints_l.put(u, new ArrayList<Integer>());
        }
        return this.waypoints_l.get(u);
    }*/
    
    private int getSafeWaypointFlags(final UUID u) {
        if (!this.waypoints_f.containsKey(u)) {
            this.waypoints_f.put(u, 0);
        }
        return this.waypoints_f.get(u);
    }
    
    private ArrayList<Double> getSafeWaypointSpeeds(final CommandSender commandSender) {
        return this.getSafeWaypointSpeeds((Player)commandSender);
    }
    
    private ArrayList<Double> getSafeWaypointPitch(final CommandSender commandSender) {
        return this.getSafeWaypointPitch((Player)commandSender);
    }
    
    private ArrayList<Double> getSafeWaypointYaw(final CommandSender commandSender) {
        return this.getSafeWaypointYaw((Player)commandSender);
    }
    
    private ArrayList<String> getSafeWaypointMessages(final CommandSender commandSender) {
        return this.getSafeWaypointMessages((Player)commandSender);
    }
    
    private ArrayList<List<String>> getSafeWaypointCommands(final CommandSender commandSender) {
        return this.getSafeWaypointCommands((Player)commandSender);
    }
    
    private ArrayList<Integer> getSafeWaypointOptions(final CommandSender commandSender) {
        return this.getSafeWaypointOptions((Player)commandSender);
    }

    private ArrayList<Boolean> getSafeWaypointInstants(CommandSender commandSender) {
        return this.getSafeWaypointInstants((Player)commandSender);
    }
    
    private ArrayList<Double> getSafeWaypointDelays(final CommandSender commandSender) {
        return this.getSafeWaypointDelays((Player)commandSender);
    }
    
    private void clearCache(final Player player) {
        this.wm.put(player.getUniqueId(), false);
        this.waypoints_t.put(player.getUniqueId(), -1.0);
    }
    
    private void clearPathName(final Player player) {
        this.pathnames.put(player.getUniqueId(), MODIFIED_PATH);
    }
    
    private boolean hasCache(final Player player) {
        return this.getCache(player) != null && this.getIsCacheUpToDate(player);
    }
    
    private boolean getIsCacheUpToDate(final Player player) {
        final Boolean b = this.wm.get(player.getUniqueId());
        return b != null && b;
    }
    
    private ArrayList<Location> getCache(final Player player) {
        return this.wx.get(player.getUniqueId());
    }
    
    protected String lts(final Location location) {
        return "(" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + ")";
    }
    
    protected String lts2(final Location location) {
        return "(" + this.fts(location.getX()) + "," + this.fts(location.getY()) + "," + this.fts(location.getZ()) + ")";
    }
    
    // three-dimensional Catmull-Rom spline point computation
    static Location catmull_rom_3d(final double n, final Location location, final Location location2, final Location location3, final Location location4) {
        if (n > 1.0 || n < 0.0) {
            throw new IllegalArgumentException("s must be between 0.0 and 1.0 (was: " + Double.toString(n) + ")");
        }
        double x1 = location.getX();
        double y1 = location.getY();
        double z1 = location.getZ();
        double x2 = location2.getX();
        double y2 = location2.getY();
        double z2 = location2.getZ();
        double x3 = location3.getX();
        double y3 = location3.getY();
        double z3 = location3.getZ();
        double x4 = location4.getX();
        double y4 = location4.getY();
        double z4 = location4.getZ();
        return new Location(location.getWorld(), 0.5 * (2.0 * x2 + (x3 - x1) * n + (2.0 * x1 - 5.0 * x2 + 4.0 * x3 - x4) * n * n + (x4 - 3.0 * x3 + 3.0 * x2 - x1) * n * n * n), 0.5 * (2.0 * y2 + (y3 - y1) * n + (2.0 * y1 - 5.0 * y2 + 4.0 * y3 - y4) * n * n + (y4 - 3.0 * y3 + 3.0 * y2 - y1) * n * n * n), 0.5 * (2.0 * z2 + (z3 - z1) * n + (2.0 * z1 - 5.0 * z2 + 4.0 * z3 - z4) * n * n + (z4 - 3.0 * z3 + 3.0 * z2 - z1) * n * n * n));
    }

    private void stop(final Player player, final PathPlaybackStoppedEvent.StopCause cause) {
        stop(player, true, cause);
    }
    private void stop(final Player player, boolean noPlaylist, final PathPlaybackStoppedEvent.StopCause cause) {
        final UUID uniqueId = player.getUniqueId();
        if (noPlaylist)
            pl_playing.put(uniqueId, false);
        boolean wasPlaying = isTrue(this.playing.get(uniqueId));
        this.paused.put(uniqueId, false);
        this.playing.put(uniqueId, false);
        try {
            player.setGameMode((GameMode)this.old_gm.get(uniqueId));
        }
        catch (Exception ex) {}
        try {
            player.setFlySpeed((float)this.old_fsp.get(uniqueId));
        }
        catch (Exception ex) {}
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        if ((this.getSafeWaypointFlags(player) & 0x1) != 0x0) {
            player.teleport((Location)this.getSafeWaypoints(player).get(0));
        }
        if ((this.getSafeWaypointFlags(player) & 0x2) != 0x0) {
            player.teleport((Location)this.old_loc.get(uniqueId));
        }
        else {
            try {
                player.setAllowFlight((boolean)this.old_af.get(uniqueId));
            }
            catch (Exception ex2) {}
            try {
                player.setFlying((boolean)this.old_f.get(uniqueId));
            }
            catch (Exception ex3) {}
        }
        if (wasPlaying) {
            PathPlaybackStoppedEvent evt = new PathPlaybackStoppedEvent(player, getPlayingPath(player), cause, pbids.get(player.getUniqueId()));
            getServer().getPluginManager().callEvent(evt);
        }
        if (pl_playing.get(uniqueId)) {
            if (!this.findNextSuitablePath(player)) {
                if (pl_looping.get(uniqueId))
                    player.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "No more playable paths found.");
                else
                    player.sendMessage((short_prefix ? "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('7') + "]" : "" + make_color('7') + "[" + make_color('8') + "" + make_color('l') + "[**]<| " + make_color('e') + "Server" + make_color('6') + "Cinematics" + make_color('7') + "]") + " " + make_color('c') + "End of playlist.");
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    ServerCinematics.this.play(player, player, false, StartCause.PLAYLIST);
                }
            }.runTaskLater(this, 1L);
        }
    }
    
    private static String make_color(char c) {
        return ChatColor.getByChar(c).toString();
    }

    private void stopGlobalUUID(final UUID uniqueId) {
        this.paused.put(uniqueId, false);
        this.playing.put(uniqueId, false);
        if (pl_playing.get(uniqueId)) {
            if (!this.findNextSuitablePath(uniqueId)) {
                stopGlobal();
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    Player p = getServer().getOnlinePlayers().iterator().next();
                    ServerCinematics.this.playWithFakeUUID(p, p, uniqueId, false, StartCause.PLAYLIST);
                }
            }.runTaskLater(this, 1L);
        }
    }
    
    /*private void sendMultilineMessage(final CommandSender commandSender, final String s) {
        this.sendMultilineMessage(commandSender, s, "");
    }*/
    String lastWorld = "";
    private boolean findNextSuitablePath(Player player) {
        UUID u = player.getUniqueId();
        int old_index = pl_index.get(u);
        int check_index = old_index;
        ArrayList<String> list = pl_paths.get(u);
        int length = list.size();
        boolean double_loop = false;
        while (true) {
            if (++check_index == old_index) {
                // no paths;
                return false;
            }
            if (check_index == length) {
                if (pl_looping.get(u)) {
                    if (double_loop) return false;
                    double_loop = true;
                    check_index = -1;
                    continue;
                } else
                    return false;
            } 
            if (this.loadForPlaylist(player, list.get(check_index), true) > 0) {
                pl_index.put(u, check_index);
                return true;
            }
        }
    }
    private boolean findNextSuitablePath(UUID u) {
        int old_index = pl_index.get(u);
        int check_index = old_index;
        ArrayList<String> list = pl_paths.get(u);
        int length = list.size();
        boolean double_loop = false;
        while (true) {
            if (++check_index == old_index) {
                // no paths;
                return false;
            }
            if (check_index == length) {
                if (pl_looping.get(u)) {
                    if (double_loop) return false;
                    double_loop = true;
                    check_index = -1;
                    continue;
                } else
                    return false;
            } 
            if (this.loadForPlaylist(u, list.get(check_index)) > 0) {
                pl_index.put(u, check_index);
                return true;
            }
        }
    }

    private int loadForPlaylist(Player player, String string, boolean teleport) {
        int res = loadForPlaylist(player.getUniqueId(), string);
        if (res > 0 && teleport) {
            try {
                player.teleport(this.getSafeWaypoints(player).get(0));
            } catch (Exception ex) {
                return -1;
            }
        }
        return res;
    }

    private int loadForPlaylist(UUID u, String string) {
        try {
            final File file3 = new File(this.paths, string);
            if (!file3.isFile()) {
                return -2;
            }
            final String s8 = new String(Files.readAllBytes(file3.toPath()), StandardCharsets.UTF_8);
            int n11 = 0;
            this.clearCache(u);
            this.waypoints.put(u, new ArrayList<Location>());
            this.waypoints_s.put(u, new ArrayList<Double>());
            this.waypoints_y.put(u, new ArrayList<Double>());
            this.waypoints_p.put(u, new ArrayList<Double>());
            this.waypoints_m.put(u, new ArrayList<String>());
            this.waypoints_c.put(u, new ArrayList<List<String>>());
            this.waypoints_f.put(u, 0);
            this.waypoints_t.put(u, -1.0);
            this.waypoints_d.put(u, new ArrayList<Double>());
            this.waypoints_l.put(u, new ArrayList<Integer>());
            this.waypoints_i.put(u, new ArrayList<Boolean>());
            final ArrayList<Location> safeWaypoints7 = this.getSafeWaypoints(u);
            final ArrayList<Double> safeWaypointSpeeds7 = this.getSafeWaypointSpeeds(u);
            final ArrayList<Double> safeWaypointYaw9 = this.getSafeWaypointYaw(u);
            final ArrayList<Double> safeWaypointPitch9 = this.getSafeWaypointPitch(u);
            final ArrayList<String> safeWaypointMessages3 = this.getSafeWaypointMessages(u);
            final ArrayList<List<String>> safeWaypointCommands3 = this.getSafeWaypointCommands(u);
            //final World world3 = player4.getWorld();
            final String s9 = s8.split("#")[0];
            final String s10 = s8.split("#")[1];
            final float float3 = Float.parseFloat(s9.split(",")[2]);
            final float float4 = Float.parseFloat(s9.split(",")[3]);
            int n12 = 0;
            int safeFlags2 = 0;
            this.pathnames.put(u, string);
            if (s9.split(",").length > 4) {
                safeFlags2 = Integer.parseInt(s9.split(",")[4]);
            }
            if (Bukkit.getServer().getWorld(s9.split(",")[0]) == null) {
                return -1;
            }
            double safeTime = -1;
            if (s9.split(",").length > 5) {
                safeTime = Double.parseDouble(s9.split(",")[5]);
            }
            final World aworld = Bukkit.getServer().getWorld(s9.split(",")[0]);
            String[] split6;
            for (int length4 = (split6 = s10.split(Pattern.quote("|"))).length, n13 = 0; n13 < length4; ++n13) {
                final String s11 = split6[n13];
                try {
                    final String[] split7 = s11.split(",");
                    final Location location6 = new Location(aworld, Double.parseDouble(split7[0]), Double.parseDouble(split7[1]), Double.parseDouble(split7[2].split(";")[0]));
                    if (n12 == 0) {
                        location6.setYaw(float3);
                        location6.setPitch(float4);
                    }
                    safeWaypoints7.add(location6);
                    safeWaypointCommands3.add(new ArrayList<String>());
                    if (split7[3].indexOf(10) >= 0) {
                        safeWaypointSpeeds7.add(Double.parseDouble(split7[3].split("\n")[0]));
                        int n14 = 0;
                        String[] split8;
                        for (int length5 = (split8 = split7[3].split("\n")).length, n15 = 0; n15 < length5; ++n15) {
                            final String s12 = split8[n15];
                            if (n14++ >= 1) {
                                safeWaypointCommands3.get(n12).add(s12.replace("\uf555", ","));
                            }
                        }
                    }
                    else {
                        safeWaypointSpeeds7.add(Double.parseDouble(split7[3]));
                    }
                    try {
                        if (split7[2].split(";").length > 3) {
                            this.waypoints_i.get(u).add(!split7[2].split(";")[3].equalsIgnoreCase("0"));
                        } else {
                            this.waypoints_i.get(u).add(false);
                        }
                        if (split7[2].split(";").length < 2) {
                            throw new ArrayIndexOutOfBoundsException();
                        }
                        double d = Double.parseDouble(split7[2].split(";")[1]);
                        int lf = Integer.parseInt(split7[2].split(";")[2]);
                        this.waypoints_d.get(u).add(d);
                        this.waypoints_l.get(u).add(lf);
                    } catch (Exception ex) {
                        this.waypoints_d.get(u).add(0.0);
                        this.waypoints_l.get(u).add(0);
                    }
                    if (split7.length > 4) {
                        final String[] split9 = split7[4].split(":");
                        final String[] split10 = split9[1].split("\\$", 2);
                        if (split10.length > 1) {
                            safeWaypointMessages3.add(split10[1].replace("\uf555", ","));
                        }
                        else {
                            safeWaypointMessages3.add("");
                        }
                        safeWaypointYaw9.add(this.formatAngleYaw(Double.parseDouble(split9[0])));
                        safeWaypointPitch9.add(this.formatAnglePitch(Double.parseDouble(split10[0])));
                    }
                    else {
                        safeWaypointYaw9.add(444.0);
                        safeWaypointPitch9.add(444.0);
                    }
                    ++n12;
                }
                catch (Exception ex15) {
                    if (safeWaypointYaw9.size() > safeWaypointPitch9.size()) {
                        safeWaypointYaw9.remove(safeWaypointYaw9.size() - 1);
                    }
                    if (safeWaypointMessages3.size() > safeWaypointYaw9.size()) {
                        safeWaypointMessages3.remove(safeWaypointMessages3.size() - 1);
                    }
                    if (safeWaypointSpeeds7.size() > safeWaypointYaw9.size()) {
                        safeWaypointSpeeds7.remove(safeWaypointSpeeds7.size() - 1);
                    }
                    if (safeWaypoints7.size() > safeWaypointSpeeds7.size()) {
                        safeWaypoints7.remove(safeWaypoints7.size() - 1);
                    }
                    if (safeWaypointCommands3.size() > safeWaypoints7.size()) {
                        safeWaypointCommands3.remove(safeWaypointCommands3.size() - 1);
                    }
                    ++n11;
                }
            }
            this.waypoints.put(u, safeWaypoints7);
            this.waypoints_s.put(u, safeWaypointSpeeds7);
            this.waypoints_y.put(u, safeWaypointYaw9);
            this.waypoints_p.put(u, safeWaypointPitch9);
            this.waypoints_m.put(u, safeWaypointMessages3);
            this.waypoints_c.put(u, safeWaypointCommands3);
            this.waypoints_f.put(u, safeFlags2);
            this.waypoints_t.put(u, safeTime);
            this.speed.put(u, Double.parseDouble(s9.split(",")[1]));
            if (safeWaypoints7.size() == 0)
                return 0;
            lastWorld = s9.split(",")[0];
            return n12 - n11;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    
    private void deserializePath(Player player, CinematicPath path) {
        final UUID u = player.getUniqueId();
        this.waypoints.put(u, new ArrayList<Location>());
        this.waypoints_s.put(u, new ArrayList<Double>());
        this.waypoints_y.put(u, new ArrayList<Double>());
        this.waypoints_p.put(u, new ArrayList<Double>());
        this.waypoints_m.put(u, new ArrayList<String>());
        this.waypoints_c.put(u, new ArrayList<List<String>>());
        this.waypoints_d.put(u, new ArrayList<Double>());
        this.waypoints_l.put(u, new ArrayList<Integer>());
        this.waypoints_i.put(u, new ArrayList<Boolean>());
        final ArrayList<Location> safeWaypoints7 = this.getSafeWaypoints(u);
        final ArrayList<Double> safeWaypointSpeeds7 = this.getSafeWaypointSpeeds(u);
        final ArrayList<Double> safeWaypointYaw9 = this.getSafeWaypointYaw(u);
        final ArrayList<Double> safeWaypointPitch9 = this.getSafeWaypointPitch(u);
        final ArrayList<String> safeWaypointMessages3 = this.getSafeWaypointMessages(u);
        final ArrayList<List<String>> safeWaypointCommands3 = this.getSafeWaypointCommands(u);
        final ArrayList<Double> safeWaypointDelays = this.getSafeWaypointDelays(player);
        final ArrayList<Integer> safeWaypointOptions = this.getSafeWaypointOptions(player);
        final ArrayList<Boolean> safeWaypointInstants = this.getSafeWaypointInstants(player);

        this.waypoints_f.put(u, (path.shouldTeleportToStartAfterPlayback() ? 1 : 0)
                              | (path.shouldTeleportBackAfterPlayback() ? 2 : 0)
                              | (path.canPlayerTurnCameraDuringDelay() ? 4 : 0));
        this.waypoints_t.put(u, -1.0);
        
        for (CinematicWaypoint wp: path.getWaypoints()) {
            double speed = (wp.getSpeed() == null ? -1 : wp.getSpeed().doubleValue());
            double yaw = (wp.getYaw() == null ? 444 : wrapAngle(wp.getYaw().doubleValue()));
            double pitch = (wp.getPitch() == null ? 444 : wrapAngle(wp.getPitch().doubleValue()));
            
            if (improper(yaw)) yaw = 444;
            if (improper(pitch)) pitch = 444;
            
            safeWaypoints7.add(wp.getLocation());
            safeWaypointSpeeds7.add(speed);
            safeWaypointYaw9.add(yaw);
            safeWaypointPitch9.add(pitch);
            safeWaypointMessages3.add(Objects.toString(wp.getMessage(), ""));
            safeWaypointCommands3.add(wp.getCommands());
            safeWaypointDelays.add(wp.getDelay());
            safeWaypointOptions.add((wp.isInstant() ? 1 : 0));
            safeWaypointInstants.add(false);
        }

        this.pathnames.put(u, "");
    }

    private double wrapAngle(double a) {
        if (Double.isFinite(a)) {
            return ((((a + 180.0) % 360.0) + 360.0) % 360.0) - 180.0;
        } else {
            return a;
        }
    }

    private void clearCache(final UUID u) {
        this.wm.put(u, false);
        this.waypoints_t.put(u, -1.0);
    }

    private void sendMultilineMessage(final CommandSender commandSender, final String s, final String s2) {
        String[] split;
        for (int length = (split = s.split("\\r?\\n")).length, i = 0; i < length; ++i) {
            commandSender.sendMessage(String.valueOf(String.valueOf(s2)) + split[i]);
        }
    }
    
    boolean improper(final double n) {
        return Math.abs(n) > 360.0 || !Double.isFinite(n);
    }
    
    double properYaw(double n) {
        if (n < -180)
            n += 360;
        if (n > 180)
            n -= 360;
        return n;
    }

    private void clipSuggestions(List<String> suggestions, String inp) {
        Iterator<String> it = suggestions.iterator();
        while (it.hasNext()) {
            String sugg = it.next();
            if (!sugg.startsWith(inp)) {
                it.remove();
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("cam") || cmd.getName().equals("camera")) {
            if (args.length == 1) {
                List<String> suggestions = new ArrayList<>();

                if (sender instanceof Player) {
                    suggestions.add("help");
                    if (sender.hasPermission("servercinematics.play")) {
                        suggestions.add("tpmode");
                        suggestions.add("pathless");
                        suggestions.add("playlist");
                        suggestions.add("resume");
                        suggestions.add("pause");
                        suggestions.add("play");
                        suggestions.add("tplay");
                        suggestions.add("stop");
                    }
                    if (sender.hasPermission("servercinematics.edit")) {
                        suggestions.add("list");
                        suggestions.add("delay");
                        suggestions.add("option");
                        suggestions.add("flags");
                        suggestions.add("flag");
                        suggestions.add("reload");
                        suggestions.add("add");
                        suggestions.add("cmd");
                        suggestions.add("msg");
                        suggestions.add("insert");
                        suggestions.add("edit");
                        suggestions.add("speed");
                        suggestions.add("goto");
                        suggestions.add("remove");
                        suggestions.add("clone");
                        suggestions.add("load");
                        suggestions.add("save");
                        suggestions.add("clear");
                    }
                    if (sender.hasPermission("servercinematics.fplay")) {
                        suggestions.add("fclear");
                        suggestions.add("fload");
                        suggestions.add("fplay");
                        suggestions.add("fstop");
                        suggestions.add("ftplay");
                    }
                } else {
                    suggestions.add("fplay");
                    suggestions.add("fstop");
                    suggestions.add("fclear");
                    suggestions.add("fload");
                    suggestions.add("ftplay");
                }

                clipSuggestions(suggestions, args[0]);
                
                return suggestions;
            } else if (args.length == 2) {
                List<String> suggestions = new ArrayList<>();
                boolean returnEmpty = false;

                if (args[0].equalsIgnoreCase("delay")
                        || args[0].equalsIgnoreCase("option")
                        || args[0].equalsIgnoreCase("edit")
                        || args[0].equalsIgnoreCase("goto")
                        || args[0].equalsIgnoreCase("cmd")
                        || args[0].equalsIgnoreCase("msg")) {
                    returnEmpty = true;
                    int maxLen = this.getSafeWaypoints(sender).size();
                    for (int i = 0; i < maxLen; ++i) {
                        String n = Integer.toString(i);
                        if (n.startsWith(args[1])) {
                            suggestions.add(n);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("fclear")
                        || args[0].equalsIgnoreCase("fload")
                        || args[0].equalsIgnoreCase("fplay")
                        || args[0].equalsIgnoreCase("fstop")
                        || args[0].equalsIgnoreCase("ftplay")
                        || args[0].equalsIgnoreCase("clone")) {
                    returnEmpty = true;
                    for (Player p: getServer().getOnlinePlayers()) {
                        String n = p.getName();
                        if (n.toLowerCase().startsWith(args[1].toLowerCase())) {
                            suggestions.add(n);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("playlist")) {
                    suggestions.add("add");
                    suggestions.add("insert");
                    suggestions.add("remove");
                    suggestions.add("list");
                    suggestions.add("clear");
                    suggestions.add("play");
                    suggestions.add("loop");
                }

                clipSuggestions(suggestions, args[1]);
                
                if (returnEmpty || suggestions.size() > 0)
                    return suggestions;
                else
                    return null;
            } else if (args.length == 3) {
                List<String> suggestions = new ArrayList<>();

                if (args[0].equalsIgnoreCase("cmd")) {
                    suggestions.add("list");
                    suggestions.add("add");
                    suggestions.add("get");
                    suggestions.add("remove");
                } else if (args[0].equalsIgnoreCase("msg")) {
                    suggestions.add("set");
                    suggestions.add("setcolored");
                    suggestions.add("remove");
                }
                
                clipSuggestions(suggestions, args[2]);

                if (suggestions.size() > 0)
                    return suggestions;
                else
                    return null;
            }
        }
        return null;
    }
}
