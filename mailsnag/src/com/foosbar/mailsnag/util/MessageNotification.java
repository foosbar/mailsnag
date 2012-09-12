/**
 * 
 */
package com.foosbar.mailsnag.util;

import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.ui.dialogs.AbstractNotificationPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.model.Message;

/**
 * Generates the actual popup notification message with some details about the new message
 * received by the plugin.  This implementation is based on the mylyn framework and is an 
 * optional feature as of MailSnag 1.3.
 * 
 * The Activator provides a check to see if the mylyn AbstractNofiticationPopup class is
 * available to the classloader.
 * 
 * @author Kevin Kelley (dev@foos-bar.com)
 */
public class MessageNotification extends AbstractNotificationPopup implements
		INotification {

	/** Message notification being generated for */
	private final Message message;

	/** I18n resource bundle */
	private final ResourceBundle i18n;

	/** Icon used in notification popup */
	private static final Image IMG_LOGO = ImageDescriptor.createFromFile(
			Activator.class, "/icons/mail.gif").createImage();

	public MessageNotification(Message message, ResourceBundle i18n,
			Display display) {
		super(display);
		setFadingEnabled(true);
		this.message = message;
		this.i18n = i18n;
	}

	@Override
	protected void createContentArea(Composite parent) {

		parent.setLayout(new GridLayout(1, true));

		StringBuilder builder = new StringBuilder();

		builder.append("<a>").append(
				getValue(message.getSubject(),
						i18n.getString("notify.unknown.subject"), 75));
		builder.append("\n")
				.append(i18n.getString("header.from"))
				.append(": ")
				.append(getValue(message.getFrom(),
						i18n.getString("notify.unknown.from"), 75));
		builder.append("</a>");
		Link link = new Link(parent, SWT.WRAP);
		link.setBackground(parent.getBackground());
		link.setText(builder.toString());

		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				IWorkbenchWindow window = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				if (window != null) {
					Shell windowShell = window.getShell();
					if (windowShell != null) {
						if (windowShell.getMinimized()) {
							windowShell.setMinimized(false);
						}

						windowShell.open();
						windowShell.forceActive();
					}
				}
			}
		});
	}

	/**
	 * Checks for null or empty value. If the value meets either of these
	 * conditions, the default value is used.
	 * 
	 * @param value
	 * @param defaultValue
	 * @param maxLength
	 * @return
	 */
	private String getValue(String value, String defaultValue, int maxLength) {
		if (value == null || "".equals(value.trim())) {
			return defaultValue;
		}
		int end = value.length() > maxLength ? maxLength : value.length();
		return value.substring(0, end);
	}

	@Override
	protected Image getPopupShellImage(int maximumHeight) {
		return IMG_LOGO;
	}

	@Override
	protected String getPopupShellTitle() {
		return i18n.getString("notify.title");
	}

	@Override
	public boolean isFadingEnabled() {
		return true;
	}
}