package com.metropia;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.localytics.android.Localytics;

public class LocalyticsUtils {
	
	private static final String EVENT_NAME_APP_START = "APP Start";
	private static final String EVENT_NAME_SETTINGS_SUMMARY = "Settings Summary";
	private static final String EVENT_NAME_SOCIAL_SHARING = "Social Sharing";
	private static final String EVENT_NAME_APP_EXIT = "APP Exit";
	private static final String EVENT_NAME_APP_ERROR = "APP Error";
	private static final String EVENT_NAME_MAKE_RESERVATION = "Make a Reservation";
	private static final String EVENT_NAME_TRIP = "Trip";
	private static final String EVENT_NAME_VISIT_MY_METROPIA = "Visit My Metropia";
	private static final String EVENT_NAME_SAVE_MY_FAVORITE = "Save My Favorite";
	private static final String EVENT_NAME_SEND_OMW = "Send OMW";
	private static final String EVENT_NAME_RESCHEDULE_RESERVATION = "Reschedule Reservation";
	
	public static void tagAppStartFromPush() {
		Map<String, String> values = new HashMap<String, String>();
		values.put("Source", "push");
		Localytics.tagEvent(EVENT_NAME_APP_START, values);
		setProfileLastLoginDate();
	}
	
	public static void tagAppStartFromOrganic() {
		Map<String, String> values = new HashMap<String, String>();
		values.put("Source", "organic");
		Localytics.tagEvent(EVENT_NAME_APP_START, values);
		setProfileLastLoginDate();
	}
	
	private static final String LAST_LOGIN_DATE = "Last Login Date";
	
	private static void setProfileLastLoginDate() {
		Localytics.setProfileAttribute(LAST_LOGIN_DATE, new Date(System.currentTimeMillis()));
	}
	
	private static final String AUSTIN_LAUNCH = "Austin Launch";
	private static final double AUSTIN_LAUNCH_MAX_LAT = 30.293991;
	private static final double AUSTIN_LAUNCH_MAX_LON = -97.724856;
	private static final double AUSTIN_LAUNCH_MIN_LAT = 30.277129;
	private static final double AUSTIN_LAUNCH_MIN_LON = -97.743492;
	
	public static void setAustinLaunchIfInBoundingBox(double lat, double lon) {
		if(lat >= AUSTIN_LAUNCH_MIN_LAT && lat <= AUSTIN_LAUNCH_MAX_LAT && lon >=AUSTIN_LAUNCH_MIN_LON && lon <= AUSTIN_LAUNCH_MAX_LON) {
			Localytics.setProfileAttribute(AUSTIN_LAUNCH, "AustinLaunch");
		}
	}
	
	public static void tagCalendarIntegrationSettings(boolean turnOn) {
		Map<String, String> values = new HashMap<String, String>();
		values.put("Calendar Integration", turnOn ? "turn on" : "turn off");
		Localytics.tagEvent(EVENT_NAME_SETTINGS_SUMMARY, values);
	}
	
	public static final String PAYDAYS = "Paydays";
	public static final String DRIVE_SMART = "Drive Smart";
	
	public static void tagHelpOurResearchSettings(String type, boolean turnOn) {
		Map<String, String> values = new HashMap<String, String>();
		values.put("Help Our Research - " + type, turnOn ? "turn on" : "turn off");
		Localytics.tagEvent(EVENT_NAME_SETTINGS_SUMMARY, values);
	}
	
	public static final String FACEBOOK = "facebook";
	public static final String TWITTER = "twitter";
	public static final String GOOGLE_PLUS = "google+";
	public static final String EMAIL = "email";
	public static final String TEXT_MESSAGE = "text message";
	
	public static void tagSocialSharing(String type) {
		Map<String, String> values = new HashMap<String, String>();
		values.put("Platform", type);
		Localytics.tagEvent(EVENT_NAME_SOCIAL_SHARING, values);
	}
	
	public static void tagAppExit(String type) {
		Map<String, String> values = new HashMap<String, String>();
		values.put("Platform", type);
		Localytics.tagEvent(EVENT_NAME_APP_EXIT, values);
	}
	
	public static final String BAD_RESULT = "bad result";
	public static final String NETWORK_ERROR = "network error";
	
	public static void tagAppError(String type) {
		Map<String, String> values = new HashMap<String, String>();
		values.put("Type", type);
		Localytics.tagEvent(EVENT_NAME_APP_ERROR, values);
	}
	
	public static void tagMakeAReservation(int columnNum) {
		Map<String, String> values = new HashMap<String, String>();
		values.put("Time ahead", getDescFromColumnNum(columnNum));
		Localytics.tagEvent(EVENT_NAME_MAKE_RESERVATION, values);
	}
	
	private static String getDescFromColumnNum(int columnNum) {
		int timeWindowEnd = (columnNum + 1) * 15;
		if(( timeWindowEnd / 60) > 0) {
			return timeWindowEnd / 60 + "hr";
		}
		else {
			return columnNum * 15 + "~" + timeWindowEnd + "min";
		}
	}
	
	public static final String ABORTED_TRIP = "aborted trip";
	public static final String COMPLETED_TRIP = "completed trip";
	public static final String TRIP_EXITED_MANUALLY = "exited manually";
	
	public static void tagTrip(String type) {
		Map<String, String> values = new HashMap<String, String>();
		values.put("Navigation", type);
		Localytics.tagEvent(EVENT_NAME_TRIP, values);
	}
	
	public static void tagVisitMyMetropia() {
		Localytics.tagEvent(EVENT_NAME_VISIT_MY_METROPIA);
	}
	
	public static void tagSaveMyFavorite(String iconName) {
		Map<String, String> values = new HashMap<String, String>();
		values.put("Icon Type", iconName);
		Localytics.tagEvent(EVENT_NAME_SAVE_MY_FAVORITE, values);
	}
	
	public static void tagSendOnMyWay() {
		Localytics.tagEvent(EVENT_NAME_SEND_OMW);
	}
	
	public static void tagReschedule() {
		Localytics.tagEvent(EVENT_NAME_RESCHEDULE_RESERVATION);
	}
}
