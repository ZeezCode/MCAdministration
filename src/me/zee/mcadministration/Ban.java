package me.zee.mcadministration;

import java.util.UUID;

public class Ban {
	private UUID player, staff;
	private long bannedDate, length;
	private String reason;
	
	public Ban(UUID player, UUID staff, String reason, long bannedDate, long length) {
		this.player = player;
		this.staff = staff;
		this.reason = reason;
		this.bannedDate = bannedDate;
		this.length = length;
	}
	
	public UUID getPlayer() {
		return player;
	}
	
	public UUID getStaff() {
		return staff;
	}
	
	public String getReason() {
		return reason;
	}
	
	public long getBannedDate() {
		return bannedDate;
	}
	
	public long getLength() {
		return length;
	}
	
	public long getUnbanDate() {
		return (bannedDate + length);
	}
}