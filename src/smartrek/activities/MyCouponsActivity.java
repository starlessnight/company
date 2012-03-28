package smartrek.activities;

import java.util.ArrayList;

import smartrek.adapters.ImageAdapter;
import smartrek.mappers.CouponMapper;
import smartrek.models.Coupon;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class MyCouponsActivity extends FragmentActivity {
	
	private Context context;
	private ProgressDialog dialog;
	ArrayList<Coupon> coupons;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.my_coupon_screen);
	    
	    context = this.getApplicationContext();

  	   	dialog = new ProgressDialog(MyCouponsActivity.this);
  	   	dialog.setMessage("Loading Coupons...");
  	   	dialog.setIndeterminate(true);
  	   	dialog.setCancelable(false);
  	   	dialog.show();
	    
	    new BackgroundDownloadImageTask().execute();
	}
	
	
    private class BackgroundDownloadImageTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
        protected Void doInBackground(Void... v) {   
        	
            CouponMapper mapper = new CouponMapper();
            coupons = mapper.getCoupons();
            mapper.doCouponBitmapDownloads(coupons,MyCouponsActivity.this);
        	
        	
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
    	    GridView gridview = (GridView) findViewById(R.id.coupon_grid_view);
    	    gridview.setAdapter(new ImageAdapter(coupons,MyCouponsActivity.this));

    	    gridview.setOnItemClickListener(new OnItemClickListener() {
    	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    	          //  Toast.makeText(My_Coupons_Activity.this, "" + position, Toast.LENGTH_SHORT).show();
    	        	final AlertDialog dialog = new AlertDialog.Builder(MyCouponsActivity.this).create();
    				dialog.setTitle("your coupon");
					dialog.setButton("View", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							dialog.dismiss();
						}
					});
					dialog.setButton2("Share", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							dialog.dismiss();
						}
					});					
    			//	dialog.setTitle(d);

					dialog.show();
    	            
    	        }
    	    });
			dialog.dismiss();
		}
	}
}
