package com.metropia.activities;

import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.metropia.dialogs.FloatingMenuDialog;
import com.metropia.dialogs.GenericListDialog.ActionListener;
import com.metropia.activities.R;

public class GenericListActivity<ItemType> extends ActionBarActivity {
    
    protected enum Status {
        Loading, EmptyList, GenericList
    }
    
    protected List<ItemType> listItems;
    protected ArrayAdapter<ItemType> adapter;
    protected ActionListener<ItemType> actionListener;
    protected ListView listViewGeneric;
    protected TextView textViewGeneric;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_list);
        
        initViews();
        setStatus(Status.Loading);
        
        findViewById(R.id.floating_menu_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingMenuDialog dialog = new FloatingMenuDialog(GenericListActivity.this);
                dialog.show();
            }
        });
    }
    
    protected void initViews() {
        textViewGeneric = (TextView) findViewById(R.id.text_view_generic);
        listViewGeneric = (ListView) findViewById(R.id.list_view_generic);
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
