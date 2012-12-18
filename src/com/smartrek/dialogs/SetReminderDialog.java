package com.smartrek.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.smartrek.activities.R;

public final class SetReminderDialog extends AlertDialog {
	
	private ViewGroup dialogView;
	
	private Button buttonPickWeekdays;
	
	private byte weekdays;

	protected SetReminderDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.trip_save_date, null);
		
		setView(dialogView);
		setTitle("Pick a time");
		
		buttonPickWeekdays = (Button) dialogView.findViewById(R.id.button_pick_weekdays);
		buttonPickWeekdays.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				WeekdaysDialog dialog = new WeekdaysDialog(getContext(), weekdays);
				dialog.show();
			}
			
		});
		
		setButton(DialogInterface.BUTTON_POSITIVE, "OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
	}
}
