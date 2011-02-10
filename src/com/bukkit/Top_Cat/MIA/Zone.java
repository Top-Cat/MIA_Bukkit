package com.bukkit.Top_Cat.MIA;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

class Zone {
	private Block b1, b2;
	private int id, healing;
	private boolean PvP, mobs, chest, protect;
	int owner;
	private String name;
	private final MIA plugin;
	
	public Zone(MIA instance, int id, Block c1, Block c2, String name, int healing, boolean PvP, boolean mobs, boolean chest, boolean protect, int owner) {
		plugin = instance;
		
		this.id = id;
		b1 = c1;
		b2 = c2;
		this.name = name;
		this.healing = healing;
		this.PvP = PvP;
		this.mobs = mobs;
		this.chest = chest;
		this.protect = protect;
		this.owner = owner;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public boolean isPvP() {
		return PvP;
	}
	
	public boolean isMobs() {
		return mobs;
	}
	
	public boolean isProtected() {
		return protect;
	}
	
	public boolean isChestProtected() {
		return chest;
	}
	
	public boolean inZone(Location l) {
		if (l.getWorld() == b1.getWorld() && l.getBlockX() <= Math.max(b1.getX(), b2.getX()) && l.getBlockX() >= Math.min(b1.getX(), b2.getX()) && l.getBlockY() <= Math.max(b1.getY(), b2.getY()) && l.getBlockY() >= Math.min(b1.getY(), b2.getY()) && l.getBlockZ() <= Math.max(b1.getZ(), b2.getZ()) && l.getBlockZ() >= Math.min(b1.getZ(), b2.getZ())) {
			// In zone!
			return true;
		}
		return false;
	}
	
	public boolean ownzone(Player p) {
    	return plugin.playerListener.userinfo.get(p.getDisplayName()).getId() == owner;
    }
	
	public boolean heal() {
		if ((Math.random() * 1000) < healing) {
			return true;
		}
		return false;
	}
}