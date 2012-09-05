package com.smartrek.dialogs;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.models.Address;

public class FavoriteAddressListDialog extends GenericListDialog<Address> {
	
	public interface ActionListener extends GenericListDialog.ActionListener<Address> {}

	public FavoriteAddressListDialog(Context context, List<Address> listItems) {
		super(context, listItems);
		setAdapter(new FavoriteAddressListAdapter(context, listItems));
		setTitle("Favorite addresses");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setButton(DialogInterface.BUTTON_NEUTRAL, "Add", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (actionListener != null) {
					actionListener.onClickNeutralButton();
				}
			}
			
		});
		
		super.onCreate(savedInstanceState);
		textViewGeneric.setText("You don't have any favorite address");
		
		registerForContextMenu(listViewGeneric);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getOwnerActivity().getMenuInflater();
	    inflater.inflate(R.menu.context, menu);
	}

	private class FavoriteAddressListAdapter extends ArrayAdapter<Address> {
		
		private LayoutInflater inflater;
		
		public FavoriteAddressListAdapter(Context context, List<Address> items) {
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
}
