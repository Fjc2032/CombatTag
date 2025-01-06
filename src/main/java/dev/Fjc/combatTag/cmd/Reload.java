package dev.Fjc.combatTag.cmd;

import dev.Fjc.combatTag.CombatTag;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class Reload extends CombatTag implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("combattag-reload")) {

            Player player = (Player) sender;

            if (!player.hasPermission("fjc.combattag.reload")) {
                return true;
            } else {
                getConfig().set("Permission.Remove", permissionList);
                YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
                saveConfig();

                player.sendMessage(ChatColor.BLUE + "Configuration reloaded");
                player.sendMessage("Entity duration: " + this.getConfig().get("CombatDuration.Entity"));
                player.sendMessage("Player duration: " + this.getConfig().get("CombatDuration.Player"));
                player.sendMessage("Set world: " + this.getConfig().get("WorldName"));
                player.sendMessage("Permissions set: " + permissionList);
            }
        }
        return false;
    }
}
