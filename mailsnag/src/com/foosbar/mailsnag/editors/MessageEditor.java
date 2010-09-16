package com.foosbar.mailsnag.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.foosbar.mailsnag.model.Message;

public class MessageEditor extends EditorPart {

	public final static String ID = "com.foosbar.mailsnag.editors.MessageEditor";
	//private Label subjectLabel;
	//private Link link;
	private Text bodyText;
	private Message message;

	public MessageEditor() {
		super();
	}

	public void dispose() {
		super.dispose();
	}

	/*
	private Message getMessage() {
		return ((MessageEditorInput)getEditorInput()).getMessage();
	}
	*/
	
	@Override
	public void doSave(IProgressMonitor arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (!(input instanceof MessageEditorInput)) 
			throw new PartInitException("Invalid Input: Must be MessageEditorInput");
		setSite(site); 
		setInput(input);
		this.message = ((MessageEditorInput) input).getMessage();
		setPartName(this.message.getSubject());
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		top.setLayout(layout);
		// top banner
		Composite banner = new Composite(top, SWT.NONE);
		banner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.numColumns = 3;
		layout.horizontalSpacing = 0;
		banner.setLayout(layout);

		// setup bold font
		Font boldFont = JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT);

		GridData spanCell = new GridData();
		spanCell.horizontalAlignment = GridData.FILL;
		spanCell.horizontalSpan = 2;
		
		GridData endCell = new GridData();
		endCell.horizontalAlignment = GridData.FILL;
		
		
		Label l = new Label(banner, SWT.WRAP);
		l.setText("From:  ");
		
		l = new Label(banner, SWT.WRAP);
		l.setText(this.message.getFrom());
		l.setFont(boldFont);
		l.setLayoutData(spanCell);

		/*
		l = new Label(banner, SWT.WRAP);
		l.setText("Date:  Today" );
		l.setFont(boldFont);
		l.setLayoutData(endCell);
		*/
		
		l = new Label(banner, SWT.WRAP);
		l.setText("To:  ");

		l = new Label(banner, SWT.WRAP);
		l.setText(this.message.getToString());
		l.setFont(boldFont);
		l.setLayoutData(spanCell);

		// Setting Subject
		l = new Label(banner, SWT.WRAP);
		l.setText("Subject:  ");
		
		l = new Label(banner, SWT.WRAP);
		l.setText(this.message.getSubject());
		l.setFont(boldFont);
		l.setLayoutData(spanCell);

		//subjectLabel = new Label(banner, SWT.WRAP);
		//subjectLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
/*
		final GridData gd_link = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_link.widthHint = 423;

		link = new Link(banner, SWT.NONE);
		link.setLayoutData(gd_link);
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MessageDialog
						.openInformation(getSite().getShell(),
								"Not Implemented",
								"Imagine the address book or a new message being created now.");
			}
		});
*/
		// message contents
		bodyText = new Text(top, SWT.MULTI | SWT.WRAP);
		bodyText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		bodyText.setText(message.getMessage());
		//ISelectionService selectionService = getSite().getPage()e
				//.getWorkbenchWindow().getSelectionService();
		//selectionService.addSelectionListener(this);

		//dbc = new DataBindingContext();
		//Realm realm = SWTObservables.getRealm(parent.getShell().getDisplay());

		//IObservableValue subjectObservable = BeansObservables
		//		.observeDetailValue(realm, messageValue, "subject",
		//				String.class);
		//ISWTObservableValue subjectLabelObservable = SWTObservables
		//		.observeText(subjectLabel);
		//dbc.bindValue(subjectLabelObservable, subjectObservable, null, null);

		//dbc.bindValue(
		//		SWTObservables.observeText(bodyText, SWT.Modify),
		//		BeansObservables.observeDetailValue(realm, messageValue,"body", String.class), 
		//		null, 
		//		null);
		//dbc.bindValue(
		//		SWTObservables.observeSelection(spamButton),
		//		BeansObservables.observeDetailValue(realm, messageValue,"spam", boolean.class), 
		//		null, 
		//		null);
		//dbc.bindValue(
		//		SWTObservables.observeText(date),
		//		BeansObservables.observeDetailValue(realm, messageValue,"date", null), 
		//		null, 
		//		null);
		//dbc.bindValue(
		//		new LinkObservableValue(link),
		//		BeansObservables.observeDetailValue(realm, messageValue,"from", null), 
		//		null, 
		//		null);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
