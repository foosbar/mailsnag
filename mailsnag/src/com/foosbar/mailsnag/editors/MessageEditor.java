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

import java.text.NumberFormat;
import java.util.Collection;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
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
 * <li>page 2 Raw Data for inspecting the entire email stream.
 * </ul>
 */
public class MessageEditor extends MultiPageEditorPart implements
		IResourceChangeListener {

	public static final String ID = "com.foosbar.mailsnag.editors.MessageEditor";

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

		int count = 0;
		for (final Attachment attachment : attachments) {
			StyledText text = new StyledText(body, SWT.WRAP);
			text.setEditable(false);
			text.setText(Integer.toString(++count));
			text.setLayoutData(new TableWrapData());

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

			text = new StyledText(body, SWT.WRAP);
			text.setEditable(false);
			text.setText(attachment.getMimeType());
			text.setLayoutData(new TableWrapData());

			NumberFormat format = NumberFormat.getInstance();

			text = new StyledText(form.getBody(), SWT.WRAP);
			text.setEditable(false);
			text.setText(format.format(attachment.getSize()) + " bytes");
			text.setLayoutData(new TableWrapData());

		}

		int index = addPage(composite);
		setPageText(index, BUNDLE.getString("editor.attachments"));
	}

	/**
	 * Creates a page/tab to display the text email for preview.
	 */
	void createTextPage() {

		if (messageData == null || !messageData.hasTextMessage()) {
			return;
		}

		Composite composite = new Composite(getContainer(), SWT.NONE);

		FillLayout layout = new FillLayout();
		composite.setLayout(layout);

		StyledText text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);
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

			/*
			 * File file =
			 * MessageStore.createTempHtmlFile(messageData.getHtmlMessage());
			 * 
			 * WebBrowserEditor editor = new WebBrowserEditor();
			 * WebBrowserEditorInput input = new
			 * WebBrowserEditorInput(file.toURI().toURL());
			 * 
			 * setPageText( addPage(editor, input),
			 * BUNDLE.getString("editor.htmlFormat"));
			 */
			Composite composite = new Composite(getContainer(), SWT.NONE);
			composite.setLayout(new FillLayout());

			Browser browser = new Browser(composite, SWT.H_SCROLL
					| SWT.V_SCROLL);

			StringBuilder path = new StringBuilder("file:///")
					.append(Activator.getDefault().getStateLocation()
							.toString()).append('/')
					.append(message.getAttachmentDir());

			System.out.println("PATH => " + path);

			String filteredText = InlineFilter.filter(message,
					messageData.getHtmlMessage(), path.toString());

			System.out.println(filteredText);
			browser.setText(filteredText);
			browser.setCapture(true);

			// Get Preferences
			// IPreferenceStore pStore =
			// Activator.getDefault().getPreferenceStore();

			// Execute Javascript?
			// browser.setJavascriptEnabled(pStore
			// .getBoolean(PreferenceConstants.PARAM_JAVASCRIPT));

			setPageText(addPage(composite),
					BUNDLE.getString("editor.htmlFormat"));

		} catch (Exception e) {
			e.printStackTrace();
			e.getCause().printStackTrace();
			ErrorDialog.openError(getSite().getShell(),
					"Error creating nested text editor", null, null);
		}
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