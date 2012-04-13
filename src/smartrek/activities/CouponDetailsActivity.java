package smartrek.activities;

import java.io.IOException;
import java.text.SimpleDateFormat;

import smartrek.mappers.CouponMapper;
import smartrek.models.Coupon;
import smartrek.models.User;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class CouponDetailsActivity extends Activity {
	private Coupon coupon;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.coupon_details);
		
		Bundle extras = getIntent().getExtras();
		coupon = (Coupon) extras.getParcelable("coupon");
		
		ImageView imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setImageBitmap(coupon.getBitmap());
		
		TextView textViewVender = (TextView) findViewById(R.id.textViewVendor);
		textViewVender.setText(coupon.getVendor());
		
		TextView textViewValidDate = (TextView) findViewById(R.id.textViewValidDate);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		textViewValidDate.setText(String.format("Valid until %s", dateFormat.format(coupon.getValidDate())));
		
		TextView textViewDescription = (TextView) findViewById(R.id.textViewDescription);
		textViewDescription.setText(coupon.getDescription());
		
		Button shareButton = (Button) findViewById(R.id.buttonShare);
		shareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	    		Intent intent = new Intent(CouponDetailsActivity.this, CouponSendActivity.class);

	    		Bundle extras = new Bundle();
	    		extras.putParcelable("coupon", coupon);
	    		intent.putExtras(extras);
	    		startActivity(intent);
			}
		});
		shareButton.setVisibility((extras.containsKey("ownership") && extras.getBoolean("ownership")) ? View.VISIBLE : View.INVISIBLE);
	
		LinearLayout layoutAcceptReject = (LinearLayout) findViewById(R.id.layoutAcceptReject);
		layoutAcceptReject.setVisibility((extras.containsKey("received") && extras.getBoolean("received")) ? View.VISIBLE : View.INVISIBLE);
		
		Button acceptButton = (Button) findViewById(R.id.buttonAccept);
		acceptButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				User currentUser = User.getCurrentUser(CouponDetailsActivity.this);
				new CouponAcceptTask().execute(coupon.getSenderUid(), currentUser.getId());
			}
		});
		
		Button rejectButton = (Button) findViewById(R.id.buttonReject);
		rejectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				User currentUser = User.getCurrentUser(CouponDetailsActivity.this);
				new CouponRejectTask().execute(coupon.getSenderUid(), currentUser.getId());
			}
		});
		
		
		Button cancelButton = (Button) findViewById(R.id.buttonCancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		cancelButton.setVisibility((extras.containsKey("sent") && extras.getBoolean("sent")) ? View.VISIBLE : View.INVISIBLE);
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}
	
	private class CouponAcceptTask extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			// FIXME: Potential array out of boundary exception
			int suid = (Integer) params[0];
			int ruid = (Integer) params[1];
			
			CouponMapper mapper = new CouponMapper();
			try {
				mapper.acceptCoupon(coupon, suid, ruid);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			finish();
		}
	}
	
	private class CouponRejectTask extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			// FIXME: Potential array out of boundary exception
			int suid = (Integer) params[0];
			int ruid = (Integer) params[1];
			
			CouponMapper mapper = new CouponMapper();
			try {
				mapper.rejectCoupon(coupon, suid, ruid);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			finish();
		}
	}
	
	private class CouponSendCancelTask extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			// FIXME: Potential array out of boundary exception
			int suid = (Integer) params[0];
			int ruid = (Integer) params[1];
			
			CouponMapper mapper = new CouponMapper();
			try {
				mapper.cancelSentCoupon(coupon);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			finish();
		}
	}
}
