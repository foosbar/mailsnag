package com.foosbar.mailsnag.model;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Models a message received via SMTP.
 * 
 * @author Kevin Kelley (dev@foos-bar.com)
 */
public class Message {
	private String id;
	private String from;
	private String subject;
	private String htmlMessage;
	private String textMessage;
	private String message;
	private String to;
	private Date received;
	private boolean read;
	
	public Message() {
		//to = new ArrayList<String>();
		received = new Date(System.currentTimeMillis());
		read = false;
	}
	
	public Message(String id, String from, String to, String subject, String content) {
		this();
		this.id = id;
		this.from = from;
		this.subject = subject;
		this.message = content;
		this.to = to;
		
		parseMessage();

	}
	
	public String getId() {
		if(id == null) {
			Matcher regexId = Pattern.compile("Message-ID:\\s*(.*)").matcher(message);
			if(regexId.find())
				id = regexId.group(1);
		}
		return id;
	}
	
	public String getFrom() {
		if(from == null) {
			Matcher regexFrom = Pattern.compile("MAIL FROM:\\s*?<(.*?)>\\s*?").matcher(message);
			if(regexFrom.find())
				from = regexFrom.group(1);
		}
		return from;
	}
	
	public String getSubject() {
		if(subject == null) {
			Matcher regexSubj = Pattern.compile("Subject:\\s*(.*)").matcher(message);
			if(regexSubj.find())
				subject = regexSubj.group(1);
		}
		return subject;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
		parseMessage();
	}

	/*
	private void addTo(String to) {
		this.to.add(to);
	}
	*/
	
	public String getTo() {
		if(to == null) {
			StringBuilder toSB = new StringBuilder();
			Matcher regexTo = Pattern.compile("RCPT TO:\\s*?<(.*?)>\\s*?").matcher(message);
			
			while(regexTo.find()) {
				if(toSB.length() > 0)
					toSB.append("; ");
				toSB.append(regexTo.group(1));
			}
			
			to = toSB.toString();
		}
		return to;
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

	public String getHtmlMessage() {
		return htmlMessage;
	}

	private void setHtmlMessage(String htmlMessage) {
		this.htmlMessage = htmlMessage;
	}

	public String getTextMessage() {
		return textMessage;
	}

	private void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer("Message: ");
		buff.append(getId()).append("\n");
		buff.append("From: ").append(getFrom()).append("\n");
		buff.append("To: ").append(getTo()).append("\n");
		buff.append("Content:\n").append(getMessage());
		return buff.toString();
	}
	
	private void parseMessage() {
		
	}
}