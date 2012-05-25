package smartrek.adapters;

import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import smartrek.activities.R;
import smartrek.mappers.CouponMapper;
import smartrek.models.Coupon;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CouponAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private List<Coupon> items;
	
	public CouponAdapter(Activity activity, List<Coupon> items) {
		super();
		this.items = items;
		this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		if(items != null) {
			return items.size();
		}
		else {
			return 0;
		}
	}

	@Override
	public Object getItem(int arg0) {
		if(items != null) {
			return items.get(arg0);
		}
		else {
			return null;
		}
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(view == null) {
			view = inflater.inflate(R.layout.coupon_list_item, null);
			
			Coupon coupon = (Coupon) getItem(position);
			
//			TextView textViewVendor = (TextView) view.findViewById(R.id.textViewVendor);
//			textViewVendor.setText(coupon.getVendor());
			
			TextView textViewDescription = (TextView) view.findViewById(R.id.textViewDescription);
			textViewDescription.setText(coupon.getDescription());
			
			// TODO: Date format localization
			SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
			
			TextView textViewExpiration = (TextView) view.findViewById(R.id.textViewExpiration);
			textViewExpiration.setText(String.format("Expires on %s", df.format(coupon.getValidDate())));
			
			ImageView imageView = (ImageView) view.findViewById(R.id.imageViewLogo);
			
			if (coupon.getBitmap() != null) {
				imageView.setImageBitmap(coupon.getBitmap());
			}
			else {
				Log.d("CouponAdapter", "Downloading coupon image...");
				new CouponImageTask(imageView, coupon).execute();
			}
		}
		return view;
	}
	
    private final class CouponImageTask extends AsyncTask<Void, Void, Void> {
    	
    	private ImageView imageView;
    	private Coupon coupon;
    	
    	/**
    	 * 
    	 * @param imageView An image view to be updated
    	 */
    	public CouponImageTask(ImageView imageView, Coupon coupon) {
    		super();
    		this.imageView = imageView;
    		this.coupon = coupon;
    	}

		@Override
		protected Void doInBackground(Void... params) {
			CouponMapper mapper = new CouponMapper();
			try {
				mapper.downloadImage(coupon);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v) {
			imageView.setImageBitmap(coupon.getBitmap());
		}
    
    }
}
