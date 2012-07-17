package com.smartrek.adapters;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.models.Address;

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
