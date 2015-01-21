package com.metropia.ui.menu;

import android.app.Activity;
import android.content.Intent;

import com.actionbarsherlock.view.MenuItem;
import com.metropia.activities.DebugOptionsActivity;
import com.metropia.activities.LandingActivity2;
import com.metropia.activities.MapDisplayActivity;
import com.metropia.activities.MyMetropiaActivity;
import com.metropia.activities.MyTripsActivity;
import com.metropia.activities.R;
import com.metropia.activities.ShareActivity;
import com.metropia.activities.WebMyMetropiaActivity;
import com.metropia.models.User;
import com.metropia.utils.Misc;

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
//	       if (activity.getClass().equals(ReservationConfirmationActivity.class)) {
//	           activity.setResult(RouteActivity.RESERVATION_CONFIRM_ENDED);
//	       }
           Intent intent = null;
           switch (itemId) {
           
//           case R.id.home:
//               if (!activity.getClass().equals(LandingActivity2.ENABLED?LandingActivity2.class:LandingActivity.class)) {
//                   intent = new Intent(activity, LandingActivity2.ENABLED?LandingActivity2.class:LandingActivity.class);
//                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                   activity.startActivity(intent);
//                   activity.finish();
//               }
//               break;
//           
//           case R.id.route:
//               if (!activity.getClass().equals(HomeActivity.class)) {
//                   intent = new Intent(activity, HomeActivity.class);
//                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                   activity.startActivity(intent);
//                   activity.finish();
//               }
//               break;
               
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
               if(WebMyMetropiaActivity.hasMyMetropiaUrl(activity)){
                   if (!activity.getClass().equals(WebMyMetropiaActivity.class)) {
                       intent = new Intent(activity, WebMyMetropiaActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                       intent.putExtra(WebMyMetropiaActivity.WHICH_PAGE, WebMyMetropiaActivity.MY_METROPIA_PAGE);
                       activity.startActivity(intent);
                   }
               }else{
                   if (!activity.getClass().equals(MyMetropiaActivity.class)) {
                       intent = new Intent(activity, MyMetropiaActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                       activity.startActivity(intent);
                   }
               }
               break;

//           case R.id.reservation:
//               if (!activity.getClass().equals(ReservationListActivity.class)) {
//                   intent = new Intent(activity, ReservationListActivity.class);
//                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                   activity.startActivity(intent);
//                   activity.finish();
//               }
//               break;

           case R.id.logout_option:
               User.logout(activity);

               intent = new Intent(activity, LandingActivity2.class);
               intent.putExtra(LandingActivity2.LOGOUT, true);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               activity.startActivity(intent);
               activity.finish();
               break;

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
        		           + "\n\n" + Misc.APP_DOWNLOAD_LINK);
                   activity.startActivity(intent);
        	   }
        	   break;
           
           case R.id.my_trips:
        	   if (!activity.getClass().equals(MyTripsActivity.class) && MyTripsActivity.hasUrl(activity)) {
                   intent = new Intent(activity, MyTripsActivity.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   activity.startActivity(intent);
               }
        	   break;
        	   
           default:
               activity.setResult(Activity.RESULT_CANCELED);
           }
	   }
	   
}
