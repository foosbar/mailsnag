package com.foosbar.mailsnag.smtp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.preferences.PreferenceConstants;
import com.foosbar.mailsnag.util.MessageStore;
import com.foosbar.mailsnag.views.MessagesView;
import com.foosbar.mailsnag.views.MessagesView.ViewContentProvider;

public class MailHandler
		extends Thread {

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

	private static final String NEWLINE = System.getProperty("line.separator");

	private final Socket socket;

	private final boolean debug;

	private final IPreferenceStore pStore;

	public MailHandler(Socket socket) {
		super("Email Handler Thread");

		this.socket = socket;

		this.pStore =
				Activator.getDefault().getPreferenceStore();

		this.debug =
				this.pStore.getBoolean(PreferenceConstants.PARAM_DEBUG);
	}

	@Override
	public void run() {

		try {

			StringBuilder msgBody = new StringBuilder();

			if (this.debug) {
				System.out.println("Incoming Message; Handler Thread Running");
			}

			PrintWriter out = new PrintWriter(this.socket.getOutputStream(),
					true);

			respond(RSPN_HI, out);

			BufferedReader in =
					new BufferedReader(new InputStreamReader(
							this.socket.getInputStream()));

			boolean readingData = false;

			String inputLine = null;
			while ((inputLine = in.readLine()) != null) {

				if (!readingData) {

					if (this.debug) {
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
						readingData = false;
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
						if (this.debug) {
							System.out.println("Closing connection");
						}
						break;
					}

					// Start of Data - Confirm end of data string
					m = CMD_DATA.matcher(inputLine);
					if (m.matches()) {
						readingData = true;
						respond(RSPN_END_DATA, out);
						continue;
					}

					// Didn't match any other commands. Keep reading.
					respond(RSPN_OK, out);
					continue;
				}
				// Reading the data block
				else {

					if (this.debug) {
						System.out.println(inputLine);
					}

					if (inputLine.equals(".")) {
						readingData = false;
						respond(RSPN_OK, out);
						continue;
					} else {
						// Issue 11 - I believe the trim
						// was implemented to fix a previous issue.
						// msgBody.append(inputLine.trim())

						// Write line to message body
						msgBody.append(inputLine)
								.append(NEWLINE);
						continue;
					}
				}
			}

			if (msgBody.length() > 0) {
				// Persist message
				final Message message = MessageStore
						.persist(msgBody.toString());

				// Update the Content Provider
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessagesView view = (MessagesView) PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage().findView(MessagesView.ID);
						ViewContentProvider provider =
								(ViewContentProvider) view.getViewer()
										.getContentProvider();
						provider.add(message);
					}
				});
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (this.socket != null && !this.socket.isClosed()) {
					this.socket.close();
				}
			} catch (Exception e) {
			}
		}
	}

	private void respond(String response, PrintWriter writer) {

		if (this.debug) {
			System.out.print("Server Response: " + response);
		}

		writer.print(response);
		writer.flush();
	}
}