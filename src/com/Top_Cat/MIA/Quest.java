package com.Top_Cat.MIA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Quest {
    private String id, name, desc, compl, datatxt, prov, rewa;
    private Type t;
    private String pre;
    private int cost, prize;
    //private boolean chest;
    private MIA plugin;
    
    private List<String> completed = new ArrayList<String>();
    private HashMap<String, Integer> progress = new HashMap<String, Integer>();
    
    protected int[] changeblocks = new int [350];
        
    
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
        //this.chest = chest;
        this.rewa = rewa;
        this.prov = prov;
        
        start.addQuestStart(this);
        end.addQuestEnd(this);
        
        for (int i = 0; i < 350; i++)
            changeblocks[i] = i;
        changeblocks[1] = 4;
        changeblocks[2] = 3;
        changeblocks[16] = 263;
        changeblocks[56] = 264;
        changeblocks[55] = 331;
        changeblocks[60] = 3;
        changeblocks[63] = 323;
        changeblocks[68] = 323;
        changeblocks[73] = 331;
        changeblocks[74] = 331;
        changeblocks[75] = 76;
        changeblocks[78] = 332;
    }
    
    public void setComplete(String s) {
        completed.add(s.toLowerCase());
    }
    
    public void setProgress(String s, Integer i) {
        progress.put(s, i);
    }
    
    public boolean avail(Player p) {
        return (!isActive(p) && (pre == null || plugin.mf.quest.get(pre).isComplete(p)) && !isComplete(p));
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return desc;
    }
    
    public Set<String> getPlayers() {
        return progress.keySet();
    }
    
    public boolean isActive(Player p) {
        return progress.containsKey(p.getDisplayName().toLowerCase());
    }
    
    public int getProgress(Player p) {
        if (isActive(p)) {
            int pro = progress.get(p.getDisplayName().toLowerCase()); 
            switch (t) {
                case Harvest:
                    if (countItems(p, Integer.parseInt(datatxt.split(",")[1])) < pro) {
                        pro = countItems(p, Integer.parseInt(datatxt.split(",")[1]));
                    }
                case Gather:
                    pro = countItems(p, Integer.parseInt(datatxt.split(",")[1]));
            }
            if (pro > totalProgress()) {
                return totalProgress();
            }
            return pro;
        }
        return 0;
    }
    
    public int countItems(Player p, int itemid) {
        int out = 0;
        for (ItemStack i : p.getInventory().getContents()) {
            if (i.getTypeId() == changeblocks[itemid]) {
                out += i.getAmount();
            }
        }
        return out;
    }
    
    public boolean isComplete(Player p) {
        return  (isActive(p) && getProgress(p) >= totalProgress()) || (!isActive(p) && completed.contains(p.getDisplayName().toLowerCase()));
    }
    
    public void harvest(Player p, int itemid) {
        if (t == Type.Harvest && isActive(p) && !isComplete(p) && Integer.parseInt(datatxt.split(",")[1]) == itemid) {
            incrementProgress(p, 1);
        }
    }
    
    public void build(Player p, int itemid) {
        if (t == Type.Build && isActive(p) && !isComplete(p) && Integer.parseInt(datatxt.split(",")[1]) == itemid) {
            incrementProgress(p, 1);
        }
    }
    
    public void kill(Player p, String killed) {
        if (t == Type.Assasin && isActive(p) && killed.replace("Craft", "").equalsIgnoreCase(datatxt.split(",")[1])) {
            incrementProgress(p, 1);
        }
    }
    
    public void move(Player p, Location l) {
        if (t == Type.Find) {
            String[] bd = datatxt.split(",");
            Integer[] bdi = new Integer[bd.length];
            int j = 0;
            for (String i : bd)
                bdi[j++] = Integer.parseInt(i);
            
            if (plugin.getServer().getWorlds().indexOf(l.getWorld()) == bdi[0] && l.getBlockX() <= Math.max(bdi[1], bdi[4]) && l.getBlockX() >= Math.min(bdi[1], bdi[4]) && l.getBlockY() <= Math.max(bdi[2], bdi[5]) && l.getBlockY() >= Math.min(bdi[2], bdi[5]) && l.getBlockZ() <= Math.max(bdi[3], bdi[6]) && l.getBlockZ() >= Math.min(bdi[3], bdi[6])) {
                incrementProgress(p, 1);
            }
        }
    }
    
    public void incrementProgress(Player p, int i) {
        progress.put(p.getDisplayName().toLowerCase(), progress.get(p.getDisplayName().toLowerCase()) + i);
        // Update SQL
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
            if (isComplete(p)) {
                plugin.mf.sendmsg(p, plugin.c+ name + " Progress: Complete!");
            } else {
                plugin.mf.sendmsg(p, plugin.c+ name + " Progress: " + getProgress(p) + "/" + totalProgress());
            }
        }
    }
    
    public void dispData(Player p, String s, String t) {
        plugin.mf.sendmsg(p, plugin.c+ name + " " + s + ": " + t);    
    }
    
    public void accept(Player p) {
        if (!isActive(p) && !isComplete(p)) {
            if (cost != 0) {
                if (!plugin.playerListener.cbal(p, -cost)) { // Give user their moneys
                    return;
                }
                plugin.mf.sendmsg(p, plugin.c+ prize + " ISK was taken to pay for the quest");
            }
            plugin.mf.sendmsg(p, plugin.c+ "Accepted quest " + name);
            
            progress.put(p.getDisplayName().toLowerCase(), 0);
            
            if (prov != null && prov.length() > 0) {
                String[] itemi = prov.split(":");
                String[] itemj = itemi[1].split(" ");
                
                ItemStack k = new ItemStack(Integer.parseInt(itemj[0]), Integer.parseInt(itemj[1]));
                p.getInventory().addItem(k);
                
                plugin.mf.sendmsg(p, plugin.c+ "You received " + Integer.parseInt(itemj[1]) + " " + itemi[0]);
            }
            
            plugin.mf.quest_accept(this, p);
        }
    }
    
    public void show(Player p) {
        plugin.mf.sendmsg(p, " ");
        plugin.mf.sendmsg(p, plugin.c+ "Quest: " + name);
        plugin.mf.sendmsg(p, " ");
        String [] descLines = desc.split("@");
        for (String s : descLines) {
            String temp = s;
            while (temp.length() > 60) {
                int lastSpace = temp.substring(0,60).lastIndexOf(' ');
                plugin.mf.sendmsg(p, plugin.c+ temp.substring(0,lastSpace));
                temp = temp.substring(lastSpace+1);
            }
            plugin.mf.sendmsg(p, plugin.c+ temp);
        }
        if (prov != null && !prov.equals("")) {
            plugin.mf.sendmsg(p, plugin.c+ "   Provided: " + prov.split(":")[0]);
        }
        if (rewa != null && !rewa.equals("")) {
            plugin.mf.sendmsg(p, plugin.c+ "   Reward: " + rewa.split(":")[0]);
        }
        if (cost > 0) {
            plugin.mf.sendmsg(p, plugin.c+ "   Cost: " + cost);
        }
        if (prize > 0) {
            plugin.mf.sendmsg(p, plugin.c+ "   Prize: " + prize);
        }
        plugin.mf.sendmsg(p, plugin.c+ "Type '/quest accept' to accept this quest.");
    }
    
    public void complete(NPC n, Player p) {
        if (isComplete(p)) {
            // You completed! YAY!
            if (prize != 0) {
                plugin.playerListener.cbal(p, prize); // Give user their moneys
                plugin.mf.sendmsg(p, plugin.c+ "You received " + prize + " ISK");
            }
            plugin.mf.sendmsg(p, plugin.c+compl);
            
            // If gather or harvest take items and move to chest
            if (t == Type.Gather || t == Type.Harvest) {
                int itemid = Integer.parseInt(datatxt.split(",")[1]);
                int amm = Integer.parseInt(datatxt.split(",")[2]);
                for (ItemStack i : p.getInventory().getContents()) {
                    if (amm > 0 && i.getTypeId() == changeblocks[itemid]) {
                        int of = amm - i.getAmount();
                        if (of >= 0) {
                            amm -= i.getAmount();
                            p.getInventory().removeItem(i);
                            n.getChest().getInventory().addItem(i);
                        } else {
                            int amm2 = i.getAmount() - Math.abs(of);
                            amm = 0;
                            i.setAmount(Math.abs(of));
                            n.getChest().getInventory().addItem(new ItemStack(i.getType(), amm2));
                        }
                        
                    }
                }
            }
            
            if (rewa != null && rewa.length() > 0) {
                String[] itemi = rewa.split(":");
                String[] itemj = itemi[1].split(" ");
                
                ItemStack k = new ItemStack(Integer.parseInt(itemj[0]), Integer.parseInt(itemj[1]));
                p.getInventory().addItem(k);
                
                plugin.mf.sendmsg(p, plugin.c+ "You received " + Integer.parseInt(itemj[1]) + " " + itemi[0]);
            }
            
            plugin.mf.quest_complete(this, p);
            progress.remove(p.getDisplayName().toLowerCase());
            completed.add(p.getDisplayName().toLowerCase());
            plugin.mf.post_tweet(p.getDisplayName() + " completed the quest '" + name + "'! Their stats: http://thomasc.co.uk/minecraft/" + p.getDisplayName()+ "/");
        }
    }
}