package smartrek.tasks;

import java.util.List;

import smartrek.mappers.ContactsMapper;
import smartrek.models.User;
import android.os.AsyncTask;

public final class ContactsFetchTask extends AsyncTask <Object, Object, List<User>> {
	
	AsyncTaskCallback<List<User>> callback;
	
	public void setCallback(AsyncTaskCallback<List<User>> callback) {
		this.callback = callback;
	}
	
    @Override
    protected void onPreExecute () {
    	if(callback != null) {
    		callback.onPreExecute();
    	}
    }

	@Override
	protected List<User> doInBackground(Object... params) {
        ContactsMapper mapper = new ContactsMapper();
        
        int uid = (Integer) params[0];
        List<User> contacts = mapper.getContacts(uid);
        
        if(callback != null) {
        	callback.onExecute();
        }
        
		return contacts;
	}
	
	@Override
	protected void onPostExecute(List<User> contacts) {
		//setListAdapter(new ContactItemAdapter(ContactsActivity.this, R.layout.contact_list_item, contacts));
		
		if(callback != null) {
			callback.onPostExecute(contacts);
		}
	}
}