package com.cdms.codrive.classes;

import com.parse.ParseFile;

public class MyFile {
	
	private String name;
	private long size;
	private User user; //TODO change to residing owner if data structure changes
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

}
