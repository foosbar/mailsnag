package com.foosbar.mailsnag.smtp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.preferences.PreferenceConstants;

public class MailHandler extends Thread {
	
	static final String PROTOCOL_EHLO = "EHLO";
	static final String PROTOCOL_HELO = "HELO";

	static final String SEND_HI  = "220 Welcome to MailSnag by Foos-Bar\r\n";
	static final String SEND_OK  = "250 Ok\r\n"; 
	static final String SEND_BYE = "221 Bye\r\n";
	static final String SEND_END_DATA = "354 End data with <CRLF>.<CRLF>\r\n";

	static final String READ_DATA = "DATA";
	static final String READ_MAIL_FROM = "MAIL FROM:";
	static final String READ_MESSAGE_ID = "Message-ID:";
	static final String READ_QUIT = "QUIT";
	static final String READ_RCPT_TO = "RCPT TO:";
	static final String READ_SUBJECT = "Subject:";
	
	private Socket socket;
	private Message message;
	private TableViewer viewer;
	
	public MailHandler(Socket socket, TableViewer viewer) {
		super("Email Handler Thread");
		this.socket = socket;
		this.viewer = viewer;
		message = new Message();
	}

	public void run() {
		try {
			
			boolean debug = 
				Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.PARAM_DEBUG);
			
			StringBuilder msgBody = new StringBuilder();
			
			if(debug)
				System.out.println("Incoming Message; Handler Thread Running");
			
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			
			out.print(SEND_HI);
		    out.flush();			

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			boolean connected = true;
			
			while (connected) {
				String inputLine = in.readLine();
				
				if(debug)
					System.out.println(inputLine);
				
				if(inputLine.startsWith(READ_QUIT)) {

					if(debug)
						System.out.println("Closing connection");
					
					out.print(SEND_BYE);
					connected = false;
					break;
					
				} 
				else if(inputLine.startsWith(PROTOCOL_EHLO)) {
				
					String server = inputLine.substring(PROTOCOL_EHLO.length());

					out.print("250 Hello " + server.trim() + "\r\n");
					
					out.flush();

				}
				else if(inputLine.startsWith(READ_MAIL_FROM)) {
					
					String mailFrom = inputLine.substring(READ_MAIL_FROM.length()+1, inputLine.length()-1);
					
					if(debug)
						System.out.println(" > Parsed FROM Address: " + mailFrom);

					message.setFrom(mailFrom);
					out.print(SEND_OK);
					out.flush();
					
				}
				else if(inputLine.startsWith(READ_RCPT_TO)) {
					
					String toAddress = inputLine.substring(READ_RCPT_TO.length()+1, inputLine.length()-1);
					
					message.addTo(toAddress);

					if(debug)
						System.out.println(" > Parsed TO Address: " + toAddress);
					
					out.print(SEND_OK);
					out.flush();
				}
				else if(inputLine.startsWith(READ_DATA)) {
					
					out.print(SEND_END_DATA);
					out.flush();
					
					boolean receiving = true;
					
					while ( receiving ) {

						//Prevents accidental neverending loop.
						receiving = false;
						
						//Read next line of data
						String line = in.readLine();
						
						if(debug)
							System.out.println(line);
						
						//If blank line, do nothing
						if(line == null)
							continue;
						
						if(line.equals(".")) {
							out.print(SEND_OK);
							out.flush();
							break;
						}
						if(line.startsWith(READ_SUBJECT)) {
							
							String subject = line.substring(READ_SUBJECT.length());
							message.setSubject(subject);
							
							if(debug)
								System.out.println(" > Parsed SUBJECT: " + subject);
							
						} else if(line.startsWith(READ_MESSAGE_ID)) {
							String id = line.substring(READ_MESSAGE_ID.length());
							message.setId(id);

							if(debug)
								System.out.println(" > Parsed MESSAGE-ID: " + id);
						}

						msgBody.append(line);
						receiving = true; //Take another turn in the loop.
					}
					
					message.setMessage(msgBody.toString());
					
					if(debug)
						System.out.println("Out of data WHILE loop");
					
					continue;
				}
				else {

					out.print(SEND_BYE);
					out.flush();
					connected = false;
					break;
					
				}
			}
			
			out.close();
			in.close();

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
}