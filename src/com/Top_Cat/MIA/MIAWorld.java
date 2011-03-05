package com.Top_Cat.MIA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

public class MIAWorld {
	
	private List<String> commands = new ArrayList<String>();
	private List<Player> users = new ArrayList<Player>();
	private boolean townchat, mobs, pvp;
	private int healing, id;
	private String name;
	private final MIA plugin;
	
	public MIAWorld(MIA instance, int id, String name, String coms, boolean townchat, boolean mobs, boolean pvp, int healing) {
		commands = Arrays.asList(coms.split(","));
		this.townchat = townchat;
		this.mobs = mobs;
		this.pvp = pvp;
		this.healing = healing;
		this.name = name;
		plugin = instance;
		this.id = id;
	}
	
	public boolean canCommand(String com0) {
		return commands.contains(com0);
	}
	
	public int getId() {
		return id;
	}
	
	public boolean townChat() {
		return townchat;
	}
	
	public Player[] getplayers() {
		getplayersList();
		Player[] out = new Player[users.size()];
		int j = 0;
		for (Player i : users) {
			out[j++] = i;
		}
		return out;
	}
	
	public List<Player> getplayersList() {
		users.clear();
		for (Player i : plugin.getServer().getOnlinePlayers()) {
			if (i.getLocation().getWorld().getName().equals(name)) {
				users.add(i);
			}
		}
		return users;
	}
	
	public void addPlayer(Player p) {
		users.add(p);
	}
	
	public void removePlayer(Player p) {
		users.remove(p);
	}
	
	public boolean isPvP() {
		return pvp;
	}
	
	public boolean isMobs() {
		return mobs;
	}
	
	public int getHealing() {
		return healing;
	}
	
}