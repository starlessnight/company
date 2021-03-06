package com.metropia.tasks;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.metropia.models.User;
import com.metropia.requests.ContactsRequest;
import com.metropia.utils.ExceptionHandlingService;

public final class ContactsFetchTask extends AsyncTask <Object, Object, List<User>> {
	
	private AsyncTaskCallback<List<User>> callback;
	private ExceptionHandlingService ehs;
	private Context ctx;
	
	public ContactsFetchTask(Context context) {
		ehs = new ExceptionHandlingService(context);
		ctx = context;
	}
	
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
        
        int uid = (Integer) params[0];

		ContactsRequest request = new ContactsRequest(uid);
		List<User> contacts = null;
		try {
			contacts = request.execute(ctx);
		}
		catch (Exception e) {
			ehs.registerException(e);
		}

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