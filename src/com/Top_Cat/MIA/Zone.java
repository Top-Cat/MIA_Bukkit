package com.Top_Cat.MIA;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

class Zone {
	private Block b1, b2;
	private int id, healing;
	private boolean PvP, mobs, chest, protect, spleef, fire;
	int owner;
	private String name;
	private final MIA plugin;
	private townshape ts = townshape.CUBE;
	private List<Zone> children = new ArrayList<Zone>();
	
	public enum townshape {
		SQUARE, CIRCLE, CUBE, SPHERE
	}
	
	public Zone(MIA instance, int id, Block c1, Block c2, String name, int healing, boolean PvP, boolean mobs, boolean chest, boolean protect, boolean spleef, boolean fire, int owner, townshape ts) {
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
		this.spleef = spleef;
		this.fire = fire;
		if (spleef) {
			SpleefGame g = null;
			for (Zone i : plugin.playerListener.spleefgames.keySet()) {
				if (i.id == id && !(i instanceof Town)) {
					g = plugin.playerListener.spleefgames.get(i);
					plugin.playerListener.spleefgames.remove(i);
				}
			}
			if (g == null) {
				g = new SpleefGame(plugin, this);
			}
			plugin.playerListener.spleefgames.put(this, g);
		}
	}
	
	public Zone(MIA instance, int id, Block c1, Block c2, String name, int healing, boolean PvP, boolean mobs, boolean chest, boolean protect, boolean spleef, boolean fire, int owner) {
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
		this.spleef = spleef;
		this.fire = fire;
		if (spleef) {
			SpleefGame g = null;
			for (Zone i: plugin.playerListener.spleefgames.keySet()) {
				if (i.id == id && !(i instanceof Town)) {
					g = plugin.playerListener.spleefgames.get(i);
					plugin.playerListener.spleefgames.remove(i);
				}
			}
			if (g == null) {
				g = new SpleefGame(plugin, this);
			}
			plugin.playerListener.spleefgames.put(this, g);
		}
	}
	
	public boolean getFire() {
		return fire;
	}
	
	public void addChild(Zone z) {
		children.add(z);
	}
	
	public List<Zone> getChildren() {
		return children;
	}
	
	public Block[] cornerblocks() {
		Block[] bs = new Block[2];
		bs[0] = b1;
		bs[1] = b2;
		return bs;
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
	
	public void setPvP(boolean pvp) {
		this.PvP = pvp;
	}
	
	public void setMobs(boolean mobs) {
		this.mobs = mobs;
	}
	
	public void setHealing(int healing) {
		this.healing = healing;
	}
	
	public boolean isSpleefArena() {
		if (!spleef || (ts == townshape.CUBE && b1.getY() == b2.getY())) {
			return spleef;
		}
		return false;
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
				if (l.getWorld() == b1.getWorld() && l.getBlockX() <= Math.max(b1.getX(), b2.getX()) && l.getBlockX() >= Math.min(b1.getX(), b2.getX()) && l.getBlockY() <= Math.max(b1.getY(), b2.getY()) && l.getBlockY() >= Math.min(b1.getY(), b2.getY()) && l.getBlockZ() <= Math.max(b1.getZ(), b2.getZ()) && l.getBlockZ() >= Math.min(b1.getZ(), b2.getZ())) {
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
	
	public Location getcenter() {
		Location l = plugin.getServer().getWorlds().get(0).getSpawnLocation();
		switch (ts) {
			case CUBE:
				l.setWorld(b1.getWorld());
				l.setX((b1.getX() + b2.getX()) / 2);
				l.setZ((b1.getZ() + b2.getZ()) / 2);
				l.setY(((b1.getY() + b2.getY()) / 2) + 1);
				break;
			case SQUARE:
				l.setX((b1.getX() + b2.getX()) / 2);
				l.setZ((b1.getZ() + b2.getZ()) / 2);
				l.setY(b1.getWorld().getHighestBlockYAt((int) Math.floor((b1.getX() + b2.getX()) / 2), (int) Math.floor((b1.getZ() + b2.getZ()) / 2)));
				break;
			case CIRCLE:
			case SPHERE:
				l = b1.getLocation();
				break;
		}
		return l;
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