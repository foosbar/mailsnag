package com.foosbar.mailsnag.smtp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.util.MessageStore;
import com.foosbar.mailsnag.views.MessagesView.ViewContentProvider;

public class MailHandler extends Thread {
	
	private static final String CMD_DATA = "DATA";
	private static final String CMD_EHLO = "EHLO";
	private static final String CMD_NOOP = "NOOP";
	private static final String CMD_QUIT = "QUIT";
	private static final String CMD_RSET = "RSET";
	private static final String CMD_VRFY = "VRFY";
	
	private static final String RSPN_HI  = "220 Welcome to MailSnag by Foos-Bar\r\n";
	private static final String RSPN_OK  = "250 Ok\r\n"; 
	private static final String RSPN_BYE = "221 Bye\r\n";
	private static final String RSPN_END_DATA = "354 End data with <CRLF>.<CRLF>\r\n";
	
	private static final String NEWLINE = System.getProperty("line.separator");
	
	private Socket socket;
	//private Message message;
	private TableViewer viewer;
	
	private boolean debug;
	
	private IPreferenceStore pStore;
	
	public MailHandler(Socket socket, TableViewer viewer) {
		super("Email Handler Thread");
		
		this.socket = socket;
		
		this.viewer = viewer;
		
		this.pStore = 
			Activator.getDefault().getPreferenceStore();
		
		this.debug = 
			pStore.getBoolean(PreferenceConstants.PARAM_DEBUG);
	}

	public void run() {
		
		try {
			
			StringBuilder msgBody = new StringBuilder();
			
			if(debug)
				System.out.println("Incoming Message; Handler Thread Running");
			
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			respond(RSPN_HI, out);

			BufferedReader in = 
				new BufferedReader(new InputStreamReader(socket.getInputStream()));

			boolean readingData = false;
			
			String inputLine = null;
			while ( (inputLine = in.readLine()) != null ) {

				if(!readingData) {
				
					if(debug)
						System.out.println("Client Command: " + inputLine);
					
					// Greeting - Say Hello and move on.
					if(inputLine.startsWith(CMD_EHLO)) {
						String server = inputLine.substring(CMD_EHLO.length());
						respond("250 Hello " + server.trim() + "\r\n", out);
						continue;
					}
					
					//No operation
					else if(inputLine.startsWith(CMD_NOOP)) {
						respond(RSPN_OK, out);
						continue;
					}

					//No operation
					else if(inputLine.startsWith(CMD_RSET)) {
						readingData = false;
						respond(RSPN_OK, out);
						continue;
					}

					//Verify
					else if(inputLine.startsWith(CMD_VRFY)) {
						respond(RSPN_OK, out);
						continue;
					}
	
					// The End
					else if(inputLine.startsWith(CMD_QUIT)) {
						respond(RSPN_BYE, out);
						if(debug)
							System.out.println("Closing connection");
						break;
					}
	
					// Start of Data - Confirm end of data string
					else if(inputLine.startsWith(CMD_DATA)) {
						readingData = true;
						respond(RSPN_END_DATA, out);
						continue;
					}
					
					else {
						respond(RSPN_OK, out);
						continue;
					}
				}
				//Reading the data block
				else {
					
					if(debug)
						System.out.println(inputLine);

					if(inputLine.equals(".")) {
						readingData = false;
						respond(RSPN_OK, out);
						continue;
					} else {
						// Write line to message body
						msgBody.append(inputLine.trim())
							.append(NEWLINE);
						continue;
					}
				}
			}
			
			String content = msgBody.toString();
			
			//Persist message
			final Message message = MessageStore.persist(content);
			
			//Update the Content Provider
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					ViewContentProvider content = 
						(ViewContentProvider) viewer.getContentProvider();
					content.add(message);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(socket != null && !socket.isClosed())
					socket.close();
			} catch(Exception e) {
			}
		}
	}

	private void respond(String response, PrintWriter writer) {
		
		if(debug)
			System.out.print("Server Response: " + response);
		
		writer.print(response);
		writer.flush();
	}
}