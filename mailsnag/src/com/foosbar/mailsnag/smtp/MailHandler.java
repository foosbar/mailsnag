/*******************************************************************************
 * Copyright (c) 2010-2011 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Kelley - initial API and implementation
 *******************************************************************************/
package com.foosbar.mailsnag.smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.util.MessageStore;
import com.foosbar.mailsnag.views.MessagesView;
import com.foosbar.mailsnag.views.MessagesView.ViewContentProvider;

/**
 * The MailHandler is responsible for conversing with the SMTP client. It
 * handles all communication and keeps parsing emails until the QUIT command is
 * executed or the connection timesout.
 * 
 * @author Kevin Kelley
 */
public class MailHandler extends Thread {

	private static final Pattern CMD_DATA = Pattern.compile("^DATA",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern CMD_GREETING = Pattern.compile(
			"^(HELO|eHlo)\\s+?(.+?)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern CMD_NOOP = Pattern.compile("^NOOP",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern CMD_RSET = Pattern.compile("^RSET",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern CMD_VRFY = Pattern.compile("^VRFY",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern CMD_QUIT = Pattern.compile("^QUIT",
			Pattern.CASE_INSENSITIVE);

	private static final String RSPN_HI = "220 Welcome to MailSnag by Foos-Bar\r\n";
	private static final String RSPN_OK = "250 Ok\r\n";
	private static final String RSPN_BYE = "221 Bye\r\n";
	private static final String RSPN_END_DATA = "354 End data with <CRLF>.<CRLF>\r\n";

	private static final String END_OF_DATA = ".";

	private static final String NEWLINE = System.getProperty("line.separator");

	private final Socket socket;

	private final boolean debug;

	private final IPreferenceStore pStore;

	public MailHandler(Socket socket) {
		super("Email Handler Thread");
		this.socket = socket;
		pStore = Activator.getDefault().getPreferenceStore();
		debug = pStore.getBoolean(PreferenceConstants.PARAM_DEBUG);
	}

	@Override
	public void run() {

		try {

			StringBuilder msgBody = null;

			if (debug) {
				System.out.println("Incoming Message; Handler Thread Running");
			}

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			respond(RSPN_HI, out);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			String inputLine = null;
			while ((inputLine = in.readLine()) != null) {

				if (debug) {
					System.out.println("Client Command: " + inputLine);
				}

				// Greeting - Say Hello and move on.
				Matcher m = CMD_GREETING.matcher(inputLine);
				if (m.matches()) {
					String server = m.group(2);
					respond("250 Hello " + server.trim() + "\r\n", out);
					continue;
				}
				// Greeting - Say Hello and move on.
				// if(inputLine.startsWith(CMD_EHLO)) {
				// String server = inputLine.substring(CMD_EHLO.length());
				// respond("250 Hello " + server.trim() + "\r\n", out);
				// respond("250-AUTH PLAIN LOGIN DIGEST-MD5 CRAM-MD5 GSSAPI",
				// out);
				// respond("250-AUTH=PLAIN LOGIN DIGEST-MD5 CRAM-MD5 GSSAPI",
				// out);
				// continue;
				// }

				// No operation
				m = CMD_NOOP.matcher(inputLine);
				if (m.matches()) {
					respond(RSPN_OK, out);
					continue;
				}

				// Reset
				m = CMD_RSET.matcher(inputLine);
				if (m.matches()) {
					// Resets the content already read.
					msgBody = new StringBuilder();
					respond(RSPN_OK, out);
					continue;
				}

				// Verify
				m = CMD_VRFY.matcher(inputLine);
				if (m.matches()) {
					respond(RSPN_OK, out);
					continue;
				}

				// The End
				m = CMD_QUIT.matcher(inputLine);
				if (m.matches()) {
					respond(RSPN_BYE, out);
					if (debug) {
						System.out.println("Closing connection");
					}
					break;
				}

				// Start of Data - Confirm end of data string
				m = CMD_DATA.matcher(inputLine);
				if (m.matches()) {
					msgBody = new StringBuilder();
					// readingData = true;
					respond(RSPN_END_DATA, out);
					try {
						readMessageBody(in, msgBody);
					} catch (Exception e) {
						// failed to completely parse email - get rid
					}
					saveMessage(msgBody);
					msgBody = null;
					respond(RSPN_OK, out);
					continue;
				}

				// Didn't match any other commands. Keep reading.
				respond(RSPN_OK, out);
				continue;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * As long as the msgBody is not null and not empty, the content will be
	 * saved as an eml file and added the the MessagesView.
	 * 
	 * @param msgBody
	 */
	private void saveMessage(StringBuilder msgBody) {
		if (msgBody != null && msgBody.length() > 0) {
			// Persist message
			final Message message = MessageStore.persist(msgBody.toString());

			// Update the Content Provider
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					for (IWorkbenchWindow bench : PlatformUI.getWorkbench()
							.getWorkbenchWindows()) {
						MessagesView view = (MessagesView) bench
								.getActivePage().findView(MessagesView.ID);
						ViewContentProvider provider = (ViewContentProvider) view
								.getViewer().getContentProvider();
						provider.add(message);
					}
				}
			});
		}
	}

	/**
	 * After the DATA command has be received, this method will continue to read
	 * the email data until a line containing "." is parsed. That character on a
	 * newline singles the end of the data.
	 * 
	 * @param reader
	 * @param messageBody
	 * @throws IOException
	 */
	private void readMessageBody(BufferedReader reader,
			StringBuilder messageBody) throws IOException {

		String inputLine = null;
		while ((inputLine = reader.readLine()) != null) {

			if (debug) {
				System.out.println(inputLine);
			}

			if (inputLine.equals(END_OF_DATA)) {
				return;
			} else {
				// Write line to message body
				messageBody.append(inputLine).append(NEWLINE);
				continue;
			}
		}
	}

	private void respond(String response, PrintWriter writer) {
		if (debug) {
			System.out.print("Server Response: " + response);
		}
		writer.print(response);
		writer.flush();
	}
}