package com.metropia.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.metropia.models.Address;
import com.metropia.activities.R;

@Deprecated
public class FavoriteAddressAdapter extends ArrayAdapter<Address> {
	
	private LayoutInflater inflater;
	
	public FavoriteAddressAdapter(Context context, List<Address> items) {
		super(context, R.layout.favorite_address_list, items);
		
		if(items != null) {
			notifyDataSetChanged();
		}
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(view == null) {
			view = inflater.inflate(R.layout.favorite_address_list_item, parent, false);
		}
		Address item = getItem(position);
		
		TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
		textViewName.setText(item.getName());
		
		TextView textViewAddress = (TextView) view.findViewById(R.id.textViewAddress);
		textViewAddress.setText(item.getAddress());
		
		return view;
	}

}
