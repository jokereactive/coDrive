package com.cdms.codrive.classes;

import java.util.Date;

import com.cdms.codrive.classes.Interaction.Status;

public class InteractionLog {
	
	Status status;
	Date date;

	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
}
