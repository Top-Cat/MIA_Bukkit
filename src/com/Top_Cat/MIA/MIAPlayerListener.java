package com.Top_Cat.MIA;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.Top_Cat.MIA.Quest.Type;
import com.maxmind.geoip.IPLocation;
import com.maxmind.geoip.LookupService;
import com.maxmind.geoip.regionName;

/**
 * Handle events for all Player related events
 * @author Thomas Cheyney
 */
public class MIAPlayerListener extends PlayerListener {
    private final MIA plugin;
    final timer timer;
    
    public class timer implements Runnable {
    	int updatec = 115;
    	
    	List<Sheep> s = new ArrayList<Sheep>();
    	
		@Override
		public void run() {
			for (World j : plugin.getServer().getWorlds()) {
		    	for (LivingEntity i : j.getLivingEntities()) {
		    		if (i instanceof Sheep && !s.contains(i)) {
		    			int dc = (int) Math.floor(Math.random() * DyeColor.values().length);
		    			((Sheep) i).setColor(DyeColor.values()[dc]);
		    			s.add((Sheep) i);
		    		}
		    	}
			}
	    	
	    	Date time = new Date();
	    	World w = plugin.getServer().getWorld("Final");
    		//System.out.println(ws.get(0).getTime());
    		long mtime = w.getTime() + 6000;
    		if (mtime > 24000) {
    			mtime -= 24000;
    		}
    		int hours = (int) Math.floor(mtime / 1000);
    		mtime = (((mtime - (hours * 1000)) * 60)  / 1000);
    		String mp = "";
    		String hp = "";
    		if (hours < 10)
    			hp = "0";
    		if (mtime < 10)	
    			mp = "0";
    		
    		Sign mtsign = ((Sign) w.getBlockAt(409, 4, -354).getState());
    		mtsign.setLine(2, hp + hours + ":" + mp + mtime);
    		mtsign.update();
    		
    		Sign sign = ((Sign) w.getBlockAt(409, 4, -353).getState());
    		SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
    		sign.setLine(1, sdf.format(time) + " GMT"); //sign_rss[(int) Math.round(Math.random() * 4)]
    		time.setTime(time.getTime() - 18000000);
    		sign.setLine(2, sdf.format(time) + " EST");
    		time.setTime(time.getTime() - 10800000);
    		sign.setLine(3, sdf.format(time) + " PST");
    		sign.update();
	    	
    		if (updatec++ % 12 == 0) {
    			for (Player k : plugin.getServer().getOnlinePlayers()) {
    				plugin.mf.updatestats(k, 2, 13, k.getHealth(), true);
    			}
    			plugin.mf.updatestats();
    		}
    		if (updatec > 120) {
    			updatec = 0;
    			plugin.mf.rebuild_cache();
    			plugin.getServer().getWorld("Creative").setTime(1000);
    		}
		}
    	
    }

    HashMap<String, OnlinePlayer> userinfo = new HashMap<String, OnlinePlayer>();
    
    public MIAPlayerListener(MIA instance) {
        plugin = instance;
        timer = new timer();
    }
    
    @Override
    public void onPlayerItem(PlayerItemEvent event) {
    	if (event.getItem().getType() == Material.LAVA_BUCKET || event.getItem().getType() == Material.WATER_BUCKET || event.getItem().getType() == Material.WOODEN_DOOR || event.getItem().getType() == Material.IRON_DOOR) {
    		event.setCancelled(plugin.blockListener.useitem_block(event.getBlockClicked(), event.getPlayer()));
    	}
    }
    
    public boolean cbal(Player p, int ammount) {
    	return cbal(p.getDisplayName(), ammount);
    }
    
