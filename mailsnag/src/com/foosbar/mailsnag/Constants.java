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
package com.foosbar.mailsnag;

/**
 * Contains all the constants needed by the MailSnag Plugin
 * 
 * @author Kevin Kelley (dev@foos-bar.com)
 */
public final class Constants {

	public static final String EMAIL_EXTENSION = ".eml";

	public static final String[] FILESIZE_UNITS = new String[] { "B", "KB", "MB", "GB", "TB" };
	
	public static final String MYLYN_NOTIFICATION_CLASS = "org.eclipse.mylyn.commons.ui.dialogs.AbstractNotificationPopup";

	public static final String SMTP_SERVER_NAME = "SMTPServer";
	
	/**
	 * Private constructor to prevent instantiating a utility class.
	 * Could also create constants class as an interface which could in
	 * turn be extended, but that is unlikely in the context of this plugin.
	 */
	private Constants() {
		// This space intentionally left blank.
	}
	
}
