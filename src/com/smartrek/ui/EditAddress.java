package com.smartrek.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import com.smartrek.models.Address;

public final class EditAddress extends EditText {
	
	private boolean currentLocationInUse;
	private Address address;

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
		currentLocationInUse = true;
		address = null;
		
		setText("Current location");
		setTextColor(Color.BLUE);
		setEnabled(false);
	}
	
	public void unsetAddress() {
		currentLocationInUse = false;
		address = null;
		
		setText("");
		setTextColor(getTextColors().getDefaultColor());
		setEnabled(true);
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
		setText(address.getAddress());
		this.address = address;
	}

}
