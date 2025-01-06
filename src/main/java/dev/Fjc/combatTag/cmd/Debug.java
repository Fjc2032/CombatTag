package dev.Fjc.combatTag.cmd;

import dev.Fjc.combatTag.CombatTag;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.Fjc.combatTag.api.API;

public class Debug extends CombatTag implements CommandExecutor {

    API api = new API();


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("combattag-debug")) {
            Player player = (Player) sender;
            if (!player.hasPermission("fjc.combattag.debug")) {
                return true;
            } else {
                api.isTimerEnabled();
                player.sendMessage(String.valueOf(api.isTimerEnabled()));

                api.currentTimerPositionEntity();
                player.sendMessage(String.valueOf(api.currentTimerPositionEntity()));

                api.currentTimerPositionPlayer();
                player.sendMessage(String.valueOf(api.currentTimerPositionPlayer()));

                api.isPluginEnabled();
                player.sendMessage(String.valueOf(api.isPluginEnabled()));

                api.playersInCombat();
                player.sendMessage(String.valueOf(api.playersInCombat()));

                api.attackerId();
                player.sendMessage(String.valueOf(api.attackerId()));

            }
        }
        return false;
    }
}
