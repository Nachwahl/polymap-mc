package dev.nachwahl.polymap.commands;

import dev.nachwahl.polymap.util.FileBuilder;
import dev.nachwahl.polymap.util.MySQL;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapLinkCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileBuilder fb = new FileBuilder("plugins/PolyMap", "config.yml");
        Player p = (Player) sender;
        if(!p.hasPermission("bteg.maplink")) {
            p.sendMessage("§b§lBTEG §7» §cYou don't have the permission to execute this command.");
            return false;
        }
        if(args.length != 1) {
            p.sendMessage("§b§lBTEG §7» §cUsage: /maplink <Discord Email-Address>");
            return false;
        }

        Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
        Matcher mat = pattern.matcher(args[0]);
        if(!mat.matches()) {
            p.sendMessage("§b§lBTEG §7» §cPlease provide a vaild email address, which is linked to your discord account.");
            return false;
        }
        try {
            if(MySQL.checkUser(p.getUniqueId())) {
                p.sendMessage("§b§lBTEG §7» §cYou already linked an account. Please unlink it with /mapunlink first.");
                return false;
            }
            MySQL.createUser(p.getUniqueId(), args[0]);
            p.sendMessage(fb.getString("prefix") + "  §aAccount linked successfully.");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;
    }
}
