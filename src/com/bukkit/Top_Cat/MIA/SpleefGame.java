package com.bukkit.Top_Cat.MIA;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SpleefGame {
	private class SpleefPlayer {

		SpleefGame g;
		boolean alive = false;
		Location priorloc;
		Player player;
		boolean ready = false;
		
		public SpleefPlayer(Player p, SpleefGame g) {
			player = p;
			priorloc = p.getLocation();
			this.g = g;
			
			p.teleportTo(g.z.getcenter());
		}
		
		public void reset() {
			player.teleportTo(g.z.getcenter());
			alive = false;
			ready = false;
		}
		
		public void leaveGame() {
			player.teleportTo(priorloc);
		}
		
		public void setReady(boolean b) {
			ready = b;
		}
		
		public boolean isReady() {
			return ready;
		}
		
		public void setAlive(boolean b) {
			alive = b;
		}
		
		public boolean isAlive() {
			return alive;
		}
		
	}
	
	Zone z;
	int max;
	int ythreshold;
	boolean activegame = false;
	final MIA plugin;
	HashMap<Player, SpleefPlayer> players = new HashMap<Player, SpleefPlayer>();
	ArrayList<ArrayList<ArrayList<Integer>>> pattern = new ArrayList<ArrayList<ArrayList<Integer>>>();
	
	public void move(Player p, Location nl) {
		if (activegame && players.get(p).isAlive()) {
			if (nl.getBlockY() < ythreshold) {
				players.get(p).setAlive(false);
				plugin.mf.sendmsg(getPlayers(), p.getDisplayName() + " dropped! (" + alive() + " players left)");
				gameCheck();
			}
		}
	}
	
	public int alive() {
		int alive = 0;
		for (SpleefPlayer i : players.values()) {
			if (i.isAlive()) {
				alive++;
			}
		}
		return alive;
	}
	
	public void gameCheck() {
		if (alive() < 2) {
			Player w = null;
			for (SpleefPlayer i : players.values()) {
				if (i.isAlive()) {
					w = i.player;
				}
			}
			
			// Game over
			for (SpleefPlayer i : players.values()) {
				i.reset();
			}
			if (w != null) {
				plugin.mf.sendmsg(getPlayers(), w.getDisplayName() + " won the game!");
			}
			activegame = false;
			resetfloor();
		}
	}
	
	public Player[] getPlayers() {
		Player[] pls = new Player[players.size()];
		int j = 0;
		for (SpleefPlayer i : players.values()) {
			pls[j++] = i.player;
		}
		return pls;
	}
	
	public void resetfloor() {
		Block[] bs = z.cornerblocks();
		int x = Math.min(bs[0].getX(), bs[1].getX());
		int z = Math.min(bs[0].getZ(), bs[1].getZ());
		int w = Math.max(bs[0].getX(), bs[1].getX()) - x;
		int h = Math.max(bs[0].getZ(), bs[1].getZ()) - z;
		ythreshold = bs[0].getY();
		for (int i = 0; i <= w; i++) {
			for (int j = 0; j <= h; j++) {
				bs[0].getWorld().getBlockAt(x + i, bs[0].getY(), z + j).setTypeId(pattern.get(0).get(i).get(j));
				bs[0].getWorld().getBlockAt(x + i, bs[0].getY(), z + j).setData((byte) (int) pattern.get(1).get(i).get(j));
			}
		}
	}
	
	public SpleefGame(MIA instance, Zone z) {
		this.z = z;
		this.plugin = instance;
		max = instance.mf.spleefmax(z.getId());
		pattern = instance.mf.getpattern(z.getId());
	}
	
	public boolean playerPlaying(Player p) {
		return players.containsKey(p);
	}
	
	public boolean addPlayer(Player p) {
		if (players.size() < max && !activegame) {
			if (players.containsKey(p)) {
				return false;
			}
			resetfloor();
			players.put(p, new SpleefPlayer(p, this));
			plugin.mf.sendmsg(getPlayers(), p.getDisplayName() + " joined the spleef!");
			return true;
		}
		return false;
	}
	
	public void removePlayer(Player p) {
		players.get(p).leaveGame();
		players.remove(p);
		plugin.mf.sendmsg(getPlayers(), p.getDisplayName() + " left the spleef!");
		
		if (players.size() < 2) {
			activegame = false;
			resetfloor();
			for (SpleefPlayer i : players.values()) {
				i.reset();
			}
		}
	}
	
	public void setReadyPlayer(Player p, boolean b) {
		players.get(p).setReady(b);
		if (players.size() > 1) {
			boolean startgame = true;
			for (SpleefPlayer i : players.values()) {
				if (!i.isReady()) {
					startgame = false;
				}
			}
			
			if (startgame) {
				//Countdown!
				for (SpleefPlayer i : players.values()) {
					i.setAlive(true);
				}
				activegame = true;
				plugin.mf.sendmsg(getPlayers(), "Game started!");
			}
		}
	}
}
