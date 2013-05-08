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
