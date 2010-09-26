package com.foosbar.mailsnag.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;

public class MessageParser {

	public static final Message parse(String message) {
		Message m = new Message();
		m.setMessage(message);
		
		Session session = Session.getDefaultInstance(new Properties());
		InputStream is = null;

		try {
			is = new ByteArrayInputStream(message.getBytes());
			MimeMessage mimeMessage = new MimeMessage(session, is);
			
			if(mimeMessage.getContent() instanceof MimeMultipart) {
				parseMultipart(m, mimeMessage);
			} else {
				parseSinglepart(m, mimeMessage);
			}

			// Set From Addresses
			Address[] from = mimeMessage.getFrom();
			if(from != null && from.length > 0) {
				StringBuilder sb = new StringBuilder();
				for(Address f : from) {
					if(sb.length() > 0)
						sb.append("; ");
					sb.append(f.toString());
				}
				m.setFrom(sb.toString());
			}

			// Set To Addresses
			Address[] to = mimeMessage.getRecipients(RecipientType.TO);
			if(to != null && to.length > 0) {
				StringBuilder sb = new StringBuilder();
				for(Address t : to) {
					if(sb.length() > 0)
						sb.append("; ");
					sb.append(t.toString());
				}
				m.setTo(sb.toString());
			}
			
			// Set the ID
			m.setId(mimeMessage.getMessageID());
			
			// Set Subject
			m.setSubject(mimeMessage.getSubject());
			
			// Set Received Date (Actually the Sent Date)
			parseDate(m, mimeMessage);
			
		} catch(IOException e) {
		} catch(MessagingException e) {
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception e) { }
		}

		return m;
	}
	
	private static void parseMultipart(Message m, MimeMessage mimeMessage) throws IOException, MessagingException {
		MimeMultipart content = (MimeMultipart)mimeMessage.getContent();
		int count = content.getCount();
		
		for(int x = 0; x < count; x++) {
			BodyPart bp = content.getBodyPart(x);
			String contentType = bp.getContentType().toLowerCase();
			if(contentType.startsWith("text/plain;")) {
				m.setTextMessage(bp.getContent().toString());
				continue;
			}
			if(contentType.startsWith("text/html;")) {
				m.setHtmlMessage(bp.getContent().toString());
				continue;
			}
		}
	}

	private static void parseSinglepart(Message m, MimeMessage mimeMessage) throws IOException, MessagingException {
		
		String contentType = mimeMessage.getContentType().toLowerCase();
		String content = mimeMessage.getContent().toString();
		
		if(contentType.startsWith("text/plain;"))
			m.setTextMessage(content);
		
		if(contentType.startsWith("text/html;"))
			m.setHtmlMessage(content);

	}
	
	private static void parseDate(Message m, MimeMessage mimeMessage) throws MessagingException {
		
		Date d = mimeMessage.getSentDate();
		
		if(d == null)
			d = mimeMessage.getReceivedDate();
		
		if(d == null)
			d = new Date(System.currentTimeMillis());
		
		m.setReceived(d);
	}
}