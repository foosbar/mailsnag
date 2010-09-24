package com.foosbar.mailsnag.editors;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

/**
 * @author Kevin Kelley (dev@foos-bar.com)
 */
public class ExternalMessageEditorInput implements IStorageEditorInput {

	private IStorage storage;
	
	public ExternalMessageEditorInput(IStorage storage) {
		this.storage = storage;
	}
	
	public IStorage getStorage() throws CoreException {
		return storage;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return storage.getName();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return null;
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

}
