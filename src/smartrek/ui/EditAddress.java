package smartrek.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.EditText;

public final class EditAddress extends EditText {
	
	private boolean currentLocationInUse;

	public EditAddress(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setAddressAsCurrentLocation() {
		currentLocationInUse = true;
		
		setText("Current location");
		setTextColor(Color.BLUE);
		setEnabled(false);
	}
	
	public void unsetAddress() {
		currentLocationInUse = false;
		
		setText("");
		setTextColor(Color.BLACK);
		setEnabled(true);
	}
	
	public boolean isCurrentLocationInUse() {
		return currentLocationInUse;
	}

}
