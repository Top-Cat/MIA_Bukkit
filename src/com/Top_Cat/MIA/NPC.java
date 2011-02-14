package com.Top_Cat.MIA;

import org.bukkit.Location;

import redecouverte.npcspawner.NpcSpawner;

public class NPC {
	
	public NPC(int id, String name, Location l) {
		NpcSpawner.SpawnBasicHumanNpc(String.valueOf(id), name, l.getWorld(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
	}
	
}