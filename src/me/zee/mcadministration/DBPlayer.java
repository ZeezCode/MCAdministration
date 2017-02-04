package me.zee.mcadministration;

import java.util.UUID;

public class DBPlayer {
	private String rank, password, salt, lastName;
	private UUID uuid;
	private int kills, deaths;
	private long lastSeen;
	
	public DBPlayer(UUID uuid, String rank, String password, String salt, String lastName, int kills, int deaths, long lastSeen) {
		this.uuid = uuid;
		this.rank = rank;
		this.password = password;
		this.salt = salt;
		this.lastName = lastName;
		this.kills = kills;
		this.deaths = deaths;
		this.lastSeen = lastSeen;
	}
	
	public UUID getUniqueID() {
		return uuid;
	}
	
	public String getRank() {
		return rank;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getSalt() {
		return salt;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getDeaths() {
		return deaths;
	}
	
	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}
	
	public int getKills() {
		return kills;
	}
	
	public void setKills(int kills) {
		this.kills = kills;
	}
	
	public long getLastSeen() {
		return lastSeen;
	}
	
	public void setLastSeen(long lastSeen) {
		this.lastSeen = lastSeen;
	}
}