package me.zee.mcadministration;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.zee.mcadministration.executors.*;
import net.milkbowl.vault.permission.Permission;

public class MCAdministration extends JavaPlugin implements Listener {
	public HashMap<UUID, Long> connectionTime = null;
	public HashMap<UUID, Long> mutedPlayers = null; //UUID = player, Long = timestamp at which player was muted
	public Permission permission = null;
	public DatabaseHandler dbHandler = null;
	public Utilities util = null;
	
	/**
	 * TODO:
	 *  - Web port
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
		connectionTime = new HashMap<UUID, Long>();
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
		getCommand("setrank").setExecutor(new CMD_SetRank(this));
		getCommand("changepassword").setExecutor(new CMD_ChangePassword(this));
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
		DBPlayer playerInfo = dbHandler.getPlayerInfo(ply.getUniqueId());
		if (playerInfo == null) return;
		playerInfo.setLastName(ply.getName());
		playerInfo.setLastSeen(util.getTimestamp());
		dbHandler.updatePlayer(playerInfo);
		if (!playerInfo.getRank().equals(permission.getPrimaryGroup(ply))) { //If rank saved in DB is different than user's current rank
			for (String group : permission.getPlayerGroups(ply)) {
				permission.playerRemoveGroup(ply, group);
			}
			permission.playerAddGroup(ply, playerInfo.getRank());
		}
		
		connectionTime.put(ply.getUniqueId(), util.getTimestamp());
	}
	
	/**
	 * <p>Hooked to server disconnect system, ran when player leaves server</p>
	 * 
	 * @param e PlayerQuitEvent, contains info about who is leaving
	 */
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		DBPlayer ply = dbHandler.getPlayerInfo(e.getPlayer().getUniqueId());
		if (ply != null) {
			UUID uuid = e.getPlayer().getUniqueId();
			long newPlayTime = ply.getPlayTime() + (util.getTimestamp() - connectionTime.get(uuid));
			connectionTime.remove(uuid);
			ply.setPlayTime(newPlayTime);
			ply.setLastSeen(util.getTimestamp());
			dbHandler.updatePlayer(ply);
		}
	}
	
	/**
	 * <p>Hooked to player death system, ran when player dies</p>
	 * 
	 * @param e PlayerDeathEvent e, contains about info about player who died
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		DBPlayer target = dbHandler.getPlayerInfo(e.getEntity().getUniqueId());
		long timestamp = util.getTimestamp();
		if (target != null) {
			target.setDeaths(target.getDeaths() + 1);
			target.setLastSeen(timestamp);
			dbHandler.updatePlayer(target);
			util.sendMessage(e.getEntity(), "You now have " + target.getDeaths() + " counted death" + (target.getDeaths() == 1 ? "" : "s") + "!");
		}
		
		if (e.getEntity().getKiller() != null) {
			DBPlayer killer = dbHandler.getPlayerInfo(e.getEntity().getKiller().getUniqueId());
			if (killer != null) {
				killer.setKills(killer.getKills()+1);
				killer.setLastSeen(timestamp);
				dbHandler.updatePlayer(killer);
				util.sendMessage(e.getEntity().getKiller(), "You now have " + killer.getKills() + " counted kill" + (killer.getKills() == 1 ? "" : "s") + "!");
			}
		}
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