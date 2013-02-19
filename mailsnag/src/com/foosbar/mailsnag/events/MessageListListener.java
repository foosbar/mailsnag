/*******************************************************************************
 * Copyright (c) 2010-2013 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Enrico - initial API and implementation
 *******************************************************************************/
package com.foosbar.mailsnag.events;

import com.foosbar.mailsnag.model.Message;

/**
 * Adds event handling capabilities for the Message List.
 * 
 * @author Enrico
 */
public interface MessageListListener {

	/**
	 * Event triggered when a new message has been received and added to the
	 * list.
	 * 
	 * @param message
	 *            the message added
	 */
	public void messageAdded(Message message);

	/**
	 * Event triggered when a message has been deleted from the message view
	 * list.
	 * 
	 * @param message
	 *            the message deleted
	 */
	public void messageRemoved(Message message);

	/**
	 * A user can delete all messages at once. This event will be triggered in
	 * such a situation.
	 */
	public void messageAllRemoved();

}
