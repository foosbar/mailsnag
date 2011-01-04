/**
 * 
 */
package com.foosbar.mailsnag.editors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.foosbar.mailsnag.Activator;
import com.foosbar.mailsnag.model.Message;
import com.foosbar.mailsnag.model.Message.Attachment;

/**
 * @author Kevin Kelley (dev@foos-bar.com)
 */
public class AttachmentEditorInput implements IPathEditorInput {

	private Attachment attachment;
	private IPath path;
	
	public AttachmentEditorInput(Attachment attachment) {
		this.attachment = attachment;
		
		Message m = this.attachment.getMessage();
		path = 
			Activator.getDefault().getStateLocation()
				.append(m.getAttachmentDir())
				.append(attachment.getId())
				.append(attachment.getName()); 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		return attachment.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class arg0) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPathEditorInput#getPath()
	 */
	public IPath getPath() {
		return path;
	}
}
