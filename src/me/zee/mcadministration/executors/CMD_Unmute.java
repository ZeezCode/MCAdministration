package me.zee.mcadministration.executors;

import me.zee.mcadministration.MCAdministration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Unmute implements CommandExecutor {
	private MCAdministration plugin;
	
	/**
	 * <p>Constructor</p>
	 * 
	 * @param util Instance of plugin's Utilities class
	 * @param dbHandler Instance of plugin's DatabaseHandler class
	 */
	public CMD_Unmute(MCAdministration plugin) {
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
			if (!plugin.permission.has(calling_ply, "mca.mute")) {
				plugin.util.sendError(calling_ply, "You do not have access to this command!");
				return true;
			}
		}
		
		if (args.length >= 1) {
			Player target_ply = Bukkit.getPlayer(args[0]);
			if (target_ply == null) {
				plugin.util.sendError(sender, "Target not found!");
				return true;
			}
			
			if (!plugin.util.isPlayerMuted(target_ply.getUniqueId())) {
				plugin.util.sendError(sender, "This player isn't muted!");
				return true;
			}
			
			if (plugin.dbHandler.logAction("Unmute", target_ply.getUniqueId(), (calling_ply == null ? null : calling_ply.getUniqueId()), null, 0l, plugin.util.getTimestamp())) {
				plugin.util.setPlayerMuted(calling_ply, target_ply, "N/A", false);
				plugin.util.sendMessage(sender, "You have unmuted " + target_ply.getName());
			} else {
				plugin.util.sendError(sender, "Failed to log action! Player was not unmuted!");
			}
		} else {
			plugin.util.sendError(sender, "Invalid command format! Use /unmute <player>");
		}
		
		return true;
	}

}