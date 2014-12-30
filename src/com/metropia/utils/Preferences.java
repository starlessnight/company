package com.metropia.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences {
	
	public static final String GLOBAL = "Global";
	public static final String AUTH = "Auth";
	
	public static class Global {
		public static final String GCM_REG_ID = "GCMRegistrationID";
		public static final String LICENSE_AGREEMENT = "LicenseAgreement";
		public static final String INTRO_FINISH = "IntroFinish";
		public static final String TUTORIAL_FINISH = "TutorialFinish";
	}
	
	public static SharedPreferences getGlobalPreferences(Context context) {
		return context.getSharedPreferences(GLOBAL, Context.MODE_PRIVATE);
	}
	
	public static SharedPreferences getAuthPreferences(Context context) {
		return context.getSharedPreferences(AUTH, Context.MODE_PRIVATE);
	}

}
