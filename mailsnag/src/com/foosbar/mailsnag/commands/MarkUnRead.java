/*******************************************************************************
 * Copyright (c) 2010-2012 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Kelley - initial API and implementation
 *******************************************************************************/
package com.foosbar.mailsnag.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.foosbar.mailsnag.views.MessagesView;

/**
 * Handler for marking a message "un-read". From the view standpoint, the
 * message entry text will be updated in bold.
 * 
 * @author kkelley (dev@foos-bar.com)
 * 
 */
public class MarkUnRead extends AbstractStatusCommand {

	/**
	 * Main execution
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbenchPart part = HandlerUtil.getActivePart(event);

		if (part instanceof MessagesView) {

			final MessagesView viewer = (MessagesView) part;

			viewer.showLogo();

			IStructuredSelection iss = (IStructuredSelection) HandlerUtil
					.getCurrentSelection(event);

			markReadStatus(iss, Status.UNREAD);
		}
		return null;
	}
}