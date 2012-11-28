package com.smartrek.ui.menu;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import com.smartrek.activities.CouponsTabActivity;
import com.smartrek.activities.DebugOptionsActivity;
import com.smartrek.activities.HomeActivity;
import com.smartrek.activities.LoginActivity;
import com.smartrek.activities.MapDisplayActivity;
import com.smartrek.activities.R;
import com.smartrek.activities.ReservationListActivity;
import com.smartrek.models.User;

/**
 * A common interface to bring up the application menu
 * 
 */
public final class MainMenu {
	public static void onMenuItemSelected(Activity activity, int featureId, MenuItem item) {
		
		Intent intent = null;
		switch (item.getItemId()) {
		
		case R.id.route:
			if (!activity.getClass().equals(HomeActivity.class)) {
				intent = new Intent(activity, HomeActivity.class);
				activity.startActivity(intent);
			}
		    break;
			
//		case R.id.contacts:
//			intent = new Intent(activity, ContactsActivity.class);
//			activity.startActivity(intent);
//			break;

		case R.id.map_display_options:
			if (!activity.getClass().equals(MapDisplayActivity.class)) {
				intent = new Intent(activity, MapDisplayActivity.class);
				int displayed = 0;
				intent.putExtra("mapmode", 1);
				activity.startActivityForResult(intent, displayed);
			}
			break;

		case R.id.mycoupons:
			intent = new Intent(activity, CouponsTabActivity.class);
			activity.startActivity(intent);
			break;

		case R.id.reservation:
			if (!activity.getClass().equals(ReservationListActivity.class)) {
				intent = new Intent(activity, ReservationListActivity.class);
				activity.startActivity(intent);
			}
			break;

		case R.id.logout_option:
			User.logout(activity);

			// TODO: Is this right way to do it?
			intent = new Intent(activity, LoginActivity.class);
			activity.startActivity(intent);
			break;
			
//        case R.id.crash:
//            ((HomeActivity) null).getApplication();
//            break;
//
//        case R.id.clear_cache:
//            Cache.getInstance().clear();
//            break;

        case R.id.debug_options:
        	if (!activity.getClass().equals(DebugOptionsActivity.class)) {
        		intent = new Intent(activity, DebugOptionsActivity.class);
            	activity.startActivity(intent);
        	}
            break;
		}
	}
}
