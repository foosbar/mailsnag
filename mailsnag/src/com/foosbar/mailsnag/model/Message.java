package com.foosbar.mailsnag.model;

import java.util.Date;

/**
 * Models a message received via SMTP.
 * 
 * @author Kevin Kelley (dev@foos-bar.com)
 */
public class Message {
	
	private String cc;
	private String filename;
	private String from;
	private String htmlMessage;
	private String id;
	private String message;
	private boolean read;
	private Date received;
	private String subject;
	private String textMessage;
	private String to;

	public Message() {
	}
	
	public String getCc() {
		return cc;
	}

	public String getFilename() {
		return filename;
	}

	public String getFrom() {
		return from;
	}

	public String getHtmlMessage() {
		return htmlMessage;
	}
	
	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}
	
	public Date getReceived() {
		return received;
	}
	
	public String getSubject() {
		return subject;
	}

	public String getTextMessage() {
		return textMessage;
	}

	public String getTo() {
		return to;
	}

	public boolean hasHtmlMessage() {
		return htmlMessage != null;
	}

	public boolean hasTextMessage() {
		return textMessage != null;
	}
	
	public boolean isRead() {
		return read;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setFrom(String from) {
		this.from = from;
	}
	
	public void setHtmlMessage(String htmlMessage) {
		this.htmlMessage = htmlMessage;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public void setReceived(Date received) {
		this.received = received;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}

	public void setTo(String to) {
		this.to = to;
	}
}