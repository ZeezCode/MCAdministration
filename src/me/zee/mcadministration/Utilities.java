package me.zee.mcadministration;

import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Utilities {
	private MCAdministration plugin;
	private String[] banLengths = {
			"5m", //5 minutes
			"30m", //30 minutes
			"1h", //1 hour
			"12h", //12 hours
			"1d",  //1 day
			"2d", //2 days
			"4d", //4 days
			"1w", //1 week
			"2w", //2 weeks
			"3w", //3 weeks
			"1m" //1 month
		};
	
	/**
	 * <p>Constructor</p>
	 * @param plugin Main class of plugin
	 */
	public Utilities(MCAdministration plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * <p>Returns the prefix set in plugin config</p>
	 * 
	 * @return String The prefix set in plugin configuration
	 */
	public String getPrefix() {
		return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix"));
	}
	
	/**
	 * <p>Returns whether or not the player with the given UUID is muted</p>
	 * 
	 * @param uuid The UUID of the player being checked
	 * @return boolean Whether or not the player is muted
	 */
	public boolean isPlayerMuted(UUID uuid) {
		return plugin.mutedPlayers.containsKey(uuid);
	}
	
	/**
	 * <p>Returns whether or not a given player is banned</p>
	 * 
	 * @param uuid UUID of player to check
	 * @return boolean Whether or not player is banned
	 */
	public boolean isPlayerBanned(UUID uuid) {
		Ban ban = plugin.dbHandler.getPlayerBan(uuid);
		if (ban == null) return false;		
		if (ban.getLength() == 0) return true;
		if (ban.getUnbanDate() > getTimestamp()) return true;
		else { //Ban expired, allow entry but remove ban from bans table first
			plugin.dbHandler.removeBan(ban.getPlayer());
			return false;
		}
	}
	
	/**
	 * <p>Returns fully formatted ban message to use when kicking banned players</p>
	 * 
	 * @param uuid UUID of banned player
	 * @return String Fully formatted ban message
	 */
	public String getBannedMessage(UUID uuid) {
		Ban ban = plugin.dbHandler.getPlayerBan(uuid);
		if (ban == null) return null;
		
		String reason = "You are currently server banned for \"" + ban.getReason() + "\"\n"
				+ "You will be unbanned in: " + (ban.getLength() == 0 ? "NEVER" : getPrettyLength(ban.getUnbanDate() - getTimestamp()));
		
		return reason;
	}
	
	/**
	 * <p>Returns whether or not a given staff member can unban a given player</p>
	 * 
	 * @param sender Staff member whose permissions are to be checked
	 * @param uuid UUID of banned player
	 * @return boolean Whether or not given staff member can unban given player
	 */
	public boolean staffCanUnban(CommandSender sender, UUID uuid) {
		if (!(sender instanceof Player)) return true;
		Player ply = (Player) sender;
		if (plugin.permission.has(sender, "mca.unban.unrestricted")) return true;
		Ban ban = plugin.dbHandler.getPlayerBan(uuid);
		if (ban.getStaff() == ply.getUniqueId()) return true;
		return false;
	}
	
	/**
	 * <p>Sends a message to a CommandSender</p>
	 * 
	 * @param ply The player being sent the message
	 * @param msg The message being sent to the player
	 */
	public void sendMessage(CommandSender ply, String msg) {
		ply.sendMessage(getPrefix() + ChatColor.YELLOW + msg);
	}
	
	/**
	 * <p>Sends an error message to a CommandSender in RED text</p>
	 * 
	 * @param ply The player being sent the error
	 * @param msg The message being sent to the player
	 */
	public void sendError(CommandSender ply, String msg) {
		ply.sendMessage(getPrefix() + ChatColor.RED + msg);
	}
	
	/**
	 * <p>Kicks a given player from the server</p>
	 * 
	 * @param calling_ply The staff member using the command
	 * @param target_ply The player being kicked
	 * @param reason The reason target_ply is being kicked
	 */
	public void kickPlayer(Player calling_ply, Player target_ply, String reason) {
		target_ply.kickPlayer("You have been kicked by " + (calling_ply == null ? "Server" : calling_ply.getName()) + " for: " + reason);
	}
	
	/**
	 * <p>Slays a given player</p>
	 * 
	 * @param calling_ply The staff member using the command
	 * @param target_ply The player being slain
	 * @param reason The reason target_ply is being slain
	 */
	public void slayPlayer(Player calling_ply, Player target_ply, String reason) {
		target_ply.setHealth(0d);
		sendMessage(target_ply, "You have been slain by " + (calling_ply == null ? "Server" : calling_ply.getName()) + " for: " + reason);
	}
	
	/**
	 * <p>Sets the health of a given player</p>
	 * 
	 * @param calling_ply The staff member using the command
	 * @param target_ply The player whose health is being set
	 * @param newHP The new HP of target_ply
	 * @param reason The reason target_ply's health is being set
	 */
	public void setHealth(Player calling_ply, Player target_ply, double newHP, String reason) {
		target_ply.setHealth(newHP);
		sendMessage(target_ply, "Your health has been set to " + (newHP / 2.0) + " hearts by " + (calling_ply == null ? "Server" : calling_ply.getName()) + " for: " + reason);
	}
	
	/**
	 * <p>Warns a given player</p>
	 * 
	 * @param calling_ply The staff member using the command
	 * @param target_ply The player being warned
	 * @param reason The reason target_ply is being warned
	 */
	public void warnPlayer(Player calling_ply, Player target_ply, String reason) {
		sendMessage(target_ply, "You've been warned by " + (calling_ply == null ? "Server" : calling_ply.getName()) + " for: " + reason);
	}
	
	/**
	 * <p>Mutes a given player</p>
	 * 
	 * @param calling_ply The staff member using the command
	 * @param target_ply The player being muted
	 * @param reason The reason target_ply is being muted
	 */
	@Deprecated
	public void mutePlayer(Player calling_ply, Player target_ply, String reason) {
		if (!isPlayerMuted(target_ply.getUniqueId())) {
			plugin.mutedPlayers.put(target_ply.getUniqueId(), getTimestamp());
			sendMessage(target_ply, "You've been muted by " + (calling_ply == null ? "Server" : calling_ply.getName()) + " for: " + reason);
		}
	}
	
	/**
	 * <p>Mutes or unmutes a given player</p>
	 * 
	 * @param calling_ply The staff member using the command
	 * @param target_ply The player being (un)muted
	 * @param reason The reason target_ply is being muted (ignore if unmute)
	 * @param muted Whether to mute or unmute target_ply
	 */
	public void setPlayerMuted(Player calling_ply, Player target_ply, String reason, boolean muted) {
		if (muted) { //Try to mute player
			if (!isPlayerMuted(target_ply.getUniqueId())) //If player isn't already muted
				plugin.mutedPlayers.put(target_ply.getUniqueId(), getTimestamp());
		} else { //Try to unmute player
			if (isPlayerMuted(target_ply.getUniqueId())) //If player is already muted
				plugin.mutedPlayers.remove(target_ply.getUniqueId());
		}
		sendMessage(target_ply, "You have been " + (muted ? "muted" : "unmuted") + " by " + (calling_ply == null ? "Server" : calling_ply.getName()) + (muted ? " for: " + reason : ""));
	}
	
	/**
	 * <p>Kicks a player from server, saying who banned them and why</p>
	 * 
	 * @param calling_ply The staff member using the command
	 * @param target_ply The player being banned
	 * @param reason The reason target_ply is being banned
	 * @param permanent Whether or not the ban is permanent
	 */
	public void banPlayer(Player calling_ply, Player target_ply, String reason, boolean permanent) {
		target_ply.kickPlayer("You have been " + (permanent ? "permanently" : "temporarily") + " banned by " + (calling_ply == null ? "Server" : calling_ply.getName()) + " for: " + reason);
	}
	
	/**
	 * <p>Announces a given message to the entire server with the plugin's prefix</p>
	 * 
	 * @param message The message to be announced
	 */
	public void announceMessage(String message) {
		Bukkit.broadcastMessage(getPrefix() + ChatColor.YELLOW + message);
	}
	
	/**
	 * <p>Returns current UNIX timestamp</p>
	 * 
	 * @return long The current UNIX timestamp
	 */
	public long getTimestamp() {
		return new Date().getTime() / 1000;
	}
	
	/**
	 * <p>Returns whether or not toParse is a valid integer</p>
	 * 
	 * @param toParse String to be parsed
	 * @return boolean Whether or not toParse is a valid integer
	 */
	public boolean isValidInt(String toParse) {
		try {
			Integer.parseInt(toParse);
		} catch(Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * <p>Returns whether or not toParse is a valid UUID</p>
	 * 
	 * @param toParse String to be parsed
	 * @return boolean Whether or not toParse is a valid UUID
	 */
	public boolean isValidUUID(String toParse) {
		try {
			UUID.fromString(toParse);
		} catch(Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * <p>Returns formatted length of time from a given amount of seconds</p>
	 * 
	 * @param length Length of time in seconds
	 * @return String Formatted length of time
	 */
	public String getPrettyLength(long length) {
		DecimalFormat df = new DecimalFormat("0.00");
		
		if (length >= 86400) { //>=one day
			double days = length / 86400d;
			return df.format(days) + " day(s)";
		} else if (length >= 3600) { //>=one hour
			double hours = length / 3600d;
			return df.format(hours) + " hour(s)";
		} else if (length >= 60) { //>=one minute
			double minutes = length / 60d;
			return df.format(minutes) + " minute(s)";
		} else if (length == 0) {
			return "Permanent";
		} else {
			return length + " second(s)";
		}
	}
	
	/**
	 * <p>Returns whether or not the given ban length is a permitted ban length</p>
	 * 
	 * @param length The ban length to be tested
	 * @return boolean Whether or not the given ban length is permitted
	 */
	public boolean isAllowedBanLength(String length) {
		for (String bLength : banLengths) {
			if (bLength.equalsIgnoreCase(length)) return true;
		}
		return false;
	}
	
	/**
	 * <p>Returns a String[] of all permitted ban lengths</p>
	 * 
	 * @return String[] All permitted ban lengths
	 */
	public String[] getBanLengths() {
		return banLengths;
	}
	
	/**
	 * <p>Returns the length, in seconds, of a given ban length</p>
	 * 
	 * @param length String version of a ban length
	 * @return long Length, in seconds, of a given ban length
	 */
	public long lengthStrToNum(String length) {
		switch(length) {
		case "5m": //5 minutes
			return (5 * 60);
		case "30m": //30 minutes
			return (30 * 60);
		case "1h": //1 hour
			return (60 * 60);
		case "12h": //12 hours
			return (12 * (60 * 60));
		case "1d": //1 day
			return (24 * (60 * 60));
		case "2d": //2 days
			return (2 * (24 * (60 * 60)));
		case "4d": //4 days
			return (4 * (24 * (60 * 60)));
		case "1w": //1 week
			return (7 * (24 * (60 * 60)));
		case "2w": //2 weeks
			return (14 * (24 * (60 * 60)));
		case "3w": //3 weeks
			return (21 * (24 * (60 * 60)));
		case "1m": //1 month
			return (31 * (24 * (60 * 60)));
		default:
			return 0;
		}
	}
	
	/**
	 * <p>Encrypts a given String to SHA256</p>
	 * 
	 * @param input The string to be encrypted
	 * @return String The encrypted String
	 */
	public String sha256(String input) {
		try {
			MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
			byte[] result = mDigest.digest(input.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < result.length; i++) {
				sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
			}

			return sb.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * <p>Generates a new salt with a given length</p>
	 * 
	 * @param length The length of the salt to be created
	 * @return String The newly generated salt
	 */
	public String getNewSalt(int length) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random gen = new Random();
        while (salt.length() < length) {
            int index = (int) (gen.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
}