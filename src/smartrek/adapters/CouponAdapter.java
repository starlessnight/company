package smartrek.adapters;

import java.util.List;

import smartrek.activities.R;
import smartrek.models.Coupon;
import android.app.Activity;
import android.content.Context;
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
			
			TextView textViewVendor = (TextView) view.findViewById(R.id.textViewVendor);
			textViewVendor.setText(coupon.getVendor());
			
			TextView textViewDescription = (TextView) view.findViewById(R.id.textViewDescription);
			textViewDescription.setText(coupon.getDescription());
			
			ImageView imageView = (ImageView) view.findViewById(R.id.imageView1);
			imageView.setImageBitmap(coupon.getBitmap());
		}
		return view;
	}

}
