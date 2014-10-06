package com.smartrek;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.smartrek.models.User;

public class CrashlyticsUtils {
	
	public static void start(Context ctx) {
		Crashlytics.start(ctx);
	}
	
	public static void initUserInfo(User user) {
		Crashlytics.setUserIdentifier(user.getName());
        Crashlytics.setUserEmail(user.getEmail());
        Crashlytics.setUserName(user.getFirstname() + " " + user.getLastname());
	}

}
