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
package com.foosbar.mailsnag.editors;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.model.Message.Attachment;
import com.foosbar.mailsnag.model.MessageData;
import com.foosbar.mailsnag.util.InlineFilter;

/**
 * A multi-tab editor for inspecting emails
 * <ul>
 * <li>page 0 (Optional) Html Message preview
 * <li>page 1 (Optional) Text Message preview
 * <li>page 2 (Optional) Attachments Preview
 * <li>page 3 Raw Data for inspecting the entire email stream.
 * </ul>
 */
public class MessageEditor extends MultiPageEditorPart implements
IResourceChangeListener {

	public static final String ID = "com.foosbar.mailsnag.editors.MessageEditor";

	// Formats the size of the attachment
	private static final DecimalFormat FILE_SIZE_FORMATTER = new DecimalFormat(
			"#,##0.##");

	// Locale Specific Date Formatter
	private static final DateFormat DATE_FORMATTER = DateFormat
			.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG,
					Locale.getDefault());

	/* Locale Specific Resource Bundle */
	private static final ResourceBundle BUNDLE = Activator.getResourceBundle();

	/* Message Data */
	private Message message;

	/* Message Data */
	private MessageData messageData;

	public MessageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Creates a page/tab to display the raw email for inspection.
	 */
	void createRawPage() {
		Composite composite = new Composite(getContainer(), SWT.NONE);

		FillLayout layout = new FillLayout();
		composite.setLayout(layout);

		StyledText text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);
		text.setText(messageData.getMessage());
		text.setMargins(5, 5, 5, 5);

		int index = addPage(composite);
		setPageText(index, BUNDLE.getString("editor.rawData"));
	}

	/**
	 * Creates a page/tab to display attachments.
	 */
	void createAttachments() {

		if (!message.hasAttachments()) {
			return;
		}

		Collection<Attachment> attachments = message.getAttachments().values();

		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);

		FormToolkit toolkit = new FormToolkit(composite.getDisplay());
		ScrolledForm form = toolkit.createScrolledForm(composite);
		form.setText(BUNDLE.getString("editor.attachments.title"));

		TableWrapLayout twlayout = new TableWrapLayout();
		twlayout.numColumns = 4;

		Composite body = form.getBody();
		body.setLayout(twlayout);

		ResourceBundle bundle = Activator.getResourceBundle();

		// Add Headers
		addStyleTextCell(bundle.getString("header.number.short"), body, true);
		addStyleTextCell(bundle.getString("header.filename"), body, true)
		.setLeftMargin(0);
		addStyleTextCell(bundle.getString("header.mimetype"), body, true);
		addStyleTextCell(bundle.getString("header.filesize"), body, true)
		.setAlignment(SWT.RIGHT);

		int count = 0;
		for (final Attachment attachment : attachments) {

			// Add Counter Cell
			addStyleTextCell(Integer.toString(++count) + ".", body, false)
			.setAlignment(SWT.RIGHT);

			Hyperlink link = toolkit.createHyperlink(body, "Click Here",
					SWT.WRAP);
			link.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {

					// Not necessary - done when editor is created. see
					// #createPages();
					// MessageStore.persistAttachment(attachment,
					// messageData.getMessage());

					try {
						IDE.openEditor(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage(),
								new AttachmentEditorInput(attachment),
								IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID, true);

					} catch (PartInitException ex) {
						ex.printStackTrace();
					}
				}
			});
			link.setText(attachment.getName());
			link.setLayoutData(new TableWrapData());

			// Adds MimeType
			StyledText mimetype = addStyleTextCell(
					getMimeTypeBasic(attachment),
					body, false);
			// Because I can't set it on the link.
			// mimetype.setLeftMargin(10);

			// Adds Filesize
			addStyleTextCell(readableFileSize(attachment.getSize()), body,
					false).setAlignment(SWT.RIGHT);
		}

		int index = addPage(composite);
		setPageText(index, BUNDLE.getString("editor.attachments"));
	}

	/**
	 * Adds a styled text element and bold the text if necessary.
	 * 
	 * @param text
	 *            Text to display
	 * @param composite
	 *            Composite to add StyledText to
	 * @param bolded
	 *            if true, the entire text region will be bold
	 * @return
	 */
	private StyledText addStyleTextCell(String text, Composite composite,
			boolean bolded) {
		StyledText cell = new StyledText(composite, SWT.WRAP);
		cell.setEditable(false);
		cell.setText(text);
		cell.setLayoutData(new TableWrapData());
		cell.setMargins(10, 0, 10, 0);
		if (bolded) {
			StyleRange range = new StyleRange();
			range.fontStyle = SWT.BOLD;
			range.start = 0;
			range.length = text.length();
			cell.setStyleRange(range);
		}
		return cell;
	}

	/**
	 * Formats a number of bytes into a human readable form. Copied from Will
	 * work up until Terabytes.
	 * 
	 * @see <a
	 *      href="http://stackoverflow.com/a/5599842">http://stackoverflow.com/a/5599842</a>
	 * 
	 * @param size
	 * @return
	 */
	private String readableFileSize(long size) {
		if (size <= 0) {
			return "0";
		}
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return FILE_SIZE_FORMATTER.format(size / Math.pow(1024, digitGroups))
				+ " " + units[digitGroups];
	}

	/**
	 * Returns the first portion of the mime-type. Often times the mime-type
	 * will contain the filename as well.
	 * 
	 * @param attachment
	 * @return
	 */
	private String getMimeTypeBasic(Attachment attachment) {
		String mimeType = attachment.getMimeType();
		if (mimeType != null && mimeType.contains(";")) {
			return mimeType.substring(0, mimeType.indexOf(';'));
		}
		return mimeType;
	}

	/**
	 * Creates a page/tab to display the text email for preview.
	 */
	void createTextPage() {

		if (messageData == null || !messageData.hasTextMessage()) {
			return;
		}

		Composite composite = new Composite(getContainer(), SWT.NONE);

		populateEmailBanner(composite);

		StyledText text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		text.setLayoutData(gridData);
		text.setEditable(false);
		text.setWordWrap(true);
		text.setMargins(5, 5, 5, 5);
		text.setText(messageData.getTextMessage());

		int index = addPage(composite);
		setPageText(index, BUNDLE.getString("editor.textFormat"));
	}

	/**
	 * Creates a page/tab to display the html email for preview.
	 */
	void createHtmlPage() {

		if (messageData == null || !messageData.hasHtmlMessage()) {
			return;
		}

		try {
			Composite composite = new Composite(getContainer(), SWT.NONE);

			populateEmailBanner(composite);

			StringBuilder path = new StringBuilder("file:///")
			.append(Activator.getDefault().getStateLocation().toString())
			.append('/')
			.append(message.getAttachmentDir());

			String filteredText = InlineFilter.filter(message,
					messageData.getHtmlMessage(), path.toString());

			try {
				Browser browser = new Browser(composite, SWT.NONE | SWT.BORDER);
				browser.setText(filteredText);
				browser.setCapture(true);

				browser.execute("document.getElementsByTagName('body')[0].style.overflow='hidden'");

				GridData gridData = new GridData();
				gridData.horizontalSpan = 2;
				gridData.horizontalAlignment = SWT.FILL;
				gridData.grabExcessHorizontalSpace = true;
				gridData.verticalAlignment = SWT.FILL;
				gridData.grabExcessVerticalSpace = true;
				browser.setLayoutData(gridData);

			} catch (SWTError error) {
				if (error.code == 2) {
					IStatus status = new Status(IStatus.ERROR,
							Activator.PLUGIN_ID, error.getLocalizedMessage());
					ErrorDialog.openError(getSite().getShell(), null,
							"MailSnag is unable to display the HTML output due to an Eclipse configuration error.\n\nSee http://blog.foos-bar.com/2012/05/eclipse-rcp-development-browser-control.html",
							status);
				}
			}
			setPageText(addPage(composite),
					BUNDLE.getString("editor.htmlFormat"));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			ErrorDialog.openError(getSite().getShell(),
					"Error creating nested text editor", null, null);
		}
	}

	/**
	 * Creates the display related to the email fields.  Allows
	 * the text and html displays to look more like an actual email
	 * message as viewed by a user.
	 * 
	 * @param composite
	 */
	private void populateEmailBanner(Composite composite) {

		composite.setLayout(new GridLayout(2, false));

		// From Line
		addEmailBannerField(BUNDLE.getString("header.from"), message.getFrom(), composite);

		// Subject
		addEmailBannerField(BUNDLE.getString("header.subject"), message.getSubject(), composite);

		// Date Received
		addEmailBannerField(BUNDLE.getString("header.received"), DATE_FORMATTER.format(message.getReceived()), composite);

		// To Field
		String to = message.getTo();
		if(to != null && !"".equals(to)) {
			addEmailBannerField(BUNDLE.getString("header.to"), message.getTo(), composite);
		}

		// To Field
		String cc = message.getCc();
		if(cc != null && !"".equals(cc)) {
			addEmailBannerField(BUNDLE.getString("header.cc"), message.getCc(), composite);
		}

		composite.layout(true);
	}

	private void addEmailBannerField(String label, String value, Composite parent) {
		// Grid layout data
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;

		new Label(parent, SWT.NONE)
		.setText(label);

		Text valueField = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		valueField.setLayoutData(gridData);
		valueField.setText(value);
		valueField.pack(true);
	}

	/**
	 * Creates the pages of the multi-page editor.
	 */
	@Override
	protected void createPages() {
		// Create HTML View of Email if necessary
		createHtmlPage();

		// Create Text-only View of Email if necessary
		createTextPage();

		// List any attachments included with Email
		createAttachments();

		// Create Raw Data View.
		createRawPage();
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page 0's tab, and updates this multi-page editor's input to
	 * correspond to the nested editor's.
	 */
	@Override
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	@Override
	public void init(IEditorSite site, IEditorInput editorInput)
			throws PartInitException {
		super.init(site, editorInput);
		if (!(editorInput instanceof MessageEditorInput)) {
			throw new PartInitException(
					"Invalid Input: Must be MessageEditorInput");
		}

		setSite(site);
		setInput(editorInput);

		message = ((MessageEditorInput) editorInput).getMessage();

		messageData = ((MessageEditorInput) editorInput).getMessageData();

		setPartName(((MessageEditorInput) editorInput).getName());
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 2) {
			return; // Do nothing
		}
	}

	/**
	 * Closes all project files on project close.
	 * 
	 * Commented out because it doesn't worked as anticipated.
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
		/*
		 * if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
		 * Display.getDefault().asyncExec(new Runnable() { public void run() {
		 * IWorkbenchPage[] pages =
		 * PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages(); for
		 * (int i = 0; i < pages.length; i++) { IEditorInput editor =
		 * getEditorInput();
		 * 
		 * if(!(editor instanceof MessageEditorInput)) continue;
		 * 
		 * MessageEditorInput mEditor = (MessageEditorInput) editor;
		 * event.getResource(). if (
		 * mEditor.getFile().getProject().equals(event.getResource()) ) {
		 * IEditorPart editorPart = pages[i].findEditor(mEditor);
		 * pages[i].closeEditor(editorPart, true); } } } }); }
		 */
	}
}