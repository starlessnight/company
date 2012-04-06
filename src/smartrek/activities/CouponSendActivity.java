package smartrek.activities;

import java.util.List;

import smartrek.adapters.ContactItemAdapter;
import smartrek.mappers.CouponMapper;
import smartrek.mappers.UserMapper;
import smartrek.models.Coupon;
import smartrek.models.User;
import smartrek.tasks.AsyncTaskCallback;
import smartrek.tasks.ContactsFetchTask;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public final class CouponSendActivity extends Activity {
	private Coupon coupon;
	private List<User> contacts;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.coupon_send);
		
		Bundle extras = getIntent().getExtras();
		coupon = (Coupon) extras.getParcelable("coupon");
		
		ListView listView = (ListView) findViewById(R.id.listViewContacts);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				User user = contacts.get(position);
				
				sendCouponTo(user);
			}
			
		});
		
		
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
				CouponSendActivity.this.contacts = results;
				
				ListView listView = (ListView) findViewById(R.id.listViewContacts);
				listView.setAdapter(new ContactItemAdapter(CouponSendActivity.this, R.layout.contact_list_item, results));
			}
        	
        });
        task.execute(uid);
	}
	
	private void sendCouponTo(User receiver) {
		//Log.d("CouponSendActivity", String.format("Sending coupon %s to %s ", coupon.getVendor(), user.getUsername()));
		
        User currentUser = User.getCurrentUser(this);
		
		new CouponSendTask().execute(coupon, currentUser.getId(), receiver.getId());
	}
	
	private class CouponSendTask extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			// FIXME: Potential array out of boundary exception
			Coupon coupon = (Coupon) params[0];
			int suid = (Integer) params[1];
			int ruid = (Integer) params[2];
			
			CouponMapper mapper = new CouponMapper();
			mapper.sendCouponTo(coupon, suid, ruid);
			
			return null;
		}
		
	}
}
