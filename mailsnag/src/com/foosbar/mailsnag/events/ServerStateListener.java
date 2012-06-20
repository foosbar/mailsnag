/*******************************************************************************
 * Copyright (c) 2010-2012 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Enrico - initial API and implementation
 * Kevin Kelley - Added ServerState enum
 *******************************************************************************/
package com.foosbar.mailsnag.events;

import com.foosbar.mailsnag.smtp.ServerState;


/**
 * Adds event handling capabilities for the SMTP Server Status.
 */
public interface ServerStateListener {

	/**
	 * Event triggered when the servers state has changed. Currently there are
	 * three states: STARTING, LISTENING and STOPPED.
	 * 
	 * @param state
	 *            the current state of the server
	 */
	public void serverStateChanged(ServerState state);

}
