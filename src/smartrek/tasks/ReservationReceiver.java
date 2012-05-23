package smartrek.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Route validation happens here
 *
 */
public final class ReservationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("ReservationReceiver", "Broadcast received.");
		
		// TODO: Validate departure time
		// TODO: Trigger GPS receiver
		
		// TODO: What's going to happen when the app terminates in the middle of validation?
	}

}
