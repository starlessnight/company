package com.smartrek.activities;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ClipDrawable;
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
import com.smartrek.models.Reservation;
import com.smartrek.models.User;
import com.smartrek.requests.AwardsFetchRequest;
import com.smartrek.requests.AwardsFetchRequest.Award;
import com.smartrek.requests.MyAwardsFetchRequest;
import com.smartrek.requests.Request;
import com.smartrek.requests.RewardsFetchRequest;
import com.smartrek.requests.RewardsFetchRequest.Reward;
import com.smartrek.requests.TrekpointFetchRequest;
import com.smartrek.requests.ValidatedReservationsFetchRequest;
import com.smartrek.ui.menu.MainMenu;
import com.smartrek.utils.Cache;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;
import com.smartrek.utils.HTTP;
import com.smartrek.utils.Misc;

public final class DashboardActivity extends ActionBarActivity {
    
    private ExceptionHandlingService ehs = new ExceptionHandlingService(this);

    private View rewardsContent;

    private View validatedTripsContent;

    private View awardsContent;
    
    private ListView rewardsList;
    
    private View rewardsDetail;
    
    private ListView validatedTripsList;
    
    private View validatedTripsDetail;
    
    private View awardsDetail;
    
    private ListView awardsList;
    
    private List<Award> allAwards = Collections.emptyList();
    
