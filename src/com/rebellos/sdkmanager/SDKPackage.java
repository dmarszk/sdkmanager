package com.rebellos.sdkmanager;
import android.app.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import org.w3c.dom.*;

public class SDKPackage
{
	String displayName;

	String pkgType;
	GradientDrawable mIcon;
	Map<String, String> values;
	List<SDKArchive> archives;
	boolean installed = false;
	public SDKPackage(Node srcNode, View view)
	{
		values = new HashMap<String, String>();
		archives = new ArrayList<SDKArchive>();
		pkgType = srcNode.getNodeName().substring(4);
		Node child = srcNode.getFirstChild();
		while (child != null)
		{
			if (child.getNodeName().startsWith("sdk:"))
			{
				values.put(child.getNodeName().substring(4), child.getTextContent());
			}
			if (child.getNodeName().equals("sdk:archives"))
			{
				Node archiveChild = child.getFirstChild();
				while (archiveChild != null)
				{
					if (archiveChild.getNodeName().equals("sdk:archive"))
						archives.add(new SDKArchive(archiveChild, view, this));
					archiveChild = archiveChild.getNextSibling();
				}

			}
			child = child.getNextSibling();
		}
	}

	public void setIcon(GradientDrawable iconShape)
	{
		mIcon = iconShape;
		updateIcon();
	}

	public void updateIcon()
	{
		int c = Color.GRAY;
		for(SDKArchive archive : archives)
		{
			if(archive.isDownloading())
			{
				c = Color.YELLOW;
				break;
			}
			else if(archive.isInstalled())
			{
				c = Color.GREEN;
				break;
			}
		}
		mIcon.setColor(c);
	}

	public void inflateMenu(ContextMenu menu)
	{
		menu.setHeaderTitle(getPackageName());
		int i = 0;
		for (SDKArchive archive : archives)
		{
			menu.add(Menu.NONE, i++, Menu.NONE, archive.getArchiveName());
		}
	}

	public boolean onMenuClick(Activity parent, MenuItem item)
	{
		SDKArchive archive = archives.get(item.getItemId());
		if (archive != null)
		{
			archive.onClick(parent);
		}
		return true;
	}

	public void onClick(View v)
	{
		Toast.makeText(v.getContext(), getPackageName(), Toast.LENGTH_LONG).show();
	}

	public boolean isInstalled()
	{
		return installed;
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
