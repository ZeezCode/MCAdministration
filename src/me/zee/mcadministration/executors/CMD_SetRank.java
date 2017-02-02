package me.zee.mcadministration.executors;

import me.zee.mcadministration.MCAdministration;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_SetRank implements CommandExecutor {
	private MCAdministration plugin;
	
	/**
	 * <p>Constructor</p>
	 * 
	 * @param util Instance of plugin's Utilities class
	 * @param dbHandler Instance of plugin's DatabaseHandler class
	 */
	public CMD_SetRank(MCAdministration plugin) {
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
			if (!plugin.dbHandler.rankHasPermission(plugin.permission.getPrimaryGroup(calling_ply), "can_set_rank")) {
				plugin.util.sendError(calling_ply, "You do not have access to this command!");
				return true;
			}
		}
		
		if (args.length >=2) {
			Player target_ply = Bukkit.getPlayer(args[0]);
			String rank = args[1];
			if (target_ply == null) {
				plugin.util.sendError(sender, "Target not found!");
				return true;
			}
			
			if (!plugin.util.getRankExists(plugin.permission.getGroups(), rank)) {
				plugin.util.sendError(sender, "Invalid rank entered!");
				return true;
			}
			rank = Character.toUpperCase(rank.charAt(0)) + rank.substring(1); //Capitalize first letter of rank
			
			if (plugin.dbHandler.logAction("Edit Rank", target_ply.getUniqueId(), (calling_ply == null ? null : calling_ply.getUniqueId()), "Changed rank from " + plugin.permission.getPrimaryGroup(target_ply) + " to " + rank, 0l, plugin.util.getTimestamp())) {
				for (String group : plugin.permission.getPlayerGroups(target_ply)) {
					plugin.permission.playerRemoveGroup(target_ply, group);
				}
				plugin.permission.playerAddGroup(target_ply, rank);
				plugin.dbHandler.setPlayerRank(target_ply.getUniqueId(), rank);
				plugin.util.sendMessage(sender, "You have successfully changed " + target_ply.getName() + "'s rank to " + rank);
			} else {
				plugin.util.sendError(sender, "Failed to log action! Player's rank was not changed!");
			}
		} else 
			plugin.util.sendError(sender, "Invalid command format! Use /setrank <player> <rank>");
		
		return true;
	}
}