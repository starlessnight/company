package smartrek.ui;

import smartrek.activities.ContactsActivity;
import smartrek.activities.CouponsTabActivity;
import smartrek.activities.LoginActivity;
import smartrek.activities.MapDisplayActivity;
import smartrek.activities.R;
import smartrek.activities.ReservationListActivity;
import smartrek.models.User;
import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

/**
 * A common interface to bring up the application menu
 *
 */
public final class CommonMenu {
    public static void onMenuItemSelected(Activity activity, int featureId, MenuItem item) {
		Intent intent = null;
		switch(item.getItemId()){
		case R.id.contacts:
			intent = new Intent(activity, ContactsActivity.class);
			activity.startActivity(intent);
			break;
			
    	case R.id.map_display_options:
    		intent = new Intent(activity, MapDisplayActivity.class);
    		int displayed = 0;
    		intent.putExtra("mapmode", 1);
    		activity.startActivityForResult(intent, displayed);
    		break;

    	case R.id.mycoupons:
    		intent = new Intent(activity, CouponsTabActivity.class);
    		activity.startActivity(intent);
    		break;
    		
    	case R.id.reservation:
    		intent = new Intent(activity, ReservationListActivity.class);
    		activity.startActivity(intent);
    		break;
    		
    	case R.id.logout_option:
			User.setCurrentUser(activity, null);
			//finish();
			
			// TODO: Is this right way to do it?
			intent = new Intent(activity, LoginActivity.class);
			activity.startActivity(intent);
    		break;
    	}
	}
}