    private int awardTripsCount;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        final TextView trekpointsLabel = (TextView) findViewById(R.id.trekpoints_label);
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
        final TextView validateTripsUpdateCnt = (TextView) findViewById(R.id.validated_trips_update_count);
        final int validatedTripsCount = MapDisplayActivity.getValidatedTripsCount(this);
        AsyncTask<Void, Void, Integer> validateTripsCntTask = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                Integer cnt = 0;
                ValidatedReservationsFetchRequest req = new ValidatedReservationsFetchRequest(uid);
                req.invalidateCache(DashboardActivity.this);
                try {
                    cnt = req.execute(DashboardActivity.this).size();
                }
                catch (Exception e) {
                    ehs.registerException(e);
                }
                return cnt;
            }
            @Override
            protected void onPostExecute(Integer cnt) {
                if (ehs.hasExceptions()) {
                    ehs.reportExceptions();
                }
                else {
                    awardTripsCount = cnt;
                    int updateCnt = cnt - validatedTripsCount;
                    if(updateCnt > 0 && validatedTripsContent != null 
                            && validatedTripsContent.getVisibility() != View.VISIBLE){
                        validateTripsUpdateCnt.setText(String.valueOf(updateCnt));
                        validateTripsUpdateCnt.setVisibility(View.VISIBLE);
                    }
                }
            }
        };
        validateTripsCntTask.execute();
        final ImageView detailRewardPicture = (ImageView) findViewById(R.id.detail_picture_reward);
        final TextView detailRewardName = (TextView) findViewById(R.id.detail_name_reward);
        final TextView detailRewardDescription = (TextView) findViewById(R.id.detail_description_reward);
        final TextView detailRewardTrekpoints = (TextView) findViewById(R.id.detail_trekpoints_reward);
        final Button redeemButton = (Button) findViewById(R.id.redeem_button);
        redeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Request.REDEEM_URL)));
            }
        });
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
                        String url = Request.IMG_HOST + reward.picture;
                        Cache cache = Cache.getInstance(DashboardActivity.this);
                        try{
                            InputStream cachedStream = cache.fetchStream(url);
                            if(cachedStream == null){
                                HTTP http = new HTTP(url);
                                http.connect();
                                InputStream tmpStream = http.getInputStream();
                                try{
                                    cache.put(url, tmpStream);
                                    is = cache.fetchStream(url);
                                }finally{
                                    IOUtils.closeQuietly(tmpStream);
                                }
                            }else{
                                is = cachedStream;
                            }
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
                fadeIn(rewardsDetail);
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
                        String url = Request.IMG_HOST + reward.picture;
                        Cache cache = Cache.getInstance(DashboardActivity.this);
                        try{
                            InputStream cachedStream = cache.fetchStream(url);
                            if(cachedStream == null){
                                HTTP http = new HTTP(url);
                                http.connect();
                                InputStream tmpStream = http.getInputStream();
                                try{
                                    cache.put(url, tmpStream);
                                    is = cache.fetchStream(url);
                                }finally{
                                    IOUtils.closeQuietly(tmpStream);
                                }
                            }else{
                                is = cachedStream;
                            }
                            rs = BitmapFactory.decodeStream(is);
                        }catch(Exception e){
                        }finally{
                            IOUtils.closeQuietly(is);
                        }
                        return rs;
                    }
                    protected void onPostExecute(final Bitmap rs) {
                        if(rs != null && pictureView != null){
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
        final TextView detailValidatedTripsTitle = (TextView) findViewById(R.id.detail_title_validated_trips);
        final TextView detailValidatedTripsDesc = (TextView) findViewById(R.id.detail_description_validated_trips);
        final TextView detailValidatedTripsOrigin = (TextView) findViewById(R.id.detail_origin_validated_trips);
        final TextView detailValidatedTripsDest = (TextView) findViewById(R.id.detail_destination_validated_trips);
        final Button shareValidatedTripsButton = (Button) findViewById(R.id.share_validated_trips_button);
        validatedTripsList = (ListView) findViewById(R.id.validated_trips_list);
        validatedTripsDetail = findViewById(R.id.validated_trips_detail);
        validatedTripsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                final Reservation reserv = (Reservation) parent.getItemAtPosition(position);
                detailValidatedTripsTitle.setText(getReservationTitle(reserv));
                detailValidatedTripsDesc.setText(getReservationDescription(reserv));
                detailValidatedTripsOrigin.setText(reserv.getOriginAddress());
                detailValidatedTripsDest.setText(reserv.getDestinationAddress());
                validatedTripsList.setVisibility(View.GONE);
                validatedTripsDetail.setVisibility(View.VISIBLE);
                fadeIn(validatedTripsDetail);
            }
        });
        final ArrayAdapter<Reservation> validatedTripsAdapter = new ArrayAdapter<Reservation>(this,
                R.layout.validated_trips_list_item, R.id.title_validated_trips){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                final Reservation reserv = getItem(position);
                TextView titleView = (TextView)view.findViewById(R.id.title_validated_trips);
                Font.setTypeface(boldFont, titleView);
                titleView.setText(getReservationTitle(reserv));
                TextView descView = (TextView)view.findViewById(R.id.description_validated_trips);
                Font.setTypeface(lightFont, descView);
                descView.setText(getReservationDescription(reserv));
                return view;
            }
        };
        validatedTripsList.setAdapter(validatedTripsAdapter);
        awardsDetail = findViewById(R.id.awards_detail);
        awardsList = (ListView) findViewById(R.id.awards_list);
        final ArrayAdapter<Award> awardsAdapter = new ArrayAdapter<Award>(this, R.layout.awards_list_item, R.id.name_award){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    final Award award = getItem(position);
                    TextView headerView = (TextView) view.findViewById(R.id.header_awards);
                    boolean hasSeparator = award.headerLabel != null;
                    if(hasSeparator){
                        headerView.setText(award.headerLabel);
                    }
                    headerView.setVisibility(hasSeparator?View.VISIBLE:View.GONE);
                    TextView nameView = (TextView) view.findViewById(R.id.name_award);
                    nameView.setText(award.name);
                    TextView descView = (TextView)view.findViewById(R.id.description_award);
                    View progressView = view.findViewById(R.id.progress_rewards);
                    boolean isTripsType = "trips".equals(award.type);
                    if(isTripsType){
                        descView.setText("complete " + award.threshold + " trips");
                        double percentage = Math.min((double) awardTripsCount / award.threshold, 1);
                        ((ClipDrawable)progressView.getBackground()).setLevel(
                            Double.valueOf(percentage * 10000).intValue());
                    }
                    descView.setVisibility(isTripsType?View.VISIBLE:View.GONE);
                    view.findViewById(R.id.progress_rewards_wrapper)
                        .setVisibility(isTripsType?View.VISIBLE:View.GONE);
                    view.findViewById(R.id.separator_awards).setVisibility(award.hideSeparator?
                        View.GONE:View.VISIBLE);
                    Font.setTypeface(boldFont, headerView, nameView);
                    Font.setTypeface(lightFont, descView);
                    final ImageView pictureView = (ImageView) view.findViewById(R.id.picture_award);
                    AsyncTask<Void, Void, Bitmap> pictureTask = new AsyncTask<Void, Void, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(Void... params) {
                            Bitmap rs = null;
                            InputStream is = null;
                            String url = Request.IMG_HOST + award.picture.replaceAll(" ", "%20");
                            Cache cache = Cache.getInstance(DashboardActivity.this);
                            try{
                                InputStream cachedStream = cache.fetchStream(url);
                                if(cachedStream == null){
                                    HTTP http = new HTTP(url);
                                    http.connect();
                                    InputStream tmpStream = http.getInputStream();
                                    try{
                                        cache.put(url, tmpStream);
                                        is = cache.fetchStream(url);
                                    }finally{
                                        IOUtils.closeQuietly(tmpStream);
                                    }
                                }else{
                                    is = cachedStream;
                                }
                                rs = BitmapFactory.decodeStream(is);
                            }catch(Exception e){
                            }finally{
                                IOUtils.closeQuietly(is);
                            }
                            return rs;
                        }
                        protected void onPostExecute(final Bitmap rs) {
                            if(rs != null && pictureView != null){
                                pictureView.setImageBitmap(rs);
                            }
                        }
                    };
                    Misc.parallelExecute(pictureTask);
                    return view;
                }
        };
        awardsList.setAdapter(awardsAdapter);
        final View rewardsTab = findViewById(R.id.rewards_tab);
        final View validatedTripsTab = findViewById(R.id.validated_trips_tab);
        final View awardsTab = findViewById(R.id.awards_tab);
        final View contentLoading = findViewById(R.id.content_loading);
        rewardsContent = findViewById(R.id.rewards_content);
        validatedTripsContent = findViewById(R.id.validated_trips_content);
        awardsContent = findViewById(R.id.awards_content);
        rewardsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!rewardsTab.isSelected()){
                    validatedTripsTab.setSelected(false);
                    awardsTab.setSelected(false);
                    rewardsTab.setSelected(true);
                    validatedTripsContent.setVisibility(View.GONE);
                    awardsContent.setVisibility(View.GONE);
                    rewardsDetail.setVisibility(View.GONE);
                    rewardsList.setVisibility(View.VISIBLE);
                    rewardsContent.setVisibility(View.VISIBLE);
                    fadeIn(rewardsContent);
                    AsyncTask<Void, Void, List<Reward>> rewardsTask = new AsyncTask<Void, Void, List<Reward>>(){
                        @Override
                        protected void onPreExecute() {
                            if(rewardsAdapter.isEmpty()){
                                contentLoading.setVisibility(View.VISIBLE);
                            }else{
                                contentLoading.setVisibility(View.GONE);
                            }
                        }
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
                                if(rewardsContent != null && rewardsContent.getVisibility() == View.VISIBLE){
                                    contentLoading.setVisibility(View.GONE);
                                }
                                rewardsAdapter.clear();
                                for (Reward r : result) {
                                    rewardsAdapter.add(r);
                                }
                                if(rewardsDetail != null && rewardsDetail.getVisibility() != View.VISIBLE){
                                    rewardsList.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    };
                    Misc.parallelExecute(rewardsTask);
                }
            }
        });
        validatedTripsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validatedTripsTab.isSelected()){
                    rewardsTab.setSelected(false);
                    awardsTab.setSelected(false);
                    validatedTripsTab.setSelected(true);
                    rewardsContent.setVisibility(View.GONE);
                    awardsContent.setVisibility(View.GONE);
                    validatedTripsDetail.setVisibility(View.GONE);
                    validatedTripsList.setVisibility(View.VISIBLE);
                    validatedTripsContent.setVisibility(View.VISIBLE);
                    fadeIn(validatedTripsContent);
                    AsyncTask<Void, Void, List<Reservation>> validatedTripsTask = new AsyncTask<Void, Void, List<Reservation>>(){
                        @Override
                        protected void onPreExecute() {
                            if(validatedTripsAdapter.isEmpty()){
                                contentLoading.setVisibility(View.VISIBLE);
                            }else{
                                contentLoading.setVisibility(View.GONE);
                            }
                        }
                        @Override
                        protected List<Reservation> doInBackground(Void... params) {
                            List<Reservation> reservations = Collections.emptyList();
                            ValidatedReservationsFetchRequest req = new ValidatedReservationsFetchRequest(uid);
                            req.invalidateCache(DashboardActivity.this);
                            try {
                                reservations = req.execute(DashboardActivity.this);
                            }
                            catch (Exception e) {
                                ehs.registerException(e);
                            }
                            Collections.sort(reservations, Collections.reverseOrder(
                                Reservation.orderByDepartureTime()));
                            return reservations;
                        }
                        @Override
                        protected void onPostExecute(List<Reservation> result) {
                            if (ehs.hasExceptions()) {
                                ehs.reportExceptions();
                            }
                            else {
                                if(validatedTripsContent != null && validatedTripsContent.getVisibility() == View.VISIBLE){
                                    contentLoading.setVisibility(View.GONE);
                                }
                                validatedTripsAdapter.clear();
                                for (Reservation r : result) {
                                    validatedTripsAdapter.add(r);
                                }
                                if(validatedTripsDetail != null && validatedTripsDetail.getVisibility() != View.VISIBLE){
                                    validatedTripsList.setVisibility(View.VISIBLE);
                                }
                                MapDisplayActivity.setValidatedTripsCount(DashboardActivity.this, result.size());
                            }
                        }
                    };
                    Misc.parallelExecute(validatedTripsTask);
                    validateTripsUpdateCnt.setVisibility(View.GONE);
                }
            }
        });
        awardsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!awardsContent.isSelected()){
                    rewardsTab.setSelected(false);
                    validatedTripsTab.setSelected(false);
                    awardsTab.setSelected(true);
                    rewardsContent.setVisibility(View.GONE);
                    validatedTripsContent.setVisibility(View.GONE);
                    awardsDetail.setVisibility(View.GONE);
                    awardsList.setVisibility(View.VISIBLE);
                    awardsContent.setVisibility(View.VISIBLE);
                    fadeIn(awardsContent);
                    new AsyncTask<Void, Void, List<Award>>(){
                        @Override
                        protected void onPreExecute() {
                            if(allAwards.isEmpty()){
                                contentLoading.setVisibility(View.VISIBLE);
                            }else{
                                contentLoading.setVisibility(View.GONE);
                            }
                        }
                        @Override
                        protected List<Award> doInBackground(Void... params) {
                            List<Award> awards = Collections.emptyList();
                            AwardsFetchRequest req = new AwardsFetchRequest();
                            req.invalidateCache(DashboardActivity.this);
                            try {
                                awards = req.execute(DashboardActivity.this);
                            }
                            catch (Exception e) {
                                ehs.registerException(e);
                            }
                            return awards;
                        }
                        @Override
                        protected void onPostExecute(List<Award> result) {
                            if (ehs.hasExceptions()) {
                                ehs.reportExceptions();
                            }
                            else {
                                allAwards = result;
                            }
                        }
                    }.execute();
                    new AsyncTask<Void, Void, List<String>>(){
                        @Override
                        protected List<String> doInBackground(Void... params) {
                            List<String> awards = Collections.emptyList();
                            MyAwardsFetchRequest req = new MyAwardsFetchRequest(uid);
                            req.invalidateCache(DashboardActivity.this);
                            try {
                                awards = req.execute(DashboardActivity.this);
                            }
                            catch (Exception e) {
                                ehs.registerException(e);
                            }
                            return awards;
                        }
                        protected void onPostExecute(java.util.List<String> result) {
                            if (ehs.hasExceptions()) {
                                ehs.reportExceptions();
                            }
                            else {
                              if(awardsContent != null && awardsContent.getVisibility() == View.VISIBLE){
                                  contentLoading.setVisibility(View.GONE);
                              }
                              List<Award> completedList = new ArrayList<Award>();
                              List<Award> inProgressList = new ArrayList<Award>();
                              for (Award a : allAwards) {
                                  if(result.contains(a.name)){
                                      completedList.add(a);
                                  }else{
                                      inProgressList.add(a);
                                  }
                              }
                              awardsAdapter.clear();
                              for(int i=0; i<completedList.size(); i++){
                                  Award a = completedList.get(i);
                                  if(i == 0){
                                      a.headerLabel = "completed awards";
                                  }
                                  if(i + 1 == completedList.size()){
                                      a.hideSeparator = true;
                                  }
                                  awardsAdapter.add(a);
                              }
                              for(int i=0; i<inProgressList.size(); i++){
                                  Award a = inProgressList.get(i);
                                  if(i == 0){
                                      a.headerLabel = "in progress";
                                  }
                                  awardsAdapter.add(a);
                              }
                              if(awardsDetail != null && awardsDetail.getVisibility() != View.VISIBLE){
                                  awardsList.setVisibility(View.VISIBLE);
                              }
                            }
                        }
                    }.execute();
                }
            }
        });
        rewardsTab.performClick();
        Font.setTypeface(boldFont, trekpointsLabel, validateTripsUpdateCnt,
            detailRewardName, detailRewardTrekpoints, detailValidatedTripsTitle, 
            redeemButton, shareValidatedTripsButton);
        Font.setTypeface(lightFont, detailRewardDescription, detailValidatedTripsDesc,
            detailValidatedTripsOrigin, detailValidatedTripsDest, 
            (TextView) findViewById(R.id.share_label));
	}
	
	@Override
	public void onBackPressed() {
	    if(rewardsContent != null && rewardsContent.getVisibility() == View.VISIBLE 
	            && rewardsDetail != null && rewardsDetail.getVisibility() == View.VISIBLE){
	        rewardsDetail.setVisibility(View.GONE);
	        rewardsList.setVisibility(View.VISIBLE);
	        fadeIn(rewardsList);
	    }else if(validatedTripsContent != null && validatedTripsContent.getVisibility() == View.VISIBLE 
	                && validatedTripsDetail != null && validatedTripsDetail.getVisibility() == View.VISIBLE){
            validatedTripsDetail.setVisibility(View.GONE);
            validatedTripsList.setVisibility(View.VISIBLE);
            fadeIn(validatedTripsList);
	    }else{
	        super.onBackPressed();
	    }
	}
	
	private void fadeIn(View v){
	    Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        v.startAnimation(anim);
	}
	
	private static String getReservationTitle(Reservation r){
	    return r.getOriginName() + " to " + r.getDestinationName();
	}
	
	private static String getReservationDescription(Reservation r){
        return r.getFormattedDepartureTime() + ", "  + r.getCredits() + " trekpoints";
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
