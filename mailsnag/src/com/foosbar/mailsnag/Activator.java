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
package com.foosbar.mailsnag;

import java.util.ResourceBundle;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.foosbar.mailsnag.events.ServerStateListener;
import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.smtp.Server;
import com.foosbar.mailsnag.smtp.ServerState;
import com.foosbar.mailsnag.smtp.ServerThreadGroup;
import com.foosbar.mailsnag.util.MessageStore;
import com.foosbar.mailsnag.util.NotificationManager;

/**
 * The activator class controls the plug-in life cycle. Its treated as a
 * singleton that controls the SMTP server lifecycle as well.
 * 
 * @author kkelley (dev@foos-bar.com)
 */
public class Activator extends AbstractUIPlugin implements IStartup {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.foos-bar.mailsnag";

	// Locale Specific Resource Bundle
	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("i18n.Resources");

	// The shared instance
	private static Activator plugin;

	// SMTP Server
	private final Server server = new Server();

	// Handles message events with popups
	private final NotificationManager notificationManager = new NotificationManager();

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/**
	 * Activates the plugin. The SMTP server will be started as well, if the
	 * user has set the automatic preference to true.
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framewActivatorork
	 *      .BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// Start Server if User preference selected autostart
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				handleNotificationPreference();

				// Get user preferences
				IPreferenceStore store = Activator.getDefault()
						.getPreferenceStore();

				// Start server if the preferences indicate such.
				if (store.getBoolean(PreferenceConstants.PARAM_STARTUP)) {
					Activator.getDefault().startServer();
				}

				store.addPropertyChangeListener(new IPropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent event) {
						handleNotificationPreference();
					}
				});
			}
		});
	}

	private void handleNotificationPreference() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (store.getBoolean(PreferenceConstants.PARAM_NOTIFICATION_ENABLED)) {
			MessageStore.addMessageListListener(notificationManager);
		} else {
			MessageStore.removeMessageListListener(notificationManager);
		}
	}

	/**
	 * Creates a new ThreadGroup for the SMTP Server.
	 */
	public synchronized void startServer() {
		if (ServerState.STOPPED == server.getStatus()) {
			ThreadGroup tg = new ServerThreadGroup("SMTPServer");
			new Thread(tg, server).start();
		}
	}

	/**
	 * Returns the status of the server
	 * 
	 * @return the ServerStatus
	 */
	public ServerState getServerState() {
		return server.getStatus();
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 *      )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {

		if (server != null) {
			Activator.getDefault().stopServer();
		}

		// Get Persist Preference
		boolean persist = plugin.getPreferenceStore().getBoolean(
				PreferenceConstants.PARAM_PERSIST);

		// If not persisting, delete all messages
		if (!persist) {
			MessageStore.removeAll();
		}

		super.stop(context);
	}

	public void stopServer() {
		server.close();
	}

	public void addServerStateListener(ServerStateListener listener) {
		server.addServerStateListener(listener);
	}

	public void removeServerStateListener(ServerStateListener listener) {
		server.removeServerStateListener(listener);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static ResourceBundle getResourceBundle() {
		return BUNDLE;
	}

	/**
	 * Checks the preference store for the debug flag. If the option has been
	 * checked, then this will return true.
	 * 
	 * @return
	 */
	public static boolean isDebugMode() {
		return plugin.getPreferenceStore().getBoolean(
				PreferenceConstants.PARAM_DEBUG);
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Registers plugin for startup when Workbench starts Can be overridden by
	 * user in Preferences > General > Startup
	 * 
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		// TODO Auto-generated method stub
	}

	/**
	 * Checks if
	 * 
	 * @return
	 */
	public static boolean isNotificationAvailable() {
		try {
			Class.forName("org.eclipse.mylyn.commons.ui.dialogs.AbstractNotificationPopup");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}