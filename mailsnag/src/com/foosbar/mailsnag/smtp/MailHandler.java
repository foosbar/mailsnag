package com.foosbar.mailsnag.smtp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.model.MessageParser;
import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.util.MessageStore;

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
	
	private IPreferenceStore pStore;
	
	public MailHandler(Socket socket, TableViewer viewer) {
		super("Email Handler Thread");
		
		this.socket = socket;
		
		this.viewer = viewer;
		
		this.pStore = 
			Activator.getDefault().getPreferenceStore();
	}

	public void run() {
		
		try {
			
			boolean debug = 
				pStore.getBoolean(PreferenceConstants.PARAM_DEBUG);
			
			StringBuilder msgBody = new StringBuilder();
			
			if(debug)
				System.out.println("Incoming Message; Handler Thread Running");
			
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			respond(SEND_HI, out);

			BufferedReader in = 
				new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			boolean connected = true;

			Pattern p = Pattern.compile("\\r\\n\\.\\r\\n", Pattern.MULTILINE);
			
			while (connected) {
				String inputLine = in.readLine();

				if(debug)
					System.out.println(inputLine);

				// The End
				if(inputLine.startsWith(READ_QUIT)) {
					if(debug)
						System.out.println("Closing connection");

					respond(SEND_BYE, out);
					connected = false;
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
					respond(SEND_END_DATA, out);

					// Write line to message body
					msgBody.append(inputLine.trim())
						.append(NEWLINE);
					
					continue;
				}

				// Write line to message body
				msgBody.append(inputLine.trim())
					.append(NEWLINE);
			
				// If end of data has been reached.
				if(p.matcher(msgBody).find()) {
					respond(SEND_BYE, out);
					connected = false;
					break;
				} 
				// else, continue getting data.
				else {
					respond(SEND_OK, out);
					continue;
				}
			}
			
			// Parse details of the file
			message = MessageParser.parse(msgBody.toString());

			//Persist file
			MessageStore.persist(message, msgBody.toString());
			
			//Update the Table Viewer
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.add(message);
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
		writer.print(response);
		writer.flush();
	}
}