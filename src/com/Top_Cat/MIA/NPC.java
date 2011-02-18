package com.Top_Cat.MIA;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import redecouverte.npcspawner.BasicHumanNpc;
import redecouverte.npcspawner.NpcSpawner;

public class NPC {
	
	List<String> dialogs = new ArrayList<String>();
	int proxy, id;
	final MIA plugin;
	BasicHumanNpc bhn;
	Location l;
	String name;
	String prefix = "";
	List<Quest> start_quests = new ArrayList<Quest>();
	List<Quest> end_quests = new ArrayList<Quest>();
	
	public NPC(MIA instance, int id, String name, Location l, int inhand, int proxy, boolean pre) {
		if (pre)
			prefix = "NPC-";
		bhn = NpcSpawner.SpawnBasicHumanNpc(String.valueOf(id), prefix + name, l.getWorld(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
		if (inhand > 0)
			bhn.getBukkitEntity().setItemInHand(new ItemStack(inhand, 1));
		
		this.proxy = proxy;
		this.id = id;
		this.l = l;
		this.name = name;
		plugin = instance;
		dialogs = instance.mf.getdialogs(this);
	}
	
	public void addQuestStart(Quest q) {
		start_quests.add(q);
	}
	
	public void addQuestEnd(Quest q) {
		end_quests.add(q);
	}
	
	public void destroy() {
		NpcSpawner.RemoveBasicHumanNpc(bhn);
	}
	
	public void update() {
		NpcSpawner.RemoveBasicHumanNpc(bhn); // Until I find a better way of broadcasting the position of the npc
		bhn = NpcSpawner.SpawnBasicHumanNpc(String.valueOf(id), prefix + name, l.getWorld(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
	}
	
	public String premessage(String s) {
		return plugin.c+"[NPC] " + name + ": §f" + s;
	}
	
	public void interact(Player p) {
		boolean quests = false;
		for (Quest i : end_quests) {
			if (i.isComplete(p)) {
				i.complete(p);
				return;
			}
		}
		
		List<Quest> a_quests = new ArrayList<Quest>();
		for (Quest i : start_quests) {
			if (i.avail(p)) {
				quests = true;
				a_quests.add(i);
			}
		}
		
		if (quests) {
			plugin.mf.sendmsg(p, plugin.c+"Available quests:");
			int j = 0;
			for (Quest i : a_quests) {
				plugin.mf.sendmsg(p, plugin.c+"   " + (++j) + ": " + i.getName());
			}
			plugin.mf.sendmsg(p, plugin.c+"To view a quest type: /quest view #");
			plugin.playerListener.userinfo.get(p.getDisplayName()).setQuestList(a_quests);
		} else {
			if (dialogs.size() > 0)
				plugin.mf.sendmsg(p, premessage(dialogs.get((int) Math.floor(Math.random() * dialogs.size()))));
		}
	}
	
	public boolean isEntity(Entity i) {
		return (bhn.getBukkitEntity().getEntityId() == i.getEntityId());
	}
	
	public int getId() {
		return id;
	}
	
	public int getProxyId() {
		return proxy;
	}
	
	public NPC getProxy() {
		return plugin.mf.npcs.get(proxy);
	}
	
}