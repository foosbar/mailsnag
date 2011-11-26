/**
 * 
 */
package com.foosbar.mailsnag.util;

import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.internal.provisional.commons.ui.AbstractNotificationPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.model.Message;

/**
 * @author kevin
 * 
 */
@SuppressWarnings("restriction")
public class MessageNotification extends AbstractNotificationPopup implements
		INotification {

	private final Message message;

	private static final ResourceBundle I18N = Activator.getResourceBundle();

	private static final Image IMG_LOGO = ImageDescriptor.createFromFile(
			Activator.class, "/icons/mail.gif").createImage();

	public MessageNotification(Message message, Display display) {
		// super(display, SWT.NO_TRIM | SWT.NO_FOCUS | SWT.TOOL);
		super(display);
		setFadingEnabled(true);
		this.message = message;
	}

	@Override
	protected void createContentArea(Composite parent) {

		parent.setLayout(new GridLayout(1, true));
		Label testLabel = new Label(parent, SWT.WRAP);
		testLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		StringBuilder builder = new StringBuilder();

		builder.append(getValue(message.getSubject(),
				I18N.getString("notify.unknown.subject"), 75));
		builder.append("\n")
				.append(I18N.getString("header.from"))
				.append(": ")
				.append(getValue(message.getFrom(),
						I18N.getString("notify.unknown.from"), 75));

		testLabel.setText(builder.toString());
		testLabel.setBackground(parent.getBackground());
	}

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
		return I18N.getString("notify.title");
	}

	@Override
	public boolean isFadingEnabled() {
		return true;
	}
}