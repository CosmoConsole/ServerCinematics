package email.com.gmail.cosmoconsole.bukkit.camera;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ServerCinematics path.
 */
public class CinematicPath {
    private List<CinematicWaypoint> p;
    private boolean flag_0;
    private boolean flag_1;
    private boolean flag_2;
    
    public CinematicPath() {
        p = new ArrayList<>();
        flag_0 = flag_1 = flag_2 = false;
    }
    
    public List<CinematicWaypoint> getWaypoints() {
        return p;
    }
    
    public void addWaypoint(CinematicWaypoint waypoint) {
        p.add(waypoint);
    }
    
    public boolean shouldTeleportToStartAfterPlayback() {
        return flag_0;
    }
    
    public void setShouldTeleportToStartAfterPlayback(boolean state) {
        flag_0 = state;
    }
    
    public boolean shouldTeleportBackAfterPlayback() {
        return flag_1;
    }
    
    public void setShouldTeleportBackAfterPlayback(boolean state) {
        flag_1 = state;
    }
    
    public boolean canPlayerTurnCameraDuringDelay() {
        return flag_2;
    }
    
    public void setCanPlayerTurnCameraDuringDelay(boolean state) {
        flag_2 = state;
    }
}
