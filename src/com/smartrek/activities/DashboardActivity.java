package com.smartrek.activities;

import java.text.DecimalFormat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.models.User;
import com.smartrek.requests.TrekpointFetchRequest;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.Misc;

public final class DashboardActivity extends ActionBarActivity {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        findViewById(R.id.rewards_tab).setSelected(true);
        final TextView trekpointsLabel = (TextView) findViewById(R.id.trekpoints_label);
        Font.setTypeface(boldFont, trekpointsLabel);
        
        User user = User.getCurrentUser(this);
        final int uid = user.getId();
        AsyncTask<Void, Void, Long> trekpointsTask = new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... params) {
                Long trekpoints = 0L;
                TrekpointFetchRequest req = new TrekpointFetchRequest(uid);
                req.invalidateCache(DashboardActivity.this);
                try {
                    trekpoints = req.execute(DashboardActivity.this);
                }
                catch (Exception e) {
                    ehs.registerException(e);
                }
                return trekpoints;
            }
            @Override
            protected void onPostExecute(Long trekpoints) {
                if (ehs.hasExceptions()) {
                    ehs.reportExceptions();
                }
                else {
                    DecimalFormat fmt = new DecimalFormat("#,###");
                    trekpointsLabel.setText(fmt.format(trekpoints));
                }
            }
        };
        Misc.parallelExecute(trekpointsTask);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater mi = getSupportMenuInflater();
        mi.inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        MainMenu.onMenuItemSelected(this, featureId, item);
        
        return super.onMenuItemSelected(featureId, item);
    }
	
}
