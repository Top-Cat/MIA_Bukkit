package com.Top_Cat.MIA;

import java.sql.DriverManager;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.World.Environment;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * MIA for Bukkit
 *
 * @author Top_Cat
 */
public class MIA extends JavaPlugin {
    public final MIAPlayerListener playerListener = new MIAPlayerListener(this);
    private final MIAEntityListener entityListener = new MIAEntityListener(this);
    public final MIABlockListener blockListener = new MIABlockListener(this);
    public final MIAVehicleListener vehicleListener = new MIAVehicleListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    MIAFunctions mf;
    public final String d = "\u00C2\u00A7";
	public final String c = d+"e";
    Connection conn;
    
    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events
        try {
        	Class.forName("com.mysql.jdbc.Driver").newInstance();
        	// Step 2: Establish the connection to the database. 
            conn = DriverManager.getConnection(new sqllogin().url);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mf = new MIAFunctions(this);
		mf.post_tweet("The Minecraft server has started!");
    	
    	getServer().getScheduler().scheduleAsyncRepeatingTask(this, playerListener.timer, 20, 50);
    	getServer().getScheduler().scheduleAsyncRepeatingTask(this, vehicleListener.timer, 20, 20);
    	
        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_TOGGLE_SNEAK, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);
        
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.REDSTONE_CHANGE , blockListener, Priority.Normal, this);
        
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.EXPLOSION_PRIME, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_COMBUST, entityListener, Priority.Normal, this);
        
        pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Priority.Normal, this);

       	getServer().createWorld("Nether", Environment.NETHER);
        getServer().createWorld("Survival", Environment.NORMAL);
        getServer().createWorld("Creative", Environment.NORMAL);
        
        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
        
        mf.rebuild_cache();
        
        playerListener.loadallusers();
        for (Player i : getServer().getOnlinePlayers()) {
        	Date time = new Date();
    		playerListener.logintimes.put(i, time.getTime() / 1000);
    		mf.worlds.get(i.getWorld().getName()).addPlayer(i);
        }
        mf.cache_npcs();
    }
    
    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
    	for (Player i : getServer().getOnlinePlayers()) {
    		Date time = new Date();
    		if (playerListener.logintimes.containsKey(i)) {
    			mf.updatestats(i, 2, 4, (int) ((time.getTime() / 1000) - playerListener.logintimes.get(i)));
    		}
    		
    		for (Quest j : mf.quest.values()) {
	    		if (j.isActive(i)) {
	    			mf.save_progress(i, j);
	    		}
	    	}
    	}
    	
    	for (NPC i : mf.npcs.values()) {
    		i.destroy();
    	}
    	
    	mf.updatestats();
        System.out.println("Goodbye world!");
    }
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }

    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
    
    /*@Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    }*/
}