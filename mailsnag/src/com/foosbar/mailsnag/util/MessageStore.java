package com.foosbar.mailsnag.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.UUID;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.model.Message;

public class MessageStore {

	private static final String FILE_EXT = ".eml";

	// Save message to directory
	//    workspace/.metadata/.plugins/com.foosbar.mailsnag
	public static final void persist(Message message, String data) {
		
		Writer writer = null;
		
		try {
			//Create random filename
			message.setFilename(getRandomFilename());
				
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
	
	public static final String load(Message message) {
		return load(message.getFilename());
	}
	
	public static final String load(String filename) {
		Reader reader = null;
		
		try {

			StringBuilder builder = new StringBuilder();
			
			reader = new BufferedReader(new FileReader(new File(Activator.getDefault().getStateLocation().toFile(), filename)));
	        
			char[] buffer = new char[8192];
	        while (reader.read(buffer) > 0)
	            builder.append(buffer);
			
	        return builder.toString();
	        
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		} finally {
			try {
				if(reader != null)
					reader.close();
			} catch(Exception e) { }
		}
	}
	
	public static final void removeAll() {
		File dir = Activator.getDefault().getStateLocation().toFile();
		if(dir.isDirectory()) {
			File[] files = dir.listFiles(new EmailFilenameFilter());
			for(File file : files)
				file.delete();
		}
	}
	
	private static final String getRandomFilename() {
		return UUID.randomUUID().toString() + FILE_EXT;
	}
}
