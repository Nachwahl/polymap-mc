package dev.nachwahl.polymap.commands;

import dev.nachwahl.polymap.util.FileBuilder;
import dev.nachwahl.polymap.util.MySQL;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class MapUnlinkCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileBuilder fb = new FileBuilder("plugins/PolyMap", "config.yml");
        Player p = (Player) sender;
        if(!p.hasPermission("polymap.mapunlink")) {
            p.sendMessage(fb.getString("prefix") + "  §cYou don't have the permission to execute this command.");
            return false;
        }
        try {
            if(!MySQL.checkUser(p.getUniqueId())) {
                p.sendMessage(fb.getString("prefix") + "  §cYou have no linked account.");
                return false;
            }
            MySQL.deleteUser(p.getUniqueId());
            p.sendMessage(fb.getString("prefix") + "  §aAccount unlinked successfully.");

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
}
