package dev.Fjc.combatTag.api;

import dev.Fjc.combatTag.CombatTag;
import org.bukkit.entity.Player;

import java.util.HashMap;



public class API extends CombatTag {

    public boolean isTimerEnabled() {
        if (combatTimers.containsKey(defender) || (combatTimers1.containsKey(defender1))) {
            return true;
        } else {
            return false;
        }

    }
    public Integer currentTimerPositionEntity() {
        return combatTimers.get(defender);
    }
    public int currentTimerPositionPlayer() {
        return combatTimers1.get(defender1);
    }
    public boolean isPluginEnabled() {
        return getServer().getPluginManager().isPluginEnabled(this);
    }
    public Player playersInCombat() {
        return defender.getPlayer();
    }
    public Player attackerId() {
        return attacker.getPlayer();
    }

}
