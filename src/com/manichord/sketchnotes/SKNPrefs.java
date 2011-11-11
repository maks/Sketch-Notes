package com.manichord.sketchnotes;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SKNPrefs extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
