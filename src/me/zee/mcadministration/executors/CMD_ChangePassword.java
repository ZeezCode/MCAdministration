package me.zee.mcadministration.executors;

import me.zee.mcadministration.DBPlayer;
import me.zee.mcadministration.MCAdministration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_ChangePassword implements CommandExecutor {
	private MCAdministration plugin;
	
	/**
	 * <p>Constructor</p>
	 * 
	 * @param util Instance of plugin's Utilities class
	 * @param dbHandler Instance of plugin's DatabaseHandler class
	 */
	public CMD_ChangePassword(MCAdministration plugin) {
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
			if (!plugin.permission.has(calling_ply, "mca.changepassword")) {
				plugin.util.sendError(calling_ply, "You do not have access to this command!");
				return true;
			}
		} else
			plugin.util.sendError(sender, "Sorry, but you must be a player to use this command!");
		DBPlayer playerInfo = plugin.dbHandler.getPlayerInfo(calling_ply.getUniqueId());
		
		if (playerInfo == null) { //Player is not registered
			plugin.util.sendError(sender, "You must register an account before you may change your password!");
			return true;
		}
		
		if (args.length>=3) {
			String enteredPass = plugin.util.sha256( plugin.util.sha256(args[0]) + plugin.util.sha256(playerInfo.getSalt()) );
			if (enteredPass.equals(playerInfo.getPassword())) { //Player entered correct old password
				if (args[1].equals(args[2])) {
					String newPass = plugin.util.sha256( plugin.util.sha256(args[1]) + plugin.util.sha256(playerInfo.getSalt()) );
					playerInfo.setPassword(newPass);
					plugin.dbHandler.setPlayerPassword(playerInfo);
					plugin.util.sendMessage(sender, "You have successfully changed your password!");
				} else {
					plugin.util.sendError(sender, "The two new passwords do not match!");
				}
			} else {
				plugin.util.sendError(sender, "You entered an incorrect password!");
			}
		} else
			plugin.util.sendError(sender, "Invalid command format! Use /changepassword <old pass> <new pass> <new pass>");
		return true;
	}
}