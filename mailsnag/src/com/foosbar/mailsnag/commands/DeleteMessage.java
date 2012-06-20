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

/**
 * Handler for deleting selected messages. One or more messages can be selected
 * in the Email view and deleted all at once. This handler will get an
 * IStructuredSelection object containing references to those items selected.
 * 
 * @author kkelley (dev@foos-bar.com)
 * 
 */
public class DeleteMessage extends AbstractHandler {

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

			if (!iss.isEmpty()) {
				String message = getConfirmationMessage(iss.size());

				ResourceBundle BUNDLE = Activator.getResourceBundle();
				boolean confirm = MessageDialog.openConfirm(viewer.getViewer()
						.getControl().getShell(),
						BUNDLE.getString("action.delete.confirm"), message);

				if (confirm) {
					deleteMessages(iss, viewer);
				}
			}
		}
		return null;
	}

	/**
	 * Formats the confirmation message based on the selection size to be
	 * deleted.
	 * 
	 * @param size
	 *            Number of items selected for deletion
	 * @return the confirmation message
	 */
	private String getConfirmationMessage(int size) {

		ResourceBundle BUNDLE = Activator.getResourceBundle();

		if(size == 1) {
			return BUNDLE.getString("action.delete.confirm.single");
		}

		return String.format(BUNDLE
				.getString("action.delete.confirm.plural"), size);
	}

	/**
	 * Deletes the message from the filesystem and removes the entry from the
	 * Mail View.
	 * 
	 * @param iss
	 *            The selected items
	 * @param viewer
	 *            The viewer that contains the selected items
	 */
	private void deleteMessages(IStructuredSelection iss, MessagesView viewer) {
		@SuppressWarnings("unchecked")
		Iterator<Object> it = iss.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof Message) {
				Message m = (Message) obj;
				MessageStore.delete(m);
			}
		}
		viewer.getViewer().refresh();
	}
}