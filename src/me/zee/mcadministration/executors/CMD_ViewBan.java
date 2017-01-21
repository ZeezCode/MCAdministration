package me.zee.mcadministration.executors;

import java.text.SimpleDateFormat;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.zee.mcadministration.Ban;
import me.zee.mcadministration.MCAdministration;

public class CMD_ViewBan implements CommandExecutor {
	private MCAdministration plugin;
	
	/**
	 * <p>Constructor</p>
	 * 
	 * @param util Instance of plugin's Utilities class
	 * @param dbHandler Instance of plugin's DatabaseHandler class
	 */
	public CMD_ViewBan(MCAdministration plugin) {
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
			if (!plugin.permission.has(calling_ply, "mca.viewban")) {
				plugin.util.sendError(calling_ply, "You do not have access to this command!");
				return true;
			}
		}
		
		if (args.length >= 1) {
			if (!plugin.util.isValidUUID(args[0])) {
				plugin.util.sendError(sender, "Invalid UUID specified!");
				return true;
			}
			
			UUID uuid = UUID.fromString(args[0]);
			
			if (Bukkit.getOfflinePlayer(uuid) == null) {
				plugin.util.sendError(sender, "Player not found!");
				return true;
			}
			
			//No need to log command's usage, just execute
			if (!plugin.util.isPlayerBanned(uuid)) {
				plugin.util.sendError(sender, "Player is not banned!");
			} else {
				Ban ban = plugin.dbHandler.getPlayerBan(uuid);
				SimpleDateFormat sDF = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
				String msg = ChatColor.GREEN + "BAN INFO (" + uuid.toString() + ")\n"
								+ChatColor.GREEN + "Player: " + ChatColor.WHITE + Bukkit.getOfflinePlayer(ban.getPlayer()).getName() + "\n"
								+ChatColor.GREEN + "Reason: " + ChatColor.WHITE + ban.getReason() + "\n"
								+ChatColor.GREEN + "Length: " + ChatColor.WHITE + plugin.util.getPrettyLength(ban.getLength()) + "\n"
								+ChatColor.GREEN + "Admin: " + ChatColor.WHITE + Bukkit.getOfflinePlayer(ban.getStaff()).getName() + "\n"
								+ChatColor.GREEN + "Date Banned: " + ChatColor.WHITE + sDF.format(ban.getBannedDate() * 1000); //*1000 b/c timestamp is saved in seconds but Java uses timestamps in milliseconds
				plugin.util.sendMessage(sender, msg);
			}
			
		} else {
			plugin.util.sendError(sender, "Invalid command format! Use /viewban <player>");
		}
		
		return true;
	}
	
}