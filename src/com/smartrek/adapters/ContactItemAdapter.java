package com.smartrek.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.models.User;

public class ContactItemAdapter extends ArrayAdapter<User> {
	
	private int textViewResourceId;
	
	public ContactItemAdapter(Context context, int textViewResourceId,
			List<User> objects) {
		super(context, textViewResourceId, objects);
		
		this.textViewResourceId = textViewResourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(textViewResourceId, parent, false);
		}

		User u = getItem(position);
		
		TextView textViewName = (TextView)view.findViewById(R.id.textViewName);
		// FIXME: Need to consider i18n
		if(!"".equals(u.getFirstname()) && !"".equals(u.getLastname())) {
			textViewName.setText(String.format("%s %s", u.getFirstname(), u.getLastname()));
		}
		else {
			textViewName.setText("(Not available)");
			textViewName.setTextColor(Color.GRAY);
		}

		TextView textViewUsername = (TextView)view.findViewById(R.id.textViewUsername);
		textViewUsername.setText(u.getUsername());
		
		return view;
	}
}