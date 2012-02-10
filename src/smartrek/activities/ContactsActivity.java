package smartrek.activities;

import java.util.List;

import smartrek.mappers.ContactsMapper;
import smartrek.mappers.UserMapper;
import smartrek.models.User;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ContactsActivity extends ListActivity {
	
	private List<User> contacts;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // FIXME: This is just a temporary solution. LoginActivity must store
        // an instance of User when it caches it.
        SharedPreferences sharedPref = getSharedPreferences(LoginActivity.LOGIN_PREFS, MODE_PRIVATE);
        int uid = sharedPref.getInt(UserMapper.UID, -1);
        String username = sharedPref.getString(UserMapper.USERNAME, "");
        User user = new User(uid, username);
        
		new ContactsFetchTask().execute(user);
	}
	
	private class ContactsFetchTask extends AsyncTask <User, Object, Object> {

		@Override
		protected Object doInBackground(User... params) {
	        ContactsMapper mapper = new ContactsMapper();
	        
	        User user = params[0];
	        contacts = mapper.getContacts(user);
	        
			return null;
		}
		
		@Override
		protected void onPostExecute(Object o) {
			setListAdapter(new ContactItemAdapter(ContactsActivity.this, R.layout.contact_list_item, contacts));
		}
		
	}
	
	private class ContactItemAdapter extends ArrayAdapter<User> {
		
		private int textViewResourceId;
		private List<User> objects;

		public ContactItemAdapter(Context context, int textViewResourceId,
				List<User> objects) {
			super(context, textViewResourceId, objects);
			
			this.textViewResourceId = textViewResourceId;
			this.objects = objects;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			User u = objects.get(position);
			
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(textViewResourceId, parent, false);
			
			TextView textView1 = (TextView)view.findViewById(R.id.textViewName);
			// FIXME: Need to consider i18n
			textView1.setText(String.format("%s %s (%s)", u.getFirstname(), u.getLastname(), u.getUsername()));

			return view;
		}
	}



}
