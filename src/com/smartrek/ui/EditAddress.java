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
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		super.onKeyUp(keyCode, event);
		address = null;
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
		setTextColor(Color.BLACK);
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
