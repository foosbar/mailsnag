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
package com.foosbar.mailsnag.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.foosbar.mailsnag.views.MessagesView;

/**
 * Handler for opening a messsage. The message will have been selected from the
 * Mail View and either double clicked, or "Right-Click" > Open Message from
 * menu.
 * 
 * @author kkelley (dev@foos-bar.com)
 * 
 */
public class OpenMessage extends AbstractHandler {

	/**
	 * Main Execution
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof MessagesView) {
			MessagesView viewer = (MessagesView) part;
			viewer.openMessage();
		}
		return null;
	}
}