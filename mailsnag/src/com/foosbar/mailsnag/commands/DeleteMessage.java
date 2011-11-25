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
package com.foosbar.mailsnag.commands;

import java.util.Iterator;
import java.util.ResourceBundle;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.util.MessageStore;
import com.foosbar.mailsnag.views.MessagesView;
import com.foosbar.mailsnag.views.MessagesView.ViewContentProvider;

/**
 * Handler for deleting selected messages.
 * 
 * @author kkelley
 * 
 */
public class DeleteMessage extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbenchPart part = HandlerUtil.getActivePart(event);

		if (part instanceof MessagesView) {

			final MessagesView viewer = (MessagesView) part;

			viewer.showLogo();

			IStructuredSelection iss = (IStructuredSelection) HandlerUtil
					.getCurrentSelection(event);

			int size = iss.size();

			if (size == 0) {
				return null;
			}

			ResourceBundle BUNDLE = Activator.getResourceBundle();

			String message = size == 1 ? BUNDLE
					.getString("action.delete.confirm.single") : String.format(
					BUNDLE.getString("action.delete.confirm.plural"), size);

			boolean confirm = MessageDialog.openConfirm(viewer.getViewer()
					.getControl().getShell(),
					BUNDLE.getString("action.delete.confirm"), message);

			if (confirm) {
				deleteMessages(iss, viewer);
			}
		}
		return null;
	}

	private void deleteMessages(IStructuredSelection iss, MessagesView viewer) {
		@SuppressWarnings("unchecked")
		Iterator<Object> it = iss.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj != null && obj instanceof Message) {
				Message m = (Message) obj;
				MessageStore.delete((Message) obj);
				((ViewContentProvider) viewer.getViewer().getContentProvider())
						.remove(m);
			}
		}
		viewer.getViewer().refresh();
	}
}