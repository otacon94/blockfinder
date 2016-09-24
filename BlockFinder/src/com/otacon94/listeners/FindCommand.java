package com.otacon94.listeners;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.otacon94.blockfinder.BlockFinder;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.CoreProtectAPI.ParseResult;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author otacon94
 */
public class FindCommand implements CommandExecutor {

	private static final String DEFAULTRADIUS = 20 + "";

	private static final String BLOCKID = "b";
	private static final String USERID = "u";
	private static final String RADIUSID = "r";
	private static final String HISTORY = "-h";

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase(BlockFinder.COMMAND)) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a  Player to use this command!");
				return false;
			}
			Player player = (Player) sender;
			if (args != null) {
				parseInput(player, args);
			} else {
				sender.sendMessage("You have to specify at least the Block Type! (Ex: b:hopper");
				return false;
			}
		}
		return true;
	}

	private void parseInput(Player player, String[] args) {
		try {
			List<String> playerSet = new LinkedList<>();
			List<String> typeSet = new LinkedList<>();
			String radius = DEFAULTRADIUS;
			boolean history = false;
			// simply parse, assuming input without spaces like: "u:player1,player2
			// b:<id1>,<id2> r:10"
			for (int i = 0; i < args.length; i++) {
				StringTokenizer st = new StringTokenizer(args[i], ": ,");
				if (st.hasMoreTokens()) {
					String token = st.nextToken();
					if (token.equalsIgnoreCase(BLOCKID)) {
						while (st.hasMoreTokens()) {
							typeSet.add(st.nextToken());
						}
					} else if (token.equalsIgnoreCase(USERID)) {
						while (st.hasMoreTokens()) {
							playerSet.add(st.nextToken().toLowerCase());
						}
					} else if (token.equalsIgnoreCase(RADIUSID)) {
						radius = st.nextToken();
					} else if (token.equalsIgnoreCase(HISTORY) ){
						history=true;
					}
				}
			}
			if (typeSet.size() != 0) {
				if (playerSet.size() != 0) {
					if( history ){
						searchHistory(player, typeSet, playerSet, radius);
					}else{
						searchRadius(player,typeSet,radius);
					}
				}else{
					searchRadius(player, typeSet, radius);
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			player.sendMessage("Error reading blocks id! Are sure you are using number?");
		}
	}

	/**
	 * Searches for specified block type in the area specified by the given
	 * radius
	 * 
	 * @param player
	 *            - The Player who called the command
	 * @param rad
	 *            - The radius in which to search
	 * @param mat
	 *            - The material names list of the blocks to search
	 */
	private void searchRadius(Player player, List<String> mat, String rad) {
		Thread t = new Thread(){
			@Override
			public void run() {
				Location l = player.getLocation();
				World world = l.getWorld();
				int count = 0;
				try {
					int radius = Integer.valueOf(rad);
					player.sendMessage("Searching for: " + mat + " in a radius of: " + rad);
					for (int i = (int) l.getX() - radius; i < l.getX() + radius; i++) {
						for (int j = (int) l.getY() - radius; j < l.getY() + radius; j++) {
							for (int k = (int) l.getZ() - radius; k < l.getZ() + radius; k++) {
								Block block = world.getBlockAt(i, j, k);
								if (mat.contains(block.getType().toString().toLowerCase())) {
									constructMessage(player, block.getType().toString(), i, j, k);
									count++;
								}
							}
						}
					} // for x
					player.sendMessage("Found " + count + " blocks into a radius of: " + rad);
				} catch (Exception e) {
					player.sendMessage("Please specify a correct radius or a correct material");
					player.sendMessage("Usage: /findblock <material> <radius>");
				}
			}
		};
		t.start();
	}
	
	/**
	 * Searches for specified block type in the area specified by the given
	 * radius
	 * 
	 * @param send
	 *            - The Player who called the command
	 * @param rad
	 *            - The radius in which to search
	 * @param yrad
	 *            - The y radius in which to search
	 * @param mat
	 *            - The material names list of the blocks to search
	 * @param player
	 *            - The player names list to search
	 */
	private void searchHistory(Player sender, List<String> mat, List<String> player, String rad) {
		Thread t = new Thread() {
			@Override
			public void run() {
				// get core protect plugin to check in blocks history
				CoreProtect co = (CoreProtect) Bukkit.getPluginManager().getPlugin("CoreProtect");
				if (co == null) { // there is no history
					sender.sendMessage("It wans't possible to check into the blocks history. Failed to get CoreProtect Plugin");
					sender.sendMessage("I'll check if someone of the specified blocks is present!");
					searchRadius(sender, mat, rad);
					return;
				}
				// get coreprotect api
				CoreProtectAPI coApi = co.getAPI();
				Location l = sender.getLocation();
				int count = 0;
				try {
					int radius = Integer.valueOf(rad);
					sender.sendMessage("Searching for: " + mat + " placed by: " + player + " in a radius of: " + rad);
					List<Object> materialList = getMaterialList(mat);
					//it is possible to specify only placement and removal by passing and actionid list with 0 and 1 ad elements
					List<String[]> values = coApi.performLookup(Integer.MAX_VALUE, player, null, materialList, null,
							null, radius, l );
					if( values!=null ){//there are logs!
						for (String[] entry : values) {
							ParseResult result = coApi.parseResult(entry);
							constructMessage(sender, result);
							count++;
						}
					}
					sender.sendMessage("Found " + count + " blocks into a radius of: " + rad);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					sender.sendMessage("Please specify a correct radius");
				} catch (Exception e){
					e.printStackTrace();
					sender.sendMessage("Error retrieving blocks history");
				}
			}

			
		};
		t.start();
	}
	
	private List<Object> getMaterialList(List<String> mat) {
		List<Object> matList = new LinkedList<>();
		for(String s: mat){
			System.out.println("name: "+s+" matched: "+Material.matchMaterial(s));
			matList.add(Material.matchMaterial(s));
		}
		return matList;
	}

	/**
	 * Construct a simple message to send when a block is found
	 * 
	 * @param receiver
	 *            - the Player to which send the message
	 * @param mat
	 *            - The BlockMaterial found
	 * @param x
	 *            - x coordinate of the block
	 * @param y
	 *            - y coordinate of the block
	 * @param z
	 *            - z coordinate of the block
	 */
	private void constructMessage(Player receiver, String mat, int x, int y, int z) {
		String msg = mat.toUpperCase() + " at: " + x + " , " + y + " , " + x+ " ";
		sendMessage(receiver, msg, x, y, z);
	}

	/**
	 * Construct a comples message to send when a block is found
	 * 
	 * @param receiver
	 *            - the Player to which send the message
	 * @param result 
	 * 			  - The ParseResult of the log found
	 */
	private void constructMessage(Player receiver, ParseResult result) {
		int hours = result.getTime() / 3600;
		String msg = result.getType().toString() + " at: " + result.getX() + " , " + result.getY() + " , " +
				result.getZ() + " " + result.getActionString() + " by " + result.getPlayer() + " " + hours + " h ago";
		sendMessage(receiver, msg, result.getX(), result.getY(), result.getZ());
	}
	
	/**
	 * Sends the constructedMessage to the player and add a teleport to the specified position
	 * @param receiver -  the player to which send the message
	 * @param msg - the constructed message
	 * @param x - the x position of the block
	 * @param y - the y position of the block
	 * @param z - the z position of the block
	 */
	private void sendMessage(Player receiver,String msg,int x,int y, int z){
		TextComponent message = new TextComponent(msg);
		message.setColor(ChatColor.GREEN);
		message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + x + " " + y + " " + z));
		message.setHoverEvent(
				new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Teleport to position!").create()));
		receiver.spigot().sendMessage(message);
	}

}
