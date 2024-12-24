package dev.Fjc.combatTag;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatTag extends JavaPlugin implements Listener {

    private static final String OPEN_WORLD = "Open_World";

    private final Map<Player, Integer> combatTimers = new ConcurrentHashMap<>();

    private final Map<UUID, PermissionAttachment> perms = new ConcurrentHashMap<>();

    public Player defender;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        this.getConfig().set("CombatDuration", 10);

        if (!getConfig().contains("CombatDuration")) {
            getConfig().set("CombatDuration", 10);
            saveConfig();
        }


    }

    @Override
    public void onDisable() {
        if (combatTimers.containsKey(defender)) {
            cancelCombatTimer(defender);

            PermissionAttachment dPerms = perms.remove(defender.getUniqueId());
            if (dPerms != null)
                defender.removeAttachment(dPerms);

            getLogger().warning("One or more players had a combat timer active. Attempting to stop them now.");
            try {
                wait(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            getLogger().info("Shutting down CombatTag");
        } else {
            getLogger().info("Shutting down CombatTag");
        }
        // Plugin shutdown logic
    }


    @EventHandler
    public void onCombatEntity(EntityDamageByEntityEvent event, String[] args) {
        if (event.getEntity() instanceof Player) {
            Player defender = (Player) event.getEntity();
            Entity attacker = event.getDamager();
            World world = defender.getWorld();
            //Gets Player object defender, Entity object attacker, and World object world


            if (attacker instanceof LivingEntity && !(attacker instanceof Player) && world.getName().equalsIgnoreCase(OPEN_WORLD)) {

                if (!combatTimers.containsKey(defender)) {
                    startCombatTimer(defender);
                    //If the timer hasn't started already, start the timer
                } else {
                    getLogger().warning("The combat tag failed to activate!");
                }

                defender.sendMessage(ChatColor.RED + "You are now in combat! " + ChatColor.LIGHT_PURPLE + "(Entity)");
                //Sends the player a message when the timer starts

                PermissionAttachment attachment = defender.addAttachment(this);
                perms.put(defender.getUniqueId(), attachment);

                perms.get(defender.getUniqueId()).unsetPermission("essentials.back");
                perms.get(defender.getUniqueId()).unsetPermission("essentials.warp");
                perms.get(defender.getUniqueId()).unsetPermission("essentials.tpa");




            } //Null checks, if the player changes dimensions or something.
        }


    }

    private void startCombatTimer(Player defender) {
        //Logic for timer
        int combatDuration = (int) this.getConfig().get("CombatDuration");

        combatTimers.put(defender, combatDuration);

        new BukkitRunnable() {

            @Override
            public void run() {
                if (!combatTimers.containsKey(defender)) {
                    cancel();
                    return;
                }

                int remainingTime = combatTimers.get(defender);

                if (remainingTime <= 0) {

                    defender.sendMessage(ChatColor.BLUE + "You are no longer in combat.");
                    combatTimers.remove(defender);

                    PermissionAttachment dPerms = perms.remove(defender.getUniqueId());
                    if (dPerms != null) {
                        defender.removeAttachment(dPerms);
                    }
                    cancel();

                } else {
                    combatTimers.put(defender, remainingTime - 1);
                }
            }
        }.runTaskTimer(this, 0, 20);

    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (combatTimers.containsKey(player)) {
            cancelCombatTimer(player);

            player.setHealth(0);
            getServer().broadcastMessage(player.getName() + " logged out while in combat and has perished.");

            PermissionAttachment dPerms = perms.remove(player.getUniqueId());
            if (dPerms != null) {
                player.removeAttachment(dPerms);
            }
        }

    }
    private void cancelCombatTimer(Player defender) {
        combatTimers.remove(defender);

    }



}
