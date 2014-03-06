package com.smartrek.ui.menu;

import android.app.Activity;
import android.content.Intent;

import com.actionbarsherlock.view.MenuItem;
import com.smartrek.activities.DashboardActivity;
import com.smartrek.activities.DebugOptionsActivity;
import com.smartrek.activities.HomeActivity;
import com.smartrek.activities.LandingActivity;
import com.smartrek.activities.LandingActivity2;
import com.smartrek.activities.MapDisplayActivity;
import com.smartrek.activities.R;
import com.smartrek.activities.ReservationConfirmationActivity;
import com.smartrek.activities.ReservationListActivity;
import com.smartrek.activities.RouteActivity;
import com.smartrek.activities.ShareActivity;
import com.smartrek.models.User;
import com.smartrek.utils.Misc;

/**
 * A common interface to bring up the application menu
 * 
 */
public final class MainMenu {
	public static void onMenuItemSelected(Activity activity, int featureId, MenuItem item) {
	    onMenuItemSelected(activity, featureId, item.getItemId());
	}
	
	   public static void onMenuItemSelected(Activity activity, int featureId, android.view.MenuItem item) {
	       onMenuItemSelected(activity, featureId, item.getItemId());
	    }
	   
	   public static void onMenuItemSelected(Activity activity, int featureId, int itemId) {
	       if (activity.getClass().equals(ReservationConfirmationActivity.class)) {
	           activity.setResult(RouteActivity.RESERVATION_CONFIRM_ENDED);
	       }
           Intent intent = null;
           switch (itemId) {
           
           case R.id.home:
               if (!activity.getClass().equals(LandingActivity2.ENABLED?LandingActivity2.class:LandingActivity.class)) {
                   intent = new Intent(activity, LandingActivity2.ENABLED?LandingActivity2.class:LandingActivity.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   activity.startActivity(intent);
                   activity.finish();
               }
               break;
           
           case R.id.route:
               if (!activity.getClass().equals(HomeActivity.class)) {
                   intent = new Intent(activity, HomeActivity.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   activity.startActivity(intent);
                   activity.finish();
               }
               break;
               
//       case R.id.contacts:
//           intent = new Intent(activity, ContactsActivity.class);
//           activity.startActivity(intent);
//           break;

           case R.id.map_display_options:
               if (!activity.getClass().equals(MapDisplayActivity.class)) {
                   intent = new Intent(activity, MapDisplayActivity.class);
                   int displayed = 0;
                   intent.putExtra("mapmode", 1);
                   activity.startActivityForResult(intent, displayed);
                   if (!activity.getClass().equals(LandingActivity2.class)) {
                       activity.finish();
                   }
               }
               break;
               
           case R.id.dashboard:
               if (!activity.getClass().equals(DashboardActivity.class)) {
                   intent = new Intent(activity, DashboardActivity.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   activity.startActivity(intent);
                   activity.finish();
               }
               break;

           case R.id.reservation:
               if (!activity.getClass().equals(ReservationListActivity.class)) {
                   intent = new Intent(activity, ReservationListActivity.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   activity.startActivity(intent);
                   activity.finish();
               }
               break;

           case R.id.logout_option:
               User.logout(activity);

               intent = new Intent(activity, LandingActivity2.ENABLED?LandingActivity2.class:LandingActivity.class);
               intent.putExtra(LandingActivity.LOGOUT, true);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               activity.startActivity(intent);
               activity.finish();
               break;
               
//         case R.id.crash:
//             ((HomeActivity) null).getApplication();
//             break;
   //
//         case R.id.clear_cache:
//             Cache.getInstance().clear();
//             break;

           case R.id.debug_options:
               if (!activity.getClass().equals(DebugOptionsActivity.class)) {
                   intent = new Intent(activity, DebugOptionsActivity.class);
                   activity.startActivity(intent);
               }
               break;
               
           case R.id.share_menu:
        	   if(!activity.getClass().equals(ShareActivity.class)) {
        		   intent = new Intent(activity, ShareActivity.class);
        		   intent.putExtra(ShareActivity.TITLE, "More Metropians = Less Traffic");
        		   intent.putExtra(ShareActivity.SHARE_TEXT, "I helped solve traffic congestion using Metropia Mobile!"
        		           + "\n\n" + Misc.getGooglePlayAppUrl(activity));
                   activity.startActivity(intent);
        	   }
        	   break;
        	   
           default:
               activity.setResult(Activity.RESULT_CANCELED);
           }
	   }
	   
}
