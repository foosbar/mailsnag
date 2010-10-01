package com.foosbar.mailsnag.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.foosbar.mailsnag.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.PARAM_PORT, 25);
		store.setDefault(PreferenceConstants.PARAM_DEBUG, false);
		store.setDefault(PreferenceConstants.PARAM_PERSIST, true);
	}

}
