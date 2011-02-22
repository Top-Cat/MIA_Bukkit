package com.Top_Cat.MIA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.Top_Cat.MIA.Quest.Type;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

public class MIAFunctions {
    private final MIA plugin;
    
    public MIAFunctions(MIA instance) {
        plugin = instance;
        
        sblock = plugin.getServer().getWorlds().get(0).getBlockAt(409, 4, -354);
        gzone = new Zone(plugin, 0, sblock, sblock, "everywhere", 0, true, true, false, false, false, 0);
    }
    
    public ArrayList<ArrayList<ArrayList<Integer>>> getpattern(int zid) {
    	ArrayList<ArrayList<ArrayList<Integer>>> out = new ArrayList<ArrayList<ArrayList<Integer>>>();
    	ArrayList<ArrayList<Integer>> blktypes = new ArrayList<ArrayList<Integer>>();
    	ArrayList<ArrayList<Integer>> blkvals = new ArrayList<ArrayList<Integer>>();
    	PreparedStatement pr;
		try {
			String q = "SELECT pattern FROM spleef WHERE zone = '" + zid + "'";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery();
			r.next();
			String p = r.getString("pattern");
			String[] rs = p.split(";");
			for (String row : rs) {
				String[] blks = row.split(",");
				ArrayList<Integer> arowt = new ArrayList<Integer>();
				ArrayList<Integer> arowv = new ArrayList<Integer>();
				for (String blk : blks) {
					String[] bd = blk.split("-");
					arowt.add(Integer.parseInt(bd[0]));
					arowv.add(Integer.parseInt(bd[1]));
				}
				blktypes.add(arowt);
				blkvals.add(arowv);
			}
			out.add(blktypes);
			out.add(blkvals);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
    }
    
    public int spleefmax(int zid) {
    	PreparedStatement pr;
		try {
			String q = "SELECT max FROM spleef WHERE zone = '" + zid + "'";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery();
			r.next();
			return r.getInt("max");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
    }
    
    public void spleefplayed(Player p, boolean win) {
    	PreparedStatement pr;
		try {
			String wintxt = "";
			if (win) {
				wintxt = ", swins = swins + 1";
			}
			String q = "UPDATE users SET spleef = spleef + 1" + wintxt + " WHERE name = '" + p.getDisplayName() + "'";
			pr = plugin.conn.prepareStatement(q);
			pr.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    RequestToken requestToken;
    Twitter twitter;
    sqllogin sqllogin = new sqllogin();
    
    public void post_tweet(String s) {
    	long time = plugin.getServer().getWorlds().get(0).getFullTime();
    	s += " (" + String.valueOf(time).substring(3) + ")";
    	
        twitter = new TwitterFactory().getInstance();
        AccessToken accessToken = sqllogin.accessToken;
        twitter.setOAuthConsumer(sqllogin.consumerKey, sqllogin.consumerSecret);
        twitter.setOAuthAccessToken(accessToken);
        
        Status status;
		try {
			status = twitter.updateStatus(s);
	        System.out.println("Successfully updated the status to [" + status.getText() + "].");
		} catch (TwitterException e) {
			System.out.println("Didn't update status. " + e); 
			//e.printStackTrace();
		}
        
    }
    
    public void rebuild_cache() {
    	plugin.playerListener.spleefgames.clear();
    	cache_zones();
    	cache_towns();
    	cache_worlds();
    }
    
    public HashMap<String, MIAWorld> worlds = new HashMap<String, MIAWorld>();
    
    public void cache_worlds() {
    	worlds.clear();
    	PreparedStatement pr;
		try {
			String q = "SELECT * FROM worlds";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery();
			while (r.next()) {
				worlds.put(r.getString("name"), new MIAWorld(r.getInt("Id"), r.getString("commands"), r.getBoolean("townchat"), r.getBoolean("mobs"), r.getBoolean("pvp"), r.getInt("healing")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public List<Town> towns = new ArrayList<Town>();
    
    public void cache_towns() {
    	towns.clear();
    	PreparedStatement pr;
		try {
			String q = "SELECT a.*, b.radius FROM towns as a, town_types as b WHERE a.ttype = b.Id";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery();
			while (r.next()) {
				String[] cs = r.getString("center").split(",");
				List<String> usrs = new ArrayList<String>();
				
				String q2 = "SELECT name FROM users WHERE town = '" + r.getInt("Id") + "'";
				pr = plugin.conn.prepareStatement(q2);
				ResultSet r2 = pr.executeQuery();
				
				while (r2.next()) {
					usrs.add(r2.getString("name"));
				}
				
				Block b = plugin.getServer().getWorlds().get(r.getInt("world")).getBlockAt(Integer.parseInt(cs[0]), 0, Integer.parseInt(cs[1]));
				Town t = new Town(plugin, r.getInt("Id"), b, r.getString("name"), r.getInt("mayor"), Town.towntypes.TOWN, usrs);
				towns.add(t);
				zones.add(t);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    HashMap<String, NPC> npcs = new HashMap<String, NPC>();
    HashMap<String, Quest> quest = new HashMap<String, Quest>();
    HashMap<Quest.Type, HashMap<String, Quest>> quest_sort = new HashMap<Quest.Type, HashMap<String, Quest>>();
    
    public void cache_npcs() {
    	npcs.clear();
    	for (Type i : Type.values()) {
    		quest_sort.put(i, new HashMap<String, Quest>());
    	}
    	PreparedStatement pr;
		try {
			String q = "SELECT * FROM npcs";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery();
			
			while (r.next()) {
				Location l = new Location(plugin.getServer().getWorlds().get(r.getInt("world")), r.getDouble("posX"), r.getDouble("posY"), r.getDouble("posZ"), r.getInt("rotation"), r.getInt("pitch"));
				npcs.put(r.getString("npc_id"), new NPC(plugin, r.getInt("Id"), r.getString("name"), l, r.getInt("item_in_hand"), r.getInt("proxy"), r.getBoolean("prefix")));
			}
			
			q = "SELECT * FROM quests";
			pr = plugin.conn.prepareStatement(q);
			r = pr.executeQuery();
			
			while (r.next()) {
				String type = r.getString("quest_type");
				Type t = null;
				if (type.equalsIgnoreCase("harvest")) {
					t = Type.Harvest;
				} else if (type.equalsIgnoreCase("gather")) {
					t = Type.Gather;
				} else if (type.equalsIgnoreCase("build")) {
					t = Type.Build;
				} else if (type.equalsIgnoreCase("assasin")) {
					t = Type.Assasin;
				} else if (type.equalsIgnoreCase("find")) {
					t = Type.Find;
				}
				Quest qu = new Quest(plugin, r.getString("id"), r.getString("quest_name"), r.getString("completion_text"), r.getString("data"), r.getString("quest_desc"), npcs.get(r.getString("start_npc")), npcs.get(r.getString("end_npc")), t, r.getString("prereq"), r.getInt("cost"), r.getInt("prize"), r.getBoolean("chestb"), r.getString("items_provided"), r.getString("rewards"));
				quest.put(r.getString("id"), qu);
				quest_sort.get(t).put(r.getString("id"), qu);
			}
			
			q = "SELECT * FROM quests_completed";
			pr = plugin.conn.prepareStatement(q);
			r = pr.executeQuery();
			
			while (r.next()) {
				quest.get(r.getString("quest_id")).setComplete(r.getString("player_name"));
			}
			
			q = "SELECT * FROM quests_active";
			pr = plugin.conn.prepareStatement(q);
			r = pr.executeQuery();
			
			while (r.next()) {
				quest.get(r.getString("quest_id")).setProgress(r.getString("player_name"), r.getInt("progress"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void quest_complete(Quest q, Player p) {
    	PreparedStatement pr;
		try {
			String s = "DELETE FROM quests_active WHERE player_name = '" + p.getDisplayName() + "' and quest_id = '" + q.getId() + "'";
			pr = plugin.conn.prepareStatement(s);
			pr.executeUpdate();
			
			s = "INSERT INTO quests_completed (player_name, quest_id) VALUES('" + p.getDisplayName().toLowerCase() + "', '" + q.getId() + "')";
			pr = plugin.conn.prepareStatement(s);
			pr.executeUpdate();
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    }
    
    public void quest_accept(Quest q, Player p) {
    	PreparedStatement pr;
		try {
			String s = "INSERT INTO quests_active (player_name, quest_id) VALUES ('" + p.getDisplayName().toLowerCase() + "', '" + q.getId() + "')";
			pr = plugin.conn.prepareStatement(s);
			pr.executeUpdate();
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    }
    
    public void save_progress(Player p, Quest q) {
    	PreparedStatement pr;
		try {
			String s = "UPDATE quests_active SET progress = '" + q.getProgress(p) + "' WHERE player_name = '" + p.getDisplayName().toLowerCase() + "' and quest_id = '" + q.getId() + "'";
			pr = plugin.conn.prepareStatement(s);
			pr.executeUpdate();
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    }
    
    public List<String> getdialogs(NPC n) {
    	List<String> d = new ArrayList<String>();
    	PreparedStatement pr;
		try {
			String q = "SELECT * FROM npcs_dialog WHERE npc_id = " + n.getId() + " OR npc_id = " + n.getProxyId();
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery();
			
			while (r.next()) {
				d.add(r.getString("dialog_text"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d;
    }
    
    public Player[] checkWorld(Player[] p, World w, World s) {
    	Player[] out = new Player[p.length];
    	int i = 0;
    	List<Player> q = worlds.get(w.getName()).getplayersList();
    	List<Player> t = worlds.get(s.getName()).getplayersList();
    	for (Player o : p) {
    		if (q.contains(o) || t.contains(o)) {
    			out[i++] = o;
    		}
    	}
    	return out;
    }
    
    public Town townR(Player p) {
    	if (towns.size() == 0)
    		rebuild_cache();
    	
    	for (Town i : towns) {
    		if (i.intown(p)) {
	    		return i;
    		}
    	}
    	return null;
    }
    
    public int insidetown(Object o) {
    	Object town = insidetown(o, false);
    	if (town != null) {
    		return ((Number) insidetown(o, false)).intValue();
    	} else {
    		return 0;
    	}
    }
    
    public Object insidetown(Object o, boolean ret) {
    	Town t = null;
    	if (o instanceof Block) {
    		t = intownR(((Block) o).getLocation());
    	} else if (o instanceof Location) {
    		t = intownR((Location) o);
    	} else if (o instanceof Player) {
    		t = intownR(((Player) o).getLocation());
    	}
    	
    	if (ret) {
    		return t;
    	} else if (t != null) {
    		return t.getId();
    	}
		return null;
    }
    
    private Town intownR(Location l) {
    	if (towns.size() == 0)
    		rebuild_cache();
    	
    	for (Town i : towns) {
    		if (i.inZone(l)) {
	    		return i;
    		}
    	}
    	return null;
    }
    
    public void updatestats(HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> stats, HashMap<String, HashMap<Integer, HashMap<Integer, Boolean>>> overw) {
    	PreparedStatement pr;
		String u = "";
		try {
			String q = "SELECT * FROM stats";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery();
			while (r.next()){
				if (stats.containsKey(r.getString("player"))) {
					if (stats.get(r.getString("player")).containsKey(r.getInt("type"))) {
						if (stats.get(r.getString("player")).get(r.getInt("type")).containsKey(r.getInt("blockid"))) {
							int amm = stats.get(r.getString("player")).get(r.getInt("type")).get(r.getInt("blockid"));
							String ad = "'";
							if (!overw.get(r.getString("player")).get(r.getInt("type")).get(r.getInt("blockid"))) {
								ad = "count + '";
							}
							u = "UPDATE stats SET count = " + ad + amm + "' WHERE Id = '" + r.getInt("Id") + "'";
							pr = plugin.conn.prepareStatement(u);
							//System.out.println(u);
							pr.executeUpdate();
							stats.get(r.getString("player")).get(r.getInt("type")).remove(r.getInt("blockid"));					}
					}
				}
			}
			for (String p : stats.keySet()) {
				for (Integer p2 : stats.get(p).keySet()) {
					for (Integer p3 : stats.get(p).get(p2).keySet()) {
						int amm = stats.get(p).get(p2).get(p3);
						u = "INSERT INTO stats (player, type, blockid, count) VALUES ('" + p + "', '" + p2 + "', '" + p3 + "', '" + amm + "')";
						pr = plugin.conn.prepareStatement(u);
						pr.executeUpdate();
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Meow :" + u);
			e.printStackTrace();
		}
    }
    
    HashMap<Block, ArrayList<String>> dests = new HashMap<Block, ArrayList<String>>();
    
    public ArrayList<String> getDest(Block sign) {
    	if (dests.containsKey(sign)) {
    		return dests.get(sign);
    	} else {
	    	PreparedStatement pr;
	    	ArrayList<String> out = new ArrayList<String>();
			try {
				String blk = sign.getX() + "," + sign.getY() + "," + sign.getZ();
				String q = "SELECT name, cblock FROM stargates WHERE cblock != '" + blk + "' and network = (SELECT network FROM stargates WHERE cblock = '" + blk + "')";
				pr = plugin.conn.prepareStatement(q);
				ResultSet r = pr.executeQuery();
				while (r.next()) {
					out.add(r.getString("name"));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dests.put(sign, out);
	    	return out;
    	}
    }
    
    ArrayList<Block> gateblocks = new ArrayList<Block>();
    
    public void getGates() {
    	if (gateblocks.size() == 0) {
	    	PreparedStatement pr;
	    	ArrayList<Block> out = new ArrayList<Block>();
			try {
				String q = "SELECT cblock, world FROM stargates";
				pr = plugin.conn.prepareStatement(q);
				ResultSet r = pr.executeQuery();
				while (r.next()) {
					String[] so = r.getString("cblock").split(",");
					Integer[] l = new Integer[3];
					for (int i = 0; i < 3; i++) {
						l[i] = Integer.parseInt(so[i]);
					}
					out.add(plugin.getServer().getWorlds().get(r.getInt("world")).getBlockAt(l[0], l[1], l[2]));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			gateblocks = out;
    	}
    }
    
    public String gateName(Block sign) {
    	PreparedStatement pr;
		try {
			String blk = sign.getX() + "," + sign.getY() + "," + sign.getZ();
			String q = "SELECT name FROM stargates WHERE cblock = '" + blk + "'";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery();
			if (r.first()) {
				return r.getString("name");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    
    public Integer gateId(String name) {
    	PreparedStatement pr;
		try {
			String q = "SELECT Id FROM stargates WHERE name= '" + name + "'";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery();
			if (r.first()) {
				return r.getInt("Id");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return 0;
    }
    
    public Integer[] gateSign(Object gateid) {
    	Integer[] out = new Integer[4];
    	HashMap<String, String> inf = gateData(gateid);
		String[] so = inf.get("cblock").split(",");
		for (int i = 0; i < 3; i++) {
			out[i] = Integer.parseInt(so[i]);
		}
		out[3] = Integer.parseInt(inf.get("world"));
    	return out;
    }
    
    public HashMap<String, String> gateData(Block fromsign) {
    	getGates();
    	if (fromsign.getType() == Material.STONE_BUTTON) {
    		for (Block i : gateblocks) {
    			if (i.getWorld() == fromsign.getWorld() && (i.getX() == fromsign.getX() && Math.abs(i.getZ() - fromsign.getZ()) == 3) || (i.getZ() == fromsign.getZ() && Math.abs(i.getX() - fromsign.getX()) == 3)) {
    				fromsign = i;
    				break;
    			}
    		}
    	}
    	return gateData(gateId(gateName(fromsign)));
    }
    
    public HashMap<String, String> gateData(String fromsign) {
    	return gateData(gateId(fromsign));
    }
    
    public HashMap<String, String> gateData(Object gateid) {
    	if (gateid instanceof String) {
    		return gateData((String) gateid);
    	} else if (gateid instanceof Block) {
    		return gateData((Block) gateid);
    	} else if (gateid instanceof Integer) {
    		return gateData((Integer) gateid);
    	}
    	return null;
    }
    HashMap<Integer, HashMap<String, String>> gateinfo_cache = new HashMap<Integer, HashMap<String, String>>();
    public HashMap<String, String> gateData(Integer id) {
    	if (gateinfo_cache.containsKey(id)) {
    		return gateinfo_cache.get(id);
    	} else {
	    	HashMap<String, String> out = new HashMap<String, String>();
	    	PreparedStatement pr;
			try {
				String q = "SELECT * FROM stargates WHERE Id= '" + id + "'";
				pr = plugin.conn.prepareStatement(q);
				ResultSet r = pr.executeQuery();
				if (r.first()) {
					out.put("Id", r.getString("Id"));
					out.put("name", r.getString("name"));
					out.put("world", r.getString("world"));
					out.put("cblock", r.getString("cblock"));
					out.put("rot", r.getString("rot"));
					out.put("network", r.getString("network"));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			gateinfo_cache.put(id, out);
	    	return out;
    	}
    }
    
    public int playertownId(Player p) {
    	return plugin.playerListener.userinfo.get(p.getDisplayName()).getTown();
    }
    
    public void sendmsg(Player rec, String msg) {
		Player[] pa = new Player[1];
		pa[0] = rec;
		sendmsg(pa, msg);
	}
    
    public void sendmsg(Player[] rec, String msg) {
    	for (Player p : rec) {
    		if (p != null)
    		p.sendMessage(msg);
    	}
    }
    
    public Integer[] intarray(String[] in) {
    	Integer[] out = new Integer[in.length];
    	int k = 0;
    	for (String i : in) {
    		out[k++] = Integer.parseInt(i);
    	}
		return out;
    }
    
    public void teleport(Player p, Location l) {
    	if (!p.isOp() && p.getWorld() != l.getWorld() && !((p.getWorld().getName().equals("Nether") && l.getWorld().getName().equals("Final")) || (p.getWorld().getName().equals("Final") && l.getWorld().getName().equals("Nether")))) {
    		//Players inventory must be empty
    		int t = 0;
    		for (ItemStack i : p.getInventory().getContents()) {
    			t += i.getAmount();
    		}
    		if (t > 0) {
    			sendmsg(p, "Your inventory must be empty to travel to this world!");
    			return;
    		}
    	}
    	plugin.mf.worlds.get(p.getWorld().getName()).removePlayer(p);
    	l.getWorld().loadChunk(l.getBlockX(), l.getBlockZ());
    	p.teleportTo(l);
    	plugin.mf.worlds.get(l.getWorld().getName()).addPlayer(p);
    }
    
    List<Zone> zones = new ArrayList<Zone>();
    Block sblock;
    Zone gzone;
    
    public void cache_zones() {
    	zones.clear();
    	PreparedStatement pr;
		try {
			String q = "SELECT * FROM zones";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery()	;
			while (r.next()) {
				String c = r.getString("corners");
				String[] bls = c.split(":");
				Integer[] cs = intarray(bls[0].split(","));
				Integer[] cs2 = intarray(bls[1].split(","));
				Block b1 = plugin.getServer().getWorlds().get(r.getInt("world")).getBlockAt(cs[0], cs[1], cs[2]);
				Block b2 = plugin.getServer().getWorlds().get(r.getInt("world")).getBlockAt(cs2[0], cs2[1], cs2[2]);
				Zone nz = new Zone(plugin, r.getInt("Id"), b1, b2, r.getString("name"), r.getInt("healing"), r.getBoolean("PvP"), r.getBoolean("mobs"), r.getBoolean("chest"), r.getBoolean("protect"), r.getBoolean("spleef"), r.getInt("owner"));
				zones.add(nz);
				int par = r.getInt("parent");
				if (par > 0) {
					zones.get(par - 1).addChild(nz);
				} else {
					gzone.addChild(nz);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public int insidezone(Object o) {
    	Object zone = insidezone(o, false);
    	if (zone != null) {
    		return ((Number) zone).intValue();
    	} else {
    		return 0;
    	}
    }
    
    public Object insidezone(Object o, boolean ret) {
    	return insidezone(o, ret, true);
    }
    
    public Object insidezone(Object o, boolean ret, boolean town) {
    	Zone t = null;
    	if (o instanceof Block) {
    		t = inzoneR(((Block) o).getLocation(), town);
    	} else if (o instanceof Location) {
    		t = inzoneR((Location) o, town);
    	} else if (o instanceof Player) {
    		t = inzoneR(((Player) o).getLocation(), town);
    	}
    	
    	if (ret) {
    		return t;
    	} else if (t != null) {
    		return t.getId();
    	}
		return null;
    }
    
    private Zone inzoneR(Location ps, boolean town) {
    	if (zones.size() == 0)
    		rebuild_cache();
    	
    	gzone.setMobs(worlds.get(ps.getWorld().getName()).isMobs());
    	gzone.setPvP(worlds.get(ps.getWorld().getName()).isPvP());
    	gzone.setHealing(worlds.get(ps.getWorld().getName()).getHealing());
    	return inzoneR(ps, town, zones, gzone);
    }
    
    private Zone inzoneR(Location ps, boolean town, List<Zone> lz, Zone z) {
    	for (Zone i : lz) {
    		if (i.inZone(ps) && (!(i instanceof Town) || town)) {
    			return inzoneR(ps, town, i.getChildren(), i);
    		}
    	}
		return z;
    }
    
    public boolean ownzone(Player p) {
    	return ((Zone) insidezone(p, true)).ownzone(p);
    }
    
    public void changestock(int zone, int itemid, int itemamm) {
    	PreparedStatement pr;
		try {
			String q = "UPDATE ishopitems as a, ishop as b SET stock = stock + '" + itemamm + "' WHERE a.name = b.name and b.zone = '" + zone + "' and a.itemId = '" + itemid + "'";
			pr = plugin.conn.prepareStatement(q);
			pr.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public HashMap<Integer, Integer[]> shopitems(int zone) {
    	HashMap<Integer, Integer[]> out = new HashMap<Integer, Integer[]>();
    	PreparedStatement pr;
		try {
			String q = "SELECT a.itemId, a.buy, a.sell, a.stock FROM ishopitems as a, ishop as b WHERE a.name = b.name and b.zone = '" + zone + "'";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery();
			while (r.next()) {
				Integer[] o = new Integer[3];
				o[0] = r.getInt("buy");
				o[1] = r.getInt("sell");
				o[2] = r.getInt("stock");
				out.put(r.getInt("itemId"), o);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return out;
    }
    
    public boolean inowntown(Player p) {
    	if (playertownId(p) == insidetown(p))
    		return true;
    	return false;
    }
    
    public boolean notothertown(Player p) {
    	if (playertownId(p) == insidetown(p) || insidetown(p) == 0)
    		return true;
    	return false;
    }
    
    public void openportal(Block sign) {
    	sign.getWorld().loadChunk(sign.getWorld().getChunkAt(sign));
    	ePortal(sign, Material.FIRE);
    }
    
    public void closeportal(Block sign, World w) {
    	ePortal(sign, Material.AIR);
    	
		Sign si = (Sign) sign.getState();
		si.setLine(0, "--" + plugin.mf.gateName(sign) + "--");
		si.setLine(1, "Right click to");
        si.setLine(2, "use the gate");
        si.setLine(3, " (" + plugin.mf.gateData(sign).get("network") + ") ");
        si.update();
    }
    
    public void spawn(Player p) {
    	World w = plugin.getServer().getWorld("Final");
    	teleport(p, new Location(w, 467d, 114d, -325d, 180, 0));
    }
    
    public void ePortal(Block sign, Material m) {
    	Integer[] l = new Integer[3];
		l[0] = sign.getX();
		l[1] = sign.getY();
		l[2] = sign.getZ();
		
		
		Integer rot = Integer.parseInt(gateData(sign).get("rot"));

		int xo2 = 1;
		int zo2 = -1;
		if (rot == 0) {
			xo2 = -1;
			zo2 = -1;
		} else if (rot == 2) {
			xo2 = 1;
			zo2 = 1;
		} else if (rot == 3) {
			xo2 = -1;
			zo2 = 1;
		}

		sign.getWorld().getBlockAt(l[0] + xo2, l[1] - 1, l[2] + zo2).setType(m);
    }
    
    HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> stats = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
    HashMap<String, HashMap<Integer, HashMap<Integer, Boolean>>> overw = new HashMap<String, HashMap<Integer, HashMap<Integer, Boolean>>>();
    
    public void updatestats(Player p, int type, int id) {
    	updatestats(p, type, id, 1, false);
    }
    
    public void updatestats(Player p, int type, int id, int amm) {
    	updatestats(p, type, id, amm, false);
    }
    
    public void updatestats(Player pl, int type, int id, int amm, Boolean overwrite) {
    	String p = pl.getDisplayName();
    	/*if (updatec++ > 100) {
    		updatec = 0;
    		System.out.println("Update, is this too frequent?");
    		updatestats(stats, overw);
    	}*/
		if (!stats.containsKey(p)){
			stats.put(p, new HashMap<Integer, HashMap<Integer, Integer>>());
			overw.put(p, new HashMap<Integer, HashMap<Integer, Boolean>>());
		}
		if (!stats.get(p).containsKey(type)){
			stats.get(p).put(type, new HashMap<Integer, Integer>());
			overw.get(p).put(type, new HashMap<Integer, Boolean>());
		}
		HashMap<Integer, Integer> pblocks = stats.get(p).get(type);
		overw.get(p).get(type).put(id, overwrite);
		if (pblocks.containsKey(id)) {
			pblocks.put(id, pblocks.get(id) + amm);
		} else {
			pblocks.put(id, amm);
		}
    }
    
    public void updatestats() {
    	updatestats(stats, overw);
    }
    
}