/*******************************************************************************
 * Copyright (c) 2010-2013 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Kelley - initial API and implementation
 * Enrico - Server & Message Event Listeners
 *******************************************************************************/
package com.foosbar.mailsnag.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.editors.MessageEditor;
import com.foosbar.mailsnag.editors.MessageEditorInput;
import com.foosbar.mailsnag.events.ServerStateListener;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.smtp.ServerState;
import com.foosbar.mailsnag.util.EmailFilenameFilter;
import com.foosbar.mailsnag.util.MessageStore;

/**
 * This is the main view for the plugin. It creates a table to display all
 * existing and new messages. The table is sortable and messages can be open in
 * the Email Editor or deleted from this part.
 * 
 * @author kkelley (dev@foos-bar.com)
 * 
 */
public class MessagesView extends ViewPart implements ServerStateListener {

	public static final String ID = "com.foosbar.mailsnag.views.MessagesView";

	// Locale Specific Resource Bundle
	private static final ResourceBundle BUNDLE = Activator.getResourceBundle();

	public static final String COL_TO = BUNDLE.getString("header.to");
	public static final String COL_CC = BUNDLE.getString("header.cc");
	public static final String COL_FROM = BUNDLE.getString("header.from");
	public static final String COL_SUBJECT = BUNDLE.getString("header.subject");
	public static final String COL_RECEIVED = BUNDLE
			.getString("header.received");

	private TableViewer viewer;
	private Action runServer;
	private Action stopServer;
	private Action openPreferences;
	private MessageContentProvider contentProvider;

	/**
	 * The constructor.
	 */
	public MessagesView() {
		super();
	}

	private void createColumns(final TableViewer viewer, Composite parent) {

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] titles = { "", COL_FROM, COL_TO, COL_CC, COL_SUBJECT,
				COL_RECEIVED };
		int[] bounds = { 28, 170, 170, 170, 275, 160 };

