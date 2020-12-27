package email.com.gmail.cosmoconsole.bukkit.camera;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;

public class SavedPlayerPath {
    UUID owner;
    long date;
    Boolean teleport;
    Boolean pathless;
    Double speed;
    String pathnames;
    ArrayList<Location> waypoints;
    ArrayList<Double> waypoints_s;
    ArrayList<Double> waypoints_y;
    ArrayList<Double> waypoints_p;
    ArrayList<String> waypoints_m;
    ArrayList<List<String>> waypoints_c;
    ArrayList<Integer> waypoints_l;
    ArrayList<Double> waypoints_d;
    ArrayList<Boolean> waypoints_i;
    Integer waypoints_f;
    Double waypoints_t;
    
    SavedPlayerPath(UUID o, Boolean t, Boolean p, Double s, String n,
            ArrayList<Location> w, ArrayList<Double> w_s, ArrayList<Double> w_y,
            ArrayList<Double> w_p, ArrayList<String> w_m, ArrayList<List<String>> w_c,
            ArrayList<Integer> w_l, ArrayList<Double> w_d, ArrayList<Boolean> w_i,
            Integer w_f, Double w_t) {
        owner = o;
        date = System.currentTimeMillis();
        teleport = t;
        pathless = p;
        speed = s;
        pathnames = n;
        waypoints = new ArrayList<>(w);
        waypoints_s = new ArrayList<>(w_s);
        waypoints_y = new ArrayList<>(w_y);
        waypoints_p = new ArrayList<>(w_p);
        waypoints_m = new ArrayList<>(w_m);
        waypoints_c = new ArrayList<>(w_c);
        waypoints_l = new ArrayList<>(w_l);
        waypoints_d = new ArrayList<>(w_d);
        waypoints_i = new ArrayList<>(w_i);
        waypoints_f = w_f;
        waypoints_t = w_t;
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public long getSavedAt() {
        return date;
    }
}
