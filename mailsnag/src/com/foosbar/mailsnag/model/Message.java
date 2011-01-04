package com.foosbar.mailsnag.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Models a message received via SMTP.
 * 
 * @author Kevin Kelley (dev@foos-bar.com)
 */
public class Message {
	
	public static final String EXTENSION = ".eml";
	
	private String cc;
	private String filename;
	private String from;
	private String id;
	private Date received;
	private String subject;
	private String to;
	private Map<String, Attachment> attachments;
	private boolean unread;
	
	public Message() {
		attachments = new HashMap<String, Attachment>();
	}
	
	public Attachment addAttachment(String id, String name, String mimeType, long size) {
		Attachment a = new Attachment();
		a.setId(id);
		a.setMimeType(mimeType);
		a.setName(name);
		a.setSize(size);
		a.setMessage(this);
		a.setIndex(attachments.size());
		attachments.put(id, a);
		
		return a;
	}
	
	public String getAttachmentDir() {
		return filename.substring(0, filename.length() - 4);
	}

	public Map<String, Attachment> getAttachments() {
		return attachments;
	}

	public String getCc() {
		if(cc == null)
			return "";
		return cc;
	}

	public String getFilename() {
		return filename;
	}

	public String getFrom() {
		if(from == null)
			return "";
		return from;
	}

	public String getId() {
		return id;
	}

	public Date getReceived() {
		return received;
	}
	
	public String getSubject() {
		if(subject == null)
			return "";
		return subject;
	}

	public String getTo() {
		if(to == null)
			return "";
		return to;
	}

	public boolean hasAttachments() {
		return !attachments.isEmpty();
	}

	public void setAttachments(Map<String, Attachment> attachments) {
		this.attachments = attachments;
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
	
	public void setId(String id) {
		this.id = id;
	}

	public void setReceived(Date received) {
		this.received = received;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setTo(String to) {
		this.to = to;
	}
	
	public boolean isUnread() {
		return unread;
	}

	public void setUnread(boolean unread) {
		this.unread = unread;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		
		if(obj == null)
			return false;
		
		if(id == null)
			return false;
		
		return id.equals( ((Message)obj).getId());
	}

	@Override
	public int hashCode() {
		return id != null ?
				id.hashCode(): 
					super.hashCode();
	}

	public class Attachment {
		
		private String filename;
		private int index;
		private String id;
		private String mimeType;
		private String name;
		private long size;
		private Message message;
		
		private Attachment() {
		}

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		public String getId() {
			return id;
		}

		protected void setId(String id) {
			this.id = id;
		}

		public int getIndex() {
			return index;
		}

		protected void setIndex(int index) {
			this.index = index;
		}

		public Message getMessage() {
			return message;
		}

		protected void setMessage(Message message) {
			this.message = message;
		}

		public String getMimeType() {
			return mimeType;
		}

		protected void setMimeType(String mimeType) {
			this.mimeType = mimeType;
		}

		public String getName() {
			return name;
		}

		protected void setName(String name) {
			this.name = name;
		}

		public long getSize() {
			return size;
		}

		protected void setSize(long size) {
			this.size = size;
		}

		@Override
		public boolean equals(Object obj) {
			if( !(obj instanceof Attachment) )
				return false;
			
			Attachment ir = (Attachment)obj;
			
			return id.equals(ir.getId());
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}
}