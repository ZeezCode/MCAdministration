package me.zee.mcadministration;

import java.util.UUID;

public class DBPlayer {
	private String rank, password, salt;
	private UUID uuid;
	
	public DBPlayer(UUID uuid, String rank, String password, String salt) {
		this.uuid = uuid;
		this.rank = rank;
		this.password = password;
		this.salt = salt;
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
}