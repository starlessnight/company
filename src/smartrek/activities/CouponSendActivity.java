package smartrek.activities;

import java.util.List;

import smartrek.adapters.ContactItemAdapter;
import smartrek.mappers.UserMapper;
import smartrek.models.User;
import smartrek.tasks.AsyncTaskCallback;
import smartrek.tasks.ContactsFetchTask;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

public class CouponSendActivity extends Activity {
	private List<User> contacts;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.coupon_send);
		
		
		
		
        SharedPreferences sharedPref = getSharedPreferences(LoginActivity.LOGIN_PREFS, MODE_PRIVATE);
        int uid = sharedPref.getInt(UserMapper.UID, -1);
        
        ContactsFetchTask task = new ContactsFetchTask();
        task.setCallback(new AsyncTaskCallback<List<User>> () {

			@Override
			public void onPreExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onExecute() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPostExecute(List<User> results) {
				ListView listView = (ListView) findViewById(R.id.listViewContacts);
				listView.setAdapter(new ContactItemAdapter(CouponSendActivity.this, R.layout.contact_list_item, results));
			}
        	
        });
        task.execute(uid);
	}
}
