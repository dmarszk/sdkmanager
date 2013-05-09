package com.rebellos.sdkmanager;

import android.os.*;
import android.preference.*;

public class SettingsActivity extends PreferenceActivity
{
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);
    }
}
