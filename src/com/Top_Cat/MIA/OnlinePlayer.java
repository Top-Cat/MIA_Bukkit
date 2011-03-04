package com.Top_Cat.MIA;

import java.util.ArrayList;
import java.util.List;

class OnlinePlayer implements Comparable<OnlinePlayer> {
  private int ammount;
  //private int cape;
  private int town, id, sg, sw;
  private String name;
  private String prefix;
  List<Quest> npc_tmp = new ArrayList<Quest>();
  Quest aquest;
  Town t;
  
  // (snip ctor, setters, etc.)

  public OnlinePlayer(MIA plugin, int bal, int cape, String name, String prefix, int town, int id, int sg, int sw) {
	  ammount = bal;
	  this.name = name;
	  this.prefix = prefix;
	  this.id = id;
	  this.town = town;
	  this.sg = sg;
	  this.sw = sw;
	  for (Town i : plugin.mf.towns) {
			if (i.getId() == town) {
				t = i;
				break;
			}
	  }
  }
  
  public List<Quest> tmpQuestList() {
	  return npc_tmp;
  }
  
  public void setQuestList(List<Quest> ql) {
	  npc_tmp = ql;
	  aquest = null;
  }
  
  public void setAQuest(Quest i) {
	  aquest = i;
  }
  
  public Quest aQuest() {
	  return aquest;
  }
  
  public void spleefplayed(boolean win) {
	  sg++;
	  if (win)
		  sw++;
  }
  
  public int[] spleefstats() {
	  int[] out = new int[2];
	  out[0] = sg;
	  out[1] = sw;
	  return out;
  }
  
  public int getBalance() {
    return ammount;
  }
  public String getName() {
    return name;
  }
  
  public int getId() {
	    return id;
  }
  
  public String getPrefix() {
	  return prefix;
  }
  
  public int getTownId() {
	  return town;
  }
  
  public Town getTown() {
	  return t;
  }
  
  public boolean cbal(int amm) {
	  if (ammount + amm > 0) {
		  ammount += amm;
		  return true;
	  }
	  return false;
  }

  public int compareTo(OnlinePlayer other) {
    if (this.ammount < other.getBalance()) {
    	return 1;
    } else if (this.ammount > other.getBalance()) {
    	return -1;
    } else {
    	return 0;
    }
  }
}