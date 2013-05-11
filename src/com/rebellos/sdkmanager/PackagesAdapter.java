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
import android.view.*;
import android.widget.*;
import java.util.*;

public class PackagesAdapter extends ArrayAdapter<SDKPackage>
{
	private final Activity context;
	private final List<SDKPackage> packages;

	static class ViewHolder
	{
		public View icon;
		public GradientDrawable iconShape;
		public TextView text;
		public SDKPackage pkg;
	}

	public PackagesAdapter(Activity context, List<SDKPackage> packages)
	{
		super(context, R.layout.packagefield, packages);
		this.context = context;
		this.packages = packages;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View rowView = convertView;
		if (rowView == null)
		{
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.packagefield, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.icon = rowView.findViewById(R.id.package_icon);
			viewHolder.iconShape = (GradientDrawable) context.getResources().getDrawable(R.drawable.circle);
			viewHolder.iconShape.mutate();
			viewHolder.icon.setBackgroundDrawable(viewHolder.iconShape);
		//	viewHolder.iconShape = new ShapeDrawable(new OvalShape());
		//	viewHolder.icon.setBackground(viewHolder.iconShape);
			viewHolder.text = (TextView) rowView.findViewById(R.id.package_label);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		SDKPackage pkg = packages.get(position);
		holder.pkg = pkg;
		holder.text.setText(pkg.getPackageName());
		pkg.setIcon(holder.iconShape);

		return rowView;
	}
}
