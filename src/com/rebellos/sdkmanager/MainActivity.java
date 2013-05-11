/*
 This file is part of SDK Manager.

 Copyright 2013 Dominik Marszk <dmarszk@gmail.com>

 SDK Manager is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 SDK Manager is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with SDK Manager; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.rebellos.sdkmanager;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import android.preference.*;
import android.provider.*;

public class MainActivity extends Activity
{
	public final static String URL_EXTRA = "com.rebellos.sdkmanager.URL_EXTRA";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }
	public void showSettings(View view)
	{
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
			
	public void fetchManifest(View view)
	{
		try
		{
			ConnectivityManager connMgr = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected())
			{
				Intent intent = new Intent(this, DisplayActivity.class);
				EditText editText = (EditText) findViewById(R.id.url_box);
				intent.putExtra(URL_EXTRA, editText.getText().toString());
				startActivity(intent);			
			}
			else
			{
				Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT).show();
			}
		}
		catch (Exception e)
		{
			Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
}
