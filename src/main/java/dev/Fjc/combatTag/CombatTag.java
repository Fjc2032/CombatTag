package dev.Fjc.combatTag;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatTag extends JavaPlugin implements Listener {

    private final String OPEN_WORLD = (String) this.getConfig().get("WorldName");
    //Name of the world that will be used. Pulled from config.yml

    private final Map<Player, Integer> combatTimers = new ConcurrentHashMap<>();
    private final Map<Player, Integer> combatTimers1 = new ConcurrentHashMap<>();
    //Stores timer combatTimers in a HashMap

    private final Map<UUID, PermissionAttachment> perms = new ConcurrentHashMap<>();
    private final Map<UUID, PermissionAttachment> perms1 = new ConcurrentHashMap<>();
    //Stores player permissions in a ConcurrentHashMap

    private final Map<Player, BukkitRunnable> combatTasks = new ConcurrentHashMap<>();
    private final Map<Player, BukkitRunnable> combatTasks1 = new ConcurrentHashMap<>();
    //Stores the BukkitRunnable task in a HashMap (runnable holds timer logic)

    public Player defender;
    //Have Player = defender to use as object
    
    //defender = entity trigger
    //defender1 = player trigger

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        this.getConfig().set("CombatDuration.Entity", 10);
        this.getConfig().set("CombatDuration.Player", 15);
        this.getConfig().set("WorldName", "world");
        
        this.getConfig().addDefault("CombatDuration.Entity", 10);
        this.getConfig().addDefault("CombatDuration.Player", 15);
        this.getConfig().addDefault("WorldName", "world");
        saveConfig();
        reloadConfig();
    }

    @Override
    public void onDisable() {
        if (combatTimers.containsKey(defender)) {

            if (defender == null) {
                getLogger().warning("Something went wrong while trying to grab a variable. Skipping...");
                getLogger().info("Shutting down CombatTag");
                return;
            }

            getLogger().warning("One or more players has a combat timer active. Attempting to stop them now.");

            if (combatTimers != null) {
            cancelCombatTimer(defender); } else {
                getLogger().severe("A fatal error has occurred. Skipping...");
                getLogger().info("Shutting down CombatTag");
                throw new NullPointerException();
            }

            PermissionAttachment dPerms = perms.remove(defender.getUniqueId());
            removeAttachment(defender);
            if (dPerms != null) {
                defender.removeAttachment(dPerms); } else {
                getLogger().severe("A fatal error has occurred. Skipping...");
                throw new RuntimeException();
            }

            getLogger().info("Shutting down CombatTag");
        } else {
            getLogger().warning("Something went wrong while trying to grab a variable. Skipping...");
            getLogger().info("Shutting down CombatTag");
        }
    }


    @EventHandler
    public void onCombatEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player defender = (Player) event.getEntity();
            Entity attacker = event.getDamager();
            World world = defender.getWorld();

            //Check if the attacker is an entity that is alive, cannot be a player, and must be in defined world
            if (attacker instanceof LivingEntity && !(attacker instanceof Player) && world.getName().equalsIgnoreCase(OPEN_WORLD)) {

                //If the timer is already active, then get the runnable from combatTasks, cast it to existingTask Runnable, and cancel
                //Otherwise, start the timer and notify the player they are in combat. This fires when Player defender is attacked.
                //Also creates a new PermissionAttachment and use it to remove select permissions from the player
                if (combatTimers.containsKey(defender)) {

                    BukkitRunnable existingTask = combatTasks.get(defender);
                    if (existingTask != null) {
                        existingTask.cancel();
                    }
                } else {
                    defender.sendMessage(ChatColor.RED + "You are now in combat! " + ChatColor.YELLOW + "(Entity)");
                }
                startCombatTimer(defender);

                addAttachment(defender, defender);
            }
        }
    }
    @EventHandler
    public void onCombatPlayer(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player defender1 = (Player) event.getEntity();
            Entity attacker = event.getDamager();
            World world = defender1.getWorld();


            if ((attacker instanceof Player) && world.getName().equalsIgnoreCase(OPEN_WORLD)) {
                if (combatTimers1.containsKey(defender1)) {

                    BukkitRunnable existingTask = combatTasks1.get(defender1);
                    if (existingTask != null) {
                        existingTask.cancel();
                    }
                } else {
                    defender1.sendMessage(ChatColor.RED + "You are now in combat! " + ChatColor.DARK_RED + "(Player)");
                }

                startCombatTimer1(defender1);

                addAttachment(defender1, defender1);

            }


        }
    }

    private void startCombatTimer(Player defender) {
        //Logic for timer - entity
        int combatDuration = (int) this.getConfig().get("CombatDuration.Entity");

        combatTimers.put(defender, combatDuration);

        BukkitRunnable task = new BukkitRunnable() {

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
        };
        combatTasks.put(defender, task);
        task.runTaskTimer(this, 0, 20);
    }
    private void startCombatTimer1(Player defender1) {
        //Logic for timer - player
        int combatDuration = (int) this.getConfig().get("CombatDuration.Player");
        
        combatTimers1.put(defender1, combatDuration);
        
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!combatTimers1.containsKey(defender1)) {
                    cancel();
                    return;
                }
                int remainingTime = combatTimers1.get(defender1);
                
                if (remainingTime <= 0) {
                    defender1.sendMessage(ChatColor.BLUE + "You are no longer in combat.");
                    combatTimers1.remove(defender1);
                }
            }
        };
        combatTasks1.put(defender1, task);
        task.runTaskTimer(this, 0, 20);
    }


    //If the player quits while the timer is running, kill the player
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (combatTimers.containsKey(player)) {
            cancelCombatTimer(player);
            removeAttachment(player);

            player.setHealth(0);
            getServer().broadcastMessage(player.getName() + " logged out while in combat and has perished.");

        }

    }

    //If the player dies while the timer is running, stop the timer and remove the permission attachment
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = (Player) event.getEntity();

        if (combatTimers.containsKey(player)) {
            cancelCombatTimer(player);
            removeAttachment(player);
        }
    }

    //Logic to stop the timer
    private void cancelCombatTimer(Player defender) {
        BukkitRunnable task = combatTasks.remove(defender);
        if (task != null) {
            task.cancel();
        }
        combatTimers.remove(defender);

    }

    //Logic to remove permission attachments (does not remove permissions from the player!)
    private void removeAttachment(Player player) {
        PermissionAttachment dPerms = perms.remove(player.getUniqueId());
        if (dPerms != null) {
            player.removeAttachment(dPerms);
        }
    }

    //Logic to add permission attachments (this removes the permissions. Wacky, right?)
    private void addAttachment(Player defender, Player defender1) {
        PermissionAttachment attachment = defender.addAttachment(this);

        if (defender != null) {
            perms.put(defender.getUniqueId(), attachment);

            perms.get(defender.getUniqueId()).unsetPermission("essentials.back");
            perms.get(defender.getUniqueId()).unsetPermission("essentials.warp");
            perms.get(defender.getUniqueId()).unsetPermission("essentials.tpa");
        } else {
            return;
        }
        PermissionAttachment attachment1 = defender1.addAttachment(this);

        if (defender1 != null) {
            perms1.put(defender1.getUniqueId(), attachment1);

            perms1.get(defender1.getUniqueId()).unsetPermission("essentials.back");
            perms1.get(defender1.getUniqueId()).unsetPermission("essentials.warp");
            perms1.get(defender1.getUniqueId()).unsetPermission("essentials.tpa");
        }
    }

    //Logic to reload the plugin via command
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("combattag-reload")) {

            Player player = (Player) sender;

            if (!player.hasPermission("fjc.combattag.reload")) {
                return true;
            } else {
                saveConfig();
                reloadConfig();
                player.sendMessage(ChatColor.BLUE + "Configuration reloaded");
            }
        }
        return false;
    }
}

