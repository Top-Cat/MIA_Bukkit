package com.Top_Cat.MIA;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

public class MIAVehicleListener extends VehicleListener {
    
    final MIA plugin;
    private HashMap<Vehicle, Double[]> heldcarts = new HashMap<Vehicle, Double[]>();
    
    final timer timer;
    
    public class timer implements Runnable {
        
        @Override
        public void run() {
            for (Vehicle i : heldcarts.keySet()) {
                Double[] j = heldcarts.get(i);
                j[0]--;
                if (j[0] <= 0) {
                    heldcarts.remove(i);
                    int x = 8;
                    int z = 8;
                    if (j[1] < 0) {
                        x *= -1;
                    }
                    if (j[2] < 0) {
                        z *= -1;
                    }
                    setVelocity(i, x, z);
                } else {
                    if (i.getPassenger() instanceof Player) {
                        plugin.mf.sendmsg((Player) i.getPassenger(), "Countdown :" + j[0]);
                    }
                    heldcarts.put(i, j);
                }
            }
        }
        
    }
    
    public MIAVehicleListener(MIA instance) {
        plugin = instance;
        timer = new timer();
    }
    
    public void increaseVelocity(Vehicle v, int amm) {
        Vector newVelocity = v.getVelocity();
        
        if (newVelocity.getX() > 0) {
            newVelocity.setX(newVelocity.getX() + amm);
        } else {
            newVelocity.setX(newVelocity.getX() - amm);
        }
        
        if (newVelocity.getZ() > 0) {
            newVelocity.setZ(newVelocity.getZ() + amm);
        } else {
            newVelocity.setZ(newVelocity.getZ() - amm);
        }
        v.setVelocity(newVelocity);
    }
    
    public void setVelocity(Vehicle v, int amm) {
        setVelocity(v, amm, amm);
    }
    
    public void setVelocity(Vehicle v, int x, int z) {
        Vector newVelocity = v.getVelocity();
        newVelocity.setX(x);
        newVelocity.setZ(z);
        v.setVelocity(newVelocity);
    }
    
    @Override
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ() || event.getFrom().getBlockY() != event.getTo().getBlockY()) {
            Block b = event.getTo().getWorld().getBlockAt(event.getTo().getBlockX(), event.getTo().getBlockY() - 1, event.getTo().getBlockZ());
            if (!b.isBlockPowered()) {
                switch(b.getType()) {
                    case GOLD_BLOCK:
                        increaseVelocity(event.getVehicle(), 8);
                        break;
                    case IRON_BLOCK:
                        event.getVehicle().eject();
                        break;
                    case OBSIDIAN:
                        Double[] i = new Double[3];
                        i[0] = 6.0;
                        i[1] = event.getVehicle().getVelocity().getX();
                        i[2] = event.getVehicle().getVelocity().getZ();
                        setVelocity(event.getVehicle(), 0);
                        heldcarts.put(event.getVehicle(), i);
                        break;
                }
            }
        }
    }
    
}