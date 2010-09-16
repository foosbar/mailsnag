package com.foosbar.mailsnag.views;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.smtp.Server;

public class MessagesView extends ViewPart {

	private TableViewer viewer;
	private Action runServer;
	private Action stopServer;
	private Action openMessage;
	private Action removeMessage;
	
	private static final ImageDescriptor IMG_MESSAGE = ImageDescriptor.createFromFile(Activator.class, "/icons/message.gif"); 
	private static final ImageDescriptor IMG_RUN  = ImageDescriptor.createFromFile(Activator.class, "/icons/run.gif");
	private static final ImageDescriptor IMG_STOP = ImageDescriptor.createFromFile(Activator.class, "/icons/stop.gif");

	public class ViewContentProvider implements IStructuredContentProvider {
		List<Message> messages = new ArrayList<Message>();

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
	
		public void dispose() {
		}
		
		public Object[] getElements(Object parent) {
			return messages.toArray();
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
					return message.getToString();
				case 3:
					return message.getSubject();
				case 4:
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
	class NameSorter extends ViewerSorter {
	}

	private void createColumns(TableViewer viewer, Composite parent) {
		String[] titles = { "", "From", "To", "Subject", "Received" };
		int[] bounds = { 26, 200, 200, 350, 200};
		
		for (int i = 0; i < titles.length; i++) {
			TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
			TableColumn column = viewerColumn.getColumn();
			column.setText(titles[i]);
			column.setWidth(bounds[i]);
			column.setResizable( i > 0 );
			column.setMoveable( i > 0 );
			if(i == 0)
				column.setAlignment(SWT.CENTER);
			else if(i == titles.length-1)
				column.setAlignment(SWT.RIGHT);
		}
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}


	/**
	 * The constructor.
	 */
	public MessagesView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		createColumns(viewer,parent);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.foos-bar.mailsnag.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
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

	private Server server = new Server(
			Activator.getDefault().getPreferenceStore().getInt(
					PreferenceConstants.PARAM_PORT), this);
	
	private void makeActions() {
		
		runServer = new Action() {
			public void run() {
				stopServer.setEnabled(true);
				runServer.setEnabled(false);
				new Thread(server).start();
			}
		};
		runServer.setText("Start");
		runServer.setToolTipText("Start Email Listener");
		runServer.setImageDescriptor(IMG_RUN);

		stopServer = new Action() {
			public void run() {
				server.close();
				stopServer.setEnabled(false);
				runServer.setEnabled(true);
			}
		};
		stopServer.setText("Stop");
		stopServer.setToolTipText("Stop Email Listener");
		stopServer.setImageDescriptor(IMG_STOP);
		stopServer.setEnabled(false);
	
		removeMessage = new Action() {
			public void run() {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if(obj != null && obj instanceof Message)
					viewer.remove(obj);
			}
		};
		removeMessage.setText("Delete");

		openMessage = new Action() {
			public void run() {
				openMessage();
			}
		};
		openMessage.setText("Open");

		//doubleClickAction = new MessageViewAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), viewer.getSelection());

		/*
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
		*/
	}

	/* Original Method
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		        if (obj != null && obj instanceof Message) {
		        	final Message m = (Message)obj;
		            Display.getDefault().asyncExec(new Runnable(){
		                public void run() {
		        			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		        			MessageEditorInput input = new MessageEditorInput(m);
		        			try {
		        				page.openEditor(input, MessageEditor.ID);
		        			} catch(PartInitException e) {
		        				e.printStackTrace();
		        			}
		                }
		            });
		            return;
		        }
			}
		});
	}
	*/
	
	private void openMessage() {
		Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
        if (obj != null && obj instanceof Message) {
        	final Message m = (Message)obj;
        	Display.getDefault().asyncExec(new Runnable(){
                public void run() {
        			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        			try {
        				File fileToOpen = File.createTempFile("email",".eml");
        				fileToOpen.deleteOnExit();
        				Writer writer = new BufferedWriter(new FileWriter(fileToOpen));
        				writer.write(m.getMessage());
        				writer.close();
        				if (fileToOpen.exists() && fileToOpen.isFile()) {
        					IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(fileToOpen.getAbsolutePath()));
        					if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
        					    try {
        					    	IDE.openEditor(page, fileToOpen.toURI(), IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID, true);
        					    	//IDE.openEditor(page, fileToOpen.toURI(), "multitab_editor.editors.MultiPageEditor", true);
        					    } catch (PartInitException e) {
        					        /* some code */
        					    }
        					}
        				} else {
        				    //Do something if the file does not exist
        				}
        			} catch(Exception e) {
        				e.printStackTrace();
        			}
                }
            });
        	m.setRead(true);
            return;
        }
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				openMessage();
			}
		});
	}
	
	/*
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Incoming Messages",
			message);
	}
	*/
	
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