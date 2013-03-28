package com.smartrek.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.smartrek.activities.R;
import com.smartrek.utils.Font;
import com.smartrek.utils.datetime.RecurringTime;

public final class SetReminderDialog extends Dialog {
	
	public interface ActionListener {
		void onClickPositiveButton(RecurringTime recurringTime);
		void onClickNegativeButton();
	}
	
	private ActionListener listener;
	
	private ViewGroup dialogView;
	
	private TimePicker timePicker;
	
	private Button buttonPickWeekdays;
	
	private RecurringTime recurringTime;

	protected SetReminderDialog(Context context, RecurringTime recurringTime) {
		super(context, R.style.PopUpDialog);
		this.recurringTime = recurringTime != null ? recurringTime : new RecurringTime();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.trip_save_date, null);
		
		setContentView(dialogView);
		
		TextView titleView = (TextView) dialogView.findViewById(R.id.title);
        titleView.setText("When do you want to arrive?");
		
		timePicker = (TimePicker) dialogView.findViewById(R.id.timepicker);
		
		if (recurringTime != null) {
			timePicker.setCurrentHour((int) recurringTime.getHour());
			timePicker.setCurrentMinute((int) recurringTime.getMinute());
		}
		else {
			Time time = new Time();
			time.setToNow();
			
			timePicker.setCurrentHour(time.hour);
			timePicker.setCurrentMinute(time.minute);
		}
		
		timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				recurringTime.setHour(hourOfDay);
				recurringTime.setMinute(minute);
			}
		});
		
		buttonPickWeekdays = (Button) dialogView.findViewById(R.id.button_pick_weekdays);
		buttonPickWeekdays.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				WeekdaysDialog dialog = new WeekdaysDialog(getContext(), recurringTime.getWeekdays());
				dialog.setActionListener(new WeekdaysDialog.ActionListener() {
					
					@Override
					public void onClickPositiveButton(byte weekdays) {
						recurringTime.setWeekdays(weekdays);
					}
					
					@Override
					public void onClickNegativeButton() {
					}
				});
				dialog.show();
			}
			
		});
		
		Button setButton = (Button) dialogView.findViewById(R.id.set_button);
		setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onClickPositiveButton(recurringTime);
                } 
            }
        });
		
		View.OnClickListener onClickNegative = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onClickNegativeButton();
                }
            }
        };
		
		Button backButton = (Button) dialogView.findViewById(R.id.back_button);
        backButton.setOnClickListener(onClickNegative);
		
		View closeIcon = dialogView.findViewById(R.id.close_icon);
        closeIcon.setOnClickListener(onClickNegative);
        
        AssetManager assets = getContext().getAssets();
        Font.setTypeface(Font.getBold(assets), titleView, setButton, backButton);
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
	}
	
	public ActionListener getActionListener() {
		return listener;
	}

	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}
}
