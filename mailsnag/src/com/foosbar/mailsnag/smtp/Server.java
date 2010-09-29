package com.foosbar.mailsnag.smtp;

import java.io.IOException;
import java.net.ServerSocket;

import org.eclipse.jface.preference.IPreferenceStore;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.views.MessagesView;

public class Server implements Runnable {

	private boolean listening;
	private MessagesView view;
	private ServerSocket serverSocket;
	
	public Server() {
	}

	public Server(MessagesView view) {
		this.view = view;
	}
	
	public MessagesView getView() {
		return view;
	}
	
	public void run() {

		listening = true;

		// Get Preferences
		IPreferenceStore pStore = 
			Activator.getDefault().getPreferenceStore();
		
		// Display Debug Messages?
		boolean debug = 
			pStore.getBoolean(PreferenceConstants.PARAM_DEBUG);
		
		// Port to listen on.
		int port = 
			pStore.getInt(PreferenceConstants.PARAM_PORT);
		
		try {
			serverSocket = new ServerSocket(port);
			if(debug)
				System.out.println("MailSnag Server listening on port " + port);
		} catch (IOException e) {
			//Print error to System.err
			e.printStackTrace(System.err);
			
			//Exit thread execution
			throw new RuntimeException(e);
		}
		
		view.disableStartServer();
		view.enableStopServer();
		
		try {
			while (listening) {
				final MailHandler handler = new MailHandler(serverSocket.accept(), view.getViewer());
				handler.start();
			}
		} catch(IOException e) {
			if(debug)
				System.err.println(e);
		} finally {
			
			if(serverSocket != null && !serverSocket.isClosed()) {
				if(debug)
					System.out.println("Shutting down MailSnag");
				
				try {
					serverSocket.close();
				} catch(IOException e) {}
			}
			
			serverSocket = null;
			
			view.disableStopServer();
			view.enableStartServer();
		}
	}
	
	public void close() { 
		this.listening = false;

		if(serverSocket != null) {
			try {
				serverSocket.close();
			} catch(Exception e) {
				e.printStackTrace(System.err);
			}
		}
		
	}
}