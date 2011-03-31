package com.Top_Cat.MIA;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import com.Top_Cat.MIA.Quest.Type;

/**
 * Handle events for all Player related events
 * @author Thomas Cheyney
 */
public class MIAEntityListener extends EntityListener {
    private final MIA plugin;
    
    public MIAEntityListener(MIA instance) {
        plugin = instance;
    }
    
    @Override
    public void onEntityDamage(EntityDamageEvent event) {
    	Entity damager = null;
    	if (event instanceof EntityDamageByEntityEvent) {
    		EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent)event;
	    	if (event.getEntity() instanceof HumanEntity && sub.getDamager() instanceof Player) {
	    		for (NPC i : plugin.mf.npcs.values()) {
	    			if (i.isEntity(event.getEntity())) {
	    				i.interact((Player) sub.getDamager());
	    				event.setCancelled(true);
	    			}
	    		}
	    	}
	    	damager = sub.getDamager();
    	}
    	if (event.getEntity().getWorld().getName().equals("Creative") && event.getEntity() instanceof Player) {
    		event.setCancelled(true);
    	}
    	if (!event.isCancelled() && (event instanceof EntityDamageByEntityEvent || event instanceof EntityDamageByProjectileEvent || event instanceof EntityDamageByBlockEvent))
    		event.setCancelled(onDamage(damager, event.getEntity(), event.getDamage()));
    }
    
    public boolean onDamage(Entity attacker, Entity defender) {
    	return onDamage(attacker, defender, 0);
    }
    
    HashMap<Player, Player> lastattacker = new HashMap<Player, Player>();
    Entity lastkilled = null;
    
    public boolean onDamage(Entity attacker, Entity defender, int damage) {
    	if (defender instanceof Player) {
	    	boolean zonea = false;
	    	if (attacker instanceof Player) {
	    		zonea = ((Zone) plugin.mf.insidezone(defender.getLocation(), true)).isPvP((Player) defender);
	    	} else if (attacker instanceof Creature) {
	    		zonea = ((Zone) plugin.mf.insidezone(defender.getLocation(), true)).isMobs();
	    	}
    		
	    	if ((plugin.mf.insidetown(defender.getLocation()) > 0 && attacker instanceof Entity) || !zonea) {
	        	return true;
	    	} else if (attacker instanceof Player) {
		    	if (((Player) defender).getHealth() - damage <= 0) {
		    		lastattacker.put((Player) defender, (Player) attacker);
		    	}
	    	}
	    	plugin.mf.updatestats((Player) defender, 2, 12, damage);
    	} else if (attacker instanceof Player && defender instanceof Creature && lastkilled != defender) {
    		if (((Creature) defender).getHealth() - damage <= 0 && ((Creature) defender).getHealth() > 0) {
    			String def = defender.getClass().getSimpleName();
    			for (Quest i : plugin.mf.quest_sort.get(Type.Assasin).values()) {
        			i.kill(((Player) attacker), def);
        		}
	    		lastkilled = defender;
	    	}
    	}
    	return false;
    }
    
    @Override
    public void onEntityDeath(EntityDeathEvent event) {
    	Entity defender = event.getEntity();
    	if (defender instanceof Player) {
    		if (lastattacker.containsKey(defender)) {
	    		Player attacker = lastattacker.get(defender);
		    	// Death
				int amm = (int) (plugin.playerListener.userinfo.get(((Player) defender).getDisplayName()).getBalance() * 0.05);
				plugin.playerListener.cbal(
						((Player) defender).getDisplayName(),
						-amm
				);
				plugin.playerListener.cbal(
						((Player) attacker).getDisplayName(),
						amm
				);
				plugin.mf.sendmsg(plugin.getServer().getOnlinePlayers(), ((Player) attacker).getDisplayName() + " got " + amm + " ISK for killing " + ((Player) defender).getDisplayName());
	    	}
    		//plugin.mf.spawn((Player) defender);
    	}
    	// DMC
    	//event.getDrops()
    }
    
    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
    	for (Block i : event.blockList()) {
        	if (plugin.mf.insidetown(i) > 0 || !((Zone) plugin.mf.insidezone(i, true)).isMobs()) {
        		event.setCancelled(true);
        	}
    	}
    }
    
    @Override
    public void onExplosionPrime(ExplosionPrimeEvent event) {
    	//event.getRadius()
    }
    
    @Override
    public void onEntityCombust(EntityCombustEvent event) {
    	if (event.getEntity() instanceof Player && ((Player) event.getEntity()).getWorld().getName().equals("Creative")) {
    		event.setCancelled(true);
    		((Player) event.getEntity()).setHealth(20);
    	}
    }
    
    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {
    	if (!(event.getEntity() instanceof Player)) {
	    	if (plugin.playerListener.st == 1) {
		    	if (!((Zone) plugin.mf.insidezone(event.getLocation(), true)).isMobs()) {
		    		event.setCancelled(true);
		    	}
	    	}
    	}
    }
}

