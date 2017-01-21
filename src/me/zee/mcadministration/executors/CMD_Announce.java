package me.zee.mcadministration.executors;

import me.zee.mcadministration.MCAdministration;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Announce implements CommandExecutor {
	private MCAdministration plugin;
	
	/**
	 * <p>Constructor</p>
	 * 
	 * @param util Instance of plugin's Utilities class
	 * @param dbHandler Instance of plugin's DatabaseHandler class
	 */
	public CMD_Announce(MCAdministration plugin) {
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
			if (!plugin.permission.has(calling_ply, "mca.announce")) {
				plugin.util.sendError(calling_ply, "You do not have access to this command!");
				return true;
			}
		}
		
		if (args.length >= 1) {
			String message = "";
			for (int i=0; i<args.length; i++) {
				message += args[i] + " ";
			}
			message = ChatColor.translateAlternateColorCodes('&', message);
			if (plugin.dbHandler.logAction("Announce", null, (calling_ply == null ? null : calling_ply.getUniqueId()), message, 0l, plugin.util.getTimestamp())) {
				plugin.util.announceMessage(message); //No need for confirmation message
			} else {
				plugin.util.sendError(sender, "Failed to log action! Announcement was not made!");
			}
		} else {
			plugin.util.sendError(sender, "Invalid command format! Use /announce <message>");
		}	
		return true;
	}
}