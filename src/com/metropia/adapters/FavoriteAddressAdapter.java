package com.metropia.adapters;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.metropia.activities.R;
import com.metropia.models.FavoriteIcon;
import com.metropia.tasks.ICallback;
import com.metropia.tasks.ImageLoader;
import com.metropia.utils.Dimension;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;
import com.metropia.utils.Geocoding.Address;

public class FavoriteAddressAdapter extends ArrayAdapter<Address> {
	
	public static final String NO_AUTOCOMPLETE_RESULT = "No results found.";
	
	EditText searchBox;
	
	public FavoriteAddressAdapter(Context context, final EditText searchBox) {
		super(context, R.layout.dropdown_select, R.id.name);
		this.searchBox= searchBox;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		Address item = getItem(position);
		View namePanel = view.findViewById(R.id.name_panel);
		TextView name = (TextView) view.findViewById(R.id.name);
		name.setText(item.getName());
		name.setEllipsize(null);
		TextView address = (TextView) view.findViewById(R.id.address);
		address.setText(item.getAddress());
		TextView distance = (TextView) view.findViewById(R.id.distance);
		final ImageView favIcon = (ImageView) view.findViewById(R.id.fav_icon);
		if(item.getDistance() >= 0) {
			distance.setVisibility(View.VISIBLE);
			distance.setText("> " + item.getDistance() + "mi");
		}
		else {
			distance.setVisibility(View.GONE);
		}
		
		FavoriteIcon icon = FavoriteIcon.fromName(item.getIconName(), null);
		if(icon==null && StringUtils.isNotBlank(item.getIconUrl())) {
			new ImageLoader(getContext(), item.getIconUrl(), new ICallback() {
				@Override
				public void run(Object... obj) {
					if (obj[0]==null) return;
					Drawable drawable = (Drawable) obj[0];
					
					favIcon.setImageDrawable(drawable);
					favIcon.setVisibility(View.VISIBLE);
				}
			}).execute(true);
		}
		else if (icon!=null) {
			favIcon.setImageBitmap(Misc.getBitmap(getContext(), icon.getFavoritePageResourceId(getContext()), 2));
			favIcon.setVisibility(View.VISIBLE);
		}
		else {
			favIcon.setImageBitmap(Misc.getBitmap(getContext(), R.drawable.poi_pin, 1));
			favIcon.setVisibility(View.VISIBLE);
		}
		
		
		Font.setTypeface(Font.getBold(getContext().getAssets()), name, distance);
		Font.setTypeface(Font.getLight(getContext().getAssets()), address);
		namePanel.requestLayout();
		name.requestLayout();
		distance.requestLayout();
		address.requestLayout();
		favIcon.requestLayout();
		int leftRightPadding = Dimension.dpToPx(10, getContext().getResources().getDisplayMetrics());
		int topBottomPadding = Dimension.dpToPx(2, getContext().getResources().getDisplayMetrics());
		view.setPadding(leftRightPadding, topBottomPadding, leftRightPadding, position == getCount() - 1 ? leftRightPadding : topBottomPadding);
		return view;
	}
	
	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<Address> all = new ArrayList<Address>();
				List<Address> result = new ArrayList<Address>();
				for(int i = 0 ; i < getCount() ; i++) {
					all.add(getItem(i));
				}
				if(constraint != null) {
					result.clear();
					for (Address addr : all) {
						if(addr.getName().toLowerCase().startsWith(constraint.toString().toLowerCase()) || addr.getAddress().toLowerCase().startsWith(constraint.toString().toLowerCase())){
							result.add(addr);
						}
					}
					FilterResults filterResults = new FilterResults();
					filterResults.values = result;
					filterResults.count = result.size();
					return filterResults;
				} else {
					return new FilterResults();
				}
			}
			
			@Override
			protected void publishResults(CharSequence constraint,	FilterResults results) {
				ArrayList<Address> filteredList = (ArrayList<Address>) results.values;
				if(results != null && results.count > 0) {
					clear();
					for (Address c : filteredList) {
						add(c);
					}
					notifyDataSetChanged();
				}
			}
			
			@Override
			public CharSequence convertResultToString(Object selected) {
				String selectedAddr = ((Address)selected).getAddress();
				String selectedName = ((Address)selected).getName();
				if(NO_AUTOCOMPLETE_RESULT.equals(selectedName) && StringUtils.isBlank(selectedAddr) && searchBox != null) {
					return searchBox.getText();
				}
				return selectedAddr;
			}
			
		};
		return filter;
	}
}