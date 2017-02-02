package me.zee.mcadministration.executors;

import me.zee.mcadministration.MCAdministration;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_TempBan implements CommandExecutor {
	private MCAdministration plugin;
	
	/**
	 * <p>Constructor</p>
	 * 
	 * @param util Instance of plugin's Utilities class
	 * @param dbHandler Instance of plugin's DatabaseHandler class
	 */
	public CMD_TempBan(MCAdministration plugin) {
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
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player calling_ply = null;
		if (sender instanceof Player) {
			calling_ply = (Player) sender;
			if (!plugin.dbHandler.rankHasPermission(plugin.permission.getPrimaryGroup(calling_ply), "can_ban")) {
				plugin.util.sendError(calling_ply, "You do not have access to this command!");
				return true;
			}
		}
		
		if (args.length >= 3) {
			Player target_ply = Bukkit.getPlayer(args[0]);
			String lengthStr = args[1].toLowerCase().trim(), reason = "";
			long length = 0;
			if (target_ply == null) {
				plugin.util.sendError(sender, "Target not found!");
				return true;
			}
			
			if (!plugin.util.isAllowedBanLength(lengthStr)) {
				String errorMsg = "Invalid ban length specified! You may choose from one of the following: ";
				for (String bLength : plugin.util.getBanLengths())
					errorMsg += bLength + ", ";
				plugin.util.sendError(sender, errorMsg);
				return true;
			}
			length = plugin.util.lengthStrToNum(lengthStr);
			
			for (int i=2; i<args.length; i++) {
				reason += args[i] + " ";
			}
			
			if (plugin.dbHandler.logAction("Ban", target_ply.getUniqueId(), (calling_ply == null ? null : calling_ply.getUniqueId()), reason, length, plugin.util.getTimestamp())) {
				plugin.util.banPlayer(calling_ply, target_ply, reason, false);
				plugin.util.sendMessage(sender, "You have temporarily banned " + target_ply.getName() + " for: " + reason);
			} else {
				plugin.util.sendError(sender, "Failed to log action! Player was not banned!");
			}
		} else {
			plugin.util.sendError(sender, "Invalid command format! Use /tempban <player> <length> <reason>");
		}
		
		return true;
	}
	
}