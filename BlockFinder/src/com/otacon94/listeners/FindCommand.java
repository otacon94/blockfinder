package com.otacon94.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.otacon94.blockfinder.BlockFinder;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author otacon94
 */
public class FindCommand implements CommandExecutor {
	
	private static final String DEFAULTRADIUS = 100+"";
	private static final String DEFAULTMATERIAL = Material.DIAMOND_ORE.toString();
	
	private JavaPlugin plugin;
	

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase(BlockFinder.COMMAND)) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a  Player to use this command!");
				return false;
			}
			Player player = (Player)sender;
			if( args!=null ){
				if( args.length==0 ){
					searchDefault(player);
				}else if( args.length==1 ){
					searchRadius(player,args[0]);
				}else if( args.length>1 ){
					searchRadius(player,args[0],args[1]);
				}
			}else{
				searchDefault(player);
			}
		}
		return true;
	}
	
	private void searchDefault(Player player){
		searchRadius(player, DEFAULTRADIUS, DEFAULTMATERIAL);
	}
	
	private void searchRadius(Player player,String rad){
		searchRadius(player, rad, DEFAULTMATERIAL);
	}
	
	private void searchRadius(Player player,String rad, String mat){
		Location l = player.getLocation();
		World world = l.getWorld();
		int count=0;
		try {
			int radius = Integer.valueOf(rad);
			player.sendMessage("Searching for: "+mat+" in a radius of: "+rad);
			for(int i=(int)l.getX()-radius;i<l.getX()+radius;i++){
				for(int j=(int)l.getY()-radius;j<l.getY()+radius;j++){
					for(int k=(int)l.getZ()-radius;k<l.getZ()+radius;k++){
						Block block = world.getBlockAt(i, j, k);
						if( block.getType().toString().equalsIgnoreCase(mat) ){
							String msg = mat.toString()+" at: "+i+" , "+j+" , "+k+" ";
							TextComponent message = new TextComponent( msg );
							message.setColor(ChatColor.GREEN);
							message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/tp "+i+" "+j+" "+k ) );
							message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Teleport!").create() ) );;
							player.spigot().sendMessage(message);
							count++;
						}
					}
				}
			}//for x
			player.sendMessage("Found "+count+" blocks into a radius of: "+rad);
		} catch (Exception e) {
			player.sendMessage("Please specify a correct radius or a correct material");
		}
		
	}

}
