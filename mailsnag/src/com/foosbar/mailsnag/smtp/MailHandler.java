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
import com.foosbar.mailsnag.model.MessageParser;
import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.util.MessageStore;
import com.foosbar.mailsnag.views.MessagesView.ViewContentProvider;

public class MailHandler extends Thread {
	
	private static final String PROTOCOL_EHLO = "EHLO";
	private static final String SEND_HI  = "220 Welcome to MailSnag by Foos-Bar\r\n";
	private static final String SEND_OK  = "250 Ok\r\n"; 
	private static final String SEND_BYE = "221 Bye\r\n";
	private static final String SEND_END_DATA = "354 End data with <CRLF>.<CRLF>\r\n";
	private static final String READ_DATA = "DATA";
	private static final String READ_QUIT = "QUIT";
	private static final String NEWLINE = System.getProperty("line.separator");
	
	private Socket socket;
	private Message message;
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

			respond(SEND_HI, out);

			BufferedReader in = 
				new BufferedReader(new InputStreamReader(socket.getInputStream()));

			boolean dataBlock = false;
			
			String inputLine = null;
			while ( (inputLine = in.readLine()) != null ) {

				if(debug)
					System.out.println(inputLine);

				//No operation
				if(inputLine.startsWith("NOOP")) {
					respond(SEND_OK, out);
					continue;
				}

				//Verify
				if(inputLine.startsWith("VRFY")) {
					respond(SEND_OK, out);
					continue;
				}

				// The End
				if(inputLine.startsWith(READ_QUIT)) {
					respond(SEND_BYE, out);
					if(debug)
						System.out.println("Closing connection");
					break;
				}

				// Greeting - Say Hello and move on.
				if(inputLine.startsWith(PROTOCOL_EHLO)) {
					String server = inputLine.substring(PROTOCOL_EHLO.length());
					respond("250 Hello " + server.trim() + "\r\n", out);
					continue;
				}
				
				// Start of Data - Confirm end of data string
				if(inputLine.startsWith(READ_DATA)) {
					// Write line to message body
					msgBody.append(inputLine.trim())
						.append(NEWLINE);
					dataBlock = true;
					respond(SEND_END_DATA, out);
					continue;
				}

				if(dataBlock && inputLine.equals(".")) {
					respond(SEND_BYE, out);
					if(debug)
						System.out.println("End of data");
					break;
				}
				
				// Write line to message body
				msgBody.append(inputLine.trim())
					.append(NEWLINE);
			
				respond(SEND_OK, out);
				continue;
			}
			
			// Parse details of the file
			message = MessageParser.parse(msgBody.toString());

			//Persist file
			MessageStore.persist(message, msgBody.toString());
			
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
			System.out.println("Server Response: " + response);
		
		writer.print(response);
		writer.flush();
	}
}