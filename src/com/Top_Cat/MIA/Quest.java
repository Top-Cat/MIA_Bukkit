package com.Top_Cat.MIA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

public class Quest {
	
	private String id, name, desc, compl, datatxt, prov, rewa;
	private Type t;
	private String pre;
	private int cost, prize;
	private boolean chest;
	private MIA plugin;
	
	private List<String> completed = new ArrayList<String>();
	private HashMap<String, Integer> progress = new HashMap<String, Integer>();
	
	public enum Type {
		Harvest,
		Gather,
		Build,
		Assasin,
		Find
	}
	
	public Quest(MIA instance, String id, String name, String compl, String data, String desc, NPC start, NPC end, Type t, String q, int cost, int prize, boolean chest, String prov, String rewa) {
		plugin = instance;
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.compl = compl;
		this.datatxt = data;
		this.t = t;
		this.pre = q;
		this.cost = cost;
		this.prize = prize;
		this.chest = chest;
		
		start.addQuestStart(this);
		end.addQuestEnd(this);
	}
	
	public void setComplete(String s) {
		completed.add(s);
	}
	
	public void setProgress(String s, Integer i) {
		progress.put(s, i);
	}
	
	public boolean completed(Player p) {
		return completed.contains(p.getDisplayName().toLowerCase());
	}
	
	public boolean avail(Player p) {
		return ((pre == null || plugin.mf.quest.get(pre).completed(p)) && !completed.contains(p.getDisplayName().toLowerCase()));
	}
	
	public String getName() {
		return name;
	}
	
}