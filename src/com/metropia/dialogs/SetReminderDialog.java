package com.metropia.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.metropia.utils.Font;
import com.metropia.utils.datetime.RecurringTime;
import com.metropia.activities.R;

public final class SetReminderDialog extends Dialog {
	
    private static final String timeAM = "AM";
    
    private static final String timePM = "PM";
    
    private static final int minHour = 1;
    
    private static final int maxHour = 12;
    
    private static final int minMinute = 0;
    
    private static final int maxMinute = 59;
    
    private static final byte[] weekdayVals = {RecurringTime.SUN, RecurringTime.MON,
        RecurringTime.TUE, RecurringTime.WED, RecurringTime.THU, RecurringTime.FRI,
        RecurringTime.SAT};
    
	public interface ActionListener {
		void onClickPositiveButton(RecurringTime recurringTime);
		void onClickNegativeButton();
	}
	
	private ActionListener listener;
	
	private ViewGroup dialogView;
	
	private RecurringTime recurringTime;
	
	private static class TextViewDimension {
	    
	    float textSize;
	    
	    int paddingTop;
	    
	    int paddingBottom;
	    
	    TextViewDimension(TextView v){
	        textSize = v.getTextSize();
	        paddingTop = v.getPaddingTop();
	        paddingBottom = v.getPaddingBottom();
	    }
	    
	    void reset(TextView v){
	        v.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
	        v.setPadding(v.getPaddingLeft(), paddingTop, v.getPaddingRight(), 
                paddingBottom);
	    }
	    
	    void setPadding(TextView v) {
	        float tTextSize = v.getTextSize();
	        if(tTextSize < textSize){
	            int deltaPadding = Float.valueOf((textSize - tTextSize) / 2).intValue();
	            v.setPadding(v.getPaddingLeft(), paddingTop + deltaPadding, v.getPaddingRight(), 
                    paddingBottom + deltaPadding);
	        }
	    }
	    
	}
	
	private TextViewDimension backButtonDim;
	
	private TextViewDimension setButtonDim;

	protected SetReminderDialog(Context context, RecurringTime recurringTime) {
		super(context, R.style.PopUpDialog);
		this.recurringTime = new RecurringTime();
		if(recurringTime == null){
            Time time = new Time();
            time.setToNow();
            this.recurringTime = new RecurringTime();
            this.recurringTime.setHour(time.hour);
            this.recurringTime.setMinute(time.minute);
        }else{
            this.recurringTime.setHour(recurringTime.getHour());
            this.recurringTime.setMinute(recurringTime.getMinute());
            this.recurringTime.setWeekdays(recurringTime.getWeekdays());
        }
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    AssetManager assets = getContext().getAssets();
	    final Typeface boldFont = Font.getBold(assets);
	    final Typeface lightFont = Font.getLight(assets);
	    
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.trip_save_date, null);
		
		setContentView(dialogView);
		
		TextView titleView = (TextView) dialogView.findViewById(R.id.title);
        titleView.setText("When do you want to arrive?");
                
        int hour = recurringTime.getHour();
        int minute = recurringTime.getMinute();
        
        final TextView timeAmPmView = (TextView) dialogView.findViewById(R.id.time_am_pm);
        timeAmPmView.setText(hour <= maxHour?timeAM:timePM);
        
        timeAmPmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView amPmView = (TextView) v;
                amPmView.setText(timeAM.equals(amPmView.getText())?timePM:timeAM);
            }
        });
        
        final EditText timeHourView = (EditText) dialogView.findViewById(R.id.time_hour);
        setNumberRange(timeHourView, minHour, maxHour);
        timeHourView.setText(String.valueOf(hour > maxHour?(hour - maxHour):hour));
        
        dialogView.findViewById(R.id.time_hour_up)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int newVal = Integer.parseInt(timeHourView.getText().toString()) + 1;
                    timeHourView.setText(String.valueOf(newVal > maxHour?minHour:newVal));
                }
            });
        
        dialogView.findViewById(R.id.time_hour_down)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int newVal = Integer.parseInt(timeHourView.getText().toString()) - 1;
                    timeHourView.setText(String.valueOf(newVal < minHour?maxHour:newVal));
                }
            });
        
        final EditText timeMinuteView = (EditText) dialogView.findViewById(R.id.time_minute);
        setNumberRange(timeMinuteView, minMinute, maxMinute);
        timeMinuteView.setText(String.valueOf(minute));
        
        dialogView.findViewById(R.id.time_minute_up)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int newVal = Integer.parseInt(timeMinuteView.getText().toString()) + 1;
                    timeMinuteView.setText(String.valueOf(newVal > maxMinute?minMinute:newVal));
                }
            });
    
        dialogView.findViewById(R.id.time_minute_down)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int newVal = Integer.parseInt(timeMinuteView.getText().toString()) - 1;
                    timeMinuteView.setText(String.valueOf(newVal < minMinute?maxMinute:newVal));
                }
            });
        
        ViewGroup weekdayChkGrp = (ViewGroup)dialogView.findViewById(R.id.weekday_checkbox_group);
        List<CheckedTextView> weekdayCheckboxes = new ArrayList<CheckedTextView>();
        for(int i=0; i<weekdayChkGrp.getChildCount(); i++){
            CheckedTextView wCheckbox = (CheckedTextView)weekdayChkGrp.getChildAt(i);
            byte wVal = weekdayVals[i];
            boolean checked = (recurringTime.getWeekdays() & wVal) > 0;
            wCheckbox.setChecked(checked);
            if(checked){
                Font.setTypeface(boldFont, wCheckbox);
            }else{
                Font.setTypeface(lightFont, wCheckbox);
            }
            wCheckbox.setTag(wVal);
            wCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckedTextView chkbox = (CheckedTextView) v;
                    chkbox.toggle();
                    byte weekdays = recurringTime.getWeekdays();
                    byte val = (Byte) chkbox.getTag();
                    if(chkbox.isChecked()){
                        weekdays |= val; 
                        Font.setTypeface(boldFont, chkbox);
                    }else{
                        weekdays &= ~val;
                        Font.setTypeface(lightFont, chkbox);
                    }
                    recurringTime.setWeekdays(weekdays);
                }
            });
            weekdayCheckboxes.add(wCheckbox);
        }
		
		Button setButton = (Button) dialogView.findViewById(R.id.set_button);
		setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    int hour = Integer.parseInt(timeHourView.getText().toString());
                    if(timePM.equals(timeAmPmView.getText())){
                        hour += maxHour;
                    }
                    recurringTime.setHour(hour);
                    recurringTime.setMinute(Integer.parseInt(timeMinuteView.getText().toString()));
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
        
        Font.setTypeface(boldFont, titleView, setButton, backButton,
            timeHourView, timeAmPmView, timeMinuteView);
        
        resizeButtonText();
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
	}
	
	public void resizeButtonText(){
	    float offset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44 + 45 + 10, 
            getContext().getResources().getDisplayMetrics());
        int winWidth = getWindow().getWindowManager().getDefaultDisplay().getWidth();
        final float width = winWidth / 2.0f - offset;
        final Button backButton = (Button) dialogView.findViewById(R.id.back_button);
        backButton.post(new Runnable() {
            @Override
            public void run() {
                if(backButtonDim == null){
                    backButtonDim = new TextViewDimension(backButton);
                }
                backButtonDim.reset(backButton);
                Font.autoScaleTextSize(backButton, width);
                backButtonDim.setPadding(backButton);
            }
        });
        final Button setButton = (Button) dialogView.findViewById(R.id.set_button);
        setButton.post(new Runnable() {
            @Override
            public void run() {
                if(setButtonDim == null){
                    setButtonDim = new TextViewDimension(setButton);
                }
                setButtonDim.reset(setButton);
                Font.autoScaleTextSize(setButton, width);
                setButtonDim.setPadding(setButton);
            }
        });
    }
	
	public ActionListener getActionListener() {
		return listener;
	}

	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}
	
	private static EditText setNumberRange(final EditText v, final int min, final int max){
	    v.addTextChangedListener(new TextWatcher() {
            
            boolean filtering;
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!filtering){
                    String newText = s.toString().trim();
                    int val = StringUtils.isBlank(newText)?min:Integer.parseInt(newText);
                    while(val > max){
                        newText = newText.substring(1);
                        val = Integer.parseInt(newText);
                        newText = String.valueOf(val);
                    }
                    if(val <= min){
                        newText = String.valueOf(min);
                    }
                    filtering = true;
                    newText = StringUtils.leftPad(newText, 2, "0");
                    newText = newText.substring(newText.length() - 2);
                    v.setText(newText);
                    v.setSelection(newText.length());
                    filtering = false;
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
	    return v;
	}
	
}
