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
	public void onReceive(Context arg0, Intent arg1) {
		Log.d("ReservationReceiver", "Broadcast received.");
	}

}
