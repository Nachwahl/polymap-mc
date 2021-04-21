package dev.nachwahl.polymap.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.function.block.Counter;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import dev.nachwahl.polymap.PolyMap;
import dev.nachwahl.polymap.projection.GeographicProjection;
import dev.nachwahl.polymap.projection.ModifiedAirocean;
import dev.nachwahl.polymap.projection.ScaleProjection;
import dev.nachwahl.polymap.util.AlgoliaClient;
import dev.nachwahl.polymap.util.FileBuilder;
import dev.nachwahl.polymap.util.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;


public class MapCommand implements CommandExecutor {
    private double lat;
    private double lon;
    private String city;
    private PolyMap plugin;

    public MapCommand(PolyMap plugin) {
        this.plugin = plugin;
    }

    @Override

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileBuilder fb = new FileBuilder("plugins/PolyMap", "config.yml");
        Player p = (Player) sender;

        if(!p.hasPermission("polymap.map")) {
            p.sendMessage(fb.getString("prefix") + " §cYou don't have the permission to execute this command.");
            return false;
        }
        LocalSession sm = WorldEdit.getInstance().getSessionManager().findByName(p.getName());
        if(sm == null) {
            p.sendMessage(fb.getString("prefix") + "  §cPlease select a region via WorldEdit first.");
            return false;
        }
        Region region = null;
        try {
            region = sm.getSelection(WorldEdit.getInstance().getSessionManager().findByName(p.getName()).getSelectionWorld());
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
        }
        if(region == null) {
            p.sendMessage(fb.getString("prefix") + "  §cPlease select a region via WorldEdit first.");
            return false;
        }
        List<BlockVector2D> poly = null;
        try {
             poly = region.polygonize(50);
        } catch (IllegalArgumentException e) {
            p.sendMessage(fb.getString("prefix") + "  §cPlease select you region with under 50 points");
            return false;
        }
        p.sendMessage(fb.getString("prefix") + "  One moment please...");



        String coords = "[";
        for (BlockVector2D vector2D : poly) {
            System.out.println(toGeo(vector2D.getBlockX(), vector2D.getBlockZ())[1] + ", " + toGeo(vector2D.getBlockX(), vector2D.getBlockZ())[0]);
            this.lat = toGeo(vector2D.getBlockX(), vector2D.getBlockZ())[1];
            this.lon = toGeo(vector2D.getBlockX(), vector2D.getBlockZ())[0];
            coords = coords + "[" + toGeo(vector2D.getBlockX(), vector2D.getBlockZ())[1] + ", " + toGeo(vector2D.getBlockX(), vector2D.getBlockZ())[0] + "],";

        }
        coords = coords.substring(0, coords.length() - 1);
        coords = coords + "]";
        System.out.println(coords);
        URL url = null;


        try {
            url = new URL("https://nominatim.openstreetmap.org/reverse.php?osm_type=N&format=json&zoom=18&lon=" + this.lon + "&accept-language=de&lat=" + this.lat);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("accept", "application/json");
            Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String response = "";
            for (int i; (i = reader.read()) >= 0;)
                response += (char) i;

            System.out.println(response);
            JsonElement jsonElement = new JsonParser().parse(response);
            if(jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("city") != null) {
                this.city = jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("city").getAsString();
            } else if (jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("village") != null){
                this.city = jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("village").getAsString();
            } else if (jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("town") != null) {
                this.city = jsonElement.getAsJsonObject().get("address").getAsJsonObject().get("town").getAsString();
            } else {
                this.city = "n/A";
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sm.getRegionSelector(WorldEdit.getInstance().getSessionManager().findByName(p.getName()).getSelectionWorld()).clear();

        try {
            region.contract(new Vector().setY(region.getHeight()-1));
        } catch (RegionOperationException e) {
            e.printStackTrace();
        }

        Region finalRegion = region;
        String finalCoords = coords;
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {

            Counter counter = new Counter();
            RegionVisitor visitor = new RegionVisitor(finalRegion, counter);
            Operations.completeBlindly(visitor);
            dev.nachwahl.polymap.util.Region reg = new dev.nachwahl.polymap.util.Region(p.getName(), p.getUniqueId(), finalCoords, this.city, counter.getCount());
            AlgoliaClient algoliaClient = new AlgoliaClient();
            algoliaClient.createRegion(reg);

            /*try {
                MySQL.addArea(counter.getCount());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }*/

            try {
                MySQL.createRegion(reg);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            p.sendMessage(fb.getString("prefix") + "  Your region was created successfully and is now visible on the map.");
            p.sendMessage(fb.getString("prefix") + "  You can see your region by clicking on this link: https://map.bte-germany.de/region/" + reg.getUid());
        });



        return false;

    }
    public String stringify(List<BlockVector2D> l) {
        String rs = "";
        for (BlockVector2D vector2D : l) {
            rs = rs + ',' + vector2D.toString();
        }
        rs.substring(1);
        return rs;
    }
    private static final GeographicProjection projection = new ModifiedAirocean();
    private static final GeographicProjection uprightProj = GeographicProjection.orientProjection(projection, GeographicProjection.Orientation.upright);
    private static final ScaleProjection scaleProj = new ScaleProjection(uprightProj, 7318261.522857145, 7318261.522857145);
    /**
     * Gets the geographical location from in-game coordinates
     * @param x X-Axis in-game
     * @param z Z-Axis in-game
     * @return The geographical location (Long, Lat)
     */
    public static double[] toGeo(double x, double z) {
        return scaleProj.toGeo(x, z);
    }
    /**
     * Gets in-game coordinates from geographical location
     * @param lon Geographical Longitude
     * @param lat Geographic Latitude
     * @return The in-game coordinates (x, z)
     */
    public static double[] fromGeo(double lon, double lat) {
        return scaleProj.fromGeo(lon, lat);
    }


}
