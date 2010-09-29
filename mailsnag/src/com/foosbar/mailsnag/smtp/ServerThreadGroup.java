package com.foosbar.mailsnag.smtp;

import java.net.BindException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.views.MessagesView;


public class ServerThreadGroup extends ThreadGroup {
	
	private MessagesView view;
	
	public ServerThreadGroup(MessagesView view, String name) {
		super(name);
		this.view = view;
	}

	@Override
	public void uncaughtException(Thread t, final Throwable e) {
		Display.getDefault().asyncExec(new Runnable(){
            public void run() {
        		view.disableStopServer();
        		view.enableStartServer();
        		
        		if(e.getCause() instanceof BindException) {
        			
        			int port = 
        				Activator.getDefault().getPreferenceStore()
        					.getInt(PreferenceConstants.PARAM_PORT);
        			
        			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "The application couldn't bind to the specified port.  Check to make sure the port isn't in use by another process or you have permission to bind to the port." , e);
        			
        			ErrorDialog.openError(
        					view.getViewer().getControl().getShell(), 
        					null, 
        					"Couldn't bind SMTP server to port " + port, 
        					status
        				);
        		}        			
            }
		});
	}
	
}
