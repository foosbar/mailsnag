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
package com.foosbar.mailsnag.smtp;

import java.net.BindException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.preferences.PreferenceConstants;

public class ServerThreadGroup extends ThreadGroup {

	public ServerThreadGroup(String name) {
		super(name);
	}

	@Override
	public void uncaughtException(Thread t, final Throwable e) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (e.getCause() instanceof BindException) {

					int port = Activator.getDefault().getPreferenceStore()
							.getInt(PreferenceConstants.PARAM_PORT);

					IStatus status = new Status(
							IStatus.ERROR,
							Activator.PLUGIN_ID,
							"The application couldn't bind to the specified port.  Check to make sure the port isn't in use by another process or you have permission to bind to the port.",
							e);

					ResourceBundle bundle = Activator.getResourceBundle();

					ErrorDialog.openError(null, null, String.format(
							bundle.getString("exception.port.bind"), port),
							status);
				}
			}
		});
	}

}
