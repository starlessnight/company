package com.metropia.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import com.metropia.models.Address;

public final class EditAddress extends EditText {
	
	public static final String CURRENT_LOCATION = "Current location";
    private boolean currentLocationInUse;
	private Address address;
	
	private int defaultTextColor;

	public EditAddress(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		// if address has been altered
		if (address != null) {
			if (!getText().toString().trim().equals(address.getAddress())) {
				address = null;
			}
		}
		return false;
	}
	
	public void setAddressAsCurrentLocation() {
		if (!currentLocationInUse) {
			defaultTextColor = getCurrentTextColor();			
		}
		
		currentLocationInUse = true;
		address = null;
		
		setText(CURRENT_LOCATION);
		//setTextColor(Color.BLUE);
		//setEnabled(false);
	}
	
	public void unsetAddress() {
		if (currentLocationInUse) {
			setTextColor(defaultTextColor);
		}
		
		currentLocationInUse = false;
		address = null;
		
		setText("");
		
		// This approach does not work
		//setTextColor(getTextColors().getDefaultColor());
		
		//setEnabled(true);
	}
	
	public boolean isCurrentLocationInUse() {
		return currentLocationInUse;
	}
	
	public boolean hasAddress() {
		return address != null && address.getId() != 0;
	}
	
	public Address getAddress() {
		return address;
	}
	
	public void setAddress(Address address) {
		unsetAddress();
		setText(address.getAddress());
		this.address = address;
	}

}
