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
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.w3c.dom.*;
import android.preference.*;
import android.os.*;

public class SDKArchive
{
	String url, size, checksum, arch, os, sizeString;
	final DisplayActivity master;
	private final SDKPackage mParent;
	private boolean allowData;
	public long enqueue;
	public SDKArchive(Node srcNode, SDKPackage parent)
	{
		size = "";
		arch = "any";
		os = "any";
		this.mParent = parent;
		master = (DisplayActivity) parent.mView.getContext();
		NamedNodeMap attrs = srcNode.getAttributes();

		Node archNode = attrs.getNamedItem("arch");
		if (archNode != null)
			arch = archNode.getTextContent();

		Node osNode = attrs.getNamedItem("os");
		if (osNode != null)
			os = osNode.getTextContent();

		Node child = srcNode.getFirstChild();
		while (child != null)
		{
			if (child.getNodeName().equals("sdk:url"))
			{
				url = child.getTextContent();
			}
			else if (child.getNodeName().equals("sdk:checksum"))
			{
				checksum = child.getTextContent();
			}
			else if (child.getNodeName().equals("sdk:size"))
			{
				size = child.getTextContent();
			}
			child = child.getNextSibling();
		}
	}

	public boolean isDownloading()
	{
		return master.isDownloading && master.enqueuedArchive == this;
	}

	public boolean isInstalled()
	{
		File loc = new File(mParent.getInstallPath());
		if (loc.exists() && loc.isDirectory())
		{
			return true;
		}
		return false;
	}
	public String getDownloadUrl()
	{
		if (!url.startsWith("http"))
			url = mParent.mView.getContext().getString(R.string.url_default) + url;
		return url;
	}
	public String getDownloadPath() throws IOException
	{
		String ret = getDownloadUrl();
		File root = new File(Environment.getExternalStorageDirectory() + "/" +
							 PreferenceManager.getDefaultSharedPreferences(mParent.mView.getContext()).getString("pref_storageDir", ""));
		if (!root.exists())
			root.mkdirs();
		if (root.isFile())
			throw new IOException("Invalid sdk storage path");


		return root.getCanonicalPath() + "/" + ret.substring(ret.lastIndexOf('/'));
	}
	public String getSizeString()
	{
		if (sizeString == null)
		{
			Float fSize = Float.parseFloat(size);
			String unit = "B";
			if (fSize > 1024)
			{
				fSize /= (1024 * 1024);
				unit = "MB";
			}
			fSize = Math.round(fSize * 100) / 100.f;
			sizeString = size.isEmpty() ? "unknown size": (fSize.toString() + unit);
		}
		return sizeString;
	}
	public String getArchiveName()
	{
		return "Arch: " + arch + ", OS: " + os + ", Size: " + getSizeString();
	}
	private void initDownload()
	{
		try
		{
			DownloadManager.Request req = new DownloadManager.Request(Uri.parse(getDownloadUrl()));
			req.setTitle(mParent.getPackageName());
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			{
				req.setAllowedOverMetered(allowData);
			}
			req.setDestinationUri(Uri.fromFile(new File(getDownloadPath())));
			enqueue = master.dm.enqueue(req);
			master.enqueuedArchive = SDKArchive.this;
			master.isDownloading = true;
			mParent.updateIcon();
		}
		catch (Exception e)
		{
			Toast.makeText(mParent.mView.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	public void onClick(Activity parent)
	{
		if (master.isDownloading)
		{
			Toast t = Toast.makeText(mParent.mView.getContext(), "Download is already pending...", Toast.LENGTH_SHORT);
			t.setDuration(800);
			t.show();
			return;
		}
		allowData = PreferenceManager.getDefaultSharedPreferences(mParent.mView.getContext()).getBoolean("pref_allowNetUsage", false);
		ConnectivityManager connMgr = (ConnectivityManager) 
			mParent.mView.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
		if (netInfo == null)
		{
			Toast t = Toast.makeText(mParent.mView.getContext(), "No connection available", Toast.LENGTH_SHORT);
			t.setDuration(800);
			t.show();
			return;
		}
		else if (!allowData && netInfo.getType() != ConnectivityManager.TYPE_WIFI)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(parent);
			builder.setMessage("Wi-fi connection is unavailable. Go to the settings if you want to enable downloading.");
			builder.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id)
					{}
				});
			builder.create().show();
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(parent);
		builder.setMessage("Do you want to download " + getSizeString() + "?");
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id)
				{
					if (mParent.license.isEmpty())
					{
						initDownload();
					}
					else
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(master);
						builder.setTitle("Do you agree to the license terms?");
						builder.setMessage(mParent.license);
						builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id)
								{

									initDownload();
								}
							});
						builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id)
								{
									// User cancelled the dialog
								}
							});
						builder.create().show();
					}
				}
			});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id)
				{
					// User cancelled the dialog
				}
			});
		builder.create().show();
	}
	public void onDownloadComplete(String uriString)
	{
		String fPath = Uri.parse(uriString).getPath();
		if (PreferenceManager.getDefaultSharedPreferences(mParent.mView.getContext()).getBoolean("pref_doNotInflate", false))
		{
			Toast.makeText(mParent.mView.getContext(), "Stored archive:\n" + fPath, Toast.LENGTH_LONG).show();
			return;
		}
		try
		{
			ZipInputStream zip = new ZipInputStream(new FileInputStream(fPath));
			ZipEntry entry;
			File installDir = new File(mParent.getInstallPath());
			if (!installDir.exists() || !installDir.isDirectory())
				installDir.mkdirs();
			while ((entry = zip.getNextEntry()) != null)
			{
				File curFile = new File(installDir + "/" + entry.getName());
				if (entry.isDirectory())
				{
					curFile.mkdirs();
				}
				else
				{
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(curFile.getCanonicalPath(), false), 2048);
					byte buf[] = new byte[2048];
					int c;
					while ((c = zip.read(buf)) != -1)
						out.write(buf, 0, c);
					out.flush();
					out.close();
				}
			}

			zip.close();
		}
		catch (Exception e)
		{
			Toast.makeText(mParent.mView.getContext(), "Unzip error: " + e.getMessage(), Toast.LENGTH_LONG).show();
			return;
		}
		finally
		{
			try
			{
				new File(fPath).delete();
			}
			catch (Exception e)
			{
				Toast.makeText(mParent.mView.getContext(), "File deletion error: " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
			finally
			{
				Toast.makeText(mParent.mView.getContext(), "Installation completed under " + mParent.getInstallPath(), Toast.LENGTH_LONG).show();
			}
		}
	}
}
