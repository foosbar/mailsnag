package com.foosbar.mailsnag.smtp;

import java.net.BindException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.views.MessagesView;


public class ServerThreadGroup extends ThreadGroup {
	
	public ServerThreadGroup(String name) {
		super(name);
	}

	@Override
	public void uncaughtException(Thread t, final Throwable e) {
		Display.getDefault().asyncExec(new Runnable(){
            public void run() {

            	MessagesView view = (MessagesView) PlatformUI
    			.getWorkbench()
    				.getActiveWorkbenchWindow()
    					.getActivePage()
    						.findView(MessagesView.ID);

        		view.disableStopServer();
        		view.enableStartServer();
        		
        		if(e.getCause() instanceof BindException) {
        			
        			int port = 
        				Activator.getDefault().getPreferenceStore()
        					.getInt(PreferenceConstants.PARAM_PORT);
        			
        			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "The application couldn't bind to the specified port.  Check to make sure the port isn't in use by another process or you have permission to bind to the port." , e);
        	
        			ResourceBundle bundle = Activator.getResourceBundle();
        			
        			ErrorDialog.openError(
        					view.getViewer().getControl().getShell(), 
        					null, 
        					String.format(bundle.getString("exception.port.bind"),port), 
        					status
        				);
        		}        			
            }
		});
	}
	
}
