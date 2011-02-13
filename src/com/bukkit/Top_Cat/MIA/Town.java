package com.bukkit.Top_Cat.MIA;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Town extends Zone {
	
	public enum towntypes {
		TOWN(75);
		
		private int code;

		 private towntypes(int c) {
		   code = c;
		 }
		 
		 public int radius() {
			 return code;
		 }
	}
	
	final MIA plugin;
	private List<String> users;
	
	public Town(MIA instance, int id, Block c1, String name, int mayor, towntypes tt, List<String> usrs) {
		super(instance, id, c1, instance.getServer().getWorlds().get(0).getBlockAt(c1.getX() + tt.radius(), c1.getY(), c1.getZ()), name, 0, false, false, false, false, false, mayor, Zone.townshape.CIRCLE);
		plugin = instance;
		users = usrs;
	}
	
	@Override
	public boolean isChestProtected(Player p) {
		if (users.contains(p.getDisplayName())) {
			return false;
		}
		return true;
	}
	
	public boolean isProtected(Player p) {
		if (users.contains(p.getDisplayName())) {
			return false;
		}
		return true;
	}
	
	public boolean intown(Player p) {
		if (users.contains(p.getDisplayName())) {
			return true;
		}
		return false;
	}
	
	public List<String> getusers() {
		return users;
	}
	
	public Player[] getplayers() {
		Player[] out = new Player[users.size()];
		int j = 0;
		for (String i : users) {
			Player p = plugin.getServer().getPlayer(i);
			if (p != null)
				out[j++] = p;
		}
		return out;
	}
}