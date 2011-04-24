package com.Top_Cat.MIA;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import npclib.NPCEntity;

public class NPC {
	
	List<String> dialogs = new ArrayList<String>();
	int proxy, id, inhand;
	final MIA plugin;
	Block chest = null;
	NPCEntity bhn;
	Location l;
	String name;
	String prefix = "";
	List<Quest> start_quests = new ArrayList<Quest>();
	List<Quest> end_quests = new ArrayList<Quest>();
	
	public NPC(MIA instance, int id, String name, Location l, int inhand, int proxy, boolean pre, String chest) {
		plugin = instance;
		
		if (pre)
			prefix = "NPC-";
		bhn = plugin.m.spawnNPC(prefix + name, new Location(l.getWorld(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch()));
		
		if (inhand > 0)
			((HumanEntity) bhn.getBukkitEntity()).setItemInHand(new ItemStack(inhand, 1));
		
		this.inhand = inhand;
		this.proxy = proxy;
		this.id = id;
		this.l = l;
		this.name = name;
		dialogs = instance.mf.getdialogs(this);
		String [] cp = chest.split(",");
		if (cp.length == 4) {
			this.chest = instance.getServer().getWorlds().get(Integer.parseInt(cp[0])).getBlockAt(Integer.parseInt(cp[1]), Integer.parseInt(cp[2]), Integer.parseInt(cp[3]));
		}
	}
	
	public void addQuestStart(Quest q) {
		start_quests.add(q);
	}
	
	public void addQuestEnd(Quest q) {
		end_quests.add(q);
	}
	
	public void destroy() {
		plugin.m.despawn(prefix + name);
	}
	
	public void update() {
		bhn.setPositionRotation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
		//plugin.m.moveNPC(prefix + name, new Location(l.getWorld(), ));
		//plugin.m.despawn(prefix + name); // Until I find a better way of broadcasting the position of the npc
		//bhn = NpcSpawner.SpawnBasicHumanNpc(String.valueOf(id), prefix + name, l.getWorld(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
		//if (inhand > 0)
		//	bhn.getBukkitEntity().setItemInHand(new ItemStack(inhand, 1));
	}
	
	public String premessage(String s) {
		return plugin.c+"[NPC] " + name + ": " + plugin.d + "f" + s;
	}
	
	public void interact(Player p) {
		boolean quests = false;
		for (Quest i : end_quests) {
			if (i.isActive(p) && i.isComplete(p)) {
				i.complete(this, p);
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
	
	public Chest getChest() {
		return (Chest) chest;
	}
	
}