    public boolean cbal(String target, int ammount) {
    		OnlinePlayer inf = userinfo.get(target);
    		int res = inf.getBalance() + ammount;
    		if (inf.cbal(ammount)) {
    			// = String.valueOf(res);
    			//userinfo.put(target, inf);
    			
    			String q = "UPDATE users SET balance = '" + res + "' WHERE name = '" + target + "'";
    			try {
        			PreparedStatement pr = plugin.conn.prepareStatement(q);
					pr.executeUpdate();
					return true;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		return false;
    }
    
    public void loadallusers() {
    	try {
	    	String q = "SELECT * FROM users";
	    	PreparedStatement pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery();
			OnlinePlayer op = null;
			while (r.next()) {
				op = new OnlinePlayer(plugin, r.getInt("balance"), r.getInt("cloak"), r.getString("name"), r.getString("prefix"), r.getInt("town"), r.getInt("Id"), r.getInt("spleef"), r.getInt("swins"));
				userinfo.put(r.getString("name"), op);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    HashMap<Player, Long> logintimes = new HashMap<Player, Long>();
    
    @Override
    public void onPlayerJoin(PlayerEvent event) {
    	String nam = event.getPlayer().getDisplayName();
    	
    	PreparedStatement pr;
		try {
			String q = "SELECT * FROM users WHERE name = '" + event.getPlayer().getDisplayName() + "'";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery();
			OnlinePlayer op = null;	
			if (r.next()) {
		    	nam = r.getString("prefix") + plugin.d+"f " + r.getString("name");
		    	op = new OnlinePlayer(plugin, r.getInt("balance"), r.getInt("cloak"), event.getPlayer().getDisplayName(), r.getString("prefix"), r.getInt("town"), r.getInt("Id"), r.getInt("spleef"), r.getInt("swins"));
			} else {
				// User doesn't exist! Make a new record
				String q2 = "INSERT INTO users (name) VALUES('" + event.getPlayer().getDisplayName() + "')";
				PreparedStatement pr2 = plugin.conn.prepareStatement(q2);
				pr2.executeUpdate();
				
				r = pr2.getGeneratedKeys();
				r.next();
				
		    	nam = plugin.d+"0[G]"+plugin.d+"f " + event.getPlayer().getDisplayName();
		    	op = new OnlinePlayer(plugin, 0, 1, event.getPlayer().getDisplayName(), plugin.d+"0[G]", 0, r.getInt(1), 0, 0);
		    	plugin.mf.spawn(event.getPlayer());
			}
			userinfo.put(r.getString("name"), op);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Date time = new Date();
		plugin.mf.updatestats(event.getPlayer(), 2, 1, (int) (time.getTime() / 1000), true);
		plugin.mf.updatestats(event.getPlayer(), 2, 3, 1);
		logintimes.put(event.getPlayer(), time.getTime() / 1000);
		
		plugin.mf.worlds.get(event.getPlayer().getWorld().getName()).addPlayer(event.getPlayer());
		
		String msg = nam + " joined!";
		
		try {
		    String sep = System.getProperty("file.separator");

		    String dir = System.getProperty("user.dir"); 

		    String dbfile = dir + sep + "GeoIP.dat"; 
		    LookupService cl = new LookupService(dbfile,LookupService.GEOIP_MEMORY_CACHE);

		    IPLocation l = cl.getLocation(event.getPlayer().getAddress().getHostName());
		    
		    msg = nam + plugin.d+"a has joined the server from "+plugin.d+"b" + regionName.regionNameByCode(l.countryCode,l.region) + ", " + l.countryName + " (" + ((int) l.distance(cl.getLocation("70.86.154.98"))) + " km)";

		    cl.close();
		}
		catch (NullPointerException e) {
			msg = nam + plugin.d+"a has joined the server from a local connection (0 km)";
		}	
		catch (IOException e) {
		    System.out.println("IO Exception");
		}
		
		plugin.mf.sendmsg(plugin.getServer().getOnlinePlayers(), msg);
		
		for (NPC i : plugin.mf.npcs.values()) {
			i.update();
		}
		
		plugin.mf.sendmsg(event.getPlayer(), plugin.d + "3Welcome to the MIA minecraft server hosted by www.thorgaming.com");
		plugin.mf.post_tweet(event.getPlayer().getDisplayName() + " joined the server! Their stats: http://www.thorgaming.com/minecraft/" + event.getPlayer().getDisplayName() + "/");
		/*World pw = event.getPlayer().getWorld();
		Location pl = event.getPlayer().getLocation();
		World mw = plugin.getServer().getWorlds().get(0);
		if (pw != mw) {
			event.getPlayer().teleportTo(new Location(mw, 0, mw.getHighestBlockYAt(0, 0), 0));
			event.getPlayer().teleportTo(pl);
		}*/
    }
    
    public void onPlayerDropItem(PlayerDropItemEvent event) {
    	plugin.mf.updatestats(event.getPlayer(), 3, event.getItemDrop().getItemStack().getTypeId(), event.getItemDrop().getItemStack().getAmount());
    }
    
    @Override
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
    	plugin.mf.updatestats(event.getPlayer(), 4, event.getItem().getItemStack().getTypeId(), event.getItem().getItemStack().getAmount());
    }
    
    public void onPlayerAnimation(PlayerAnimationEvent event) {
    	if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
    		plugin.mf.updatestats(event.getPlayer(), 2, 10);
    	}
    }
    
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
    	if (event.getPlayer().isSneaking()) {
    		plugin.mf.updatestats(event.getPlayer(), 2, 11);
    	}
    }
    
    HashMap<String, String> tprequests = new HashMap<String, String>();
    HashMap<Zone, SpleefGame> spleefgames = new HashMap<Zone, SpleefGame>();
    
    @Override
    public void onPlayerCommandPreprocess(PlayerChatEvent event) {
    	plugin.mf.updatestats(event.getPlayer(), 2, 7, 1);
    	boolean canc = true;
    	String com = event.getMessage();
		String coms[] = com.split(" ");
		if (!plugin.mf.worlds.get(event.getPlayer().getWorld().getName()).canCommand(coms[0]) && !event.getPlayer().isOp()) {
			plugin.mf.sendmsg(event.getPlayer(), "Unknown command!");
			event.setCancelled(true);
			return;
		}
		if (coms[0].equalsIgnoreCase("/w")) { 
			plugin.mf.sendmsg(plugin.mf.worlds.get(event.getPlayer().getWorld().getName()).getplayers(), plugin.d+"9(WORLD) " + userinfo.get(event.getPlayer().getDisplayName()).getPrefix() + " " + event.getPlayer().getDisplayName() + ":§f " + com.substring(3));
		} else if (coms[0].equalsIgnoreCase("/g")) {
    		plugin.mf.sendmsg(plugin.getServer().getOnlinePlayers(), userinfo.get(event.getPlayer().getDisplayName()).getPrefix() + " " + event.getPlayer().getDisplayName() + ":§f " + com.substring(3));
		} else if (coms[0].equalsIgnoreCase("/clearinv") && event.getPlayer().isOp()) {
			Player p = plugin.getServer().getPlayer(coms[1]);
			PlayerInventory i = p.getInventory();
			i.clear();
			i.setBoots(new ItemStack(0));
			i.setChestplate(new ItemStack(0));
			i.setHelmet(new ItemStack(0));
			i.setLeggings(new ItemStack(0));
		} else if (coms[0].equalsIgnoreCase("/item")) {
			if (coms.length > 1) {
				int id = -1;
				try {
					id = Integer.parseInt(coms[1]);
				} catch (NumberFormatException e) {
					// Oh well ¬¬
				}
				if (Material.getMaterial(id) == null) {
					plugin.mf.sendmsg(event.getPlayer(), "The material id '" + id + "' is not recognised");
					return;
				}
				int amm = 1;
				int data = 0;
				if (coms.length > 2) {
					amm = Integer.parseInt(coms[2]);
				}
				if (coms.length > 3 && event.getPlayer().isOp()) {
					data = Integer.parseInt(coms[3]);
				}
				event.getPlayer().getInventory().addItem(new ItemStack(id, amm, (short) 0, (byte) data));
				plugin.mf.sendmsg(event.getPlayer(), "There you go!");
			} else {
				plugin.mf.sendmsg(event.getPlayer(), "Correct usage: /item <id> <ammount> <data>");
			}
    	} else if (coms[0].equalsIgnoreCase("/tpc") && coms.length > 1 && plugin.getServer().getPlayer(coms[1]) != null) {
			tprequests.put(plugin.getServer().getPlayer(coms[1]).getDisplayName(), event.getPlayer().getDisplayName());
			plugin.mf.sendmsg(plugin.getServer().getPlayer(coms[1]), "Player " + event.getPlayer().getDisplayName() + " requested to teleport to you!");
    	} else if (coms[0].equalsIgnoreCase("/deny") && tprequests.containsKey(event.getPlayer().getDisplayName())) {
    		tprequests.remove(event.getPlayer().getDisplayName());
    	} else if (coms[0].equalsIgnoreCase("/world") && coms.length > 1) {
    		plugin.mf.teleport(event.getPlayer(), new Location(plugin.getServer().getWorlds().get(Integer.parseInt(coms[1])), 0, plugin.getServer().getWorlds().get(Integer.parseInt(coms[1])).getHighestBlockYAt(0, 0), 0));
    		plugin.mf.updatestats(event.getPlayer(), 2, 5, 1);
    	} else if (coms[0].equalsIgnoreCase("/accept") && tprequests.containsKey(event.getPlayer().getDisplayName())) {
    		plugin.mf.teleport(plugin.getServer().getPlayer(tprequests.get(event.getPlayer().getDisplayName())), event.getPlayer().getLocation());
    		plugin.mf.updatestats(plugin.getServer().getPlayer(tprequests.get(event.getPlayer().getDisplayName())), 2, 5, 1);
    		tprequests.remove(event.getPlayer().getDisplayName());
    	} else if (coms[0].equalsIgnoreCase("/getpos")) {
    		Player[] p = new Player[1];
    		p[0] = event.getPlayer();
    		plugin.mf.sendmsg(p, "World: " + event.getPlayer().getWorld().getName());
    		plugin.mf.sendmsg(p, event.getPlayer().getLocation().getBlockX() + ", " + event.getPlayer().getLocation().getBlockY() + ", " + event.getPlayer().getLocation().getBlockZ());
    	} else if (coms[0].equalsIgnoreCase("/pay")) {
    		if (com.split(" ").length != 3) {
    			plugin.mf.sendmsg(event.getPlayer(), plugin.d+"bCorrect usage is: /pay <person> <ammount>");
    		} else {
    			int amm = Integer.parseInt(com.split(" ")[2]);
    			if (userinfo.containsKey(com.split(" ")[1])) {
        			//Take Money
        			cbal(event.getPlayer().getDisplayName(), -amm);
        			//Give Money
    				cbal(com.split(" ")[1], amm);
    			} else {
    				plugin.mf.sendmsg(event.getPlayer(), plugin.d+"bPlayer " + com.split(" ")[1] + " not found!");
    			}
    		}
    	} else if (coms[0].equalsIgnoreCase("/money")) {
    		if (coms.length > 1) {
	    		if (coms[1].equalsIgnoreCase("top")) {
	    			int amm = 5;
	    			if (coms.length > 2) {
	    				amm = Integer.parseInt(coms[2]);
	    			}
		    		//loadallusers();
		    		ArrayList<OnlinePlayer> ops = new ArrayList<OnlinePlayer>(userinfo.values());
		    		Collections.sort(ops);
		    		plugin.mf.sendmsg(event.getPlayer(), plugin.d+"bTop Players List");
		    		for (int i = 0; i < amm; i++) {
		    			String col = "b";
		    				if (i == 0) {
		    					col = "6";
		    				}
		    			plugin.mf.sendmsg(event.getPlayer(), plugin.d+"" + col + "#" + (i + 1) + ": " + ops.get(i).getName() + " (" + ops.get(i).getBalance() + ")");
		    		}
	    		} else if (coms[1].equalsIgnoreCase("rank")) {
		    		//loadallusers();
		    		ArrayList<OnlinePlayer> ops = new ArrayList<OnlinePlayer>(userinfo.values());
		    		Collections.sort(ops);
		    		plugin.mf.sendmsg(event.getPlayer(), plugin.d+"bCurrent rank: " + ops.indexOf(userinfo.get(event.getPlayer().getDisplayName())));
		    		//[Money] Current rank: 1
	    		} else {
	    			if (userinfo.containsKey(coms[1])) {
	    				plugin.mf.sendmsg(event.getPlayer(), plugin.d+"b[Money] " + coms[1] + "'s Balance: §f" + userinfo.get(coms[1]).getBalance() + " §bISK");
	    			}
	    		}
    		} else {
    			plugin.mf.sendmsg(event.getPlayer(), plugin.d+"b[Money] Balance: §f" + userinfo.get(event.getPlayer().getDisplayName()).getBalance() + " §bISK");
    		}
    	} else if (coms[0].equalsIgnoreCase("/shop")) {
    		if (coms.length < 2 || coms.length > 4) {
    			plugin.mf.sendmsg(event.getPlayer(), plugin.d+"bCorrect usage is: /shop <sell/buy> [id] [ammount]");
    		} else {
    			boolean opsell = true;
    			int itemid = 0;
    			int itemamm = 0;
    			if (coms[1].equalsIgnoreCase("sell") || coms[1].equalsIgnoreCase("buy")) {
    				if (coms[1].equalsIgnoreCase("buy")) {
    					opsell = false;
    				}
    				if (coms.length > 2) {
    					itemid = Integer.parseInt(coms[2]);
    				} else {
    					itemid = event.getPlayer().getItemInHand().getTypeId();
    				}
    				if (coms.length > 3) {
    					itemamm = Integer.parseInt(coms[3]);
    				} else {
    					itemamm = event.getPlayer().getItemInHand().getAmount();
    				}
    				// Check item is in shop
    				int inzone = ((Number) plugin.mf.insidezone(event.getPlayer(), false, false)).intValue();
    				HashMap<Integer, Integer[]> sitems = plugin.mf.shopitems(inzone);
    				if (sitems.containsKey(itemid) && (opsell || sitems.get(itemid)[2] >= itemamm || inzone == 0)) {
	    				if (opsell) {
	    					event.getPlayer().getInventory().removeItem(new ItemStack(itemid, itemamm));
	    					// Give Money
	    					cbal(event.getPlayer().getDisplayName(), sitems.get(itemid)[1] * itemamm);
	    					if (inzone > 0)
	    						plugin.mf.changestock(inzone, itemid, itemamm);
	    					
	    					plugin.mf.sendmsg(event.getPlayer(), plugin.d+"bReceived " + (sitems.get(itemid)[1] * itemamm) + " for " + itemamm + " " + new ItemStack(itemid, itemamm).getType().toString());
	    				} else {
	    					// Take Money
	    					cbal(event.getPlayer().getDisplayName(), -(sitems.get(itemid)[0] * itemamm));
	    					
	    					event.getPlayer().getInventory().addItem(new ItemStack(itemid, itemamm));
	    					if (inzone > 0)
	    						plugin.mf.changestock(inzone, itemid, -itemamm);
	    					
	    					plugin.mf.sendmsg(event.getPlayer(), plugin.d+"bBought " + itemamm + " " + new ItemStack(itemid, itemamm).getType().toString() + " for " + (sitems.get(itemid)[0] * itemamm));
	    				}
    				} else {
    					plugin.mf.sendmsg(event.getPlayer(), plugin.d+"bThis shop does not stock that item, or does not have sufficient stock");
    				}
    			} else {
    				plugin.mf.sendmsg(event.getPlayer(), plugin.d+"bCorrect usage is: /shop <sell/buy> [id] [ammount]");
    			}
    		}
    	} else if (coms[0].equalsIgnoreCase("/spawn")) {
    		plugin.mf.spawn(event.getPlayer());
    	} else if (coms[0].equalsIgnoreCase("/mspawn")){
    		
    		CreatureType mt = CreatureType.fromName(coms[1].equalsIgnoreCase("PigZombie") ? "PigZombie" : capitalCase(coms[1]));
    		int[] ignore = {8, 9};
    		org.bukkit.block.Block blk = (new TargetBlock(event.getPlayer(), 300, 0.2, ignore)).getTargetBlock();
    		if(mt == null){
    			event.getPlayer().sendMessage("Invalid mob type.");
    			return;
    		}
    		if(!event.getPlayer().isOp()){
    			event.getPlayer().sendMessage("You are not authorized to use that command.");
    			return;
    		}
    		if(coms.length != 2){
    			event.getPlayer().sendMessage("Correct usage is: /mspawn <Mob Name>");
    			return;
    		}
    		if(blk == null){
    			event.getPlayer().sendMessage("You must be looking at a Mob Spawner.");
    			return;
    		}
    		if(blk.getTypeId() != 52){
    			event.getPlayer().sendMessage("You must be looking at a Mob Spawner.");
    			return;
    		}
    		((org.bukkit.block.CreatureSpawner) blk.getState()).setCreatureType(mt);
    		event.getPlayer().sendMessage("Mob spawner set as " + mt.getName().toLowerCase() + ".");
    	} else if (coms[0].equalsIgnoreCase("/spleef")) {
    		if (coms[1].equalsIgnoreCase("join")) {
    			Zone i = inArena(coms[2]);
    			for (SpleefGame sg : spleefgames.values()) {
    				if (sg.playerPlaying(event.getPlayer()) && sg.z != i) {
    					sg.removePlayer(event.getPlayer());
    				}
    			}
				if (spleefgames.containsKey(i)) {
					if (!spleefgames.get(i).addPlayer(event.getPlayer())) {
						plugin.mf.sendmsg(event.getPlayer(), "Can't join game!");
					}
				} else {
					plugin.mf.sendmsg(event.getPlayer(), "Couldn't find game '" + coms[2] + "'");
				}
    		} else if (coms[1].equalsIgnoreCase("leave")) {
    			for (Zone i : plugin.mf.zones) {
    				if (i.getName().equalsIgnoreCase(coms[2]) && i.isSpleefArena()) {
    					if (spleefgames.get(i).playerPlaying(event.getPlayer())) {
    						spleefgames.get(i).removePlayer(event.getPlayer());
    					}
    					break;
    				}
    			}
    		} else if (coms[1].equalsIgnoreCase("stats")) {
    			Player[] psa = new Player[1];
    			if (coms.length > 2) {
    				psa[0] = plugin.getServer().getPlayer(coms[2]);
    			} else {
					psa[0] = event.getPlayer();
    			}
    			boolean found = false;
				for (Zone i : plugin.mf.zones) {
					if (i.isSpleefArena() && spleefgames.get(i).playerPlaying(event.getPlayer())) {
						spleefgames.get(i).showstats(psa, event.getPlayer());
						found = true;
					}
				}
				if (!found) {
					int[] ss = plugin.playerListener.userinfo.get(event.getPlayer().getDisplayName()).spleefstats();
					plugin.mf.sendmsg(psa, ss[1] + "/" + ss[0] + " All Time");
				}
    		}
    	} else if (coms[0].equalsIgnoreCase("/ready")) {
    		for (SpleefGame i : spleefgames.values()) {
    			if (i.playerPlaying(event.getPlayer())) {
    				i.setReadyPlayer(event.getPlayer(), true);
    			}
    		}
    	} else if (coms[0].equalsIgnoreCase("/q") || coms[0].equalsIgnoreCase("/quest")) {
    		if (coms[1].equalsIgnoreCase("list")) {
    			int j = 0;
    			for (Quest i : plugin.mf.quest.values()) {
    				if (i.isActive(event.getPlayer())) {
    					plugin.mf.sendmsg(event.getPlayer(), (++j) + ": " + i.getName());
    				}
    			}
    			if (j == 0) {
    				plugin.mf.sendmsg(event.getPlayer(), "No active quests!");
    			}
    		} else if ((coms[1].equalsIgnoreCase("progress") || coms[1].equalsIgnoreCase("p") || coms[1].equalsIgnoreCase("pro")) && coms.length > 2) {
    			int j = 0;
    			for (Quest i : plugin.mf.quest.values()) {
    				if (i.isActive(event.getPlayer())) {
    					if (++j == Integer.parseInt(coms[2])) {
    						i.dispProgress(event.getPlayer());
    					}
    				}
    			}
    		} else if ((coms[1].equalsIgnoreCase("description") || coms[1].equalsIgnoreCase("d")) && coms.length > 2) {
    			int j = 0;
    			for (Quest i : plugin.mf.quest.values()) {
    				if (i.isActive(event.getPlayer())) {
    					if (++j == Integer.parseInt(coms[2])) {
    						i.dispData(event.getPlayer(), "description", i.getDescription());
    					}
    				}
    			}
    		} else if ((coms[1].equalsIgnoreCase("view") || coms[1].equalsIgnoreCase("v")) && coms.length > 2) {
    			List<Quest> lq = userinfo.get(event.getPlayer().getDisplayName()).tmpQuestList();
    			if (lq.size() >= Integer.parseInt(coms[2])) {
	    			Quest i = lq.get(Integer.parseInt(coms[2]) - 1);
	    			i.show(event.getPlayer());
	    			userinfo.get(event.getPlayer().getDisplayName()).setAQuest(i);
    			}
    		} else if (coms[1].equalsIgnoreCase("accept") || coms[1].equalsIgnoreCase("a")) {
    			Quest lq = userinfo.get(event.getPlayer().getDisplayName()).aQuest();
    			if (lq != null) {
	    			lq.accept(event.getPlayer());
    			}
    		}
    	} else {
    		canc = false;
    	}
    	if (canc)
    		event.setCancelled(true);
    }
    
    public Zone inArena(String name) {
    	for (Zone i : plugin.mf.zones) {
			if (i.getName().equalsIgnoreCase(name) && i.isSpleefArena()) {
				return i;
			}
    	}
    	return null;
    }
    
    private String capitalCase(String s){
    	return s.toUpperCase().charAt(0) + s.toLowerCase().substring(1);
    }	
    
    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
    	if (event.getRespawnLocation().getWorld().getName().equalsIgnoreCase("Nether")) {
    		event.setRespawnLocation(new Location(event.getRespawnLocation().getWorld(), -0.5d, 74d, -9.5d, 180, 0));
    	} else if (event.getRespawnLocation().getWorld().getName().equalsIgnoreCase("Creative") || event.getRespawnLocation().getWorld().getName().equalsIgnoreCase("Survival")) {
    		
    	} else {
    		event.setRespawnLocation(new Location(event.getRespawnLocation().getWorld(), 467d, 114d, -325d, 180, 0));
    	}
    	
    	//respawn.add(event.getPlayer());
    	//event.setRespawnLocation(l);
    	//plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, respawnplayers, 100);
    	//plugin.mf.post_tweet(event.getPlayer().getDisplayName() + " died! Their stats: http://thomasc.co.uk/minecraft/" + event.getPlayer().getDisplayName() + "/");
    }
    
    @Override
    public void onPlayerChat(PlayerChatEvent event) {
    	String tw = "";
    	String prefix = userinfo.get(event.getPlayer().getDisplayName()).getPrefix();
    	Player[] p = plugin.getServer().getOnlinePlayers();
    	if (plugin.mf.worlds.get(event.getPlayer().getWorld().getName()).townChat()) {
    		Town t = plugin.mf.townR(event.getPlayer());
    		if (t != null) {
    			p = plugin.mf.checkWorld(t.getplayers(), plugin.getServer().getWorlds().get(0), plugin.getServer().getWorlds().get(1));
    		} else {
    			p = null;
    		}
			
			if (p == null) {
				p = plugin.getServer().getOnlinePlayers();
			} else {
				tw = plugin.d+"9(TOWN) ";
			}
    	}
		
    	plugin.mf.sendmsg(p, tw + prefix + " " + event.getPlayer().getDisplayName() + ":§f " + event.getMessage());
    	event.setCancelled(true);
    }
    
    @Override
    public void onPlayerQuit(PlayerEvent event) {
    	Date time = new Date();
		plugin.mf.updatestats(event.getPlayer(), 2, 2, (int) (time.getTime() / 1000), true);
		plugin.mf.updatestats(event.getPlayer(), 2, 4, (int) ((time.getTime() / 1000) - logintimes.get(event.getPlayer())));
		plugin.mf.worlds.get(event.getPlayer().getWorld().getName()).removePlayer(event.getPlayer());
		
    	for (Stargate i : plugin.mf.stargates.values()) {
    		i.changeGateState(false, event.getPlayer());
    	}
    	for (Zone i : plugin.mf.zones) {
			if (i.isSpleefArena() && spleefgames.containsKey(i) && spleefgames.get(i).playerPlaying(event.getPlayer())) {
				spleefgames.get(i).removePlayer(event.getPlayer());
			}
		}
    	
    	for (Quest i : plugin.mf.quest.values()) {
    		if (i.isActive(event.getPlayer())) {
    			plugin.mf.save_progress(event.getPlayer(), i);
    		}
    	}
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
    	for (SpleefGame i : spleefgames.values()) {
    		 if (i.playerPlaying(event.getPlayer())) {
    			 i.move(event.getPlayer(), event.getTo());
    		 }
    	}
    	for (Quest i : plugin.mf.quest_sort.get(Type.Find).values()) {
    		if (i.isActive(event.getPlayer()) && i.getProgress(event.getPlayer()) == 0) {
    			i.move(event.getPlayer(), event.getTo());
    		}
    	}
    	if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockY() != event.getTo().getBlockY() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
    		plugin.mf.updatestats(event.getPlayer(), 2, 6, 1);
    	}
    	
    	if (Math.sqrt(Math.pow(event.getTo().getBlockX() - 466, 2) + Math.pow(event.getTo().getBlockZ() + 303, 2)) > 1000) {
    		if (Math.sqrt(Math.pow(event.getFrom().getBlockX() - 466, 2) + Math.pow(event.getFrom().getBlockZ() + 303, 2)) < 1000) {
    			event.setCancelled(true);
    		} else {
    			double vX = event.getTo().getBlockX() - 466;
    			double vY = event.getTo().getBlockZ() + 303;
    			double magV = Math.sqrt(vX*vX + vY*vY);
    			int aX = (int) (466 + vX / magV * 1000);
    			int aY = (int) (-303 + vY / magV * 1000);
    			
    			Location dest = new Location(event.getPlayer().getWorld(), aX, event.getPlayer().getWorld().getHighestBlockYAt(aX, aY), aY);
    			plugin.mf.teleport(event.getPlayer(), dest);
    			event.setTo(dest);
    		}
    	}
    	
    	// Put lapis in! and do bedrock
    	/*Location pl = event.getPlayer().getLocation();
    	for (int i = -8; i < 8; i++) {
    		for (int k = -8; k < 8; k++) {
	    		for (int j = 0; j < 5; j++) {
	    			Block b = w.getBlockAt(pl.getBlockX() + i, j, pl.getBlockZ() + k);
	    			if (j == 0 && b.getType() != Material.BEDROCK) {
	    				b.setType(Material.BEDROCK);
	    			} else if (j > 0 && b.getType() == Material.BEDROCK) {
	    				b.setType(Material.STONE);
	    			}
	    		}
	    		if ((Math.random() * 50000000) > 49999999) {
	    			System.out.println("Deposit lapis :)");
	    		}
    		}
    	}*/
    	
    	if (((Zone) plugin.mf.insidezone(event.getPlayer(), true)).heal()) {
    		int newh = event.getPlayer().getHealth() + 1;
    		if (newh > 20) {
    			newh = 20;
    		}
    		event.getPlayer().setHealth(newh);
    	}
    	
    	Town town1 = (Town) plugin.mf.insidetown(event.getFrom(), true);
    	Town town2 = (Town) plugin.mf.insidetown(event.getTo(), true);
    	if (town1 != town2) {
    		if (town1 == null) {
    			plugin.mf.sendmsg(event.getPlayer(), plugin.d+"6Welcome to " + town2.getName());
    		} else {
    			plugin.mf.sendmsg(event.getPlayer(), plugin.d+"6Now leaving " + town1.getName());
    		}
    	}
    	
    	for (Stargate i : plugin.mf.stargates.values()) {
    			event.setTo(i.pMove(event.getPlayer(), event.getTo()));
    	}
    }
}

