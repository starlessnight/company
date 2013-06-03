package com.smartrek.activities;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.smartrek.models.User;
import com.smartrek.requests.Request;
import com.smartrek.requests.RewardsFetchRequest;
import com.smartrek.requests.RewardsFetchRequest.Reward;
import com.smartrek.requests.TrekpointFetchRequest;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.HTTP;
import com.smartrek.utils.Misc;

public final class DashboardActivity extends ActionBarActivity {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);
    
    private View rewardsDetail;

    private ListView rewardsList;
    
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
        final ImageView detailRewardPicture = (ImageView) findViewById(R.id.detail_picture_reward);
        final TextView detailRewardName = (TextView) findViewById(R.id.detail_name_reward);
        final TextView detailRewardDescription = (TextView) findViewById(R.id.detail_description_reward);
        final TextView detailRewardTrekpoints = (TextView) findViewById(R.id.detail_trekpoints_reward);
        rewardsList = (ListView) findViewById(R.id.rewards_list);
        rewardsDetail = findViewById(R.id.rewards_detail);
        rewardsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                final Reward reward = (Reward) parent.getItemAtPosition(position);
                detailRewardName.setText(reward.name);
                detailRewardDescription.setText(reward.description);
                String trekpointsText;
                if(reward.trekpoints == null){
                    trekpointsText = "any amount";
                    detailRewardTrekpoints.setCompoundDrawablesWithIntrinsicBounds(null, 
                        null, null, null);
                }else{
                    trekpointsText = reward.trekpoints.toString();
                    detailRewardTrekpoints.setCompoundDrawablesWithIntrinsicBounds(null, 
                        null, getResources().getDrawable(R.drawable.trekpoints_icon_color), 
                        null);
                }
                detailRewardTrekpoints.setText(trekpointsText);
                detailRewardPicture.setBackgroundResource(R.drawable.rewards_picture_bg);
                detailRewardPicture.setImageResource(android.R.color.transparent);
                AsyncTask<Void, Void, Bitmap> pictureTask = new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        Bitmap rs = null;
                        InputStream is = null;
                        try{
                            HTTP http = new HTTP(Request.IMG_HOST + reward.picture);
                            http.connect();
                            is = http.getInputStream();
                            rs = BitmapFactory.decodeStream(is);
                        }catch(Exception e){
                        }finally{
                            IOUtils.closeQuietly(is);
                        }
                        return rs;
                    }
                    protected void onPostExecute(final Bitmap rs) {
                        if(rs != null){
                            detailRewardPicture.setBackgroundResource(R.drawable.rewards_picture_bg_loaded);
                            detailRewardPicture.setImageBitmap(rs);
                        }
                    }
                };
                Misc.parallelExecute(pictureTask);
                rewardsList.setVisibility(View.GONE);
                rewardsDetail.setVisibility(View.VISIBLE);
                Animation fadeAnimation = AnimationUtils.loadAnimation(DashboardActivity.this, android.R.anim.fade_in);
                rewardsDetail.startAnimation(fadeAnimation);
            }
        });
        final ArrayAdapter<Reward> rewardsAdapter = new ArrayAdapter<Reward>(this, R.layout.rewards_list_item,
                R.id.name_reward){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                final Reward reward = getItem(position);
                TextView nameView = (TextView)view.findViewById(R.id.name_reward);
                Font.setTypeface(boldFont, nameView);
                nameView.setText(reward.name);
                String trekpointsText;
                TextView trekpointsView = (TextView)view.findViewById(R.id.trekpoints_reward);
                Font.setTypeface(lightFont, trekpointsView);
                if(reward.trekpoints == null){
                    trekpointsText = "any amount";
                    trekpointsView.setCompoundDrawablesWithIntrinsicBounds(null, 
                        null, null, null);
                }else{
                    trekpointsText = reward.trekpoints.toString();
                    trekpointsView.setCompoundDrawablesWithIntrinsicBounds(null, 
                        null, getResources().getDrawable(R.drawable.trekpoints_icon_color), 
                        null);
                }
                trekpointsView.setText(trekpointsText);
                final ImageView pictureView = (ImageView) view.findViewById(R.id.picture_reward);
                AsyncTask<Void, Void, Bitmap> pictureTask = new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        Bitmap rs = null;
                        InputStream is = null;
                        try{
                            HTTP http = new HTTP(Request.IMG_HOST + reward.picture);
                            http.connect();
                            is = http.getInputStream();
                            rs = BitmapFactory.decodeStream(is);
                        }catch(Exception e){
                        }finally{
                            IOUtils.closeQuietly(is);
                        }
                        return rs;
                    }
                    protected void onPostExecute(final Bitmap rs) {
                        if(rs != null){
                            pictureView.setBackgroundResource(R.drawable.rewards_picture_bg_loaded);
                            pictureView.setImageBitmap(rs);
                        }
                    }
                };
                Misc.parallelExecute(pictureTask);
                return view;
            }
        };
        rewardsList.setAdapter(rewardsAdapter);
        AsyncTask<Void, Void, List<Reward>> rewardsTask = new AsyncTask<Void, Void, List<Reward>>(){
            @Override
            protected List<Reward> doInBackground(Void... params) {
                List<Reward> rewards = Collections.emptyList();
                RewardsFetchRequest req = new RewardsFetchRequest();
                req.invalidateCache(DashboardActivity.this);
                try {
                    rewards = req.execute(DashboardActivity.this);
                }
                catch (Exception e) {
                    ehs.registerException(e);
                }
                return rewards;
            }
            @Override
            protected void onPostExecute(List<Reward> result) {
                if (ehs.hasExceptions()) {
                    ehs.reportExceptions();
                }
                else {
                    findViewById(R.id.rewrads_loading).setVisibility(View.GONE);
                    for (Reward r : result) {
                        rewardsAdapter.add(r);
                    }
                    rewardsList.setVisibility(View.VISIBLE);
                }
            }
        };
        Misc.parallelExecute(rewardsTask);
        final Button redeemButton = (Button) findViewById(R.id.redeem_button);
        redeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Request.REDEEM_URL)));
            }
        });
        Font.setTypeface(boldFont, detailRewardName, detailRewardTrekpoints, redeemButton);
        Font.setTypeface(lightFont, detailRewardDescription);
	}
	
	@Override
	public void onBackPressed() {
	    if(rewardsDetail != null && rewardsDetail.getVisibility() == View.VISIBLE){
	        rewardsDetail.setVisibility(View.GONE);
	        rewardsList.setVisibility(View.VISIBLE);
	        Animation fadeAnimation = AnimationUtils.loadAnimation(DashboardActivity.this, android.R.anim.fade_in);
	        rewardsList.startAnimation(fadeAnimation);
	    }else{
	        super.onBackPressed();
	    }
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
