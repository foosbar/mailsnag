package com.foosbar.mailsnag.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.UUID;

import javax.mail.BodyPart;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.model.Message.Attachment;
import com.foosbar.mailsnag.model.MessageData;
import com.foosbar.mailsnag.model.MessageParser;

public class MessageStore {

	// Save message to directory
	//    workspace/.metadata/.plugins/com.foosbar.mailsnag
	public static final Message persist(String data) {

		//Parse Message
		Message message = MessageParser.parse(data);

		//Create random filename
		message.setFilename(getRandomFilename());

		Writer writer = null;
		
		try {
				
			//Create file
			File file = new File(Activator.getDefault().getStateLocation().toFile(), message.getFilename());

			//Write file
			if(file != null) {
				writer = new BufferedWriter(new FileWriter(file));
				writer.write(data);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(writer != null)
					writer.close();
			} catch(Exception e) {}
		}
		
		return message;
	}
	
	// Save message to directory
	//    workspace/.metadata/.plugins/com.foosbar.mailsnag
	public static final Attachment persistAttachment(Attachment attachment, BodyPart part) {
		
		Message message = attachment.getMessage();
		
		InputStream in = null;
		OutputStream out = null;
		
		try {
			/*
			String filename = getAttachmentFilename(part);

			Attachment ma = new Attachment();
			ma.setName(filename);
			ma.setMimeType(part.getContentType());
			ma.setSize(part.getSize());
			*/
			
			//Get Attachment Directory
			File dir = new File(Activator.getDefault().getStateLocation().toFile(), message.getAttachmentDir());
			if(!dir.exists())
				dir.mkdir();

			File file = new File(dir, attachment.getName());
			
			if(file.exists())
				return attachment;

			//Write file
			if(file != null) {
				in  = new BufferedInputStream(part.getInputStream());
				out = new BufferedOutputStream(new FileOutputStream(file));
				byte[] buffer = new byte[4096];
				while(in.read(buffer) != -1)
					out.write(buffer);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(out != null)
					out.close();
			} catch(Exception e) {}
		}
		
		return attachment;
	}
	
	public static final void delete(Message message) {
		try {
			
			File file = new File(Activator.getDefault().getStateLocation().toFile(), message.getFilename());

			if(file.exists())
				file.delete();
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
		}
	}
	
	public static final Message load(String filename) {
		
		Reader reader = null;
		
		try {

			StringBuilder builder = new StringBuilder();
			
			reader = new BufferedReader(new FileReader(new File(Activator.getDefault().getStateLocation().toFile(), filename)));
	        
			char[] buffer = new char[8192];
	        while (reader.read(buffer) > 0)
	            builder.append(buffer);
			
	        Message message = MessageParser.parse(builder.toString());
	        message.setFilename(filename);
	        
	        return message;
	        
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(reader != null)
					reader.close();
			} catch(Exception e) { }
		}
		
		return null;
	}
	
	public static final MessageData loadData(Message message) {
		
		Reader reader = null;
		
		try {

			StringBuilder builder = new StringBuilder();
			
			String filename = message.getFilename();
			reader = new BufferedReader(new FileReader(new File(Activator.getDefault().getStateLocation().toFile(), filename)));
	        
			char[] buffer = new char[8192];
	        while (reader.read(buffer) > 0)
	            builder.append(buffer);
			
	        return MessageParser.parseData(builder.toString());
	        
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(reader != null)
					reader.close();
			} catch(Exception e) { }
		}
		
		return null;
	}
	
	public static final void removeAll() {
		File dir = Activator.getDefault().getStateLocation().toFile();
		if(dir.isDirectory()) {
			File[] files = dir.listFiles(new EmailFilenameFilter());
			for(File file : files)
				file.delete();
		}
	}
	
	public static final File getAttachment(Attachment attachment) {
		
		//Get MimeMessage
		
		//
		
		return null;
	}
	
	private static final String getRandomFilename() {
		return UUID.randomUUID().toString() + Message.EXTENSION;
	}
	
}