package com.foosbar.mailsnag.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message {
	private String id;
	private String from;
	private String subject;
	private String message;
	private List<String> to;
	private Date received;
	private boolean read;
	
	public Message() {
		to = new ArrayList<String>();
		received = new Date(System.currentTimeMillis());
		read = false;
	}
	
	public Message(String id, String from, String to, String subject, String content) {
		this();
		this.id = id;
		this.from = from;
		this.subject = subject;
		this.message = content;
		this.to.add(to);
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getFrom() {
		return from;
	}
	
	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public void addTo(String to) {
		this.to.add(to);
	}
	
	public List<String> getTo() {
		return to;
	}
	
	public String getToString() {
		StringBuilder builder = new StringBuilder();
		for(String addr : to) {
			if(builder.length() > 0)
				builder.append("; ");
			builder.append(addr);
		}
		return builder.toString();
	}

	public void setTo(List<String> to) {
		this.to = to;
	}
	
	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public Date getReceived() {
		return received;
	}

	/*
	public void setReceived(Date received) {
		this.received = received;
	}
	*/
	
	public String toString() {
		StringBuffer buff = new StringBuffer("Message: ");
		buff.append(getId()).append("\n");
		buff.append("From: ").append(getFrom()).append("\n");
		buff.append("To: ").append(getToString()).append("\n");
		buff.append("Content:\n").append(getMessage());
		return buff.toString();
	}
}
