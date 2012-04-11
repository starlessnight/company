package smartrek.activities;

import java.io.IOException;
import java.net.ConnectException;
import java.text.ParseException;
import java.util.ArrayList;

import org.json.JSONException;

import smartrek.adapters.CouponAdapter;
import smartrek.mappers.CouponMapper;
import smartrek.mappers.UserMapper;
import smartrek.models.Coupon;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * @deprecated
 *
 */
public class MyCouponsActivity extends FragmentActivity {
	
	private ProgressDialog dialog;
	ArrayList<Coupon> coupons;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.my_coupon_screen);
	    
  	   	dialog = new ProgressDialog(MyCouponsActivity.this);
  	   	dialog.setMessage("Loading Coupons...");
  	   	dialog.setIndeterminate(true);
  	   	dialog.setCancelable(false);
  	   	dialog.show();
	    
	    ListView gridview = (ListView) findViewById(R.id.coupon_grid_view);
	    gridview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    		Intent intent = new Intent(MyCouponsActivity.this, CouponDetailsActivity.class);

	    		Bundle extras = new Bundle();
	    		extras.putParcelable("coupon", coupons.get(position));
	    		intent.putExtras(extras);
	    		startActivity(intent);
	            
	        }
	    });
	    
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.LOGIN_PREFS, Context.MODE_PRIVATE);
        int uid = sharedPreferences.getInt(UserMapper.UID, -1);
        
	    new BackgroundDownloadImageTask().execute(uid);
	}
	
	
    private class BackgroundDownloadImageTask extends AsyncTask<Object, Void, Void> {
    	
    	@Override
        protected Void doInBackground(Object... args) {
    		
    		// FIXME: Potential array out of boundary exception
    		int uid = (Integer) args[0];
        	
            CouponMapper mapper = new CouponMapper();
            try {
				coupons = mapper.getCoupons(uid, CouponMapper.Flag.All);
				mapper.doCouponBitmapDownloads(coupons,MyCouponsActivity.this);
			} catch (ConnectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        	
        	
//    	    GridView gridview = (GridView) findViewById(R.id.coupon_grid_view);
//    	    gridview.setAdapter(new ImageAdapter(coupons,My_Coupons_Activity.this));
//
//    	    gridview.setOnItemClickListener(new OnItemClickListener() {
//    	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//    	            Toast.makeText(My_Coupons_Activity.this, "" + position, Toast.LENGTH_SHORT).show();
//    	        }
//    	    });

            return null;
        }       
        
		protected void onPostExecute(Void v) {
			ListView gridview = (ListView) findViewById(R.id.coupon_grid_view);
		    gridview.setAdapter(new CouponAdapter(MyCouponsActivity.this, coupons));
		    
			dialog.dismiss();
		}
	}
}
