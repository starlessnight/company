package com.metropia.ui;

import org.apache.commons.lang3.StringUtils;

import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.widget.EditText;

public class DelayTextWatcher implements TextWatcher {
	
	public static final String FORCE_NOTIFY_SPACE = " ";
	
	public interface TextChangeListener {
		public void onTextChanged(CharSequence text);
		public void onTextChanging();
	}
	
	private Handler delayHandler;
	private TextChangeListener listener;
	private long delay;
	private NotificationRunnable currentTask;
	private EditText mEditText;
	private String forceNotifyText;
	private String lastNotifyText;
	
	public DelayTextWatcher(EditText mEditText, TextChangeListener listener, long delay, String forceNotifyText) {
	    this.mEditText = mEditText;
		this.listener = listener;
		this.delay = delay;
		this.delayHandler = new Handler();
		this.forceNotifyText = forceNotifyText;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	    mEditText.getText().setSpan(new BackgroundColorSpan(Color.TRANSPARENT), 
            start, start + count, Spannable.SPAN_COMPOSING);
	}

	@Override
	public void afterTextChanged(Editable s) {
		delayHandler.removeCallbacks(currentTask);
		if(StringUtils.isEmpty(forceNotifyText) || !StringUtils.endsWith(s, forceNotifyText)) {
	        currentTask = new NotificationRunnable(s);
	        delayHandler.postDelayed(currentTask, delay);
        }
        if(listener!=null){
        	if(StringUtils.isEmpty(forceNotifyText) || !StringUtils.endsWith(s, forceNotifyText)) { 
        		listener.onTextChanging();
        	}
        	else {
        		if(!StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(s.toString()), lastNotifyText)) {
        			listener.onTextChanged(s);
        			lastNotifyText = StringUtils.trimToEmpty(s.toString());
        		}
        	}
        }
	}
	
	private class NotificationRunnable implements Runnable {
        private CharSequence message;

        public NotificationRunnable(CharSequence message) {
          this.message = message;
        }

        @Override
        public void run() {
          if (listener != null && !StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(message.toString()), lastNotifyText)) {
        	  listener.onTextChanged(message);
        	  lastNotifyText = StringUtils.trimToEmpty(message.toString());
          }
        }
    }

}