		for (int i = 0; i < titles.length; i++) {

			final String title = titles[i];

			TableViewerColumn vColumn = new TableViewerColumn(viewer, SWT.NONE);
			TableColumn column = vColumn.getColumn();
			column.setText(title);
			column.setWidth(bounds[i]);

			if (i == 0) {
				column.setAlignment(SWT.CENTER);
				column.setImage(Images.ATTACHMENT.createImage());
				column.setResizable(false);
				column.setMoveable(false);
			} else {
				column.setResizable(true);
				column.setMoveable(true);

				if (i == titles.length - 1) {
					column.setAlignment(SWT.RIGHT);
				}
				// Add Sorting
				column.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {

						MessageSorter sorter = (MessageSorter) viewer
								.getSorter();
						sorter.setColumnName(title);
						Table table = viewer.getTable();
						int dir = table.getSortDirection();
						TableColumn tc = (TableColumn) e.getSource();

						if (table.getSortColumn() == null) {
							dir = SWT.DOWN;
						} else if (table.getSortColumn().getText()
								.equals(title)) {
							dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
						} else {
							dir = SWT.DOWN;
						}

						table.setSortDirection(dir);
						table.setSortColumn(tc);
						viewer.refresh();
					}
				});
			}
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);
		createColumns(viewer, parent);

		contentProvider = new MessageContentProvider(loadMessages()) {
			@Override
			public void refreshView(final boolean notifyNewMessage) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						// Refresh the viewer
						getViewer().refresh();
						
						// If this refresh is due to a new message being added,
						// we need to change the icon for the viewer to indicate 
						// as such.
						if (notifyNewMessage) {
							IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getSite()
									.getService(IWorkbenchSiteProgressService.class);
							service.warnOfContentChange();
							getSite().getPage().activate(getSite().getPart());
							showNewMessages();
						}
					}
				});
			}
		};
		
		MessageStore.addMessageListListener(contentProvider);
		viewer.setContentProvider(contentProvider);

		viewer.setLabelProvider(new MessageLabelProvider());

		viewer.setInput(getViewSite());
		getSite().setSelectionProvider(viewer);

		MessageSorter sorter = new MessageSorter();
		viewer.setSorter(sorter);

		// Initial sort
		sorter.setColumnName(COL_RECEIVED);
		viewer.getTable().setSortDirection(SWT.UP);
		viewer.getTable().setSortColumn(
				viewer.getTable().getColumn(
						viewer.getTable().getColumnCount() - 1));
		viewer.refresh();

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(viewer.getControl(), "com.foos-bar.mailsnag.viewer");

		// Create the actions
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		IContextService contextService = (IContextService) getSite()
				.getService(IContextService.class);
		// IContextActivation contextActivation =
		contextService.activateContext("com.foos-bar.mailsnag.contexts");

	}

	/*
	 * private static FontData[] getModifiedFontData(FontData[] originalData,
	 * int additionalStyle) { FontData[] styleData = new
	 * FontData[originalData.length]; for (int i = 0; i < styleData.length; i++)
	 * { FontData base = originalData[i]; styleData[i] = new
	 * FontData(base.getName(), base.getHeight(), base.getStyle() |
	 * additionalStyle); } return styleData; }
	 */

	@Override
	public void dispose() {
		if (contentProvider != null) {
			MessageStore.removeMessageListListener(contentProvider);
		}
		Activator.getDefault().removeServerStateListener(this);
		super.dispose();
	}

	public void showNewMessages() {
		this.setTitleImage(Images.NEW_MESSAGES.createImage());
	}

	public void showLogo() {
		this.setTitleImage(Images.MAILSNAG_LOGO.createImage());
	}

	private List<Message> loadMessages() {
		List<Message> messages = new ArrayList<Message>();

		File dir = Activator.getDefault().getStateLocation().toFile();

		if (dir.isDirectory()) {

			File[] files = dir.listFiles(new EmailFilenameFilter());

			// Don't store contents of emails in memory. Pass file to Editor.

			for (File file : files) {
				Message message = MessageStore.load(file.getName());
				messages.add(message);
			}
		}

		return messages;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Object obj = ((IStructuredSelection) viewer.getSelection())
						.getFirstElement();
				if (obj != null && obj instanceof Message) {
					MessagesView.this.fillContextMenu(manager);
				}
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getTable());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(runServer);
		manager.add(stopServer);
		manager.add(new Separator());
		manager.add(openPreferences);
	}

	private void fillContextMenu(IMenuManager manager) {
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(runServer);
		manager.add(stopServer);
	}

	private void makeActions() {

		openPreferences = new Action() {
			@Override
			public void run() {
				PreferencesUtil
						.createPreferenceDialogOn(
								viewer.getControl().getShell(),
								"com.foosbar.mailsnag.preferences.PreferencePage",
								new String[] { "com.foosbar.mailsnag.preferences.PreferencePage" },
								null).open();
			}
		};

		openPreferences.setText(BUNDLE.getString("action.preferences"));
		openPreferences.setToolTipText(BUNDLE
				.getString("action.preferences.tooltip"));

		runServer = new Action() {
			@Override
			public void run() {
				Activator.getDefault().startServer();
			}
		};

		runServer.setText(BUNDLE.getString("action.start"));
		runServer.setToolTipText(BUNDLE.getString("action.start.tooltip"));
		runServer.setImageDescriptor(Images.RUN_SERVER);

		stopServer = new Action() {
			@Override
			public void run() {
				Activator.getDefault().stopServer();
			}
		};

		stopServer.setText(BUNDLE.getString("action.stop"));
		stopServer.setToolTipText(BUNDLE.getString("action.stop.tooltip"));
		stopServer.setImageDescriptor(Images.STOP_SERVER);

		Activator.getDefault().addServerStateListener(this);
		serverStateChanged(Activator.getDefault().getServerState());
	}

	private void setServerButtonsState(ServerState state) {
		boolean listening = state.isListening();
		runServer.setEnabled(!listening);
		stopServer.setEnabled(listening);
	}

	
	public void serverStateChanged(ServerState state) {
		setServerButtonsState(state);
	}

	/**
	 * Opens the currently selected message or messages.
	 */
	public void openMessage() {
		Iterator<Object> it = getSelectedMessagesIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof Message) {
				openMessage((Message) obj, false);
			}
		}
		return;
	}

	/**
	 * Opens the currently selected message or messages in the default system
	 * editor. The editor will be registered for .eml files by the operating
	 * system.
	 */
	public void openMessageWithSystemEditor() {
		Iterator<Object> it = getSelectedMessagesIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof Message) {
				Message m = (Message) obj;
				openMessage(m, true);
			}
		}
		return;
	}

	/**
	 * Gets an iterator containing elements selected in the Mail View
	 * 
	 * @return iterator with selected items
	 */
	@SuppressWarnings("unchecked")
	private Iterator<Object> getSelectedMessagesIterator() {
		IStructuredSelection iss = (IStructuredSelection) getViewer()
				.getSelection();
		return iss.iterator();
	}

	/**
	 * Opens a single message. If the inSystemEditor property is set to true,
	 * the plugin will request the operating system open the file ending in .eml
	 * This is typically an email application or text editor.
	 * 
	 * @param message
	 * @param inSystemEditor
	 */
	private void openMessage(Message message, boolean inSystemEditor) {
		IEditorInput input = inSystemEditor ? getSystemEditorInput(message)
				: getInternalEditorInput(message);
		String editorId = inSystemEditor ? IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID
				: MessageEditor.ID;
		try {
			IWorkbenchPage page = getSite().getPage();
			IDE.openEditor(page, input, editorId, true);
			setMessageRead(message);
			showLogo();
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private IEditorInput getInternalEditorInput(Message message) {
		return new MessageEditorInput(message);
	}

	/**
	 * This will open the email message (.eml) up in the system's preferred application.
	 * 
	 * @param message The message to be opened.
	 * @return IEditorInput
	 */
	private IEditorInput getSystemEditorInput(Message message) {
		String path = Activator.getDefault().getStateLocation().toOSString()
				+ File.separator + message.getFilename();
		IFileStore store = EFS.getLocalFileSystem().getStore(new Path(path));
		return new FileStoreEditorInput(store);
	}

	/**
	 * Mark message as being read.  This effectively unbolds the entry in the data table.
	 * 
	 * @param message
	 *            the message for which the status will be changed!
	 */
	public void setMessageRead(Message message) {
		if(contentProvider != null) {
			contentProvider.setRead(message);
		}
	}

	/**
	 * Mark message as being unread.  This effectively re-bolds the entry in the data table.
	 * 
	 * @param message
	 *            the message for which the status will be changed!
	 */
	public void setMessageUnRead(Message message) {
		if(contentProvider != null) {
			contentProvider.setUnRead(message);
		}
	}

	/**
	 * Double click event for a message entry.  It will open the message up in the default editor.
	 */
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				openMessage();
			}
		});
	}

	public TableViewer getViewer() {
		return viewer;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}