package SmarTrek.Android_Platform;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RadioButton;

public class Map_Display_Activity extends Activity {

	private CheckBox display_departure;
	private CheckBox display_travel;
	private CheckBox display_arrival;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mapdisplayoptions);
	    
	    display_departure = (CheckBox) findViewById(R.id.mdoradio0);
	    display_travel = (CheckBox) findViewById(R.id.mdoradio1);
	    display_arrival = (CheckBox) findViewById(R.id.mdoradio2);
	 
//	    display_travel.setOnClickListener(this);
//	    display_arrival.setOnClickListener(this);
	    
	    Log.d("Map_Display_Activity","In Map_Display_Activity from ");
	    
	}
	
//	@Override
//	public void onClick(View v) {
////		RadioButton rb = (RadioButton) v;
////		rb.toggle();
////		rb.invalidate();
//	}
	
	@Override
	public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("display", getRetVal());
        setResult(RESULT_OK,intent);
        //close this Activity...
        finish();
	}

	private int getRetVal(){
		int retVal = 1;
		if(display_arrival.isChecked() && !display_travel.isChecked()){
			retVal = 2;
		}
		if(display_travel.isChecked() && !display_arrival.isChecked()){
			retVal = 3;
		}
		if(display_travel.isChecked() && display_arrival.isChecked()){
			retVal = 4;
		}
		return retVal;
	}
	
}
