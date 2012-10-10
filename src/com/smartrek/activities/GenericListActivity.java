package com.smartrek.activities;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
    protected ViewGroup layoutLoading;
    protected PullToRefreshListView listViewGeneric;
    protected TextView textViewGeneric;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_list);
        
        initViews();
    }
    
    protected void initViews() {
        layoutLoading = (ViewGroup) findViewById(R.id.layout_loading);
        textViewGeneric = (TextView) findViewById(R.id.text_view_generic);
        listViewGeneric = (PullToRefreshListView) findViewById(R.id.list_view_generic);
        listViewGeneric.setOnItemClickListener(new OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                onListItemClick((ListView) l, v, position, id);
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
            Log.e("GenericListActivity", "setStatus(): Unknown status. Should not reach here");
        }
    }
    
    public void setListAdapter(ListAdapter adapter) {
        listViewGeneric.setAdapter(adapter);
    }
}
