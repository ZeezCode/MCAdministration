package me.zee.mcadministration.executors;

import me.zee.mcadministration.MCAdministration;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_SetHealth implements CommandExecutor {
	private MCAdministration plugin;
	
	/**
	 * <p>Constructor</p>
	 * 
	 * @param util Instance of plugin's Utilities class
	 * @param dbHandler Instance of plugin's DatabaseHandler class
	 */
	public CMD_SetHealth(MCAdministration plugin) {
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
			if (!plugin.dbHandler.rankHasPermission(plugin.permission.getPrimaryGroup(calling_ply), "can_sethealth")) {
				plugin.util.sendError(calling_ply, "You do not have access to this command!");
				return true;
			}
		}
		
		if (args.length >= 3) {
			Player target_ply = Bukkit.getPlayer(args[0]);
			double newHP = 0;
			String reason = "";
			if (target_ply == null) {
				plugin.util.sendError(sender, "Target not found!");
				return true;
			}
			
			if (plugin.util.isValidInt(args[1]))
				newHP = Integer.parseInt(args[1]);
			else {
				plugin.util.sendError(sender, "Invalid command format! Use /sethealth <player> <health> <reason>");
				return true;
			}
			
			if (newHP<0 || newHP > 20) {
				plugin.util.sendError(sender, "The health value must be between 0 and 20!");
				return true;
			}
			
			for (int i=2; i<args.length; i++) {
				reason += args[i] + " ";
			}
			
			if (plugin.dbHandler.logAction("Set health to " + newHP, target_ply.getUniqueId(), (calling_ply == null ? null : calling_ply.getUniqueId()), reason, 0l, plugin.util.getTimestamp())) {
				plugin.util.setHealth(calling_ply, target_ply, newHP, reason);
				plugin.util.sendMessage(sender, "You have set the health of " + target_ply.getName() + " to " + (newHP / 2.0) + " hearts for: " + reason);
			} else {
				plugin.util.sendError(sender, "Failed to log action! Player was not slain!");
			}
		} else {
			plugin.util.sendError(sender, "Invalid command format! Use /sethealth <player> <health> <reason>");
		}
		
		return true;
	}
	
}