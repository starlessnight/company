package com.smartrek.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.smartrek.utils.Preferences;

public class GCMIntentService extends GCMBaseIntentService {

	public static final String GCM_SENDER_ID = "968174328058";
	public static final String LOG_TAG = "GetAClue::GCMIntentService";

	public GCMIntentService() {
		super(GCM_SENDER_ID);
		// TODO Auto-generated constructor stub
		Log.i(LOG_TAG, "GCMIntentService constructor called");
	}

	@Override
	protected void onError(Context arg0, String errorId) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "GCMIntentService onError called: " + errorId);
	}

	@Override
	protected void onMessage(Context arg0, Intent intent) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "GCMIntentService onMessage called");
		Log.i(LOG_TAG, "Origin: " + intent.getStringExtra("origin"));
		Log.i(LOG_TAG, "Destination: " + intent.getStringExtra("destination"));
		Log.i(LOG_TAG, "Time: " + intent.getStringExtra("time"));
		Log.i(LOG_TAG, "Message: " + intent.getStringExtra("message"));
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "GCMIntentService onRegistered called");
		Log.i(LOG_TAG, "Registration id is: " + registrationId);
		
		SharedPreferences prefs = Preferences.getGlobalPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putString("GCMRegistrationID", registrationId);
		editor.commit();
	}

	@Override
	protected void onUnregistered(Context arg0, String registrationId) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "GCMIntentService onUnregistered called");
		Log.i(LOG_TAG, "Registration id is: " + registrationId);
	}
}