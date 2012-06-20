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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.events.ServerStateListener;
import com.foosbar.mailsnag.preferences.PreferenceConstants;

public class Server implements Runnable {

	private ServerState status;
	private ServerSocket serverSocket;

	private final Set<ServerStateListener> listeners = new HashSet<ServerStateListener>();

	public Server() {
		status = ServerState.STOPPED;
	}

	public void run() {

		setStatus(ServerState.STARTING);

		// Get Preferences
		IPreferenceStore pStore = Activator.getDefault().getPreferenceStore();

		// Display Debug Messages?
		boolean debug = pStore.getBoolean(PreferenceConstants.PARAM_DEBUG);

		// Port to listen on.
		int port = pStore.getInt(PreferenceConstants.PARAM_PORT);

		try {
			serverSocket = new ServerSocket(port);

			setStatus(ServerState.LISTENING);

			if (debug) {
				System.out.println("MailSnag Server listening on port " + port);
			}
		} catch (IOException e) {
			// Print error to System.err
			e.printStackTrace(System.err);

			// Exit thread execution
			throw new RuntimeException(e);
		}

		try {
			while (ServerState.LISTENING == status) {
				new MailHandler(serverSocket.accept()).start();
			}
		} catch (IOException e) {
			if (debug) {
				// Print error to System.err
				e.printStackTrace(System.err);
			}
		} finally {
			close();
		}
	}

	public void close() {
		if (serverSocket != null && !serverSocket.isClosed()) {
			// Get Preferences
			IPreferenceStore pStore = Activator.getDefault()
					.getPreferenceStore();

			// Display Debug Messages?
			boolean debug = pStore.getBoolean(PreferenceConstants.PARAM_DEBUG);

			if (debug) {
				System.out.println("Shutting down MailSnag Server");
			}

			try {
				serverSocket.close();
				setStatus(ServerState.STOPPED);

			} catch (Exception e) {
				if (debug) {
					e.printStackTrace(System.err);
				}
			}
		}
	}

	public ServerState getStatus() {
		return status;
	}

	private void setStatus(ServerState status) {
		this.status = status;
		for (ServerStateListener listener : listeners) {
			listener.serverStateChanged(status);
		}
	}

	public void addServerStateListener(ServerStateListener listener) {
		listeners.add(listener);
	}

	public void removeServerStateListener(ServerStateListener listener) {
		listeners.remove(listener);
	}
}