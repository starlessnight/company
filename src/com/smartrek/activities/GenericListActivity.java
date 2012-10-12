package com.smartrek.activities;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.markupartist.android.widget.PullToRefreshListView;
import com.smartrek.dialogs.GenericListDialog.ActionListener;

public class GenericListActivity<ItemType> extends Activity {
    
    protected enum Status {
        Loading, EmptyList, GenericList
    }
    
    protected List<ItemType> listItems;
    protected ArrayAdapter<ItemType> adapter;
    protected ActionListener<ItemType> actionListener;
    protected PullToRefreshListView listViewGeneric;
    protected TextView textViewGeneric;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_list);
        
        initViews();
        setStatus(Status.Loading);
    }
    
    protected void initViews() {
        textViewGeneric = (TextView) findViewById(R.id.text_view_generic);
        listViewGeneric = (PullToRefreshListView) findViewById(R.id.list_view_generic);
        listViewGeneric.setOnItemClickListener(new OnItemClickListener() {
            
            /**
             * With com.markupartist.android.widget.PullToRefreshListView,
             * {@code position} is off by 1. We're doing a quick hack here to
             * adjust this.
             */
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                onListItemClick((ListView) l, v, position-1, id);
            }
            
        });
    }
    
    protected void onListItemClick(ListView l, View v, int position, long id) {
        
    }
    
    public ListView getListView() {
        return listViewGeneric;
    }
    
    public void setStatus(Status status) {
        if (Status.Loading.equals(status)) {
            listViewGeneric.setVisibility(View.GONE);
            textViewGeneric.setVisibility(View.GONE);
        }
        else if (Status.EmptyList.equals(status)) {
            listViewGeneric.setVisibility(View.GONE);
            textViewGeneric.setVisibility(View.VISIBLE);
        }
        else if (Status.GenericList.equals(status)) {
            listViewGeneric.setVisibility(View.VISIBLE);
            textViewGeneric.setVisibility(View.GONE);
        }
        else {
            Log.e("GenericListActivity", "setStatus(): Unknown status. Should not reach here");
        }
    }
    
    public void setListAdapter(ListAdapter adapter) {
        listViewGeneric.setAdapter(adapter);
    }
}
