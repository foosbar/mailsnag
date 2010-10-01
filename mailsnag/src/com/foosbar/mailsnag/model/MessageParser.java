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
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import com.foosbar.mailsnag.util.MessageStore;

public class MessageParser {

	public static final Message parse(String message) {
		Message m = new Message();
		m.setMessage(message);
		
		Session session = Session.getDefaultInstance(new Properties());
		InputStream is = null;

		try {
			is = new ByteArrayInputStream(message.getBytes());
			MimeMessage mimeMessage = new MimeMessage(session, is);
			
			/*
			if(mimeMessage.getContent() instanceof MimeMultipart) {
				parseMultipart(m, mimeMessage);
			} else {
				parseSinglepart(m, mimeMessage);
			}
			*/

			// Set From Addresses
			m.setFrom(parseAddresses(mimeMessage.getFrom()));

			// Set To Addresses
			m.setTo(parseAddresses(mimeMessage.getRecipients(RecipientType.TO)));

			// Set To Addresses
			m.setCc(parseAddresses(mimeMessage.getRecipients(RecipientType.CC)));

			// Set the ID
			m.setId(mimeMessage.getMessageID());
			
			// Set Subject
			m.setSubject(mimeMessage.getSubject());
			
			// Set Received Date (Actually the Sent Date)
			parseDate(m, mimeMessage);
			
		//} catch(IOException e) {
		} catch(MessagingException e) {
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception e) { }
		}

		return m;
	}
	
	public static final MessageData parseData(Message message) {
		
		MessageData data = new MessageData();
		
		String rawContent = MessageStore.load(message);
		
		data.setMessage(rawContent);
		
		InputStream is = null;
		
		try {

			Session session = Session.getDefaultInstance(new Properties());
			
			is = new ByteArrayInputStream(rawContent.getBytes());
			
			MimeMessage mimeMessage = new MimeMessage(session, is);			
			
			if(mimeMessage.getContent() instanceof MimeMultipart)
				parseMultipart(mimeMessage, data);
			else
				parseSinglepart(mimeMessage, data);

		} catch(Exception e) {
			
			e.printStackTrace();
			
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception e) { }
		}
		
		return data;
	}
	
	private static void parseMultipart(MimeMessage mimeMessage, MessageData messageData) throws IOException, MessagingException {
		MimeMultipart content = (MimeMultipart)mimeMessage.getContent();
		int count = content.getCount();
		
		for(int x = 0; x < count; x++) {
			BodyPart bp = content.getBodyPart(x);
			String contentType = bp.getContentType().toLowerCase();
			if(contentType.startsWith("text/plain;")) {
				messageData.setTextMessage(bp.getContent().toString());
				continue;
			}
			if(contentType.startsWith("text/html;")) {
				messageData.setHtmlMessage(bp.getContent().toString());
				continue;
			}
		}
	}

	private static void parseSinglepart(MimeMessage mimeMessage, MessageData messageData) throws IOException, MessagingException {
		
		String contentType = mimeMessage.getContentType().toLowerCase();
		String content = mimeMessage.getContent().toString();
		
		if(contentType.startsWith("text/plain;"))
			messageData.setTextMessage(content);
		
		if(contentType.startsWith("text/html;"))
			messageData.setHtmlMessage(content);
	}
	
	private static void parseDate(Message m, MimeMessage mimeMessage) throws MessagingException {
		
		Date d = mimeMessage.getSentDate();
		
		if(d == null)
			d = mimeMessage.getReceivedDate();
		
		if(d == null)
			d = new Date(System.currentTimeMillis());
		
		m.setReceived(d);
	}
	
	private static String parseAddresses(Address[] addresses) throws MessagingException {
		StringBuilder sb = new StringBuilder();
		if(addresses != null && addresses.length > 0) {
			for(Address a : addresses) {
				if(sb.length() > 0)
					sb.append("; ");
				sb.append(a.toString());
			}
		}
		return sb.toString();
	}
}