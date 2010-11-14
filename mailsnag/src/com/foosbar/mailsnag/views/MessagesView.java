package com.foosbar.mailsnag.views;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.editors.MessageEditor;
import com.foosbar.mailsnag.editors.MessageEditorInput;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.smtp.Server;
import com.foosbar.mailsnag.smtp.ServerThreadGroup;
import com.foosbar.mailsnag.util.EmailFilenameFilter;
import com.foosbar.mailsnag.util.MessageStore;

public class MessagesView extends ViewPart {

	// Attachments Icon
	private static final ImageDescriptor IMG_ATTACHMENT =
		ImageDescriptor.createFromFile(Activator.class, "/icons/attachment.png");

	// Start Server Icon
	private static final ImageDescriptor IMG_RUN =
		ImageDescriptor.createFromFile(Activator.class, "/icons/run.gif");
	
	// Stop Server 
	private static final ImageDescriptor IMG_STOP =
		ImageDescriptor.createFromFile(Activator.class, "/icons/stop.gif");

	//Locale Specific Date Formatter
	private static final DateFormat DATE_FORMATTER = 
		DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());
	
	//Locale Specific Resource Bundle
	private static final ResourceBundle BUNDLE = Activator.getResourceBundle();

	public static final String COL_TO = BUNDLE.getString("header.to");
	public static final String COL_CC = BUNDLE.getString("header.cc");
	public static final String COL_FROM = BUNDLE.getString("header.from");
	public static final String COL_SUBJECT = BUNDLE.getString("header.subject");
	public static final String COL_RECEIVED = BUNDLE.getString("header.received");
	
	private TableViewer viewer;
	
	private Action runServer;
	private Action stopServer;
	private Action openMessage;
	private Action removeMessage;
	private Action openPreferences;

	//private MessagesView messagesView;
	
	/**
	 * The constructor.
	 */
	public MessagesView() {
		super();
		//messagesView = this;
	}

	public class ViewContentProvider implements IStructuredContentProvider {
		List<Message> messages = new ArrayList<Message>();

		public ViewContentProvider(List<Message> messages) {
			if(messages != null) 
				this.messages = messages;
		}
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
	
		public void dispose() {
		}
		
		public Object[] getElements(Object parent) {
			return messages.toArray();
		}
		
		public void add(Message message) {
			messages.add(message);
			getViewer().refresh();

			//Sets label bold if the view is unfocused.
			IWorkbenchSiteProgressService service = 
				(IWorkbenchSiteProgressService) getSite().getService(IWorkbenchSiteProgressService.class);
			
			service.warnOfContentChange();
		}
		
		public void remove(Message message) {
			messages.remove(message);
		}
		
		public void setRead(Message message) {
			int idx = messages.indexOf(message);
			if(idx >= 0)
				messages.get(idx).setUnread(false);
			getViewer().refresh();
		}
	}
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

	/*
	class ViewLabelProvider extends StyledCellLabelProvider {
		
		@Override
		public void update(ViewerCell cell) {
			
			Message message = (Message)cell.getElement();
			int index = cell.getColumnIndex();
			String columnText = getColumnText(message, index);
			cell.setText(columnText);
			cell.setImage(getColumnImage(message, index));

			if(message.isUnread()) {
				FontData fdata = cell.getFont().getFontData()[0];
				fdata.setStyle(SWT.BOLD);
				Font f = new Font(Display.getDefault(), fdata);
				
				StyleRange style = new StyleRange();
				style.font = f;
				style.length = cell.getText().length();
				cell.setStyleRanges(new StyleRange[] { style });
			}
			
			super.update(cell);
		}
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
					return DATE_FORMATTER.format(message.getReceived());
				default:
					throw new RuntimeException("Should not happen");
			}
		}
		
		public Image getColumnImage(Object obj, int index) {
			if(index > 0)
				return null;
			
			Message message = (Message) obj;
			
			if(message.hasAttachments())
				return getImage(obj);

			return null;
		}
		
		public Image getImage(Object obj) {
			return IMG_ATTACHMENT.createImage();
		}
	}

	private void createColumns(final TableViewer viewer, Composite parent) {
		
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] titles = { "", COL_FROM, COL_TO, COL_CC, COL_SUBJECT, COL_RECEIVED };
		int[] bounds = { 28, 170, 170, 170, 275, 160};
		
		for (int i = 0; i < titles.length; i++) {

			final String title = titles[i];
			
			TableViewerColumn vColumn = new TableViewerColumn(viewer, SWT.NONE);
			TableColumn column = vColumn.getColumn();
			column.setText(title);
			column.setWidth(bounds[i]);

			if(i == 0) {
				column.setAlignment(SWT.CENTER);
				column.setImage(IMG_ATTACHMENT.createImage());
				column.setResizable(false);
				column.setMoveable(false);
			} else {
				column.setResizable(true);
				column.setMoveable(true);
				
				if(i == titles.length-1)
					column.setAlignment(SWT.RIGHT);
				// Add Sorting
				column.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						
						MessageSorter sorter = (MessageSorter)viewer.getSorter();
						sorter.setColumnName(title);
						Table table = viewer.getTable();
						int dir = table.getSortDirection();
						TableColumn tc = (TableColumn)e.getSource();
						
						if(table.getSortColumn() == null) {
							dir = SWT.DOWN;
						} else if (table.getSortColumn().getText().equals(title)) {
							dir = (dir == SWT.UP) ? SWT.DOWN : SWT.UP;
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
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		createColumns(viewer,parent);

		viewer.setContentProvider(new ViewContentProvider(loadMessages()));
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		
		MessageSorter sorter = new MessageSorter();
		viewer.setSorter(sorter);

		//Initial sort
		sorter.setColumnName(COL_RECEIVED);
		viewer.getTable().setSortDirection(SWT.UP);
		viewer.getTable().setSortColumn(viewer.getTable().getColumn(viewer.getTable().getColumnCount()-1));
		viewer.refresh();

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.foos-bar.mailsnag.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		IPreferenceStore store = 
			Activator.getDefault().getPreferenceStore();
		
		//Start server if the preferences indicate such.
		if(store.getBoolean(PreferenceConstants.PARAM_STARTUP))
			startServer();

		//this.setTitleImage(IMG_RUN.createImage());
		
	}

	private List<Message> loadMessages() {
		List<Message> messages = new ArrayList<Message>();
		
		File dir = Activator.getDefault().getStateLocation().toFile();
		
		if(dir.isDirectory()) {
			
			File[] files = dir.listFiles(new EmailFilenameFilter());
			
			//Don't store contents of emails in memory.  Pass file to Editor.
			
			for(File file : files) {
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
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if(obj != null && obj instanceof Message)
					MessagesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
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
		manager.add(openMessage);
		manager.add(new Separator());
		manager.add(removeMessage);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(runServer);
		manager.add(stopServer);
	}

	public void enableStartServer() {
		if(runServer != null)
			runServer.setEnabled(true);
	}

	public void disableStartServer() {
		if(runServer != null)
			runServer.setEnabled(false);
	}
	
	public void enableStopServer() {
		if(stopServer != null)
			stopServer.setEnabled(true);
	}

	public void disableStopServer() {
		if(stopServer != null)
			stopServer.setEnabled(false);
	}

	private Server server = new Server(this);
	
	private void startServer() {
		ThreadGroup tg = new ServerThreadGroup(server.getView(),"SMTPServer");
		new Thread(tg,server).start();
	}
	
	private void makeActions() {

		openPreferences = new Action() {
			public void run() {
				PreferencesUtil.createPreferenceDialogOn(
						viewer.getControl().getShell(), 
						"com.foosbar.mailsnag.preferences.PreferencePage", new String[]{"com.foosbar.mailsnag.preferences.PreferencePage"}, null).open();
			}
		};
		
		openPreferences.setText(BUNDLE.getString("action.preferences"));
		openPreferences.setToolTipText(BUNDLE.getString("action.preferences.tooltip"));
		
		runServer = new Action() {
			public void run() {
				startServer();
			}
		};
		
		runServer.setText(BUNDLE.getString("action.start"));
		runServer.setToolTipText(BUNDLE.getString("action.start.tooltip"));
		runServer.setImageDescriptor(IMG_RUN);

		stopServer = new Action() {
			public void run() {
				server.close();
			}
		};
		
		stopServer.setText(BUNDLE.getString("action.stop"));
		stopServer.setToolTipText(BUNDLE.getString("action.stop.tooltip"));
		stopServer.setImageDescriptor(IMG_STOP);
		stopServer.setEnabled(false);
	
		removeMessage = new Action() {
			@SuppressWarnings("unchecked")
			public void run() {
				IStructuredSelection iss = (IStructuredSelection) viewer.getSelection();
				int size = iss.size();
				
				String message = (size == 1) ? 
						BUNDLE.getString("action.delete.confirm.single") : 
							String.format(BUNDLE.getString("action.delete.confirm.plural"),size) ;
				
				boolean confirm =
					MessageDialog.openConfirm(
						viewer.getControl().getShell(),
						BUNDLE.getString("action.delete.confirm"),
						message);
				
				if(confirm) {
					Iterator<Object> it = iss.iterator();
					while(it.hasNext()) {
						Object obj = it.next();
						if(obj != null && obj instanceof Message) {
							Message m = (Message)obj;
							MessageStore.delete((Message)obj);
							((ViewContentProvider) viewer.getContentProvider()).remove(m);
						}
						//TODO: Close open editors
					}
					getViewer().refresh();
				}
			}
		};
		removeMessage.setText(BUNDLE.getString("action.delete"));

		openMessage = new Action() {
			public void run() {
				openMessage();
			}
		};
		openMessage.setText(BUNDLE.getString("action.open"));
	}

	@SuppressWarnings("unchecked")
	private void openMessage() {
		ViewContentProvider provider = (ViewContentProvider) viewer.getContentProvider();
		IStructuredSelection iss = (IStructuredSelection) viewer.getSelection();
		Iterator<Object> it = iss.iterator();
		while(it.hasNext()) {
			Object obj = it.next();
			if(obj instanceof Message) {
				Message m = (Message)obj;
       			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
       			MessageEditorInput input = new MessageEditorInput(m);
       			try {
       				IDE.openEditor(page, input, MessageEditor.ID, true);
       				provider.setRead(m);
       			} catch(PartInitException e) {
       				e.printStackTrace();
       			}
	        }
        }
        return;
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
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}