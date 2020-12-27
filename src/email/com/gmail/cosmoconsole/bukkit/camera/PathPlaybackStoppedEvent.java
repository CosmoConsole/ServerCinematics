package email.com.gmail.cosmoconsole.bukkit.camera;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that triggers when a player stops playing a path. Includes the player and the path name (may be MODIFIED_PATH).
 */
public class PathPlaybackStoppedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String path;
    private final StopCause cause;
    private final long id;
    
    PathPlaybackStoppedEvent(Player player, String path, StopCause cause, long id) {
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
    
    public StopCause getCause() {
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
    
    enum StopCause {
        FINISHED, LEFT, MANUAL, FSTOP, PLUGIN;
    }

}
