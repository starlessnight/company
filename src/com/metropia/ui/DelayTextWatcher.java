package com.metropia.ui;

import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.widget.EditText;

public class DelayTextWatcher implements TextWatcher {
	
	public interface TextChangeListener {
		public void onTextChanged(CharSequence text);
		public void onTextChanging();
	}
	
	private Handler delayHandler;
	private TextChangeListener listener;
	private long delay;
	private NotificationRunnable currentTask;
	private EditText mEditText;
	
	public DelayTextWatcher(EditText mEditText, TextChangeListener listener, long delay) {
	    this.mEditText = mEditText;
		this.listener = listener;
		this.delay = delay;
		this.delayHandler = new Handler();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	    mEditText.getText().setSpan(new BackgroundColorSpan(Color.TRANSPARENT), 
            start, start + count, Spannable.SPAN_COMPOSING);
	}

	@Override
	public void afterTextChanged(Editable s) {
		delayHandler.removeCallbacks(currentTask);
        currentTask = new NotificationRunnable(s);
        delayHandler.postDelayed(currentTask, delay);
        if(listener!=null){
        	listener.onTextChanging();
        }
	}
	
	private class NotificationRunnable implements Runnable {
        private CharSequence message;

        public NotificationRunnable(CharSequence message) {
          this.message = message;
        }

        @Override
        public void run() {
          if (listener != null) {
            listener.onTextChanged(message);
          }
        }
    }

}
