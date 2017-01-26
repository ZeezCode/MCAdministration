package me.zee.mcadministration.executors;

import me.zee.mcadministration.MCAdministration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Register implements CommandExecutor {
	private MCAdministration plugin;
	
	/**
	 * <p>Constructor</p>
	 * 
	 * @param util Instance of plugin's Utilities class
	 * @param dbHandler Instance of plugin's DatabaseHandler class
	 */
	public CMD_Register(MCAdministration plugin) {
		this.plugin = plugin;
	}

	/**
	 * <p>Hooked to server's command system. Ran to execute the command</p>
	 * 
	 * @param sender The CommandSender who used the command
	 * @param cmd The command that was ran
	 * @param label "Name" of command that was ran
	 * @param args All argument supplied along with the command
	 * 
	 * @return boolean Whether or not command successfully ran
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player calling_ply = null;
		if (sender instanceof Player) {
			calling_ply = (Player) sender;
			if (!plugin.permission.has(calling_ply, "mca.register")) {
				plugin.util.sendError(calling_ply, "You do not have access to this command!");
				return true;
			}
		} else
			plugin.util.sendError(sender, "Sorry, but you must be a player to use this command!");
		
		if (plugin.dbHandler.getPlayerInfo(calling_ply.getUniqueId()) != null)
			plugin.util.sendError(sender, "You are already registered!");
		else {
			if (args.length >=2) {
				if (args[0].equals(args[1])) {
					plugin.dbHandler.registerPlayer(calling_ply, args[0]);
					plugin.util.sendMessage(sender, "Your account has successfully been registered!");
				} else {
					plugin.util.sendError(sender, "The two entered passwords do not match!");
				}
			} else
				plugin.util.sendError(sender, "Invalid command format! Use /register <password> <password again>");
		}
		
		return true;
	}
}