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
	private townshape ts = townshape.CUBE;
	
	public enum townshape {
		SQUARE, CIRCLE, CUBE, SPHERE
	}
	
	public Zone(MIA instance, int id, Block c1, Block c2, String name, int healing, boolean PvP, boolean mobs, boolean chest, boolean protect, int owner, townshape ts) {
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
		this.ts = ts;
	}
	
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
	
	public boolean isPvP(Player p) {
		return PvP;
	}
	
	public boolean isMobs() {
		return mobs;
	}
	
	public boolean isProtected(Player p) {
		return protect;
	}
	
	public boolean isChestProtected(Player p) {
		return chest;
	}
	
	public boolean inZone(Location l) {
		return inZone(l, false);
	}
	
	public boolean inZone(Location l, boolean debug) {
		if (debug) {
			System.out.println(b1.getX() + ", " + b2.getX());
			System.out.println(l.getBlockX() <= Math.max(b1.getX(), b2.getX()));
			System.out.println(l.getBlockX() >= Math.min(b1.getX(), b2.getX()));
		}
		switch (ts) {
			case CUBE:
				if (/*l.getWorld() == b1.getWorld() && */l.getBlockX() <= Math.max(b1.getX(), b2.getX()) && l.getBlockX() >= Math.min(b1.getX(), b2.getX()) && l.getBlockY() <= Math.max(b1.getY(), b2.getY()) && l.getBlockY() >= Math.min(b1.getY(), b2.getY()) && l.getBlockZ() <= Math.max(b1.getZ(), b2.getZ()) && l.getBlockZ() >= Math.min(b1.getZ(), b2.getZ())) {
					return true;
				}
				break;
			case SQUARE:
				if (l.getWorld() == b1.getWorld() && l.getBlockX() <= Math.max(b1.getX(), b2.getX()) && l.getBlockX() >= Math.min(b1.getX(), b2.getX()) && l.getBlockZ() <= Math.max(b1.getZ(), b2.getZ()) && l.getBlockZ() >= Math.min(b1.getZ(), b2.getZ())) {
					return true;
				}
				break;
			case CIRCLE:
				// circle center b1, b2 is on the perimiter
				double dist = Math.round(Math.sqrt(Math.pow(b1.getX() - b2.getX(), 2) + Math.pow(b1.getZ() - b2.getZ(), 2))); // Circle radius
				double dist2 = Math.round(Math.sqrt(Math.pow(b1.getX() - l.getBlockX(), 2) + Math.pow(b1.getZ() - l.getBlockZ(), 2))); // Distance from center
				if (dist2 <= dist) {
					return true;
				}
				break;
			case SPHERE:
				double dist3 = Math.round(Math.sqrt(Math.pow(b1.getX() - b2.getX(), 2) + Math.pow(b1.getY() - b2.getY(), 2) + Math.pow(b1.getZ() - b2.getZ(), 2))); // Circle radius
				double dist4 = Math.round(Math.sqrt(Math.pow(b1.getX() - l.getBlockX(), 2) + Math.pow(b1.getY() - l.getBlockY(), 2) + Math.pow(b1.getZ() - l.getBlockZ(), 2))); // Distance from center
				if (dist4 <= dist3) {
					return true;
				}
				break;
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