/*******************************************************************************
 * Copyright (c) 2010-2012 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Kelley - initial API and implementation
 *******************************************************************************/
package com.foosbar.mailsnag.commands;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.foosbar.mailsnag.model.Message;

/**
 * Handler for marking the status of a message.  Currently, that status can be READ or UNREAD.
 * 
 * @author kkelley (dev@foos-bar.com)
 * 
 */
public abstract class AbstractStatusCommand extends AbstractHandler {

	/**
	 * Iterates over all the selected messages and marks their read status to either
	 * READ or UNREAD.
	 * 
	 * @param iss
	 * @param status
	 */
	protected void markReadStatus(IStructuredSelection iss, Status status) {
		for(Iterator<Object> it = iss.iterator(); it.hasNext(); ) {
			Message m = getMessage(it.next());
			if(m != null) {
				m.setUnread(status == Status.UNREAD);
			}
		}
	}

	/**
	 * If the Object passed in is a Message, it casts the Object to Message, otherwise it
	 * just returns null so processing will skip this.
	 * @param obj
	 * @return
	 */
	private Message getMessage(Object obj) {
		if(obj instanceof Message) {
			return (Message) obj;
		}
		return null;
	}

	protected enum Status {
		READ,
		UNREAD
	}
}
