package com.smartrek.activities;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.smartrek.adapters.ContactItemAdapter;
import com.smartrek.mappers.CouponMapper;
import com.smartrek.models.Coupon;
import com.smartrek.models.User;
import com.smartrek.tasks.AsyncTaskCallback;
import com.smartrek.tasks.ContactsFetchTask;
import com.smartrek.utils.ExceptionHandlingService;

public final class CouponSendActivity extends Activity {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
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

			// FIXME: Revise this section for better readability
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final User user = contacts.get(position);
				
				AlertDialog dialog = new AlertDialog.Builder(CouponSendActivity.this).create();
				dialog.setTitle("Confirmation");
				dialog.setMessage(String.format("You are about to sent %s coupon to %s. Do you want to proceed?", coupon.getVendor(), user.getFirstname()));
				dialog.setButton(Dialog.BUTTON_POSITIVE, "Yes", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sendCouponTo(user);
						dialog.dismiss();
					}
				});
				dialog.setButton(Dialog.BUTTON_NEGATIVE, "No", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}
			
		});
		
		
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
        User user = User.getCurrentUser(this);
        task.execute(user.getId());
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
			try {
				mapper.sendCouponTo(coupon, suid, ruid);
			}
			catch (IOException e) {
				ehs.registerException(e);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			if (ehs.hasExceptions()) {
			    ehs.reportExceptions();
			}
			else {
			    finish();
			}
		}
		
	}
}
