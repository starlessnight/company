package smartrek.activities;

import java.util.List;

import smartrek.adapters.ContactItemAdapter;
import smartrek.models.User;
import smartrek.tasks.AsyncTaskCallback;
import smartrek.tasks.ContactsFetchTask;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class ContactsActivity extends ListActivity {
	
	private List<User> contacts;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        User user = User.getCurrentUser(this);
        ContactsFetchTask task = new ContactsFetchTask();
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, ContactDetailsActivity.class);
		
		Bundle extras = new Bundle();
		extras.putParcelable("user", contacts.get(position));
		intent.putExtras(extras);
		startActivity(intent);
	}
}
