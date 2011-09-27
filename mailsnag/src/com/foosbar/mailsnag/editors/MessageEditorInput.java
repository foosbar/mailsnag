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

	private final String participant;
	private final Message message;

	public MessageEditorInput(Message message) {
		super();
		participant = message.getId();
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
		if (message.getSubject() == null
				|| "".equals(message.getSubject().trim())) {
			return "<No Subject>";
		} else {
			return message.getSubject();
		}
	}

	public String getParticipant() {
		return participant;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		if (message == null) {
			return "";
		}
		return message.getFrom();
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class arg0) {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}

		if (!(obj instanceof MessageEditorInput)) {
			return false;
		}

		MessageEditorInput other = (MessageEditorInput) obj;

		if (participant == null) {
			return other.getParticipant() == null;
		}

		return participant.equals(other.participant);
	}

	@Override
	public int hashCode() {
		return participant.hashCode();
	}
}
