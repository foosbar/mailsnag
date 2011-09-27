/*******************************************************************************
 * Copyright (c) 2010-2011 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Kelley - initial API and implementation
 *******************************************************************************/
package com.foosbar.mailsnag;

import java.util.ResourceBundle;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.smtp.Server;
import com.foosbar.mailsnag.smtp.ServerThreadGroup;
import com.foosbar.mailsnag.util.MessageStore;
import com.foosbar.mailsnag.views.MessagesView;

/**
 * The activator class controls the plug-in life cycle.
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

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framewActivatorork
	 * .BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// Start Server if:
		// a) View is open
		// b) User preference selected autostart
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				// Check if view is loaded
				MessagesView view = (MessagesView) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.findView(MessagesView.ID);

				// If view is loaded, check user preference
				if (view != null) {
					// Get user preferences
					IPreferenceStore store = Activator.getDefault()
							.getPreferenceStore();

					// Start server if the preferences indicate such.
					if (store.getBoolean(PreferenceConstants.PARAM_STARTUP)) {
						Activator.getDefault().startServer();
					}
				}
			}
		});
	}

	public void startServer() {
		ThreadGroup tg = new ServerThreadGroup("SMTPServer");
		new Thread(tg, server).start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		// Get Persist Preference
		boolean persist = plugin.getPreferenceStore().getBoolean(
				PreferenceConstants.PARAM_PERSIST);

		// If not persisting, delete all messages
		if (!persist) {
			MessageStore.removeAll();
		}

		if (server != null) {
			Activator.getDefault().stopServer();
		}

		super.stop(context);
	}

	public void stopServer() {
		server.close();
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
}