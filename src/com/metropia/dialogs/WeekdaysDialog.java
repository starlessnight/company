package com.metropia.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.metropia.activities.R;

public class WeekdaysDialog extends AlertDialog {
	
	public interface ActionListener {
		void onClickPositiveButton(byte weekdays);
		void onClickNegativeButton();
	}
	
	private ActionListener listener;

	private ViewGroup dialogView;
	
	private byte weekdays;
	
	public WeekdaysDialog(Context context, byte weekdays) {
		super(context, R.style.PopUpDialog);
		this.weekdays = weekdays;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.trip_save_weekdays, null);
		
		Resources res = getContext().getResources();
		
		setView(dialogView);
		//setIcon(res.getDrawable(R.drawable.save_trip));
		setTitle("Pick weekdays");
		
		int[] checkboxIds = { R.id.checkbox_saturday, R.id.checkbox_friday,
				R.id.checkbox_thursday, R.id.checkbox_wednesday,
				R.id.checkbox_tuesday, R.id.checkbox_monday,
				R.id.checkbox_sunday };
		
		for (int i = 0; i < checkboxIds.length; i++) {
			int checkboxId = checkboxIds[i];
			
			final byte weekday = (byte)(1 << i);
			final CheckBox checkBox = (CheckBox) dialogView.findViewById(checkboxId);
			checkBox.setChecked((weekdays & weekday) != 0);
			checkBox.setOnClickListener(new View.OnClickListener() {
			
				@Override
				public void onClick(View v) {
					if (checkBox.isChecked()) {
						weekdays |= weekday;
					}
					else {
						weekdays &= ~weekday;
					}
				}
			});
		}
		
		setButton(DialogInterface.BUTTON_POSITIVE, res.getString(R.string.ok), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.onClickPositiveButton(weekdays);
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
