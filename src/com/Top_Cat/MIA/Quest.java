package com.Top_Cat.MIA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

public class Quest {
	
	String c = "§e";
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
	
	public String getDescription() {
		return desc;
	}
	
	public boolean isActive(Player p) {
		return progress.containsKey(p.getDisplayName().toLowerCase());
	}
	
	public int getProgress(Player p) {
		if (isActive(p))
			return progress.get(p.getDisplayName().toLowerCase());
		return 0;
	}
	
	public int totalProgress() {
		switch (t) {
			case Assasin:
				return Integer.parseInt(datatxt.split(",")[0]);
			case Harvest:
			case Gather:
			case Build:
				return Integer.parseInt(datatxt.split(",")[2]);
			case Find:
				return 1;
		}
		return 0;
	}
	
	public void dispProgress(Player p) {
		if (progress.containsKey(p.getDisplayName().toLowerCase())) {
			plugin.mf.sendmsg(p, c+ name + " Progress: " + getProgress(p) + "/" + totalProgress());	
		}
	}
	
	public void dispData(Player p, String s, String t) {
		plugin.mf.sendmsg(p, c+ name + " " + s + ": " + t);	
	}
	
}