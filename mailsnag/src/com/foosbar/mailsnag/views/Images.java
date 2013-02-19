/**
 * 
 */
package com.foosbar.mailsnag.views;

import org.eclipse.jface.resource.ImageDescriptor;

import com.foosbar.mailsnag.Activator;

/**
 * Contains the images used by the plug-in ui.
 * 
 * @author Kevin Kelley
 */
public final class Images {

	// Attachments Icon
	public static final ImageDescriptor ATTACHMENT = ImageDescriptor
			.createFromFile(Activator.class, "/icons/attachment.png");

	// Start Server Icon
	public static final ImageDescriptor RUN_SERVER = ImageDescriptor
			.createFromFile(Activator.class, "/icons/run.gif");

	// Stop Server
	public static final ImageDescriptor STOP_SERVER = ImageDescriptor
			.createFromFile(Activator.class, "/icons/stop.gif");

	// New Messages
	public static final ImageDescriptor NEW_MESSAGES = ImageDescriptor
			.createFromFile(Activator.class, "/icons/mail_new.gif");

	// Logo for View
	public static final ImageDescriptor MAILSNAG_LOGO = ImageDescriptor
			.createFromFile(Activator.class, "/icons/mail.gif");

	// Static utility class
	private Images() {
		// Not intended to be instantiated
	}
	
}
