package com.foosbar.mailsnag.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.model.MessageData;
import com.foosbar.mailsnag.util.MessageStore;

/**
 * @author Kevin Kelley (dev@foos-bar.com)
 */
public class MessageEditorInput implements IEditorInput {

	private String participant;
	private Message message;
	
	public MessageEditorInput(Message message) {
		super();
		participant  = message.getId();
		this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
	
	public MessageData getMessageData() {
		return MessageStore.loadData(message);
	}
	
	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		if(message.getSubject() == null || "".equals(message.getSubject().trim()))
			return "<No Subject>";
		else
			return message.getSubject();
	}

	public String getParticipant() {
		return participant;
	}
	
	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		if(message == null)
			return "";
		return message.getFrom();
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class arg0) {
		return null;
	}

	public boolean equals(Object obj) {
		if(super.equals(obj))
			return true;
		
		if(!(obj instanceof MessageEditorInput))
			return false;
		
		MessageEditorInput other = (MessageEditorInput) obj;
		
		if(participant == null)
			return (other.getParticipant() == null);
		
		return participant.equals(other.participant);
	}

	public int hashCode() {
		return participant.hashCode();
	}
}
