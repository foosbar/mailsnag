package com.foosbar.mailsnag.model;

/**
 * Models a message received via SMTP.
 * 
 * @author Kevin Kelley (dev@foos-bar.com)
 */
public class MessageData {
	
	private String htmlMessage;
	private String message;
	private String textMessage;

	public MessageData() {
	}
	
	public String getHtmlMessage() {
		return htmlMessage;
	}

	public String getMessage() {
		return message;
	}
	
	public String getTextMessage() {
		return textMessage;
	}

	public boolean hasHtmlMessage() {
		return htmlMessage != null;
	}

	public boolean hasTextMessage() {
		return textMessage != null;
	}
	
	public void setHtmlMessage(String htmlMessage) {
		this.htmlMessage = htmlMessage;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}
}