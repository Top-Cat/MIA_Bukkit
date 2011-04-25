package com.Top_Cat.MIA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import com.Top_Cat.MIA.Quest.Type;

/**
 * MIA block listener
 * @author Thomas Cheyney
 */
public class MIABlockListener extends BlockListener {
    private final MIA plugin;
    
    public MIABlockListener(final MIA plugin) {
        this.plugin = plugin;
        spades.add(Material.WOOD_SPADE);
        spades.add(Material.STONE_SPADE);
        spades.add(Material.IRON_SPADE);
        spades.add(Material.GOLD_SPADE);
        spades.add(Material.DIAMOND_SPADE);
        
        spade_fast.add(Material.DIRT);
        spade_fast.add(Material.GRASS);
        spade_fast.add(Material.SAND);
        spade_fast.add(Material.GRAVEL);
        spade_fast.add(Material.CLAY);
        spade_fast.add(Material.WOOL);
        
        picks.add(Material.WOOD_PICKAXE);
        picks.add(Material.STONE_PICKAXE);
        picks.add(Material.IRON_PICKAXE);
        picks.add(Material.GOLD_PICKAXE);
        picks.add(Material.DIAMOND_PICKAXE);
        
        pick_fast.add(Material.COAL_ORE);
        pick_fast.add(Material.BEDROCK);
        pick_fast.add(Material.IRON_ORE);
        pick_fast.add(Material.STONE);
        pick_fast.add(Material.COBBLESTONE);
        pick_fast.add(Material.SANDSTONE);
        pick_fast.add(Material.MOSSY_COBBLESTONE);
        pick_fast.add(Material.GOLD_ORE);
        pick_fast.add(Material.DIAMOND_ORE);
        pick_fast.add(Material.OBSIDIAN);
        pick_fast.add(Material.MOB_SPAWNER);
        pick_fast.add(Material.LOCKED_CHEST);
        pick_fast.add(Material.COBBLESTONE_STAIRS);
        pick_fast.add(Material.BEDROCK);
        
        axe.add(Material.WOOD_AXE);
        axe.add(Material.STONE_AXE);
        axe.add(Material.IRON_AXE);
        axe.add(Material.GOLD_AXE);
        axe.add(Material.DIAMOND_AXE);
        
        axe_fast.add(Material.WOOD);
        axe_fast.add(Material.LOG);
        axe_fast.add(Material.LEAVES);
        tools.addAll(axe);
        tools.addAll(picks);
        tools.addAll(spades);
        tools.add(Material.FISHING_ROD);
    }
    
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
    	if (event.getBlock().getType() == Material.TNT && event.getBlock().getWorld().getName() != "Creative") {
    		if (plugin.mf.insidetown(event.getBlock()) > 0 || !((Zone) plugin.mf.insidezone(event.getBlock(), true)).isMobs()) {
        		event.setCancelled(true);
        		event.getBlock().setType(Material.AIR);
        		/*for (org.bukkit.inventory.ItemStack i : event.getPlayer().getInventory().getContents()) {
        			if (i.getType() == Material.TNT) {
        				 event.getPlayer().getInventory().remove(i);
        			}
        		}*/
        	}
    	}
    	event.setCancelled(useitem_block(event.getBlock(), event.getPlayer()));
    	if (!event.isCancelled()) {
    		for (Quest i : plugin.mf.quest_sort.get(Type.Build).values()) {
    			i.build(event.getPlayer(), event.getBlock().getTypeId());
    		}
    	}
	}
    
    public boolean useitem_block(Block b, Player p) {
    	Zone z = ((Zone) plugin.mf.insidezone(b, true));
		if (z.isProtected(p) && !p.isOp()) {
			return true;
		} else {
			plugin.mf.updatestats(p, 1, b.getTypeId());
			plugin.mf.updatestats(p, 2, 8, 1);
		}
		
		for (Zone i : plugin.mf.zones) {
			if (i.inZone(b.getLocation()) && i.isSpleefArena() && plugin.playerListener.spleefgames.containsKey(i)) {
				if (plugin.playerListener.spleefgames.get(i).activegame) {
					return true;
				}
			}
    	}
		return false;
    }
    
    ArrayList<Block> b = new ArrayList<Block>();
    List<Material> spades = new ArrayList<Material>();
    List<Material> tools = new ArrayList<Material>();
    List<Material> spade_fast = new ArrayList<Material>();
    List<Material> picks = new ArrayList<Material>();
    List<Material> pick_fast = new ArrayList<Material>();
    List<Material> axe = new ArrayList<Material>();
    List<Material> axe_fast = new ArrayList<Material>();
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
		if (b.contains(event.getBlock())) {
			b.remove(event.getBlock());
    		event.setCancelled(true);
    		event.getBlock().setType(Material.AIR);
		}
		
		for (Quest i : plugin.mf.quest_sort.get(Type.Harvest).values()) {
				i.harvest(event.getPlayer(), event.getBlock().getTypeId());
		}
		plugin.mf.updatestats(event.getPlayer(), 0, event.getBlock().getTypeId());
		plugin.mf.updatestats(event.getPlayer(), 2, 9, 1);
    	
    	if (plugin.mf.stargateBlocks.containsKey(event.getBlock())) {
    		Stargate s = plugin.mf.stargateBlocks.get(event.getBlock());
    		Town t = s.getTown();
    		if (!event.getPlayer().isOp() && !(t != null && t.getMayor().isPlayer(event.getPlayer()))) {
    			event.setCancelled(true);
    		} else {
    			s.destroy(event.getPlayer());
    		}
    	}
    }
    
    @Override
    public void onBlockDamage(BlockDamageEvent event) {
    	if (event.getBlock().getType() == Material.TNT) {
    		int tnttown = plugin.mf.insidetown(event.getBlock().getLocation());
    		if (tnttown > 0 && !(tnttown == plugin.playerListener.userinfo.get(event.getPlayer().getDisplayName()).getTownId())) {
    			event.setCancelled(true);
    		}
    	}
    	Zone z = ((Zone) plugin.mf.insidezone(event.getBlock(), true));
		if (z.isProtected(event.getPlayer()) && !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
		
    	for (Zone i : plugin.mf.zones) {
			if (i.inZone(event.getBlock().getLocation()) && i.isSpleefArena() && plugin.playerListener.spleefgames.containsKey(i)) {
				if (plugin.playerListener.spleefgames.get(i).activegame && plugin.playerListener.spleefgames.get(i).playerPlaying(event.getPlayer())) {
					event.getBlock().setType(Material.AIR);
				} else {
					event.setCancelled(true);
				}
			}
    	}
    	
    	if (!event.isCancelled() && event.getBlock().getWorld().getName().equals("Creative")) {
    		if (event.getBlock().getType() == Material.TNT || (axe_fast.contains(event.getBlock().getType()) && axe.contains(event.getPlayer().getItemInHand().getType())) || (pick_fast.contains(event.getBlock().getType()) && picks.contains(event.getPlayer().getItemInHand().getType())) || (spade_fast.contains(event.getBlock().getType()) && spades.contains(event.getPlayer().getItemInHand().getType()))) {
    			event.setCancelled(true);
    			
	    		if (plugin.mf.stargateBlocks.containsKey(event.getBlock())) {
	    			Stargate s = plugin.mf.stargateBlocks.get(event.getBlock());
	        		Town t = s.getTown();
	    			if (!event.getPlayer().isOp() && !(t != null && t.getMayor().isPlayer(event.getPlayer()))) {
	    				return;
	    			} else {
	    				s.destroy(event.getPlayer());
	    			}
	    		}
	    		
	    		plugin.mf.updatestats(event.getPlayer(), 0, event.getBlock().getTypeId());
	    		plugin.mf.updatestats(event.getPlayer(), 2, 9, 1);
    			
	    		event.getBlock().setType(Material.AIR);
    		}
    	}
    }
    
    HashMap<Block, HashMap<Player, Integer>> signdes = new HashMap<Block, HashMap<Player, Integer>>();
    
    public void checklift(World w, Player p, int x, int y, int z, Location pl) {
    	if (w.getBlockAt(x, y, z).getType() == Material.WALL_SIGN) {
    		double xd = x;
			double zd = z;
			xd = pl.getX();
			zd = pl.getZ();
			x = pl.getBlockX();
			z = pl.getBlockZ();
			while (w.getBlockAt(x, y, z).getType() == Material.AIR || w.getBlockAt(x, y, z).getType() == Material.WALL_SIGN) {
				y--;
			}
			double yd = y; 
			if (w.getBlockAt(x, y, z).getType() == Material.STEP) {
				yd += .5;
			} else {
				yd++;
			}
			plugin.mf.teleport(p, new Location(w, xd, yd, zd, p.getLocation().getYaw(), p.getLocation().getPitch()));
		}
    }
    
    @Override
    public void onSignChange(SignChangeEvent event) {
    	if (event.getBlock().getType() == Material.WALL_SIGN) {
	    	int nx = 0;
	    	int nz = 0;
	    	int rot = 0;
	    	switch (new org.bukkit.material.Sign(Material.WALL_SIGN, event.getBlock().getData()).getAttachedFace()) {
	    		case WEST:
	    			rot = 3;
	    			nz = 1;
	    			break;
	    		case EAST:
	    			rot = 1;
	    			nz = -1;
	    			break;
	    		case NORTH:
	    			rot = 0;
	    			nx = -1;
	    			break;
	    		case SOUTH:
	    			rot = 2;
	    			nx = 1;
	    			break;
	    	}
	    	boolean canmake = true;
	    	boolean town = false;
	    	String network = "";
	    	Town t = plugin.playerListener.userinfo.get(event.getPlayer().getDisplayName()).getTown();
	    	if (event.getPlayer().isOp() && event.getLine(2).length() > 0) {
	    		network = event.getLine(2).toLowerCase();
	    	} else if (t != null && t.getMayor() == event.getPlayer() && plugin.mf.notothertown(event.getPlayer()) && event.getBlock().getWorld().getName().equals("Final")) {
	    		network = plugin.playerListener.userinfo.get(event.getPlayer().getDisplayName()).getTown().getName().toLowerCase();
	    		town = true;
	    	} else {
	    		canmake = false;
	    	}
			if (event.getBlock().getType() == Material.WALL_SIGN && canmake) {
				// Might our user be building a gate?
				boolean isgate = true;
				Block[] bl = new Block[14];
				bl[13] = event.getBlock().getRelative(nx, 0, nz);
				bl[0] = event.getBlock().getRelative(nx, 1, nz); // Left column
				bl[1] = event.getBlock().getRelative(nx, 2, nz);
				bl[2] = event.getBlock().getRelative(nx, -1, nz);
				bl[3] = event.getBlock().getRelative(nx, -2, nz);
				
				bl[4] = event.getBlock().getRelative(nx - nz, 2, nz + nx); // Top
				bl[5] = event.getBlock().getRelative(nx + (nz * -2), 2, nz + (nx * 2));
				bl[6] = event.getBlock().getRelative(nx + (nz * -3), 2, nz + (nx * 3));
				
				bl[7] = event.getBlock().getRelative(nx - nz, -2, nz + nx); // Bottom
				bl[8] = event.getBlock().getRelative(nx + (nz * -2), -2, nz + (nx * 2));
				bl[9] = event.getBlock().getRelative(nx + (nz * -3), -2, nz + (nx * 3));
				
				bl[10] = event.getBlock().getRelative(nx + (nz * -3), 1, nz + (nx * 3)); // Right column
				bl[11] = event.getBlock().getRelative(nx + (nz * -3), 0, nz + (nx * 3));
				bl[12] = event.getBlock().getRelative(nx + (nz * -3), -1, nz + (nx * 3));
				
				for (Block i : bl) {
					if (i.getType() != Material.OBSIDIAN) {
						isgate = false;
						//plugin.mf.sendmsg(event.getPlayer(), i.getX() + "," + i.getY() + "," + i.getZ());
					}
				}
				
				if (isgate) {
					if (town) {
						town = !plugin.playerListener.cbal(event.getPlayer(), -1000);
						plugin.mf.sendmsg(event.getPlayer(), plugin.d+"2Debited 1000 ISK. This will be refunded when you destroy the gate.");
					}
					if (!town) {
						plugin.mf.newgate(event.getBlock(), event.getLine(0), network, rot);
						plugin.mf.sendmsg(event.getPlayer(), plugin.d+"2Gate created and active!");
					}
				}
			}
    	}
    	String l2 = event.getLine(2);
    	if (l2.startsWith("Town Id #")) {
    		Town t = null;
    		for (Town i : plugin.mf.towns) {
    			if (i.getId() == Integer.parseInt(l2.substring(9))) {
    				t = i;
    				break;
    			}
    		}
    		
    		if (t != null) {
	    		event.setLine(0, "- " + t.getTownType().toString() + " -");
	    		event.setLine(1, t.getName());
	    		event.setLine(3, "Pop. " + t.getplayers().length);
    		}
    	}
    	for (int i = 0; i < 4; i++) {
			Pattern p = Pattern.compile("&([0-9a-f])");
			Matcher m = p.matcher(event.getLine(i));
			event.setLine(i, m.replaceAll(plugin.d + "$1"));	
		}
    }
    
    public boolean dogatecol(HashMap<Block, Boolean> fb, int x, int y, int z, World w, int t) {
    	if (!fb.containsKey(w.getBlockAt(x, y, z))){
    		int tmpy = y;
    		if (w.getBlockAt(x, y, z).getType() != Material.FENCE) { return false; }
	    	for (int y1 = y + 1; y1 <= y + 12; y1++) {
	            if (w.getBlockAt(x, y1, z).getType() == Material.FENCE) {
	                y = y1;
	            } else {
	                break;
	            }
	        }
    	
	    	if (w.getBlockAt(x, y + 1, z).getType() == Material.AIR) {
	            return false;
	        }
	    	
	    	if (!fb.containsKey(w.getBlockAt(x, y, z))){
	    		fb.put(w.getBlockAt(x, tmpy, z), false);
		    	fb.put(w.getBlockAt(x, y, z), true);
		    	Boolean close;
		    	if (t == -2) {
		    		close = w.getBlockAt(x, y - 1, z).getType() != Material.FENCE;
		    	} else {
		    		close = t != 0;
		    	}
		    	
		    	int minY = Math.max(0, y - 12);
		    	for (int y1 = y - 1; y1 >= minY; y1--) {
		    		Material cur = w.getBlockAt(x, y1, z).getType();
		
		            // Allowing water allows the use of gates as flood gates
		            if (cur != Material.WATER
		                    && cur != Material.STATIONARY_WATER
		                    && cur != Material.LAVA
		                    && cur != Material.STATIONARY_LAVA
		                    && cur != Material.FENCE
		                    && cur != Material.AIR) {
		                break;
		            }
		            
		            w.getBlockAt(x, y1, z).setType(close ? Material.FENCE : Material.AIR);
		            
		            dogatecol(fb, x + 1, y1, z, w, t);
		            dogatecol(fb, x - 1, y1, z, w, t);
		            dogatecol(fb, x, y1, z + 1, w, t);
		            dogatecol(fb, x, y1, z - 1, w, t);
		    	}
	            dogatecol(fb, x + 1, y, z, w, t);
	            dogatecol(fb, x - 1, y, z, w, t);
	            dogatecol(fb, x, y, z + 1, w, t);
	            dogatecol(fb, x, y, z - 1, w, t);
		    	return true;
	    	}
    	}
    	return false;
    }
    
    public void cbsigns(Block b, Player p, int t) {
    	World w = b.getWorld();
    	if (!w.getName().equals("Survival")) {
    		String signtxt = ((Sign) b.getState()).getLine(1);
    		int x = b.getX();
    		int z = b.getZ();
    		int y = b.getY();
    		
			Pattern pd = Pattern.compile("\\[Bridge:([0-9]+)\\]");
			Matcher md = pd.matcher(signtxt);
			
			Pattern pd2 = Pattern.compile("\\[Bridge:([0-9]+):([0-9]+)\\]");
			Matcher md2 = pd2.matcher(signtxt);
    		
    		if (signtxt.equalsIgnoreCase("[Gate]")) {
    			HashMap<Block, Boolean> foundblocks = new HashMap<Block, Boolean>();
    			
    			//Find gate
    			for (int x1 = x - 3; x1 <= x + 3; x1++) {
                    for (int y1 = y - 3; y1 <= y + 6; y1++) {
                        for (int z1 = z - 3; z1 <= z + 3; z1++) {
                            dogatecol(foundblocks, x1, y1, z1, w, t);
                        }
                    }
                }
    			
    		} else if (signtxt.equalsIgnoreCase("[Lift Down]") && t == -2) {
    			for (int i = b.getY() - 1; i > 0; i--) {
    				checklift(w, p, x, i, z, p.getLocation());
    			}
    		} else if (signtxt.equalsIgnoreCase("[Lift Up]") && t == -2) {
    			for (int i = b.getY() + 1; i < 128; i++) {
    				checklift(w, p, x, i, z, p.getLocation());
    			}
     		} else if (signtxt.equalsIgnoreCase("[Bridge]") || signtxt.equalsIgnoreCase("[Bridge Up]") || md.matches() || md2.matches()) {
     			
     			String amm = "1";
     			String pos = "1";
     			if (md.matches()) {
     				amm = md.replaceAll("$1");
     			}
     			if (md2.matches()) {
     				amm = md2.replaceAll("$1");
     				pos = md2.replaceAll("$2");
     			}
     			int wid = Integer.parseInt(amm);
     			int len = Integer.parseInt(pos);
     			
    			BlockFace s = new org.bukkit.material.Sign(Material.SIGN, b.getData()).getFacing();
    			int yd = signtxt.equalsIgnoreCase("[Bridge Up]") ? 1 : -1;
    			int xd = 0;
    			int zd = 0;
    			if (s == BlockFace.SOUTH) {
    				xd = -1;
    			} else if (s == BlockFace.NORTH) {
    				xd = 1;
    			} else if (s == BlockFace.WEST) {
    				zd = -1;
    			} else if (s == BlockFace.EAST) {
    				zd = 1;
    			} else {
    				System.out.println("Lolwut?");
    				System.out.println(s.toString());
    			}
    			int mi = 20;
    			if (len > mi) {
    				mi = len + 1;
    			}
    			for (int i = len; i < mi; i++) {
    				int nx = x + (i * xd);
    				int nz = z + (i * zd);
    				
    				if (w.getBlockAt(nx, b.getY(), nz).getType() == Material.SIGN_POST) {
    					String end_signtxt = ((Sign) w.getBlockAt(nx, b.getY(), nz).getState()).getLine(1);
    					if (end_signtxt.equalsIgnoreCase("[Bridge End]") || end_signtxt.equalsIgnoreCase("[Bridge]") || end_signtxt.equalsIgnoreCase("[Bridge Up]")) {
        					Block sign1 = b;
        					Block sign2 = w.getBlockAt(nx, b.getY(), nz);
        					// Check blocks below signs are of the same type
        					boolean same = true;
        					Material b1 = sign1.getRelative(0, yd, 0).getType();
        					for (int k = -wid; k <= wid; k++) {
        						Material b2 = sign1.getWorld().getBlockAt(sign1.getX() + (k * zd), sign1.getY() + yd, sign1.getZ() + (k * xd)).getType();
        						Material b3 = sign2.getWorld().getBlockAt(sign2.getX() + (k * zd), sign2.getY() + yd, sign2.getZ() + (k * xd)).getType();
        						if (b1 != b2 || b1 != b3) {
        							same = false;
        						}
        					}
        					Material[] kcl = {b1, Material.AIR, Material.STATIONARY_WATER, Material.STATIONARY_LAVA, Material.LAVA, Material.WATER};
        					List<Material> kc = Arrays.asList(kcl);
        					//plugin.mf.sendmsg(event.getPlayer(), b1.toString() + b2.toString() + b3.toString() + b4.toString() + b5.toString() + b6.toString());
        					if (same) {
        						boolean air = true;
        						boolean cle = false;
        						for (int j = 1; j < i; j++) {
        							for (int k = -wid; k <= wid; k++) {
        								Block b7b = sign1.getRelative((j * xd) + (k * zd), yd, (j * zd) + (k * xd));
        								Material b7 = b7b.getType();
        								if (b1 == b7) {
        									cle = true;
        								}
        								if (!kc.contains(b7)) {
        									air = false;
        								}
        							}
        						}
        						if (t != -2) {
        							cle = (t != 0);
        						}
        						if (air) {
        							if (cle) {
        								b1 = Material.AIR;
        							}
            						for (int j = 1; j < i; j++) {
            							for (int k = -wid; k <= wid; k++) {
            								sign1.getRelative((j * xd) + (k * zd), yd, (j * zd) + (k * xd)).setType(b1);
            							}
            						}
        						} else if (p != null) {
        							plugin.mf.sendmsg(p, "Bridge area not empty!");
        						}
        					} else if (p != null) {
        						plugin.mf.sendmsg(p, "Block types don't match!");
        					}
    					}
    					break;
    				}
    			}
    		} else if (signtxt.equalsIgnoreCase("[Door]") || signtxt.equalsIgnoreCase("[Door Down]") || signtxt.equalsIgnoreCase("[Door Up]")) {
    			BlockFace s = new org.bukkit.material.Sign(Material.SIGN, b.getData()).getFacing();
    			int xd = 0;
    			int zd = 0;
    			if (s == BlockFace.SOUTH || s == BlockFace.NORTH) {
    				xd = 1;
    			} else if (s == BlockFace.WEST || s == BlockFace.EAST) {
    				zd = 1;
    			} else {
    				System.out.println("Lolwut?");
    				System.out.println(s.toString());
    			}
    			int dl = -15;
    			int du = 15;
    			if (signtxt.equalsIgnoreCase("[Door Down]")) {
    				du = 0;
    			}
    			if (signtxt.equalsIgnoreCase("[Door Up]")) {
    				dl = 0;
    			}
    			for (int i = dl; i < du; i++) {
    				if (i == 0)
    					continue;
    				
    				int nx = x;
    				int nz = z;
    				int ny = y + i;
    				
    				if (w.getBlockAt(nx, ny, nz).getType() == Material.SIGN_POST) {
    					String end_signtxt = ((Sign) w.getBlockAt(nx, ny, nz).getState()).getLine(1);
    					if (end_signtxt.equalsIgnoreCase("[Door End]") || end_signtxt.equalsIgnoreCase("[Door]")) {
        					Block sign1 = b;
        					Block sign2 = w.getBlockAt(nx, ny, nz);
    						if (i > 0) {
    							ny = 1;
    						} else {
    							ny = -1;
    						}
        					// Check blocks below signs are of the same type
        					Material b1 = sign1.getRelative(0, ny, 0).getType();
        					Material b2 = sign2.getRelative(0, -ny, 0).getType();
        					Material b3 = sign1.getRelative(zd, ny, xd).getType();
        					Material b4 = sign1.getRelative(-zd, ny, -xd).getType();
        					Material b5 = sign2.getRelative(zd, -ny, xd).getType();
        					Material b6 = sign2.getRelative(-zd, -ny, -xd).getType();
        					//plugin.mf.sendmsg(event.getPlayer(), b1.toString() + b2.toString() + b3.toString() + b4.toString() + b5.toString() + b6.toString());
        					Material[] kcl = {b1, Material.AIR, Material.STATIONARY_WATER, Material.STATIONARY_LAVA, Material.LAVA, Material.WATER};
        					List<Material> kc = Arrays.asList(kcl);
        					if (b1 == b2 && b2 == b3 && b3 == b4 && b4 == b5 && b5 == b6) {
        						boolean air = true;
        						boolean cle = false;
        						for (int q = 2; q < Math.abs(i) - 1; q++) {
        							int j = q;
        							if (i < 0)
        								j *= -1;
        							for (int k = -1; k < 2; k++) {
        								Material b7 = sign1.getRelative((k * zd), j, (k * xd)).getType();
        								if (b1 == b7) {
        									cle = true;
        								}
        								if (!kc.contains(b7)) {
        									air = false;
        								}
        							}
        						}
        						if (t != -2) {
        							cle = (t != 0); 
        						}
        						if (air) {
        							if (cle) {
        								b1 = Material.AIR;
        							}
        							for (int q = 2; q < Math.abs(i) - 1; q++) {
            							int j = q;
            							if (i < 0)
            								j *= -1;
            							for (int k = -1; k < 2; k++) {
            								sign1.getRelative((k * zd), j, (k * xd)).setType(b1);
            							}
            						}
        						} else if (p != null) {
        							plugin.mf.sendmsg(p, "Bridge area not empty!");
        						}
        					} else if (p != null) {
        						plugin.mf.sendmsg(p, "Block types don't match!");
        					}
    					}
    					break;
    				}
    			}
    		}
		}
    }
    
    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
    	if (event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN) {
    		//System.out.println("Sign post!");
    		cbsigns(event.getBlock(), null, event.getNewCurrent());
    	}
    }
    
    @Override
    public void onBlockFromTo(BlockFromToEvent event) {
    	int town1 = plugin.mf.insidetown(event.getBlock());
    	int town2 = plugin.mf.insidetown(event.getToBlock());
    	if (town1 == 0 && town1 != town2 && event.getToBlock().getType() == Material.AIR) {
    		event.getToBlock().setType(Material.LEAVES);
    		b.add(event.getToBlock());
    	}
    }
    
    @Override
    public void onBlockIgnite(BlockIgniteEvent event) {
    	boolean f = (event.getCause() == IgniteCause.SPREAD && ((Zone) plugin.mf.insidezone(event.getBlock().getLocation(), true)).getFire());
    	boolean g = (event.getCause() == IgniteCause.FLINT_AND_STEEL && event.getPlayer() != null && plugin.mf.notothertown(event.getPlayer())); 
    	if (!(f || g)) {
    		event.setCancelled(true);
    	}
    }

    @Override
    public void onLeavesDecay(LeavesDecayEvent event) {
    	if (event.getBlock().getWorld().getName() == "Creative") {
    		event.setCancelled(true);
    	}
    }
    
}