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

public class MainActivity extends Activity
{
	public final static String URL_EXTRA = "com.rebellos.sdkmanager.URL_EXTRA";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
