package SmarTrek.AndroidPlatform.Adapters;

import java.util.ArrayList;

import SmarTrek.AndroidPlatform.Utilities.Coupon;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Coupon> coupons;
    
    public ImageAdapter(ArrayList<Coupon> coupons,Context c) {
        mContext = c;
        this.coupons = coupons;
    }

    public int getCount() {
    	return coupons.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(140, 130));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(coupons.get(position).getBitmap());
        return imageView;
    }
}
