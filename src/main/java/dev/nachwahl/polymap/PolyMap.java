package dev.nachwahl.polymap;

import dev.nachwahl.polymap.commands.MapCommand;
import dev.nachwahl.polymap.commands.MapLinkCommand;
import dev.nachwahl.polymap.commands.MapUnlinkCommand;
import dev.nachwahl.polymap.util.FileBuilder;
import dev.nachwahl.polymap.util.MySQL;
import org.bukkit.plugin.java.JavaPlugin;

public final class PolyMap extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("map").setExecutor(new MapCommand(this));
        this.getCommand("maplink").setExecutor(new MapLinkCommand());
        this.getCommand("mapunlink").setExecutor(new MapUnlinkCommand());
        new FileBuilder("plugins/PolyMap", "mysql.yml")
                .addDefault("mysql.host", "localhost")
                .addDefault("mysql.port", "3306")
                .addDefault("mysql.database", "map")
                .addDefault("mysql.user", "root")
                .addDefault("mysql.password", "")
                .copyDefaults(true).save();
        new FileBuilder("plugins/PolyMap", "config.yml")
                .addDefault("prefix", "§b§lPolyMap §7»")
                .addDefault("algolia.appid", "your_app_id")
                .addDefault("algolia.apikey", "your_api_key")
                .addDefault("algolia.index", "your_index")
                .copyDefaults(true).save();
        MySQL.connect();
        MySQL.createTables();


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
