package email.com.gmail.cosmoconsole.bukkit.camera;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that triggers when a player starts playing a path. Includes the player and the path name (may be MODIFIED_PATH).
 */
public class PathPlaybackStartedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String path;
    private final StartCause cause;
    private final long id;
    
    PathPlaybackStartedEvent(Player player, String path, StartCause cause, long id) {
        this.player = player;
        this.path = path;
        this.cause = cause;
        this.id = id;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public String getPathName() {
        return this.path;
    }
    
    public StartCause getCause() {
        return this.cause;
    }
    
    public long getId() {
        return this.id;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    enum StartCause {
        MANUAL, PLAYLIST, FPLAY, PLUGIN;
    }

}
