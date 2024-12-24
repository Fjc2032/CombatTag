package dev.Fjc.combatTag;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class CombatTag extends JavaPlugin implements Listener {

    private static final String OPEN_WORLD = "Open_World";

    private static final Map<Player, Integer> combatTimers = new HashMap<>();

    private HashMap<UUID, PermissionAttachment> perms = new HashMap<UUID, PermissionAttachment>();

    public Player defender;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        this.getConfig().set("CombatDuration", 10);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    public void onCombatEntity(EntityDamageByEntityEvent event, String[] args) {
        if (event.getEntity() instanceof Player) {
            Player defender = (Player) event.getEntity();
            Entity attacker = event.getDamager();
            World world = defender.getWorld();
            //Gets Player object defender, Entity object attacker, and World object world


            if (attacker instanceof LivingEntity && !(attacker instanceof Player) && world.getName().equals(OPEN_WORLD)) {

                if (!combatTimers.containsKey(defender)) {
                    startCombatTimer(defender);
                    //If the timer hasn't started already, start the timer
                }

                defender.sendMessage(ChatColor.RED + "You are now in combat! " + ChatColor.LIGHT_PURPLE + "(Entity)");
                //Sends the player a message when the timer starts

                PermissionAttachment attachment = defender.addAttachment(this);
                perms.put(defender.getUniqueId(), attachment);

                perms.get(defender.getUniqueId()).unsetPermission("essentials.back");
                perms.get(defender.getUniqueId()).unsetPermission("essentials.warp");
                perms.get(defender.getUniqueId()).unsetPermission("essentials.tpa");




            }
            if (attacker == null) {
                return;
            }
            if (defender == null) {
                return;
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
                    cancel();

                    PermissionAttachment pperms = perms.get(defender.getUniqueId());
                    if (pperms != null) {
                        pperms.setPermission("essentials.back", true);
                        pperms.setPermission("essentials.warp", true);
                        pperms.setPermission("essentials.tpa", true);
                    }


                } else {
                    combatTimers.put(defender, remainingTime - 1);
                }
            }
        }.runTaskTimer(this, 0, 20);

    }

    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (combatTimers.containsKey(player)) {
            cancelCombatTimer(player);
        }

    }
    private void cancelCombatTimer(Player defender) {
        combatTimers.remove(defender);

    }



}
