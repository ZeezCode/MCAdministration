package me.zee.mcadministration.executors;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.zee.mcadministration.DBPlayer;
import me.zee.mcadministration.MCAdministration;

public class CMD_Stats implements CommandExecutor {
	private MCAdministration plugin;
	
	/**
	 * <p>Constructor</p>
	 * 
	 * @param util Instance of plugin's Utilities class
	 * @param dbHandler Instance of plugin's DatabaseHandler class
	 */
	public CMD_Stats(MCAdministration plugin) {
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
			if (!plugin.dbHandler.rankHasPermission(plugin.permission.getPrimaryGroup(calling_ply), "can_view_stats")) {
				plugin.util.sendError(calling_ply, "You do not have access to this command!");
				return true;
			}
		}
		
		Player target = calling_ply;
		if (args.length >= 1) {
			if (!plugin.dbHandler.rankHasPermission(plugin.permission.getPrimaryGroup(calling_ply), "can_view_stats_others")) {
				plugin.util.sendError(calling_ply, "You do not have access to view others' stats!");
				return true;
			}
			
			target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				plugin.util.sendError(sender, "Target not found!");
				return true;
			}
		}
		
		DBPlayer targetInfo = plugin.dbHandler.getPlayerInfo(target.getUniqueId());
		if (targetInfo == null) {
			plugin.util.sendError(sender, "Target player is not registered and therefor has no tracked stats.");
			return true;
		}
		
		SimpleDateFormat sDF = new SimpleDateFormat("MMMM d, yyyy, h:mm a");
		DecimalFormat dF = new DecimalFormat("0.00");
		String lastSeen = sDF.format(targetInfo.getLastSeen() * 1000), //Java uses milliseconds
		targetInfoMsg = ChatColor.GREEN + "PLAYER INFO\n"
				+ ChatColor.GREEN + "Name: " + ChatColor.WHITE + targetInfo.getLastName() + "\n"
				+ ChatColor.GREEN + "Rank: " + ChatColor.WHITE + targetInfo.getRank() + "\n"
				+ ChatColor.GREEN + "UUID: " + ChatColor.WHITE + targetInfo.getUniqueID() + "\n"
				+ ChatColor.GREEN + "Kills: " + ChatColor.WHITE + targetInfo.getKills() + "\n"
				+ ChatColor.GREEN + "Deaths: " + ChatColor.WHITE + targetInfo.getDeaths() + "\n"
				+ ChatColor.GREEN + "Last Seen: " + ChatColor.WHITE + lastSeen + "\n"
				+ ChatColor.GREEN + "Playtime: " + ChatColor.WHITE + dF.format(targetInfo.getPlayTime() / 3600.0) + " hours";
		plugin.util.sendMessage(calling_ply, targetInfoMsg);
		
		return true;
	}
	
}