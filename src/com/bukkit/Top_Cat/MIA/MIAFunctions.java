package com.bukkit.Top_Cat.MIA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class MIAFunctions {
    private final MIA plugin;
    
    public MIAFunctions(MIA instance) {
        plugin = instance;
	}
    
    public int intown(Block b) {
    	return intown(b.getX(), b.getZ(), b.getWorld());
    }
    
    public int intown(Location l) {
    	return intown(l.getBlockX(), l.getBlockZ(), l.getWorld());
    }
    
    public int intown(Player p) {
    	return intown(p.getLocation().getBlockX(), p.getLocation().getBlockZ(), p.getWorld());
    }
    
    public HashMap<String[], Integer[]> towns = new HashMap<String[], Integer[]>();
    
    public int intown(int x, int z, World w) {
    	if (towns.size() == 0) {
	    	PreparedStatement pr;
			try {
				String q = "SELECT a.center, a.world, a.Id, b.radius FROM towns as a, town_type as b WHERE a.ttype = b.Id";
				pr = plugin.conn.prepareStatement(q);
				ResultSet r = pr.executeQuery();
				while (r.next()) {
					String c = r.getString("center");
					c += "," + r.getString("world");
					String[] cs = c.split(",");
					Integer[] v = new Integer[2];
					v[0] = r.getInt("radius");
					v[1] = r.getInt("Id");
					towns.put(cs, v);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	for (String[] i : towns.keySet()) {
    		if (plugin.getServer().getWorlds().indexOf(w) == Integer.parseInt(i[2])) {
	    		Integer[] v = towns.get(i);
				double dist = Math.round(Math.sqrt(Math.pow(x - Integer.parseInt(i[0]), 2) + Math.pow(z - Integer.parseInt(i[1]), 2)));
				if (dist <= v[0]) {
					return v[1];
				}
    		}
    	}
    	return 0;
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
							String ad = "";
							if (!overw.get(r.getString("player")).get(r.getInt("type")).get(r.getInt("blockid"))) {
								ad = "count + ";
							}
							u = "UPDATE stats SET count = " + ad + amm + " WHERE Id = " + r.getInt("Id");
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
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
    	PreparedStatement pr;
		try {
			String q = "SELECT town FROM users WHERE name = '" + p.getName() + "'";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery();
			if (r.first()) {
				return r.getInt("town");
			}
			//while (r.next()) {
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
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
    
    public int inzone(Player ps) {
    	PreparedStatement pr;
		try {
			String q = "SELECT corners, world, Id FROM zones";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery()	;
			while (r.next()) {
				String c = r.getString("corners");
				String[] bls = c.split(":");
				Integer[] cs = intarray(bls[0].split(","));
				Integer[] cs2 = intarray(bls[1].split(","));
				Location l = ps.getLocation();
				if (plugin.getServer().getWorlds().indexOf(l.getWorld()) == r.getInt("world") && l.getBlockX() <= Math.max(cs[0], cs2[0]) && l.getBlockX() >= Math.min(cs[0], cs2[0]) && l.getBlockY() <= Math.max(cs[1], cs2[1]) && l.getBlockY() >= Math.min(cs[1], cs2[1]) && l.getBlockZ() <= Math.max(cs[2], cs2[2]) && l.getBlockZ() >= Math.min(cs[2], cs2[2])) {
					// In zone!
					return r.getInt("Id");
				}
				
				//double dist = Math.sqrt(Math.pow(x - Integer.parseInt(cs[0]), 2) + Math.pow(z - Integer.parseInt(cs[1]), 2));
				//if (dist <= r.getInt("radius")) {
					//return r.getInt("Id");
				//}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return 0;
    }
    
    public int inzoneProp(Player ps, String prop) {
    	return inzoneProp(ps.getLocation(), prop);
    }
    
    public int inzoneProp(Block ps, String prop) {
    	return inzoneProp(ps.getLocation(), prop);
    }
    
    public int inzoneProp(Location ps, String prop) {
    	PreparedStatement pr;
		try {
			String q = "SELECT corners, world, " + prop + " FROM zones";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery()	;
			while (r.next()) {
				String c = r.getString("corners");
				String[] bls = c.split(":");
				Integer[] cs = intarray(bls[0].split(","));
				Integer[] cs2 = intarray(bls[1].split(","));
				if (plugin.getServer().getWorlds().indexOf(ps.getWorld()) == r.getInt("world") && ps.getBlockX() <= Math.max(cs[0], cs2[0]) && ps.getBlockX() >= Math.min(cs[0], cs2[0]) && ps.getBlockY() <= Math.max(cs[1], cs2[1]) && ps.getBlockY() >= Math.min(cs[1], cs2[1]) && ps.getBlockZ() <= Math.max(cs[2], cs2[2]) && ps.getBlockZ() >= Math.min(cs[2], cs2[2])) {
					// In zone!
					return r.getInt(prop);
				}
				
				//double dist = Math.sqrt(Math.pow(x - Integer.parseInt(cs[0]), 2) + Math.pow(z - Integer.parseInt(cs[1]), 2));
				//if (dist <= r.getInt("radius")) {
					//return r.getInt("Id");
				//}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return 0;
    }
    
    public boolean ownzone(Player p) {
    	return plugin.playerListener.userinfo.get(p.getDisplayName()).getId() == inzoneProp(p, "owner");
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
    
    public boolean heal(Player ps) {
    	PreparedStatement pr;
		try {
			String q = "SELECT corners, healing FROM zones";
			pr = plugin.conn.prepareStatement(q);
			ResultSet r = pr.executeQuery()	;
			while (r.next()) {
				String c = r.getString("corners");
				String[] bls = c.split(":");
				Integer[] cs = intarray(bls[0].split(","));
				Integer[] cs2 = intarray(bls[1].split(","));
				Location l = ps.getLocation();
				if (l.getBlockX() <= Math.max(cs[0], cs2[0]) && l.getBlockX() >= Math.min(cs[0], cs2[0]) && l.getBlockY() <= Math.max(cs[1], cs2[1]) && l.getBlockY() >= Math.min(cs[1], cs2[1]) && l.getBlockZ() <= Math.max(cs[2], cs2[2]) && l.getBlockZ() >= Math.min(cs[2], cs2[2])) {
					// In zone!
					if ((Math.random() * 500) < r.getInt("healing")) {
		    			return true;
		    		}
				}
				
				//double dist = Math.sqrt(Math.pow(x - Integer.parseInt(cs[0]), 2) + Math.pow(z - Integer.parseInt(cs[1]), 2));
				//if (dist <= r.getInt("radius")) {
					//return r.getInt("Id");
				//}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;
    }
    
    public HashMap<String, String> towninfo(int townid) {
    	HashMap<String, String> out = new HashMap<String, String>();
    	out.put("name", "everywhere");
    	if (townid > 0) {
	    	PreparedStatement pr;
			try {	
				String q = "SELECT towns.*, town_type.radius, ua.name as uname, COUNT(ub.Id) as pop FROM towns, town_type, users as ua, users as ub WHERE towns.ttype = town_type.Id and ua.id = towns.mayor and ub.town = towns.Id and towns.Id = '" + townid + "'";
				pr = plugin.conn.prepareStatement(q);
				ResultSet r = pr.executeQuery();
				if (r.first()) {
					out.put("name", r.getString("Name"));
					out.put("radius", r.getString("radius"));
					out.put("mayor", r.getString("mayor"));
					out.put("mayorname", r.getString("uname"));
					out.put("pop", r.getString("pop"));
				}
				//while (r.next()) {
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		return out;
    }
    
    public boolean inowntown(Player p) {
    	if (playertownId(p) == intown(p))
    		return true;
    	return false;
    }
    
    public boolean notothertown(Player p) {
    	if (playertownId(p) == intown(p) || intown(p) == 0)
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
    	World w = p.getWorld();
    	w.loadChunk(467, -325);
    	p.teleportTo(new Location(w, 467d, 114d, -325d, 180, 0));
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
    //int updatec = 0;
    
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