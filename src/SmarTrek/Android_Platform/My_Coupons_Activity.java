package SmarTrek.Android_Platform;

import java.util.ArrayList;

import SmarTrek.Android_Platform.Adapters.ImageAdapter;
import SmarTrek.Android_Platform.Sever_Communicator.Coupon_Communicator;
import SmarTrek.Android_Platform.Utilities.Coupon;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class My_Coupons_Activity extends Activity {
	
	private Context context;
	private ProgressDialog dialog;
	ArrayList<Coupon> coupons;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.my_coupon_screen);
	    
	    context = this.getApplicationContext();

  	   	dialog = new ProgressDialog(My_Coupons_Activity.this);
  	   	dialog.setMessage("Loading Coupons...");
  	   	dialog.setIndeterminate(true);
  	   	dialog.setCancelable(false);
  	   	dialog.show();	
	    
	    new BackgroundDownloadImageTask().execute();
	}
	
	
	
	
	
	
/*=====================================================================================================================*/
    
	/****************************************************************************************************************
	 * 
	 *
	 *
	 ****************************************************************************************************************/ 
    private class BackgroundDownloadImageTask extends AsyncTask<Void, Void, Void> {
    	
    	/****************************************************************************************************************
    	 * 
    	 *
    	 *
    	 ****************************************************************************************************************/ 
        protected Void doInBackground(Void... v) {   
        	
            Coupon_Communicator coupon_com = new Coupon_Communicator();
            coupons = coupon_com.discount();
            coupon_com.doCouponBitmapDownloads(coupons,My_Coupons_Activity.this);
        	
        	
//    	    GridView gridview = (GridView) findViewById(R.id.coupon_grid_view);
//    	    gridview.setAdapter(new ImageAdapter(coupons,My_Coupons_Activity.this));
//
//    	    gridview.setOnItemClickListener(new OnItemClickListener() {
//    	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//    	            Toast.makeText(My_Coupons_Activity.this, "" + position, Toast.LENGTH_SHORT).show();
//    	        }
//    	    });
        	try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            return null;
        }       
        
    	/****************************************************************************************************************
    	 * 
    	 *
    	 *
    	 ****************************************************************************************************************/ 
		protected void onPostExecute(Void v) {
    	    GridView gridview = (GridView) findViewById(R.id.coupon_grid_view);
    	    gridview.setAdapter(new ImageAdapter(coupons,My_Coupons_Activity.this));

    	    gridview.setOnItemClickListener(new OnItemClickListener() {
    	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    	          //  Toast.makeText(My_Coupons_Activity.this, "" + position, Toast.LENGTH_SHORT).show();
    	        	final AlertDialog dialog = new AlertDialog.Builder(My_Coupons_Activity.this).create();
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
/*=====================================================================================================================*/
}
