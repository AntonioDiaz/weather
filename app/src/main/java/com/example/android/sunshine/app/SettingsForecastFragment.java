package com.example.android.sunshine.app;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/** Created by toni on 01/11/2016. */

public class SettingsForecastFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_general);
	}
}
