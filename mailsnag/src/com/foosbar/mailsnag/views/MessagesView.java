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
package com.foosbar.mailsnag.views;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
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
import com.foosbar.mailsnag.events.MessageListListener;
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

	// Attachments Icon
	private static final ImageDescriptor IMG_ATTACHMENT = ImageDescriptor
			.createFromFile(Activator.class, "/icons/attachment.png");

	// Start Server Icon
	private static final ImageDescriptor IMG_RUN = ImageDescriptor
			.createFromFile(Activator.class, "/icons/run.gif");

	// Stop Server
	private static final ImageDescriptor IMG_STOP = ImageDescriptor
			.createFromFile(Activator.class, "/icons/stop.gif");

	// New Messages
	private static final ImageDescriptor IMG_NEW_MESSAGES = ImageDescriptor
			.createFromFile(Activator.class, "/icons/mail_new.gif");

	// Logo for View
	private static final ImageDescriptor IMG_LOGO = ImageDescriptor
			.createFromFile(Activator.class, "/icons/mail.gif");

	// Locale Specific Date Formatter
	private static final DateFormat DATE_FORMATTER = DateFormat
			.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT,
					Locale.getDefault());

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
	private ViewContentProvider contentProvider;

	/**
	 * The constructor.
	 */
	public MessagesView() {
		super();
	}

	/**
	 * Provides the data necessary to populate the Mail View.
	 * 
	 * @author kkelley (dev@foos-bar.com)
	 */
	public class ViewContentProvider implements IStructuredContentProvider,
			MessageListListener {
		List<Message> messages = new ArrayList<Message>();

		public ViewContentProvider(List<Message> messages) {
			if (messages != null) {
				this.messages = messages;
			}
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			// Nothing to do.
		}

		public void dispose() {
			messages = null;
		}

		public Object[] getElements(Object parent) {
			return messages.toArray();
		}

		/**
		 * Adds a message to the current list of messages and updates the
		 * control.
		 * 
		 * @param message
		 */
		public void messageAdded(Message message) {
			messages.add(message);
			// Refresh the viewer
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					// Refresh the viewer
					getViewer().refresh();
					// Sets label bold if the view is unfocused.
					IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getSite()
							.getService(IWorkbenchSiteProgressService.class);
					service.warnOfContentChange();
					getSite().getPage().activate(getSite().getPart());
					showNewMessages();

					/*
					 * Trying to get part to make itself visible if its hidden
					 * or not open. IWorkbenchPage page = getSite().getPage();
					 * showNewMessages(); boolean isVisible =
					 * page.isPartVisible(getSite().getPart()); if (!isVisible)
					 * { try { page.showView(ID, null,
					 * IWorkbenchPage.VIEW_VISIBLE); } catch (PartInitException
					 * e) { e.printStackTrace(); } }
					 */
				}
			});
		}

		/**
		 * Remove a message from the list
		 */
		public void messageRemoved(Message message) {
			messages.remove(message);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					// Refresh the viewer
					getViewer().refresh();
				}
			});
		}

		public void messageAllRemoved() {
			messages.clear();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					// Refresh the viewer
					getViewer().refresh();
				}
			});
		}

		/**
		 * Mark message as read - which removes the bold formatting for the
		 * message line item.
		 * 
		 * @param message
		 */
		public void setRead(Message message) {
			setReadStatus(message, true);
		}

		/**
		 * Mark message as unread - which removes the bold formatting for the
		 * message line item.
		 * 
		 * @param message
		 */
		public void setUnRead(Message message) {
			setReadStatus(message, false);
		}

		/**
		 * 
		 * @param message
		 *            the message to assign the status too
		 * @param unread
		 *            true if message is read
		 */
		private void setReadStatus(Message message, boolean read) {
			int idx = messages.indexOf(message);
			if (idx >= 0) {
				messages.get(idx).setUnread(!read);
			}
			getViewer().refresh();
		}
	}

	/**
	 * Returns the label for each of the columns based on the index for that
	 * column.
	 * 
	 * @author Kevin Kelley
	 */
	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider, ITableFontProvider, ITableColorProvider {

		/**
		 * Based on the index of each column, return the proper value to
		 * populate the data.
		 */
		public String getColumnText(Object obj, int index) {
			Message message = (Message) obj;
			switch (index) {
			case 0:
				return null;
			case 1:
				return message.getFrom();
			case 2:
				return message.getTo();
			case 3:
				return message.getCc();
			case 4:
				return message.getSubject();
			case 5:
				if (message.getReceived() == null) {
					return "";
				} else {
					return DATE_FORMATTER.format(message.getReceived());
				}
			default:
				throw new RuntimeException("Should not happen");
			}
		}

		/**
		 * Returns an attachment icon for the first column (index == 0) if the
		 * message contains an attachment
		 */
		public Image getColumnImage(Object obj, int index) {
			if (index > 0) {
				return null;
			}

			Message message = (Message) obj;

			if (message.hasAttachments()) {
				return getImage(obj);
			}

			return null;
		}

		@Override
		public Image getImage(Object obj) {
			return IMG_ATTACHMENT.createImage();
		}

		public Color getForeground(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public Color getBackground(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public Font getFont(Object element, int columnIndex) {
			return getFont((Message) element);
		}

		public Font getFont(Message message) {
			if (message.isUnread()) {
				return JFaceResources.getFontRegistry().getBold(
						JFaceResources.DIALOG_FONT);
			} else {
				return null;
			}
		}
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
				column.setImage(IMG_ATTACHMENT.createImage());
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

		contentProvider = new ViewContentProvider(loadMessages());
		MessageStore.addMessageListListener(contentProvider);
		viewer.setContentProvider(contentProvider);

		viewer.setLabelProvider(new ViewLabelProvider());

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
		this.setTitleImage(IMG_NEW_MESSAGES.createImage());
	}

	public void showLogo() {
		this.setTitleImage(IMG_LOGO.createImage());
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
		runServer.setImageDescriptor(IMG_RUN);

		stopServer = new Action() {
			@Override
			public void run() {
				Activator.getDefault().stopServer();
			}
		};

		stopServer.setText(BUNDLE.getString("action.stop"));
		stopServer.setToolTipText(BUNDLE.getString("action.stop.tooltip"));
		stopServer.setImageDescriptor(IMG_STOP);

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

	private IEditorInput getSystemEditorInput(Message message) {
		String path = Activator.getDefault().getStateLocation().toOSString()
				+ File.separator + message.getFilename();
		IFileStore store = EFS.getLocalFileSystem().getStore(new Path(path));
		return new FileStoreEditorInput(store);
	}

	public void setMessageRead(Message message) {
		ViewContentProvider provider = (ViewContentProvider) getViewer()
				.getContentProvider();
		provider.setRead(message);
	}

	public void setMessageUnRead(Message message) {
		ViewContentProvider provider = (ViewContentProvider) getViewer()
				.getContentProvider();
		provider.setRead(message);
	}

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