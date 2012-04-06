package smartrek.activities;

import java.util.List;

import smartrek.adapters.ContactItemAdapter;
import smartrek.mappers.ContactsMapper;
import smartrek.mappers.UserMapper;
import smartrek.models.User;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class ContactsActivity extends ListActivity {
	
	private List<User> contacts;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // FIXME: This is just a temporary solution. LoginActivity must store
        // an instance of User when it caches it.
        SharedPreferences sharedPref = getSharedPreferences(LoginActivity.LOGIN_PREFS, MODE_PRIVATE);
        int uid = sharedPref.getInt(UserMapper.UID, -1);
        
		new ContactsFetchTask().execute(uid);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
	}
	
	private class ContactsFetchTask extends AsyncTask <Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
	        ContactsMapper mapper = new ContactsMapper();
	        
	        // FIXME: Potential array out of boundary exception
	        int uid = (Integer) params[0];
	        contacts = mapper.getContacts(uid);
	        
			return null;
		}
		
		@Override
		protected void onPostExecute(Object o) {
			setListAdapter(new ContactItemAdapter(ContactsActivity.this, R.layout.contact_list_item, contacts));
		}
		
	}
}
