package smartrek.activities;

import java.text.SimpleDateFormat;

import smartrek.models.Coupon;
import android.app.Activity;
import android.os.Bundle;
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
	}
}
