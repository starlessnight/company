package com.smartrek.activities;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.adapters.ContactItemAdapter;
import com.smartrek.models.User;
import com.smartrek.tasks.AsyncTaskCallback;
import com.smartrek.tasks.ContactsFetchTask;
import com.smartrek.ui.menu.MainMenu;

public final class ContactsActivity extends ListActivity {
	
	private List<User> contacts;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getListView().setBackgroundDrawable(getResources().getDrawable(R.drawable.background_gradient));
		
        User user = User.getCurrentUser(this);
        ContactsFetchTask task = new ContactsFetchTask(this);
        task.setCallback(new AsyncTaskCallback<List<User>> () {

			@Override
			public void onPreExecute() {
			}

			@Override
			public void onExecute() {
			}

			@Override
			public void onPostExecute(List<User> results) {
				contacts = results;
				setListAdapter(new ContactItemAdapter(ContactsActivity.this, R.layout.contact_list_item, contacts));
			}
        	
        });
        task.execute(user.getId());
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, ContactDetailsActivity.class);
		
		Bundle extras = new Bundle();
		extras.putParcelable("user", contacts.get(position));
		intent.putExtras(extras);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		MainMenu.onMenuItemSelected(this, featureId, item);
		return super.onMenuItemSelected(featureId, item);
	}
}
