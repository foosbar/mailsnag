package com.foosbar.mailsnag.views;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
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

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.editors.MessageEditor;
import com.foosbar.mailsnag.editors.MessageEditorInput;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.model.MessageParser;
import com.foosbar.mailsnag.smtp.Server;
import com.foosbar.mailsnag.smtp.ServerThreadGroup;
import com.foosbar.mailsnag.util.EmailFilenameFilter;
import com.foosbar.mailsnag.util.MessageStore;

public class MessagesView extends ViewPart {

	public static final String COL_TO = "To";
	public static final String COL_CC = "Cc";
	public static final String COL_FROM = "From";
	public static final String COL_SUBJECT = "Subject";
	public static final String COL_RECEIVED = "Received";
	
	private TableViewer viewer;
	private MessageSorter sorter;
	
	private Action runServer;
	private Action stopServer;
	private Action openMessage;
	private Action removeMessage;
	private Action openPreferences;

	// View Icon
	private static final ImageDescriptor IMG_MESSAGE =
		ImageDescriptor.createFromFile(Activator.class, "/icons/message.gif");
	
	// Start Server Icon
	private static final ImageDescriptor IMG_RUN =
		ImageDescriptor.createFromFile(Activator.class, "/icons/run.gif");
	
	// Stop Server 
	private static final ImageDescriptor IMG_STOP =
		ImageDescriptor.createFromFile(Activator.class, "/icons/stop.gif");

	/**
	 * The constructor.
	 */
	public MessagesView() {
		super();
	}

	public class ViewContentProvider implements IStructuredContentProvider {
		Collection<Message> messages = new ArrayList<Message>();

		public ViewContentProvider(Collection<Message> messages) {
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
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		final DateFormat df = new SimpleDateFormat("EEE M/d/yyyy h:mm aaa"); 
		
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
					return df.format(message.getReceived());
				default:
					throw new RuntimeException("Should not happen");
			}
		}
		
		public Image getColumnImage(Object obj, int index) {
			if(index > 0)
				return null;
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return IMG_MESSAGE.createImage();
		}
	}

	private void createColumns(final TableViewer viewer, Composite parent) {
		
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] titles = { "", COL_FROM, COL_TO, COL_CC, COL_SUBJECT, COL_RECEIVED };
		int[] bounds = { 30, 170, 170, 170, 275, 160};
		
		for (int i = 0; i < titles.length; i++) {

			final String title = titles[i];
			
			TableViewerColumn vColumn = new TableViewerColumn(viewer, SWT.NONE);
			TableColumn column = vColumn.getColumn();
			column.setText(title);
			column.setWidth(bounds[i]);
			
			column.setResizable( i > 0 );
			column.setMoveable( i > 0 );

			if(i == 0) {
				column.setAlignment(SWT.CENTER);
			} else if(i == titles.length-1) {
				column.setAlignment(SWT.RIGHT);
			}
			
			// Add Sorting
			if(i != 0) {
				column.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						sorter.setColumnName(title);
						int dir = viewer.getTable().getSortDirection();
						TableColumn tc = (TableColumn)e.getSource();
						
						if(viewer.getTable().getSortColumn() == null) {
							dir = SWT.DOWN;
						} else if (viewer.getTable().getSortColumn().getText().equals(title)) {
							dir = (dir == SWT.UP) ? SWT.DOWN : SWT.UP;
						} else {
							dir = SWT.DOWN;
						}
						viewer.getTable().setSortDirection(dir);
						viewer.getTable().setSortColumn(tc);
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

		sorter = new MessageSorter();
		viewer.setSorter(sorter);
		
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.foos-bar.mailsnag.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private Collection<Message> loadMessages() {
		Collection<Message> messages = new ArrayList<Message>();
		
		File dir = Activator.getDefault().getStateLocation().toFile();
		
		if(dir.isDirectory()) {
			
			File[] files = dir.listFiles(new EmailFilenameFilter());
			
			//Don't store contents of emails in memory.  Pass file to Editor.
			
			for(File file : files) {
				String data = MessageStore.load(file.getName());
				Message message = MessageParser.parse(data);
				message.setFilename(file.getName());
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
	
	private void makeActions() {

		openPreferences = new Action() {
			public void run() {
				PreferencesUtil.createPreferenceDialogOn(
						viewer.getControl().getShell(), 
						"com.foosbar.mailsnag.preferences.PreferencePage", new String[]{"com.foosbar.mailsnag.preferences.PreferencePage"}, null).open();
			}
		};
		
		openPreferences.setText("Preferences");
		openPreferences.setToolTipText("Preferences");
		
		runServer = new Action() {
			public void run() {
				ThreadGroup tg = new ServerThreadGroup(server.getView(),"SMTPServer");
				new Thread(tg,server).start();
			}
		};
		
		runServer.setText("Start Listening");
		runServer.setToolTipText("Start Email Listener");
		runServer.setImageDescriptor(IMG_RUN);

		stopServer = new Action() {
			public void run() {
				server.close();
			}
		};
		
		stopServer.setText("Stop Listening");
		stopServer.setToolTipText("Stop Email Listener");
		stopServer.setImageDescriptor(IMG_STOP);
		stopServer.setEnabled(false);
	
		removeMessage = new Action() {
			@SuppressWarnings("unchecked")
			public void run() {
				IStructuredSelection iss = (IStructuredSelection) viewer.getSelection();
				int size = iss.size();
				
				String message = (size == 1) ? 
						"Are you sure you want to delete this message?" : 
							"Are you sure you want to delete " + size + " messages?" ;
				
				boolean confirm =
					MessageDialog.openConfirm(
						viewer.getControl().getShell(),
						"Confirm Delete",
						message);
				
				if(confirm) {
					Iterator<Object> it = iss.iterator();
					while(it.hasNext()) {
						Object obj = it.next();
						if(obj != null && obj instanceof Message) {
							MessageStore.delete((Message)obj);
							viewer.remove(obj);
						}
						//TODO: Close open editors
					}
				}
			}
		};
		removeMessage.setText("Delete");

		openMessage = new Action() {
			public void run() {
				openMessage();
			}
		};
		openMessage.setText("Open");
	}

	@SuppressWarnings("unchecked")
	private void openMessage() {
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