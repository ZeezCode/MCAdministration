package me.zee.mcadministration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.entity.Player;

public class DatabaseHandler {
	private MCAdministration plugin;
	private Connection connection;
	
	/**
	 * <>Constructor</p>
	 * @param plugin Main class of plugin
	 */
	public DatabaseHandler(MCAdministration plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * <p>Logs a specific action to the database</p>
	 * 
	 * @param action The action being taken
	 * @param target The player being taken action upon
	 * @param staff The player taking action
	 * @param reason Why target_ply is being acted against
	 * @param length Length of action (0 if not temp ban)
	 * @param timestamp UNIX timestamp of action
	 * 
	 * @return boolean If action was successfully inserted into database
	 */
	public boolean logAction(String action, UUID target, UUID staff, String reason, long length, long timestamp) {
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement("INSERT INTO actions VALUES (?, ?, ?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
			sql.setInt(1, 0); //Action ID (AI, ignores my value)
			sql.setString(2, action); //Action
			sql.setString(3, (target == null ? null : target.toString())); //target_ply (Null if action is "Announce")
			sql.setString(4, (staff == null ? "Server" : staff.toString())); //calling_ply
			sql.setString(5, (reason == null ? null : reason.trim())); //Reason
			sql.setLong(6, length); //Length
			sql.setLong(7, timestamp); //Timestamp
			int gen_keys = sql.executeUpdate();
			
			if (action.equalsIgnoreCase("ban") && gen_keys > 0) {
				ResultSet keys = sql.getGeneratedKeys();
				keys.next();
				int actionID = keys.getInt(1);
				PreparedStatement sql2 = connection.prepareStatement("INSERT INTO bans VALUES (?, ?, ?);");
				sql2.setInt(1, 0); //Ban ID (AI, ignores my value)
				sql2.setInt(2, actionID); //Action ID (Foreign ID)
				sql2.setString(3, target.toString()); //target_ply
				sql2.execute();
				sql2.close();
			}
			sql.close();
			
			return true;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
		return false;
	}
	
	/**
	 * <p>Returns all info regarding a given player's ban</p>
	 * 
	 * @param uuid The UUID of the banned player
	 * @return Ban Ban object containing info on player's bans
	 */
	public Ban getPlayerBan(UUID uuid) {
		Ban ban = null;
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement("SELECT * FROM bans WHERE target=?;");
			sql.setString(1, uuid.toString());
			ResultSet result = sql.executeQuery();
			if (result.next()) {
				int aid = result.getInt("aid");
				PreparedStatement sql2 = connection.prepareStatement("SELECT * FROM actions WHERE aid = ?;");
				sql2.setInt(1, aid);
				ResultSet result2 = sql2.executeQuery();
				if (result2.next()) { //Should be guaranteed  to pass, if not then there's an issue with table sync
					ban = new Ban(UUID.fromString(result2.getString("target")), UUID.fromString(result2.getString("staff")), result2.getString("reason"), result2.getLong("timestamp"), result2.getLong("length"));
				}
				result2.close();
				sql2.close();
			}
			result.close();
			sql.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
		
		return ban;
	}
	
	/**
	 * <p>Returns the information about a player saved in the database</p>
	 * 
	 * @param uuid The UUID of the player
	 * @return DBPlayer Object containing player's information saved in DB
	 */
	public DBPlayer getPlayerInfo(UUID uuid) {
		DBPlayer dbPlayer = null;
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?;");
			sql.setString(1, uuid.toString());
			ResultSet result = sql.executeQuery();
			if (result.next())
				dbPlayer = new DBPlayer(UUID.fromString(result.getString("uuid")), 
						result.getString("rank"), 
						result.getString("password"), 
						result.getString("salt"));
			result.close();
			sql.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
		return dbPlayer;
	}
	
	/**
	 * <p>Adds a new user to the players table in the database</p>
	 * 
	 * @param ply The player registering an account
	 * @param password The requested password of the player
	 */
	public void registerPlayer(Player ply, String password) {
		openConnection();
		try {
			String salt = plugin.util.getNewSalt(8);
			password = plugin.util.sha256(plugin.util.sha256(password) + plugin.util.sha256(salt));
			PreparedStatement sql = connection.prepareStatement("INSERT INTO players VALUES (?, ?, ?, ?);");
			sql.setString(1, ply.getUniqueId().toString());
			sql.setString(2, plugin.permission.getPrimaryGroup(ply));
			sql.setString(3, password);
			sql.setString(4, salt);
			sql.execute();
			sql.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * <p>Updates a player's rank in the players database table</p>
	 * 
	 * @param uuid The UUID of the player whose rank is being updated
	 * @param rank The new rank of the given player
	 */
	public void setPlayerRank(UUID uuid, String rank) {
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement("UPDATE players SET rank = ? WHERE uuid = ?;");
			sql.setString(1, rank);
			sql.setString(2, uuid.toString());
			sql.executeUpdate();
			sql.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * <p>Sets the password for a given player in the database</p>
	 * 
	 * @param playerInfo Contains necessary info about player, get this object from getPlayerInfo()
	 */
	public void setPlayerPassword(DBPlayer playerInfo) {
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement("UPDATE players SET password = ? WHERE uuid = ?;");
			sql.setString(1, playerInfo.getPassword());
			sql.setString(2, playerInfo.getUniqueID().toString());
			sql.executeUpdate();
			sql.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * <p>Removes a specific ban from database bans table</p>
	 * 
	 * @param uuid The UUID of the banned player
	 */
	public void removeBan(UUID uuid) {
		openConnection();
		try {
			PreparedStatement sql = connection.prepareStatement("DELETE FROM bans WHERE target = ?;");
			sql.setString(1, uuid.toString());
			sql.execute();
			sql.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
	
	/**
	 * <p>Opens a connection to the server's database</p>
	 */
	private synchronized void openConnection() {
		boolean shouldOpen=false;
		if (connection==null) shouldOpen=true;
		if (connection!=null)
			try {
				if (connection.isClosed()) shouldOpen=true;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		if (shouldOpen) {
			try {
				connection = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("db_host") + ":3306/" + plugin.getConfig().getString("db_name") + "?useSSL=false", 
						plugin.getConfig().getString("db_user"), 
						plugin.getConfig().getString("db_pass"));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * <p>Closes an open connection to the server's database</p>
	 */
	private synchronized void closeConnection() {
		try {
			connection.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}