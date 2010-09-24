package com.foosbar.mailsnag.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class MessageStorage implements IStorage {

	private Message message;
	
	public MessageStorage(Message message) {
		this.message = message;
	}
	
	public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(message.getMessage().getBytes());
	}

	public IPath getFullPath() {
		return null;
	}

	public String getName() {
		return "email.eml";
	}

	public boolean isReadOnly() {
		return true;
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

}
