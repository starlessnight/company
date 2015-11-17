package com.metropia;

import io.fabric.sdk.android.Fabric;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.metropia.models.User;

public class CrashlyticsUtils {
	
	public static void start(Context ctx) {
		Fabric.with(ctx, new Crashlytics());
		User user = User.getCurrentUser(ctx);
		if(user != null) {
			initUserInfo(user);
		}
	}
	
	public static void initUserInfo(User user) {
		Crashlytics.setUserIdentifier(user.getName());
        Crashlytics.setUserEmail(user.getEmail());
        Crashlytics.setUserName(user.getFirstname() + " " + user.getLastname());
	}

}
