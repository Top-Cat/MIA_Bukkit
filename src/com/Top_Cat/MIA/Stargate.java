package com.Top_Cat.MIA;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class Stargate {
	
	private Block cblock, button, bot1, bot2;
	Location tele;
	private boolean open = false;
	private boolean openp = false;
	private String network, name;
	int id;
	private Player openplayer;
	private long opentime;
	private Stargate dest;
	private HashMap<Player, Integer> dlist = new HashMap<Player, Integer>();
	private final MIA plugin;
	
	public Stargate(MIA plugin, int id, String name, String network, Block cblock, int rot) {
		int xo = 0;
		int zo = 0;
		int r = 0;
		if (rot == 0) {
			r = 270;
			zo = -1;
		} else if (rot == 1) {
			r = 0;
			xo = 1;
		} else if (rot == 2) {
			r = 90;
			zo = 1;
		} else if (rot == 3) {
			r = 180;
			xo = -1;
		}
		
		this.cblock = cblock;
		this.name = name;
		this.network = network;
		this.id = id;
		this.plugin = plugin;
		button = cblock.getRelative(xo * 3, 0, zo * 3);
		bot1 = cblock.getRelative((xo * 2) + zo, -1, (zo * 2) - xo);
		bot2 = cblock.getRelative(xo + zo, -1, zo - xo);
		tele = new Location(cblock.getWorld(), cblock.getX() + ((xo * 1.5) + (zo / 2)), cblock.getY() - 1, cblock.getZ() + ((zo * 1.5) - (xo / 2)), r, 0);
	}
	
	public void changeGateState(boolean state, Player p) {
		changeGateState(state, p, false);
	}
	
	public void changeGateState(boolean state, Player p, boolean f) {
		if (state || p == openplayer) {
			if (state != open) {
				open = state;
				if (open) {
					cblock.getWorld().loadChunk(cblock.getChunk());
					openplayer = p;
					Date d = new Date();
					opentime = d.getTime();
					if (bot1.getType() != Material.PORTAL) { bot1.setType(Material.FIRE); }
					dest = getNetworkedGates().get(dlist.get(p));
					dlist.remove(p);
					openp = !f;
					if (!f) { dest.changeGateState(true, p, true); }
				} else {
					resetSign();
					bot1.setType(Material.AIR);
				}
			}
		}
	}
	
	private List<Stargate> getNetworkedGates() {
		List<Stargate> nt = (List<Stargate>) ((ArrayList) plugin.mf.networks.get(network)).clone();
		nt.remove(this);
		return nt;
	}
	
	public void incrementDest(Player p) {
		int gpos = 0;
		if (dlist.containsKey(p)) {
			gpos = dlist.get(p) + 1;
		}
		List<Stargate> nt = getNetworkedGates();
		if (gpos >= nt.size()) {
			gpos = 0;
		}
		
		int t = gpos - 1;
		int m = gpos + 1;
		if (t < 0) { t = 0; m++; }
		if (m >= nt.size()) { m = nt.size() - 1; if (nt.size() > 2) { t--; } }
		
		Sign si = ((Sign) cblock.getState());
		si.setLine(0, "- " + name + " -");
		
		int l = 0;
		for (l = t; l <= m; l++) {
			String li = nt.get(l).name;
			if (l == gpos) li = " > " + li + " < ";
			// j = l - t;
    		si.setLine((l - t) + 1, li);
		}
		for (int l2 = (l - t) + 1; l2 < 4; l2++) {
			si.setLine(l2, "");
		}
		si.update();
		
		dlist.put(p, gpos);
	}
	
	public Block getBlock() {
		return cblock;
	}
	
	private void resetSign() {
		Sign si = (Sign) cblock.getState();
		si.setLine(0, "--" + name + "--");
		si.setLine(1, "Right click to");
        si.setLine(2, "use the gate");
        si.setLine(3, " (" + network + ") ");
        si.update();
	}
	
	public boolean getOpen() {
		return open;
	}
	
	public String getName() {
		return name;
	}
	
	public Block getButtonBlock() {
		return button;
	}
	
	public Location pMove(Player p, Location l) {
		if (open) {
			Date d = new Date();
			if (d.getTime() - opentime > 90000) {
				changeGateState(false, p);
				dest.changeGateState(false, p);
			}
			if (openp && p == openplayer && (l.getBlock() == bot1 || l.getBlock() == bot2)) {
				changeGateState(false, p);
				dest.changeGateState(false, p);
				return dest.tele;
			}
		}
		return l;
	}
	
}