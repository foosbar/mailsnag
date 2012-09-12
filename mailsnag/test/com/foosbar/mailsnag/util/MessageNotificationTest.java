package com.foosbar.mailsnag.util;

import java.util.Date;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Display;

import com.foosbar.mailsnag.model.Message;

public class MessageNotificationTest {

	public static void main(String... args) {
		
		ResourceBundle bundle = ResourceBundle.getBundle("Resources");
		
		Message m = new Message();
		m.setSubject("This is a test subject");
		m.setFrom("dev@foos-bar.com");
		m.setTo("me@foos-bar.com");
		m.setId("1");
		m.setUnread(true);
		m.setReceived(new Date(System.currentTimeMillis()));
		
		INotification notification = new MessageNotification(m, bundle, Display.getDefault());
		notification.create();
		notification.open();
		
	}
	
}
