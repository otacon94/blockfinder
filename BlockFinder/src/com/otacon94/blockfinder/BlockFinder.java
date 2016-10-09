package com.otacon94.blockfinder;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.otacon94.command.FindCommand;

/**
 * @author otacon94
 */
public class BlockFinder extends JavaPlugin  {

	public static final String COMMAND = "findblock";
	public static final String COMMANDALIAS = "fb";

	@Override
	public void onEnable() {
		Logger logger = getLogger();
		logger.info("BlockFinderPlugin is Enabled");
		this.getCommand(COMMAND).setExecutor(new FindCommand());
		this.getCommand(COMMANDALIAS).setExecutor(new FindCommand());
	}

	@Override
	public void onDisable() {
		Logger logger = getLogger();
		logger.info("BlockFinderPlugin is Disabled");
	}
}
