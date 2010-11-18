package com.foosbar.mailsnag;

import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.smtp.Server;
import com.foosbar.mailsnag.smtp.ServerThreadGroup;
import com.foosbar.mailsnag.util.MessageStore;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.foos-bar.mailsnag";

	//Locale Specific Resource Bundle
	private static final ResourceBundle BUNDLE = 
		ResourceBundle.getBundle("i18n.Resources");

	// The shared instance
	private static Activator plugin;

	// SMTP Server
	private Server server = new Server();

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void startServer() {
		ThreadGroup tg = new ServerThreadGroup("SMTPServer");
		new Thread(tg,server).start();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		// Get Persist Preference
		boolean persist = 
			plugin.getPreferenceStore().getBoolean(PreferenceConstants.PARAM_PERSIST);
		
		// If not persisting, delete all messages
		if(!persist)
			MessageStore.removeAll();

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
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
