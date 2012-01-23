package SmarTrek.AndroidPlatform;

import SmarTrek.AndroidPlatform.R;
import SmarTrek.AndroidPlatform.SeverCommunicator.Route_Communicator;
import SmarTrek.AndroidPlatform.Utilities.Route;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ConfirmTripActivity extends Activity implements OnClickListener {

	private Button confirmButton;
	private Route route;
	private int routeSelected;
	private Context context;
	
	/******************************************************************************************************************
	 * 
	 *
	 ******************************************************************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_trip);
        
        context = this;
        
        Bundle extras = getIntent().getExtras();
        
        route = new Route(extras);
        
        routeSelected = extras.getInt("selected route");
        
//		Display display = getWindowManager().getDefaultDisplay();
//		int width = display.getWidth();
//		int height = display.getHeight();
        
        ImageView imageView = new ImageView(this);

		Bitmap bitmap = (Bitmap) extras.getParcelable("image");
        
        imageView.setImageBitmap(bitmap);
        
        confirmButton = (Button) findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(this);
        
        String text = extras.getString("Vendor Name");
        text += "\n\n" + extras.getString("Coupon Description");
        text += "\n\nValid Through" + extras.getString("Valid Date"); 
        
        TextView textView = (TextView) findViewById(R.id.coupon_text);
        textView.setText(text);
        
        RelativeLayout rl =  (RelativeLayout) findViewById(R.id.coupon_root);
        rl.setBackgroundDrawable(imageView.getDrawable());
    }
	
	/******************************************************************************************************************
	 * 
	 *
	 ******************************************************************************************************************/
	@Override
	public void onClick(View arg0) {
		new BackgroundReservationTask().execute();
	}
	
	public void finishAct(){
		
		finish();
		finishActivity(-1);
	}
	
	/****************************************************************************************************************
	 * 
	 *
	 *
	 ****************************************************************************************************************/ 
    protected class BackgroundReservationTask extends AsyncTask<Void,Void,Void > {    	 
    	
    	/****************************************************************************************************************
    	 * 
    	 *
    	 *
    	 ****************************************************************************************************************/ 
    	protected Void doInBackground(Void... url) {  
    		Route_Communicator rc = new Route_Communicator();
    		rc.reservation(route);
        	try {
				Thread.sleep(100);
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
        @Override 
        protected void onPostExecute(Void v) { 	
    		Intent intent = new Intent(context, ConfirmedMapActivity.class);
    		
    		Bundle extras = new Bundle();
    		route.putOntoBundle(extras);
    		extras.putInt("selected route",routeSelected);
    		intent.putExtras(extras);
    		finishAct();
    		startActivity(intent);	
        }
	}
}