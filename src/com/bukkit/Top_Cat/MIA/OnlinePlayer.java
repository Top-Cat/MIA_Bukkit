package com.bukkit.Top_Cat.MIA;

class OnlinePlayer implements Comparable<OnlinePlayer> {
  private int ammount;
  //private int cape;
  private int town;
  private String name;
  private String prefix;
  private int id;
  
  // (snip ctor, setters, etc.)

  public OnlinePlayer(int bal, int cape, String name, String prefix, int town, int id) {
	  ammount = bal;
	  this.name = name;
	  this.prefix = prefix;
	  this.id = id;
	  this.town = town;
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