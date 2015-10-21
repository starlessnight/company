package com.metropia.utils;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

public class Speaker implements OnInitListener {
	
	private int MY_DATA_CHECK_CODE = 0;
	
	private Activity activity;
	private TextToSpeech TTS;
	
	public Speaker(Activity activity) {
		this.activity = activity;
	}
	
	public void init() {
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		activity.startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
	}
	public void shutdown() {
		TTS.shutdown();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {      
				TTS = new TextToSpeech(activity, this);
			}
			else {
				Intent installTTSIntent = new Intent();
				installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				activity.startActivity(installTTSIntent);
			}
		}
	}
	    
	
	public void speak(String text) {
		TTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}

	@Override
	public void onInit(int initStatus) {
		if (initStatus == TextToSpeech.SUCCESS) {
			TTS.setLanguage(Locale.US);
		}
		else if (initStatus == TextToSpeech.ERROR) {
			Toast.makeText(activity, "Sorry! Text To Speech failed...", Toast.LENGTH_SHORT).show();
		}
	}
}
