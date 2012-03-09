package smartrek.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.RadioButton;

public class MapDisplayActivity extends Activity {

	private CheckBox displayDeparture;
	private RadioButton displayTravel;
	private RadioButton displayArrival;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mapdisplayoptions);
	    
	    displayDeparture = (CheckBox) findViewById(R.id.mdoradio0);
	    displayTravel = (RadioButton) findViewById(R.id.mdoradio1);
	    displayArrival = (RadioButton) findViewById(R.id.mdoradio2);
	 
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
		if(displayArrival.isChecked() && !displayTravel.isChecked()){
			retVal = 2;
		}
		if(displayTravel.isChecked() && !displayArrival.isChecked()){
			retVal = 3;
		}
		if(displayTravel.isChecked() && displayArrival.isChecked()){
			retVal = 4;
		}
		return retVal;
	}
	
}
