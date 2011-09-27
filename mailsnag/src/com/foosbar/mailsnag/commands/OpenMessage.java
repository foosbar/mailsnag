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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import com.foosbar.mailsnag.editors.MessageEditor;
import com.foosbar.mailsnag.editors.MessageEditorInput;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.views.MessagesView;
import com.foosbar.mailsnag.views.MessagesView.ViewContentProvider;

public class OpenMessage extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);

		if (part instanceof MessagesView) {

			MessagesView viewer = (MessagesView) part;

			ViewContentProvider provider = (ViewContentProvider) viewer
					.getViewer().getContentProvider();
			IStructuredSelection iss = (IStructuredSelection) viewer
					.getViewer().getSelection();
			@SuppressWarnings("unchecked")
			Iterator<Object> it = iss.iterator();
			IWorkbenchPage page = part.getSite().getPage();
			while (it.hasNext()) {
				Object obj = it.next();
				if (obj instanceof Message) {
					Message m = (Message) obj;
					MessageEditorInput input = new MessageEditorInput(m);
					try {
						IDE.openEditor(page, input, MessageEditor.ID, true);
						provider.setRead(m);
						viewer.showLogo();
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}
}