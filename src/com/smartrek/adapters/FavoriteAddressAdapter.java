package com.smartrek.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.models.Address;

public class FavoriteAddressAdapter extends BaseAdapter {
	
	private Activity activity;
	private LayoutInflater inflater;
	private List<Address> items = new ArrayList<Address>();
	
	public FavoriteAddressAdapter(Activity activity, List<Address> items) {
		this.activity = activity;
		if(items != null) {
			this.items = items;
		}
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setItems(List<Address> items) {
		if(items != null) {
			this.items = items;
		}
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(view == null) {
			view = inflater.inflate(R.layout.favorite_address_list_item, null);
			
			Address item = (Address) getItem(position);
			
			TextView textViewAddress = (TextView) view.findViewById(R.id.textViewAddress);
			textViewAddress.setText(item.getAddress());
		}
		
		return view;
	}

}
