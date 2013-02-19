/*******************************************************************************
 * Copyright (c) 2010-2013 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Kelley - initial API and implementation
 *******************************************************************************/
package com.foosbar.mailsnag.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.foosbar.mailsnag.events.MessageListListener;
import com.foosbar.mailsnag.model.Message;

/**
 * Provides the data necessary to populate the Mail View.
 * 
 * @author kkelley (dev@foos-bar.com)
 */
public abstract class MessageContentProvider implements IStructuredContentProvider,
		MessageListListener {
	
	List<Message> messages = new ArrayList<Message>();

	public abstract void refreshView(boolean notifyNewMessage);
	
	public MessageContentProvider(List<Message> messages) {
		if (messages != null) {
			this.messages = messages;
		}
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		// Nothing to do.
	}

	public void dispose() {
		messages = null;
	}

	public Object[] getElements(Object parent) {
		return messages.toArray();
	}

	/**
	 * Adds a message to the current list of messages and updates the control.
	 * 
	 * @param message
	 */
	public void messageAdded(Message message) {
		messages.add(message);
		// Refresh the viewer
		refreshView(true);
	}

	/**
	 * Remove a message from the list
	 */
	public void messageRemoved(Message message) {
		messages.remove(message);
		refreshView(false);
	}

	public void messageAllRemoved() {
		messages.clear();
		refreshView(false);
	}

	/**
	 * Mark message as read - which removes the bold formatting for the message
	 * line item.
	 * 
	 * @param message
	 */
	public void setRead(Message message) {
		setReadStatus(message, true);
	}

	/**
	 * Mark message as unread - which removes the bold formatting for the
	 * message line item.
	 * 
	 * @param message
	 */
	public void setUnRead(Message message) {
		setReadStatus(message, false);
	}

	/**
	 * 
	 * @param message
	 *            the message to assign the status too
	 * @param unread
	 *            true if message is read
	 */
	private void setReadStatus(Message message, boolean read) {
		int idx = messages.indexOf(message);
		if (idx >= 0) {
			messages.get(idx).setUnread(!read);
		}
		refreshView(false);
	}
}