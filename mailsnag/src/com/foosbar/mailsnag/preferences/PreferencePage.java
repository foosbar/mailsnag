/*******************************************************************************
 * Copyright (c) 2010-2013 Foos-Bar.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kevin Kelley - initial API and implementation
 *******************************************************************************/
package com.foosbar.mailsnag.preferences;

import java.util.ResourceBundle;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.foosbar.mailsnag.Activator;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 * 
 * @author Kevin Kelley
 */
public class PreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/** i18n message bundle */
	private final ResourceBundle bundle;

	/**
	 * Default constructor
	 */
	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		bundle = Activator.getResourceBundle();
		setDescription(bundle.getString("preference.description"));
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		// The port number field
		IntegerFieldEditor port = addIntField(PreferenceConstants.PARAM_PORT,
				"preference.port");
		// Sets range of valid port numbers
		port.setValidRange(1, 65535);

		// The message persistence field
		addBooleanField(PreferenceConstants.PARAM_PERSIST, "preference.persist");

		if (Activator.isNotificationAvailable()) {
			// The notification popup field
			addBooleanField(PreferenceConstants.PARAM_NOTIFICATION_ENABLED,
					"preference.notification");
		}

		// The debug output field
		addBooleanField(PreferenceConstants.PARAM_DEBUG, "preference.debug");

		// The
		addBooleanField(PreferenceConstants.PARAM_STARTUP, "preference.startup");
	}

	/**
	 * Creates an true/false input field.
	 * 
	 * @param fieldId
	 * @param messageId
	 * @return
	 */
	private BooleanFieldEditor addBooleanField(String fieldId, String messageId) {
		return addPreferenceField(new BooleanFieldEditor(fieldId,
				bundle.getString(messageId), getFieldEditorParent()));
	}

	/**
	 * Creates an integer input field
	 * 
	 * @param fieldId
	 * @param messageId
	 * @return
	 */
	private IntegerFieldEditor addIntField(String fieldId, String messageId) {
		return addPreferenceField(new IntegerFieldEditor(fieldId,
				bundle.getString(messageId), getFieldEditorParent()));
	}

	/**
	 * Adds the new field to the collection of preferences fields and 
	 * returns the field to the user.
	 * @param field
	 * @return
	 */
	private <X extends FieldEditor> X addPreferenceField(X field) {
		addField(field);
		return field;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}