package me.zee.mcadministration;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.zee.mcadministration.executors.*;
import net.milkbowl.vault.permission.Permission;

public class MCAdministration extends JavaPlugin implements Listener {
	public HashMap<UUID, Long> mutedPlayers = null; //UUID = player, Long = timestamp at which player was muted
	public Permission permission = null;
	public DatabaseHandler dbHandler = null;
	public Utilities util = null;
	
	/**
	 * TODO:
	 *  - /register <pw> <pw> (Done)
	 *  - /changepw <oldpw> <newpw> <newpw>
	 *  - /setrank <user> <rank>
	 */
	
	/**
	 * <p>Returns the plugin's main config file</p>
	 * 
	 * @return FileConfiguration The main config file of the plugin
	 */
	public FileConfiguration getPluginConfig() {
		return getConfig();
	}
	
	/**
	 * <p>First method of plugin to be ran, initiates plugin</p>
	 */
	public void onEnable() {
		PluginDescriptionFile pdfFile = getDescription();
		getLogger().info(pdfFile.getName() + " has been enabled running version " + pdfFile.getVersion() + ".");
		setupPermissions();
		saveDefaultConfig();
		util = new Utilities(this);
		dbHandler = new DatabaseHandler(this);
		mutedPlayers = new HashMap<UUID, Long>();
		Bukkit.getPluginManager().registerEvents(this, this);
		
		getCommand("kick").setExecutor(new CMD_Kick(this));
		getCommand("slay").setExecutor(new CMD_Slay(this));
		getCommand("warn").setExecutor(new CMD_Warn(this));
		getCommand("sethealth").setExecutor(new CMD_SetHealth(this));
		getCommand("mute").setExecutor(new CMD_Mute(this));
		getCommand("unmute").setExecutor(new CMD_Unmute(this));
		getCommand("ban").setExecutor(new CMD_Ban(this));
		getCommand("tempban").setExecutor(new CMD_TempBan(this));
		getCommand("unban").setExecutor(new CMD_Unban(this));
		getCommand("announce").setExecutor(new CMD_Announce(this));
		getCommand("invsee").setExecutor(new CMD_Invsee(this));
		getCommand("viewban").setExecutor(new CMD_ViewBan(this));
		getCommand("register").setExecutor(new CMD_Register(this));
	}
	
	/**
	 * <p>Hooked to server chat, ran before message is sent to everyone. Used to cancel messages of muted players</p>
	 * 
	 * @param e Chat event, contains info about the message and player sending it
	 */
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		if (util.isPlayerMuted(e.getPlayer().getUniqueId()))
			e.setCancelled(true);
	}
	
	/**
	 * <p>Hooked to server login system, ran before player joins server</p>
	 * 
	 * @param e PlayerLoginEvent, contains info about player who is logging in
	 */
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		if (util.isPlayerBanned(e.getPlayer().getUniqueId()))
			e.disallow(Result.KICK_BANNED, util.getBannedMessage(e.getPlayer().getUniqueId()));
	}
	
	/**
	 * <p>Hooked to server login system, ran when player joins server</p>
	 * 
	 * @param e PlayerJoinEvent, contains info about who is joining
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player ply = e.getPlayer();
		String savedRank = dbHandler.getPlayerRank(ply.getUniqueId());
		if (savedRank == null) return;
		if (!savedRank.equals(permission.getPrimaryGroup(ply))) //If rank saved in DB is different than user's current rank
			permission.playerAddGroup(ply, savedRank);
	}
	
	/**
	 * <p>Initiates permission variable. Uses Vault to "connect" to server's permissions plugin</p>
	 * 
	 * @return boolean Whether or not permission provider was reached
	 */
	private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
}