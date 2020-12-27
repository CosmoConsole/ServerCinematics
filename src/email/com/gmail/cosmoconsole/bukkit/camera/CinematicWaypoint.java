package email.com.gmail.cosmoconsole.bukkit.camera;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

/**
 * Represents a ServerCinematics waypoint.
 */
public class CinematicWaypoint {
    private Location l;
    private Double s;
    private Double y;
    private Double p;
    private String m;
    private List<String> c;
    private double d;
    private boolean option_0;
    
    /**
     * Creates a waypoint with only a location.
     * 
     * @param loc The location of the waypoint.
     */
    public CinematicWaypoint(Location loc) {
        this(loc, null, null, null);
    }

    /**
     * Creates a waypoint with location, speed, yaw and pitch.
     * 
     * @param loc The location of the waypoint.
     * @param speed The speed at the waypoint.
     * @param yaw The yaw of the camera at the waypoint.
     * @param pitch The pitch of the camera at the waypoint.
     */
    public CinematicWaypoint(Location loc, Double speed, Double yaw, Double pitch) {
        if (loc == null) {
            throw new IllegalArgumentException("location of the waypoint cannot be null");
        }
        l = loc;
        setSpeed(speed);
        y = yaw;
        p = pitch;
        m = "";
        c = new ArrayList<>();
        d = 0;
        option_0 = false;
    }
    
    public Location getLocation() {
        return l;
    }
    
    public Double getSpeed() {
        return s;
    }
    
    public void setSpeed(Double d) {
        if (d == null || d.doubleValue() <= 0) {
            s = null;
        } else {
            s = d;
        }
    }
    
    public Double getYaw() {
        return y;
    }
    
    public void setYaw(Double yaw) {
        y = yaw;
    }
    
    public Double getPitch() {
        return p;
    }
    
    public void setPitch(Double pitch) {
        p = pitch;
    }
    
    public String getMessage() {
        return m;
    }
    
    public void setMessage(String message) {
        m = message;
    }
    
    public List<String> getCommands() {
        return c;
    }
    
    public double getDelay() {
        return d;
    }
    
    public void setDelay(double delay) {
        d = Math.max(0, delay);
    }
    
    public boolean isInstant() {
        return option_0;
    }
    
    public void setIsInstant(boolean instant) {
        option_0 = instant;
    }
}
