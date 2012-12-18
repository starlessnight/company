package com.smartrek.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.smartrek.activities.R;

public final class SetReminderDialog extends AlertDialog {
	
	public interface ActionListener {
		void onClickPositiveButton(int hour, int minute, byte weekdays);
		void onClickNegativeButton();
	}
	
	private ActionListener listener;
	
	private ViewGroup dialogView;
	
	private TimePicker timePicker;
	
	private Button buttonPickWeekdays;
	
	private byte weekdays;
	
	private int hour;
	
	private int minute;

	protected SetReminderDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.trip_save_date, null);
		
		Resources res = getContext().getResources();
		
		setView(dialogView);
		setTitle("Pick a time");
		
		timePicker = (TimePicker) dialogView.findViewById(R.id.timepicker);
		timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				SetReminderDialog.this.hour = hourOfDay;
				SetReminderDialog.this.minute = minute;
			}
		});
		
		buttonPickWeekdays = (Button) dialogView.findViewById(R.id.button_pick_weekdays);
		buttonPickWeekdays.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				WeekdaysDialog dialog = new WeekdaysDialog(getContext(), weekdays);
				dialog.setActionListener(new WeekdaysDialog.ActionListener() {
					
					@Override
					public void onClickPositiveButton(byte weekdays) {
						SetReminderDialog.this.weekdays = weekdays;
					}
					
					@Override
					public void onClickNegativeButton() {
					}
				});
				dialog.show();
			}
			
		});
		
		setButton(DialogInterface.BUTTON_POSITIVE, res.getString(R.string.ok), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.onClickPositiveButton(hour, minute, weekdays);
				}
			}
		});
		
		setButton(DialogInterface.BUTTON_NEGATIVE, res.getString(R.string.cancel), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.onClickNegativeButton();
				}
			}
		});
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
	}
	
	public int getHour() {
		return hour;
	}
	
	public int getMinute() {
		return minute;
	}
	
	public byte getWeekdays() {
		return weekdays;
	}

	public ActionListener getActionListener() {
		return listener;
	}

	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}
}
