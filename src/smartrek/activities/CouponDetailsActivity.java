package smartrek.activities;

import java.text.SimpleDateFormat;

import smartrek.models.Coupon;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
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
	    		Intent intent = new Intent(CouponDetailsActivity.this, ContactsActivity.class);

//	    		Bundle extras = new Bundle();
//	    		extras.putParcelable("coupon", coupons.get(position));
//	    		intent.putExtras(extras);
	    		startActivity(intent);
			}
		});
		shareButton.setVisibility((extras.containsKey("ownership") && extras.getBoolean("ownership")) ? View.VISIBLE : View.INVISIBLE);
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}
}
