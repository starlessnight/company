package com.smartrek.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FavoriteAddressAddActivity extends Activity {

	private EditText editTextName;
	private EditText editTextAddress;
	private Button buttonAdd;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorite_address_add);
		
		Bundle bundle = getIntent().getExtras();
		String address = bundle.getString("address");
		
		editTextName = (EditText) findViewById(R.id.editTextName);
		editTextAddress = (EditText) findViewById(R.id.editTextAddress);
		editTextAddress.setText(address);

		buttonAdd = (Button) findViewById(R.id.buttonAdd);
		buttonAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				buttonAddOnClick((Button) view);
			}
		});
	}
	
	private void buttonAddOnClick(Button button) {
		
	}
}
