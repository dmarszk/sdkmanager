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
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;

public class SDKPackage
{
	String displayName;

	String pkgType;
	String mInstallPath;
	String license = null;
	GradientDrawable mIcon;
	DisplayActivity mActivity;
	View mView;
	Map<String, String> values;
	List<SDKArchive> archives;
	public SDKPackage(Node srcNode, View view, DisplayActivity activity)
	{
		values = new HashMap<String, String>();
		archives = new ArrayList<SDKArchive>();
		pkgType = srcNode.getNodeName().substring(4);
		Node child = srcNode.getFirstChild();
		mActivity = activity;
		mView = view;
		while (child != null)
		{
			if (child.getNodeName().equals("sdk:archives"))
			{
				Node archiveChild = child.getFirstChild();
				while (archiveChild != null)
				{
					if (archiveChild.getNodeName().equals("sdk:archive"))
						archives.add(new SDKArchive(archiveChild, this));
					archiveChild = archiveChild.getNextSibling();
				}
			}
			else if (child.getNodeName().equals("sdk:uses-license"))
			{
				String ref = "UNKNOWN";
				NamedNodeMap attrs = child.getAttributes();
				if (attrs.getNamedItem("ref") != null)
					ref = attrs.getNamedItem("ref").getTextContent();
				if (mActivity.licenses.containsKey(ref))
					license = mActivity.licenses.get(ref);
				else
					license = "Error! Cannot evaluate license named " + ref;
			}
			else if (child.getNodeName().startsWith("sdk:"))
			{
				values.put(child.getNodeName().substring(4), child.getTextContent());
			}
			child = child.getNextSibling();
		}
	}

	public void setIcon(GradientDrawable iconShape)
	{
		mIcon = iconShape;
		updateIcon();
	}
	
	void DeleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				DeleteRecursive(child);

		fileOrDirectory.delete();
	}
	
	public void uninstall()
	{
		for (SDKArchive archive : archives)
		{
			if (archive.isDownloading())
			{
				mActivity.dm.remove(archive.enqueue);
				mActivity.isDownloading = false;
				Toast.makeText(mView.getContext(), getPackageName() + " download cancelled!", Toast.LENGTH_LONG).show();
				updateIcon();
				return;
			}
		}
		File iPath = new File(getInstallPath());
		if (iPath.exists() && iPath.isDirectory())
			DeleteRecursive(iPath);
		updateIcon();
		Toast.makeText(mView.getContext(), getPackageName() + " uninstalled!", Toast.LENGTH_LONG).show();
	}
	
	public boolean isDownloading()
	{
		for (SDKArchive archive : archives)
		{
			if (archive.isDownloading())
				return true;
		}
		return false;
	}

	public boolean isInstalled()
	{
		for (SDKArchive archive : archives)
		{
			if (archive.isInstalled())
				return true;
		}
		return false;
	}

	public void updateIcon()
	{
		int c = Color.GRAY;
		for (SDKArchive archive : archives)
		{
			if (archive.isDownloading())
			{
				c = Color.YELLOW;
				break;
			}
			else if (archive.isInstalled())
			{
				c = Color.GREEN;
				break;
			}
		}
		mIcon.setColor(c);
		mActivity.updateList();
	}

	public void inflateMenu(ContextMenu menu)
	{
		menu.setHeaderTitle(getPackageName());
		if (isDownloading())
		{
			menu.add(Menu.NONE, 256, Menu.NONE, "Cancel download");
		}
		else if (isInstalled())
		{
			menu.add(Menu.NONE, 257, Menu.NONE, "Uninstall");
		}
		else
		{
			int i = 0;
			for (SDKArchive archive : archives)
			{
				menu.add(Menu.NONE, i++, Menu.NONE, archive.getArchiveName());
			}
		}
	}

	public boolean onMenuClick(Activity parent, MenuItem item)
	{
		if (item.getItemId() >= 256)
			uninstall();
		else
		{
			SDKArchive archive = archives.get(item.getItemId());
			if (archive != null)
			{
				archive.onClick(parent);
			}
		}
		return true;
	}

	public String getInstallPath()
	{
		if (mInstallPath == null)
		{
			mInstallPath = Environment.getExternalStorageDirectory() + "/" +
				PreferenceManager.getDefaultSharedPreferences(mView.getContext()).getString("pref_storageDir", "") + "/" +
				pkgType + "/";
			if (values.containsKey("vendor"))
				mInstallPath += values.get("vendor") + "/";
			if (values.containsKey("path"))
				mInstallPath += values.get("path") + "/";
			if (pkgType.equals("platform"))
				mInstallPath += "sdk_" + values.get("version") + "/";
		}
		return mInstallPath;
	}

	public String getPackageName()
	{
		if (displayName == null)
		{
			StringBuffer buf = new StringBuffer();
			if (pkgType.equals("add-on") || pkgType.equals("extra"))
			{
				if (values.containsKey("name-display"))
					buf.append(values.get("name-display"));
				else if (values.containsKey("description"))
					buf.append(values.get("description"));
				else
					buf.append("Addon/Extra with no name");
				if (values.containsKey("api-level"))
				{
					buf.append(", API ");
					buf.append(values.get("api-level"));
				}
				if (values.containsKey("revision"))
				{
					buf.append(", rev. ");
					buf.append(values.get("revision"));
				}

			}
			else if (pkgType.equals("platform"))
			{
				if (values.containsKey("description"))
					buf.append(values.get("description"));
				else if (values.containsKey("version"))
				{
					buf.append("Android SDK Platform ");
					buf.append(values.get("version"));
				}
				else 
					buf.append("Platform with no version");
				if (values.containsKey("revision"))
				{
					buf.append(", rev. ");
					buf.append(values.get("revision"));
				}
				if (values.containsKey("api-level"))
				{
					buf.append(", API ");
					buf.append(values.get("api-level"));
				}
			}
			else
			{
				buf.append("Unknown package from node ");
				buf.append(pkgType);
			}
			displayName = buf.toString();
		}
		return displayName;
	}
}
