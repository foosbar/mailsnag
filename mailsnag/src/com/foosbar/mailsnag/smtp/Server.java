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
package com.foosbar.mailsnag.smtp;

import java.io.IOException;
import java.net.ServerSocket;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.views.MessagesView;

public class Server implements Runnable {

	private boolean listening;
	private ServerSocket serverSocket;

	public Server() {
	}

	public void run() {

		listening = true;

		// Get Preferences
		IPreferenceStore pStore = Activator.getDefault().getPreferenceStore();

		// Display Debug Messages?
		boolean debug = pStore.getBoolean(PreferenceConstants.PARAM_DEBUG);

		// Port to listen on.
		int port = pStore.getInt(PreferenceConstants.PARAM_PORT);

		try {
			serverSocket = new ServerSocket(port);
			if (debug) {
				System.out.println("MailSnag Server listening on port " + port);
			}
		} catch (IOException e) {
			// Print error to System.err
			e.printStackTrace(System.err);

			// Exit thread execution
			throw new RuntimeException(e);
		}

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessagesView view = (MessagesView) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.findView(MessagesView.ID);
				if (view != null) {
					view.disableStartServer();
					view.enableStopServer();
				}
			}
		});

		try {
			while (listening) {
				new MailHandler(serverSocket.accept()).start();
			}
		} catch (IOException e) {
			if (debug) {
				System.err.println(e);
			}
		} finally {

			if (serverSocket != null && !serverSocket.isClosed()) {
				if (debug) {
					System.out.println("Shutting down MailSnag");
				}

				try {
					serverSocket.close();
				} catch (IOException e) {
				}
			}

			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessagesView view = (MessagesView) PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().findView(MessagesView.ID);
					view.disableStopServer();
					view.enableStartServer();
				}
			});
		}
	}

	public void close() {
		listening = false;

		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}

	public boolean isListening() {
		return listening;
	}
}