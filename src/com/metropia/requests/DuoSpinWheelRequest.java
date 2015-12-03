package com.metropia.requests;

import java.util.concurrent.TimeoutException;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;

import com.metropia.TripService;
import com.metropia.dialogs.DuoStyledDialog;
import com.metropia.models.User;
import com.metropia.tasks.ICallback;
import com.metropia.ui.Wheel;
import com.metropia.utils.HTTP.Method;

public class DuoSpinWheelRequest extends Request {
	
	public DuoSpinWheelRequest(User user) {
		this.username = user.getUsername();
		this.password = user.getPassword();
		
		url = getLinkUrl(Link.passenger_spin_wheel).replaceAll("\\{user_id\\}", String.valueOf(user.getId()));
	}
	
	public int execute(Context ctx, long reservationId, int degree) throws Exception {
		JSONObject params = new JSONObject();
		params.put("reservation_id", reservationId);
		params.put("degree", degree);
		
        String str = executeHttpRequest(Method.POST, url, params, ctx);
        
        JSONObject json = new JSONObject(str);
        int bonus = json.getJSONObject("data").getInt("credit_bonus");
        return bonus;
    }
	
	public void executeAsync(final Context ctx, final long reservationId, final int degree, final ICallback cb) {
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... arg0) {
				Integer bonus = null;
				DuoSpinWheelRequest request = new DuoSpinWheelRequest(User.getCurrentUser(ctx));
				try {
					bonus = request.execute(ctx, reservationId, degree);
					TripService.finishTrip(ctx, reservationId);
				} catch (Exception e) {}

				if (cb!=null) cb.run(bonus);
				return bonus;
			}
		}.execute();
	}
}
