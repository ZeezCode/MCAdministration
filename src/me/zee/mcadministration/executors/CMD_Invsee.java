package me.zee.mcadministration.executors;

import me.zee.mcadministration.MCAdministration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Invsee implements CommandExecutor {
	private MCAdministration plugin;
	
	/**
	 * <p>Constructor</p>
	 * 
	 * @param util Instance of plugin's Utilities class
	 * @param dbHandler Instance of plugin's DatabaseHandler class
	 */
	public CMD_Invsee(MCAdministration plugin) {
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
			if (!plugin.permission.has(calling_ply, "mca.invsee")) {
				plugin.util.sendError(calling_ply, "You do not have access to this command!");
				return true;
			}
		} else {
			plugin.util.sendError(sender, "You must be a player to use this command!");
			return true;
		}
		
		if (args.length >= 2) {
			Player target_ply = Bukkit.getPlayer(args[0]);
			String reason = "";
			if (target_ply == null) {
				plugin.util.sendError(sender, "Target not found!");
				return true;
			}
			for (int i=1; i<args.length; i++) {
				reason += args[i] + " ";
			}
			
			if (plugin.dbHandler.logAction("Inv See", target_ply.getUniqueId(), (calling_ply == null ? null : calling_ply.getUniqueId()), reason, 0l, plugin.util.getTimestamp())) {
				calling_ply.openInventory(target_ply.getInventory());
				plugin.util.sendMessage(sender, "You are viewing the inventory of " + target_ply.getName() + " for: " + reason);
			} else {
				plugin.util.sendError(sender, "Failed to log action! Inventory can not be viewed!");
			}
		} else {
			plugin.util.sendError(sender, "Invalid command format! Use /invsee <player> <reason>");
		}
		
		return true;
	}
	
}