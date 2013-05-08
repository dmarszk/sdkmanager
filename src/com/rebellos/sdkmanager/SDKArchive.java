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

public class SDKArchive
{
	String url, size, checksum, arch, os, sizeString;
	View view;
	final DisplayActivity master;
	private final SDKPackage mParent;
	public long enqueue;
	public SDKArchive(Node srcNode, View view, SDKPackage parent)
	{
		size = "";
		arch = "any";
		os = "any";
		this.view = view;
		this.mParent = parent;
		master = (DisplayActivity) view.getContext();
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
		return false;
	}
	public String getDownloadUrl()
	{
		if (url.startsWith("http"))
			return url;
		return view.getContext().getString(R.string.url_default) + url;
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
	public void onClick(Activity parent)
	{
		if(master.isDownloading)
		{
			Toast t = Toast.makeText(view.getContext(), "Download is already pending...", Toast.LENGTH_SHORT);
			t.setDuration(1500);
			t.show();
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(parent);
		builder.setMessage("Do you want to download " + getSizeString() + "?");
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id)
				{
					try
					{
						DownloadManager.Request req = new DownloadManager.Request(Uri.parse(getDownloadUrl()));
						req.setTitle(mParent.getPackageName());
						enqueue = master.dm.enqueue(req);
						master.enqueuedArchive = SDKArchive.this;
						master.isDownloading = true;
						mParent.updateIcon();
						master.updateList();
					}
					catch (Exception e)
					{
						Toast.makeText(view.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
	public void onDownloadComplete(String filename)
	{
		try
		{
			ZipInputStream zip = new ZipInputStream(new FileInputStream(filename));
			ZipEntry entry = zip.getNextEntry();
			while((entry = zip.getNextEntry()) != null)
			{
				
			}
			
			zip.close();
		}
		catch (Exception e)
		{
			Toast.makeText(view.getContext(), "Unzip error: " + e.getMessage(), Toast.LENGTH_LONG);
		}
	}
}
