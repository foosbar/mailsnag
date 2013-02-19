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
package com.foosbar.mailsnag.preferences;

/**
 * Constant definitions for plug-in preferences. Each key is used to reference a
 * preference in the preference store.
 * 
 * @author Kevin Kelley
 */
public class PreferenceConstants {

	/**
	 * SMTP port server will listen on
	 */
	public static final String PARAM_PORT = "com.foos-bar.mailsnag.port";

	/**
	 * Will send debug statements to the system console
	 */
	public static final String PARAM_DEBUG = "com.foos-bar.mailsnag.debug";

	/**
	 * Enables/Disables the notification popup upon the arrival of new emails
	 */
	public static final String PARAM_NOTIFICATION_ENABLED = "com.foos-bar.mailsnag.notification.enabled";

	/**
	 * Persists the email between eclipse sessions
	 */
	public static final String PARAM_PERSIST = "com.foos-bar.mailsnag.persist";

	/**
	 * Will start the smtp server when eclipse starts up.
	 */
	public static final String PARAM_STARTUP = "com.foos-bar.mailsnag.startup";

}
