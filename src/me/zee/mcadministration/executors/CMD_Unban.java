package me.zee.mcadministration.executors;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.zee.mcadministration.MCAdministration;

public class CMD_Unban implements CommandExecutor {
	private MCAdministration plugin;
	
	/**
	 * <p>Constructor</p>
	 * 
	 * @param util Instance of plugin's Utilities class
	 * @param dbHandler Instance of plugin's DatabaseHandler class
	 */
	public CMD_Unban(MCAdministration plugin) {
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
			if (!plugin.permission.has(calling_ply, "mca.ban")) {
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
			
			if (!plugin.util.isPlayerBanned(uuid)) {
				plugin.util.sendError(sender, "Player is not banned!");
				return true;
			}
			
			if (!plugin.util.staffCanUnban(sender, uuid)) {
				plugin.util.sendError(sender, "You do not have permission to unban this player!");
				return true;
			}
			
			if (plugin.dbHandler.logAction("Unban", uuid, (calling_ply == null ? null : calling_ply.getUniqueId()), null, 0l, plugin.util.getTimestamp())) {
				plugin.dbHandler.removeBan(uuid);
				plugin.util.sendMessage(sender, "You have unbanned " + args[0]);
			} else {
				plugin.util.sendError(sender, "Failed to log action! Player was not unbanned!");
			}
		} else {
			plugin.util.sendError(sender, "Invalid command format! Use /unban <player>");
		}
		
		return true;
	}
	
}