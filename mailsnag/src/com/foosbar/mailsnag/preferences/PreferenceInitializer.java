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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.foosbar.mailsnag.Activator;

/**
 * Class used to initialize default preference values.
 * 
 * @author Kevin Kelley
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.PARAM_PORT, 25);
		store.setDefault(PreferenceConstants.PARAM_DEBUG, false);
		store.setDefault(PreferenceConstants.PARAM_PERSIST, true);
		store.setDefault(PreferenceConstants.PARAM_STARTUP, false);

		// The notification preference is only available if the mylyn
		// notification class is available
		store.setDefault(PreferenceConstants.PARAM_NOTIFICATION_ENABLED,
				Activator.isNotificationAvailable());
	}
}