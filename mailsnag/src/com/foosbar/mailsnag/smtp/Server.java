package com.foosbar.mailsnag.smtp;

import java.io.IOException;
import java.net.ServerSocket;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.views.MessagesView;

public class Server implements Runnable {

	private int port;
	private boolean listening;
	private MessagesView view;
	private ServerSocket serverSocket;
	
	public Server(int port) {
		this.port = port;
	}

	public Server(int port, MessagesView view) {
		this.port = port;
		this.view = view;
	}
	
	public void run() {

		listening = true;

		boolean debug = 
			Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.PARAM_DEBUG);

		try {
			serverSocket = new ServerSocket(port);
			if(debug)
				System.out.println("MailSnag Server listening on port " + port);
		} catch (IOException e) {

			if(debug)
				System.err.println("MailSnag could not listen on port " + port);
			
			//Print error to System.err
			e.printStackTrace(System.err);

			//Exit thread execution
			return;
		}
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