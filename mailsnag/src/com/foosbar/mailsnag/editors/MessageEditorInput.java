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
public class MessageEditorInput
		implements IEditorInput {

	private final String participant;
	private final Message message;

	public MessageEditorInput(Message message) {
		super();
		this.participant = message.getId();
		this.message = message;
	}

	public Message getMessage() {
		return this.message;
	}

	public MessageData getMessageData() {
		return MessageStore.loadData(this.message);
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		if (this.message.getSubject() == null
				|| "".equals(this.message.getSubject().trim())) {
			return "<No Subject>";
		} else {
			return this.message.getSubject();
		}
	}

	public String getParticipant() {
		return this.participant;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		if (this.message == null) {
			return "";
		}
		return this.message.getFrom();
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

		if (this.participant == null) {
			return other.getParticipant() == null;
		}

		return this.participant.equals(other.participant);
	}

	@Override
	public int hashCode() {
		if (this.participant != null) {
			return this.participant.hashCode();
		}
		return super.hashCode();
	}
}
