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
	
	public NPC(MIA instance, int id, String name, Location l, int inhand, int proxy) {
		bhn = NpcSpawner.SpawnBasicHumanNpc(String.valueOf(id), name, l.getWorld(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
		if (inhand > 0)
			bhn.getBukkitEntity().setItemInHand(new ItemStack(inhand, 1));
		
		this.proxy = proxy;
		this.id = id;
		plugin = instance;
		dialogs = instance.mf.getdialogs(this);
	}
	
	public void destroy() {
		NpcSpawner.RemoveBasicHumanNpc(bhn);
	}
	
	public void interact(Player p) {
		plugin.mf.sendmsg(p, dialogs.get((int) Math.floor(Math.random() * dialogs.size())));
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