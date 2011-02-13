package com.bukkit.Top_Cat.MIA;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimedEvent;

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
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    	event.setCancelled(onDamage(event.getDamager(), event.getEntity(), event.getDamage()));
    }
    
    public boolean onDamage(Entity attacker, Entity defender) {
    	return onDamage(attacker, defender, 0);
    }
    
    HashMap<Player, Player> lastattacker = new HashMap<Player, Player>();
    
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
    	}
    	return false;
    }
    
    @Override
    public void onEntityDamageByProjectile(EntityDamageByProjectileEvent event) {
    	event.setCancelled(onDamage(null, event.getEntity(), event.getDamage()));
    }
    
    @Override
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
    	event.setCancelled(onDamage(null, event.getEntity(), event.getDamage()));
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
        	if (plugin.mf.insidetown(i) > 0 || ((Zone) plugin.mf.insidezone(i, true)).isMobs()) {
        		event.setCancelled(true);
        	}
    	}
    }
    
    @Override
    public void onExplosionPrimed(ExplosionPrimedEvent event) {
    	//event.getRadius()
    	
    }

    //Insert Player related code here
}

