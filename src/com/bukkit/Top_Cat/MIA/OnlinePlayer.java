package com.bukkit.Top_Cat.MIA;

class OnlinePlayer implements Comparable<OnlinePlayer> {
  private int ammount;
  //private int cape;
  private int town, id, sg, sw;
  private String name;
  private String prefix;
  
  // (snip ctor, setters, etc.)

  public OnlinePlayer(int bal, int cape, String name, String prefix, int town, int id, int sg, int sw) {
	  ammount = bal;
	  this.name = name;
	  this.prefix = prefix;
	  this.id = id;
	  this.town = town;
	  this.sg = sg;
	  this.sw = sw;
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
  
  public int getTown() {
	  return town;
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