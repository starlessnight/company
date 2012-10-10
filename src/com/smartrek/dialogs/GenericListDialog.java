package com.smartrek.dialogs;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;
import com.smartrek.activities.R;

public class GenericListDialog<ItemType> extends AlertDialog {
    
    protected enum Status {
        Loading, EmptyList, GenericList
    }
	
	/**
	 * Dialog action listener
	 *
	 */
	public interface ActionListener<ItemType> {
		void onClickNegativeButton();
		void onClickNeutralButton();
		void onClickListItem(ItemType item, int position);
	}
	
	protected OnClickListener defaultNegativeButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (actionListener != null) {
				actionListener.onClickNegativeButton();
			}
		}
	};
	
	protected List<ItemType> listItems;
	protected ArrayAdapter<ItemType> adapter;
	protected ActionListener<ItemType> actionListener;
	protected ViewGroup dialogView;
	protected ViewGroup layoutLoading;
	protected PullToRefreshListView listViewGeneric;
	protected TextView textViewGeneric;
	
	protected GenericListDialog(Context context, List<ItemType> listItems) {
		super(context);
		this.listItems = listItems;
		
		if (context instanceof Activity) {
			setOwnerActivity((Activity) context);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
	    initViews();
		
		setView(dialogView);
		setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", defaultNegativeButtonListener);
		
		super.onCreate(savedInstanceState);
	}
	
	protected void initViews() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dialogView = (ViewGroup) inflater.inflate(R.layout.generic_list_dialog, null);
        layoutLoading = (ViewGroup) dialogView.findViewById(R.id.layout_loading);
        listViewGeneric = (PullToRefreshListView) dialogView.findViewById(R.id.list_view_generic);
        textViewGeneric = (TextView) dialogView.findViewById(R.id.text_view_generic);
	}
	
	/**
	 * This gets called when {@code listItems} has at least one item
	 */
	protected void initGenericList() {
		listViewGeneric.setAdapter(adapter);
		listViewGeneric.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (actionListener != null) {
					ItemType item = listItems.get(position);
					actionListener.onClickListItem(item, position);
				}
				dismiss();
			}
			
		});
	}
	
	/**
	 * This gets called when {@code listItem} is null or has no item
	 */
	protected void initEmptyList() {
		setButton(DialogInterface.BUTTON_NEGATIVE, "Dismiss", defaultNegativeButtonListener);	
	}
	
	public void setStatus(Status status) {
	    if (Status.Loading.equals(status)) {
            layoutLoading.setVisibility(View.VISIBLE);
            listViewGeneric.setVisibility(View.INVISIBLE);
            textViewGeneric.setVisibility(View.INVISIBLE);
	    }
	    else if (Status.EmptyList.equals(status)) {
            layoutLoading.setVisibility(View.INVISIBLE);
            listViewGeneric.setVisibility(View.INVISIBLE);
            textViewGeneric.setVisibility(View.VISIBLE);
	    }
	    else if (Status.GenericList.equals(status)) {
            layoutLoading.setVisibility(View.INVISIBLE);
            listViewGeneric.setVisibility(View.VISIBLE);
            textViewGeneric.setVisibility(View.INVISIBLE);
	    }
	    else {
	        Log.e("GenericListDialog", "setStatus(): Unknown status. Should not reach here");
	    }
	}
	
	public void setActionListener(ActionListener<ItemType> listener) {
		this.actionListener = listener;
	}
	
	public void setAdapter(ArrayAdapter<ItemType> adapter) {
		this.adapter = adapter;
	}

	protected List<ItemType> getListItmes() {
		return listItems;
	}
	
	protected void setListItems(List<ItemType> listItems) {
		this.listItems = listItems;
	}
}
