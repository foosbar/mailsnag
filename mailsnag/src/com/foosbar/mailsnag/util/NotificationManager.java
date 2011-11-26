/**
 * 
 */
package com.foosbar.mailsnag.util;

import org.eclipse.swt.widgets.Display;

import com.foosbar.mailsnag.model.Message;

/**
 * @author Kevin Kevin
 * 
 */
public final class NotificationManager {

	private NotificationManager() {
	}

	@SuppressWarnings("restriction")
	public static void notify(Message message, Display display) {
		MessageNotification notification = new MessageNotification(message,
				display);
		notification.create();
		notification.open();
	}

}