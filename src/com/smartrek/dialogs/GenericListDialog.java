package com.smartrek.dialogs;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.markupartist.android.widget.PullToRefreshListView;
import com.smartrek.activities.R;
import com.smartrek.utils.Font;

public class GenericListDialog<ItemType> extends Dialog {
    
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
	protected Button addButton;
	protected TextView titleView;
	protected Typeface boldFont;
	protected Typeface lightFont;
	
	protected GenericListDialog(Context context, List<ItemType> listItems) {
		super(context, R.style.PopUpDialog);
		this.listItems = listItems;
		
		if (context instanceof Activity) {
			setOwnerActivity((Activity) context);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
	    initViews();
	    setStatus(Status.Loading);
		
		setContentView(dialogView);
		
		View closeIcon = dialogView.findViewById(R.id.close_icon);
        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (actionListener != null) {
                    actionListener.onClickNegativeButton();
                }
            }
        });
		
		super.onCreate(savedInstanceState);
	}
	
	protected void initViews() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dialogView = (ViewGroup) inflater.inflate(R.layout.generic_list_dialog, null);
        layoutLoading = (ViewGroup) dialogView.findViewById(R.id.layout_loading);
        listViewGeneric = (PullToRefreshListView) dialogView.findViewById(R.id.list_view_generic);
        textViewGeneric = (TextView) dialogView.findViewById(R.id.text_view_generic);
        addButton = (Button) dialogView.findViewById(R.id.add_button);
        titleView = (TextView) dialogView.findViewById(R.id.title);
        
        AssetManager assets = getContext().getAssets();
        boldFont = Font.getBold(assets);
        lightFont = Font.getLight(assets);
        
        Font.setTypeface(boldFont, titleView);
        if(addButton != null){
            Font.setTypeface(boldFont, addButton);
        }
        Font.setTypeface(lightFont, textViewGeneric);
	}
	
	@Override
	public void setTitle(CharSequence title) {
	    titleView.setText(title);
	}
	
	/**
	 * This gets called when {@code listItems} has at least one item
	 */
	protected void initGenericList() {
		listViewGeneric.setAdapter(adapter);
		listViewGeneric.setOnItemClickListener(new OnItemClickListener() {

            /**
             * With com.markupartist.android.widget.PullToRefreshListView,
             * {@code position} is off by 1. We're doing a quick hack here to
             * adjust this.
             */
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (actionListener != null) {
					ItemType item = listItems.get(position-1);
					actionListener.onClickListItem(item, position-1);
				}
				dismiss();
			}
			
		});
	}
	
	/**
	 * This gets called when {@code listItem} is null or has no item
	 */
	protected void initEmptyList() {
		//setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getResources().getString(R.string.close), defaultNegativeButtonListener);	
	}
	
	public void setStatus(Status status) {
	    if (Status.Loading.equals(status)) {
            layoutLoading.setVisibility(View.VISIBLE);
            listViewGeneric.setVisibility(View.GONE);
            textViewGeneric.setVisibility(View.GONE);
	    }
	    else if (Status.EmptyList.equals(status)) {
            layoutLoading.setVisibility(View.GONE);
            listViewGeneric.setVisibility(View.VISIBLE);
            textViewGeneric.setVisibility(View.VISIBLE);
	    }
	    else if (Status.GenericList.equals(status)) {
            layoutLoading.setVisibility(View.GONE);
            listViewGeneric.setVisibility(View.VISIBLE);
            textViewGeneric.setVisibility(View.GONE);
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
