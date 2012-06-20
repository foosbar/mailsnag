/*******************************************************************************
 * Copyright (c) 2010-2012 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Kelley - initial API and implementation
 * Enrico - Server & Message Event Listeners
 *******************************************************************************/
package com.foosbar.mailsnag.util;

import org.eclipse.swt.widgets.Display;

import com.foosbar.mailsnag.events.MessageListListener;
import com.foosbar.mailsnag.model.Message;

/**
 * @author Kevin Kevin
 * 
 */
public final class NotificationManager implements MessageListListener {

	public NotificationManager() {
	}

	public void messageAdded(Message message) {
		Display.getDefault().asyncExec(new NotificationRunnable(message));
	}

	public void messageRemoved(Message message) {
		// Does nothing
		return;
	}

	public void messageAllRemoved() {
		// Does nothing
		return;
	}

	/**
	 * Runnable used to create and open the Message notification toaster.
	 */
	private class NotificationRunnable implements Runnable {
		private final Message message;

		public NotificationRunnable(Message message) {
			this.message = message;
		}

		@SuppressWarnings("restriction")
		public void run() {
			MessageNotification n = new MessageNotification(message, Display
					.getDefault());
			n.create();
			n.open();
		}
	}
}