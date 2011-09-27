/*******************************************************************************
 * Copyright (c) 2010-2011 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Kelley - initial API and implementation
 *******************************************************************************/
package com.foosbar.mailsnag.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import com.foosbar.mailsnag.model.Message.Attachment;
import com.foosbar.mailsnag.util.MessageStore;

public class MessageParser {

	private static final String MIME_ALT = "multipart/alternative";
	private static final String MIME_RELATED = "multipart/related";
	private static final String MIME_HTML = "text/html";
	private static final String MIME_PLAIN = "text/plain";

	private static final Pattern CONTENT_ID_REGEX = Pattern.compile("<(.*?)>");

	public static final Message parse(String message, String filename) {
		Message m = new Message();

		Session session = Session.getDefaultInstance(new Properties());
		InputStream is = null;

		try {
			is = new ByteArrayInputStream(message.getBytes());
			MimeMessage mimeMessage = new MimeMessage(session, is);

			// Set Filename
			m.setFilename(filename);

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

			// Parse the attachments
			parseAttachments(m, mimeMessage);

		} catch (MessagingException e) {
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}
		}

		return m;
	}

	public static final MessageData parseData(String rawData) {

		MessageData data = new MessageData();

		if (rawData == null) {
			return data;
		}

		data.setMessage(rawData);

		InputStream is = null;

		try {

			Session session = Session.getDefaultInstance(new Properties());

			is = new ByteArrayInputStream(rawData.getBytes());

			MimeMessage mimeMessage = new MimeMessage(session, is);

			if (mimeMessage.isMimeType("text/*")) {
				parseSinglepart(mimeMessage, data);
			} else if (mimeMessage.isMimeType("multipart/*")) {
				parseMultipart(mimeMessage, data);
			}

		} catch (Exception e) {

			e.printStackTrace();

		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}
		}

		return data;
	}

	private static void parseAttachments(Message message,
			MimeMessage mimeMessage) {
		try {

			if (!(mimeMessage.getContent() instanceof Multipart)) {
				return;
			}

			MimeMultipart content = (MimeMultipart) mimeMessage.getContent();

			parseAttachments(message, content);

		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void parseAttachments(Message message, Multipart multiPart) {
		try {

			int count = multiPart.getCount();

			for (int x = 0; x < count; x++) {
				BodyPart part = multiPart.getBodyPart(x);

				if (part.isMimeType(MIME_ALT)) {
					Object c = part.getContent();
					parseAttachments(message, (Multipart) c);
				} else if (part.isMimeType(MIME_RELATED)) {
					Object c = part.getContent();
					parseAttachments(message, (Multipart) c);
				} else if (BodyPart.INLINE.equals(part.getDisposition())) {
					String filename = getAttachmentFilename(part);
					Attachment a = message.addAttachment(getAttachmentId(part),
							filename, part.getContentType(), part.getSize());
					// Persist Attachments
					MessageStore.persistAttachment(a, part);
				} else if (BodyPart.ATTACHMENT.equals(part.getDisposition())) {
					String filename = getAttachmentFilename(part);
					Attachment a = message.addAttachment(getAttachmentId(part),
							filename, part.getContentType(), part.getSize());
					// Persist Attachments
					MessageStore.persistAttachment(a, part);
				}
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void parseMultipart(MimeMessage mimeMessage,
			MessageData messageData) throws IOException, MessagingException {
		Multipart content = (Multipart) mimeMessage.getContent();
		parseMultipart(content, messageData);
	}

	private static void parseMultipart(Multipart content,
			MessageData messageData) throws IOException, MessagingException {
		int count = content.getCount();

		for (int x = 0; x < count; x++) {
			BodyPart bp = content.getBodyPart(x);

			if (BodyPart.ATTACHMENT.equalsIgnoreCase(bp.getDisposition())) {
				continue;// Currently, do nothing
			} else {
				if (bp.isMimeType(MIME_ALT)) {
					Object c = bp.getContent();
					parseMultipart((Multipart) c, messageData);
				} else if (bp.isMimeType(MIME_RELATED)) {
					Object c = bp.getContent();
					parseMultipart((Multipart) c, messageData);
				} else if (bp.isMimeType(MIME_HTML)) {
					messageData.setHtmlMessage(bp.getContent().toString());
				} else if (bp.isMimeType(MIME_PLAIN)) {
					messageData.setTextMessage(bp.getContent().toString());
				}
			}
		}
	}

	private static void parseSinglepart(MimeMessage mimeMessage,
			MessageData messageData) throws IOException, MessagingException {

		String content = mimeMessage.getContent().toString();

		if (mimeMessage.isMimeType(MIME_PLAIN)) {
			messageData.setTextMessage(content);
		}

		if (mimeMessage.isMimeType(MIME_HTML)) {
			messageData.setHtmlMessage(content);
		}
	}

	private static void parseDate(Message m, MimeMessage mimeMessage)
			throws MessagingException {

		Date d = mimeMessage.getSentDate();

		if (d == null) {
			d = mimeMessage.getReceivedDate();
		}

		if (d == null) {
			d = new Date(System.currentTimeMillis());
		}

		m.setReceived(d);
	}

	private static String parseAddresses(Address[] addresses)
			throws MessagingException {
		StringBuilder sb = new StringBuilder();
		if (addresses != null && addresses.length > 0) {
			for (Address a : addresses) {
				if (sb.length() > 0) {
					sb.append("; ");
				}
				sb.append(a.toString());
			}
		}
		return sb.toString();
	}

	private static String getAttachmentFilename(BodyPart part)
			throws MessagingException {

		if (part.getFileName() != null) {
			return part.getFileName();
		}

		Pattern p = Pattern.compile("filename=\"(.*?)\"");

		@SuppressWarnings("unchecked")
		Enumeration<Header> e = part.getAllHeaders();
		while (e.hasMoreElements()) {
			Header header = e.nextElement();
			String name = header.getName();
			if (name != null) {
				if (name.startsWith("filename")) {
					Matcher m = p.matcher(name);
					if (m.find()) {
						return m.group(1);
					}
				}
			}
		}

		return "<missing filename>";
	}

	private static String getAttachmentId(BodyPart part)
			throws MessagingException {
		String[] idHeader = part.getHeader("Content-ID");
		if (idHeader != null && idHeader.length > 0) {
			Matcher m = CONTENT_ID_REGEX.matcher(idHeader[0]);
			if (m.find()) {
				return m.group(1);
			}
		}
		return UUID.randomUUID().toString();
	}
}