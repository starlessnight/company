package com.metropia.activities;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.dialogs.FloatingMenuDialog;
import com.metropia.dialogs.ShareDialog;
import com.metropia.models.Reservation;
import com.metropia.models.User;
import com.metropia.requests.AwardsFetchRequest;
import com.metropia.requests.Request;
import com.metropia.requests.RewardsFetchRequest;
import com.metropia.requests.TrekpointFetchRequest;
import com.metropia.requests.ValidatedReservationsFetchRequest;
import com.metropia.requests.AwardsFetchRequest.Award;
import com.metropia.requests.RewardsFetchRequest.Reward;
import com.metropia.requests.TrekpointFetchRequest.Trekpoint;
import com.metropia.ui.menu.MainMenu;
import com.metropia.utils.Cache;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.HTTP;
import com.metropia.utils.Misc;
import com.metropia.activities.R;

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
    
    private List<Award> allAwards;
    
    private Trekpoint trekpoints;

    private ArrayAdapter<Award> awardsAdapter;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        final TextView trekpointsLabel = (TextView) findViewById(R.id.trekpoints_label);
        final User user = User.getCurrentUser(this);
        final int uid = user.getId();
        AsyncTask<Void, Void, Trekpoint> trekpointsTask = new AsyncTask<Void, Void, Trekpoint>() {
            @Override
            protected Trekpoint doInBackground(Void... params) {
                Trekpoint tp = null;
                TrekpointFetchRequest req;
                if(Request.NEW_API){
                    req = new TrekpointFetchRequest(user);
                }else{
                    req = new TrekpointFetchRequest(uid);
                }
                req.invalidateCache(DashboardActivity.this);
                try {
                    tp = req.execute(DashboardActivity.this);
                }
                catch (Exception e) {
                    ehs.registerException(e);
                }
                return tp;
            }
            @Override
            protected void onPostExecute(Trekpoint tp) {
                if (ehs.hasExceptions()) {
                    ehs.reportExceptions();
                }
                else {
                    trekpoints = tp;
                    DecimalFormat fmt = new DecimalFormat("#,###");
                    trekpointsLabel.setText(fmt.format(trekpoints == null?0:trekpoints.credit));
                }
            }
        };
        final TextView validateTripsUpdateCnt = (TextView) findViewById(R.id.validated_trips_update_count);
        final View contentLoading = findViewById(R.id.content_loading);
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
        final int validatedTripsCount = MapDisplayActivity.getValidatedTripsCount(this);
        AsyncTask<Void, Void, List<Reservation>> validateTripsCntTask = new AsyncTask<Void, Void, List<Reservation>>() {
            @Override
            protected List<Reservation> doInBackground(Void... params) {
                List<Reservation> reservations = Collections.emptyList();
                ValidatedReservationsFetchRequest req;
                if(Request.NEW_API){
                    req = new ValidatedReservationsFetchRequest(user);
                }else{
                    req = new ValidatedReservationsFetchRequest(uid);
                }
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
            protected void onPostExecute(List<Reservation> reservations) {
                if (ehs.hasExceptions()) {
                    ehs.reportExceptions();
                }
                else {
                    if(validatedTripsContent != null && validatedTripsContent.getVisibility() == View.VISIBLE){
                        contentLoading.setVisibility(View.GONE);
                    }
                    validatedTripsAdapter.clear();
                    for (Reservation r : reservations) {
                        validatedTripsAdapter.add(r);
                    }
                    int updateCnt = reservations.size() - validatedTripsCount;
                    if(updateCnt > 0 && validatedTripsContent != null 
                            && validatedTripsContent.getVisibility() != View.VISIBLE){
                        validateTripsUpdateCnt.setText(String.valueOf(updateCnt));
                        validateTripsUpdateCnt.setVisibility(View.VISIBLE);
                    }
                }
            }
        };
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
                    trekpointsText = reward.trekpoints + " trekpoint" + (reward.trekpoints > 1?"s":"");
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
                        String url = (Request.NEW_API?"":Request.IMG_HOST) + reward.picture;
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
                Misc.fadeIn(DashboardActivity.this, rewardsDetail);
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
                        null, getResources().getDrawable(R.drawable.trekpoints_icon_color_offset), 
                        null);
                }
                trekpointsView.setText(trekpointsText);
                final ImageView pictureView = (ImageView) view.findViewById(R.id.picture_reward);
                final String url = (Request.NEW_API?"":Request.IMG_HOST) + reward.picture;
                if(!url.equals(pictureView.getTag())){
                    pictureView.setTag(url);
                    pictureView.setBackgroundResource(R.drawable.rewards_picture_bg);
                    pictureView.setImageResource(android.R.color.transparent);
                    AsyncTask<Void, Void, Bitmap> pictureTask = new AsyncTask<Void, Void, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(Void... params) {
                            Bitmap rs = null;
                            InputStream is = null;
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
                    @SuppressWarnings("unchecked")
                    AsyncTask<Void, Void, Bitmap> lastTask = (AsyncTask<Void, Void, Bitmap>) view.getTag(R.id.picture_reward);
                    if(lastTask != null){
                        lastTask.cancel(true);
                    }
                    view.setTag(R.id.picture_reward, lastTask);
                    Misc.parallelExecute(pictureTask);
                }
                return view;
            }
        };
        rewardsList.setAdapter(rewardsAdapter);
        final TextView detailValidatedTripsTitle = (TextView) findViewById(R.id.detail_title_validated_trips);
        final TextView detailValidatedTripsDesc = (TextView) findViewById(R.id.detail_description_validated_trips);
        final TextView detailValidatedTripsOrigin = (TextView) findViewById(R.id.detail_origin_validated_trips);
        final TextView detailValidatedTripsDest = (TextView) findViewById(R.id.detail_destination_validated_trips);
        final Button shareValidatedTripsButton = (Button) findViewById(R.id.share_validated_trips_button);
        shareValidatedTripsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reservation res = (Reservation) shareValidatedTripsButton.getTag();
                String text = "Metropia showed me the best route and time to avoid #traffic and to help others;"
                    + " #smartrek also awarded me " + res.getCredits() + " points for doing good.";
                ShareDialog.newInstance("Share Trip", text)
                    .show(getSupportFragmentManager(), null);
            }
        });
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
                shareValidatedTripsButton.setTag(reserv);
                validatedTripsList.setVisibility(View.GONE);
                validatedTripsDetail.setVisibility(View.VISIBLE);
                Misc.fadeIn(DashboardActivity.this, validatedTripsDetail);
            }
        });
        validatedTripsList.setAdapter(validatedTripsAdapter);
        awardsDetail = findViewById(R.id.awards_detail);
        awardsList = (ListView) findViewById(R.id.awards_list);
        final ImageView detailAwardPicture = (ImageView) findViewById(R.id.detail_picture_award);
        final TextView detailAwardName = (TextView) findViewById(R.id.detail_name_award);
        final TextView detailAwardDescription = (TextView) findViewById(R.id.detail_description_award);
        final TextView detailAwardLongDescription = (TextView) findViewById(R.id.detail_long_description_award);
        final TextView detailAwardShareLabel = (TextView) findViewById(R.id.share_label_awards);
        final View detailProgressView = findViewById(R.id.detail_progress_rewards);
        final View detailProgressWrapper = findViewById(R.id.detail_progress_rewards_wrapper);
        final TextView detailProgressTextView = (TextView) findViewById(R.id.detail_progress_text);
        final Button shareAwardsButton = (Button) findViewById(R.id.share_awards_button);
        shareAwardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Award award = (Award) shareAwardsButton.getTag();
                String text = "I earned my " + award.name + " badge for using"
                    + " #smartrek routes and times to avoid #traffic and help reduce congestion!";
                ShareDialog.newInstance("Share Award", text)
                    .show(getSupportFragmentManager(), null);
            }
        });
        awardsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                final Award award = (Award) parent.getItemAtPosition(position);
                detailAwardName.setText(award.name);
                detailAwardDescription.setText(award.task);
                detailAwardLongDescription.setText(award.description);
                detailAwardPicture.setImageResource(android.R.color.transparent);
                ((BitmapDrawable)detailProgressWrapper.getBackground()).setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
                ClipDrawable background = (ClipDrawable) detailProgressView.getBackground();
                background.setLevel(Double.valueOf((100 - award.percent) * 100).intValue());
                detailProgressTextView.setText(award.percent + "%");
                shareAwardsButton.setTag(award);
                shareAwardsButton.setEnabled(award.complete);
                int shareHintResId = award.getShareHintResId();
                if(shareHintResId == 0){
                    detailAwardShareLabel.setVisibility(View.GONE);
                }else{
                    detailAwardShareLabel.setText(getString(shareHintResId));
                    detailAwardShareLabel.setVisibility(View.VISIBLE);
                }
                AsyncTask<Void, Void, Bitmap> pictureTask = new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        Bitmap rs = null;
                        InputStream is = null;
                        String url = (Request.NEW_API?"":Request.IMG_HOST) + award.picture;
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
                            detailAwardPicture.setImageBitmap(rs);
                        }
                    }
                };
                Misc.parallelExecute(pictureTask);
                awardsList.setVisibility(View.GONE);
                awardsDetail.setVisibility(View.VISIBLE);
                Misc.fadeIn(DashboardActivity.this, awardsDetail);
            }
        });
        awardsAdapter = new ArrayAdapter<Award>(this, R.layout.awards_list_item, R.id.name_award){
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
                    View progressView = view.findViewById(R.id.progress_rewards);
                    View progressWrapper = view.findViewById(R.id.progress_rewards_wrapper);
                    TextView progressTextView = (TextView) view.findViewById(R.id.progress_text);
                    ((BitmapDrawable)progressWrapper.getBackground()).setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
                    ClipDrawable background = (ClipDrawable) progressView.getBackground();
                    background.setLevel(Double.valueOf((100 - award.percent) * 100).intValue());
                    progressTextView.setText(award.percent + "%");
                    view.findViewById(R.id.separator_awards).setVisibility(award.hideSeparator?
                        View.GONE:View.VISIBLE);
                    Font.setTypeface(boldFont, headerView, nameView, progressTextView);
                    final ImageView pictureView = (ImageView) view.findViewById(R.id.picture_award);
                    final String url = (Request.NEW_API?"":Request.IMG_HOST) + award.picture.replaceAll(" ", "%20");
                    if(!url.equals(pictureView.getTag())){
                        pictureView.setTag(url);
                        pictureView.setImageResource(android.R.color.transparent);
                        AsyncTask<Void, Void, Bitmap> pictureTask = new AsyncTask<Void, Void, Bitmap>() {
                            @Override
                            protected Bitmap doInBackground(Void... params) {
                                Bitmap rs = null;
                                InputStream is = null;
                                
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
                        @SuppressWarnings("unchecked")
                        AsyncTask<Void, Void, Bitmap> lastTask = (AsyncTask<Void, Void, Bitmap>) view.getTag(R.id.picture_award);
                        if(lastTask != null){
                            lastTask.cancel(true);
                        }
                        view.setTag(R.id.picture_award, pictureTask);
                        Misc.parallelExecute(pictureTask);
                    }
                    return view;
                }
        };
        awardsList.setAdapter(awardsAdapter);
        final View rewardsTab = findViewById(R.id.rewards_tab);
        final View validatedTripsTab = findViewById(R.id.validated_trips_tab);
        final View awardsTab = findViewById(R.id.awards_tab);
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
                    Misc.fadeIn(DashboardActivity.this, rewardsContent);
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
                    Misc.fadeIn(DashboardActivity.this, validatedTripsContent);
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
                            ValidatedReservationsFetchRequest req;
                            if(Request.NEW_API){
                                req = new ValidatedReservationsFetchRequest(user);
                            }else{
                                req = new ValidatedReservationsFetchRequest(uid);
                            }
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
                    Misc.fadeIn(DashboardActivity.this, awardsContent);
                    AsyncTask<Void, Void, List<Award>> allAwardsTask = new AsyncTask<Void, Void, List<Award>>(){
                        @Override
                        protected void onPreExecute() {
                            if(allAwards == null || allAwards.isEmpty()){
                                contentLoading.setVisibility(View.VISIBLE);
                            }else{
                                contentLoading.setVisibility(View.GONE);
                            }
                        }
                        @Override
                        protected List<Award> doInBackground(Void... params) {
                            List<Award> awards = Collections.emptyList();
                            AwardsFetchRequest req;
                            if(Request.NEW_API){
                                req = new AwardsFetchRequest(user);
                            }else{
                                req = new AwardsFetchRequest(uid);
                            }
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
                                if(allAwards != null){
                                    if(awardsContent != null && awardsContent.getVisibility() == View.VISIBLE){
                                        contentLoading.setVisibility(View.GONE);
                                    }
                                    awardsAdapter.clear();
                                    for(int i=0; i<allAwards.size(); i++){
                                        Award a = allAwards.get(i);
                                        if(a.complete && i == 0){
                                            a.headerLabel = "Completed Awards";
                                        }else if(!a.complete && i == 0 || !a.complete && allAwards.get(i - 1).complete){
                                            a.headerLabel = "In Progress";
                                        }
                                        a.hideSeparator = i + 1 < allAwards.size() && a.complete
                                            && !allAwards.get(i + 1).complete;
                                        awardsAdapter.add(a);
                                    }
                                    if(awardsDetail != null && awardsDetail.getVisibility() != View.VISIBLE){
                                        awardsList.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }
                    };
                    Misc.parallelExecute(allAwardsTask);
                }
            }
        });
        Misc.parallelExecute(trekpointsTask);
        Misc.parallelExecute(validateTripsCntTask);
        rewardsTab.performClick();
        
        findViewById(R.id.floating_menu_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FloatingMenuDialog dialog = new FloatingMenuDialog(DashboardActivity.this);
                dialog.show();
            }
        });
        
        Font.setTypeface(boldFont, trekpointsLabel, validateTripsUpdateCnt,
            detailRewardName, detailAwardName, detailProgressTextView, detailRewardTrekpoints, 
            detailValidatedTripsTitle, redeemButton, shareValidatedTripsButton, shareAwardsButton);
        Font.setTypeface(lightFont, detailRewardDescription, detailAwardDescription,
            detailAwardLongDescription, detailValidatedTripsDesc, 
            detailValidatedTripsOrigin, detailValidatedTripsDest,
            (TextView) findViewById(R.id.share_label_validated_trips),
            detailAwardShareLabel);
        
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	@Override
	public void onBackPressed() {
	    if(rewardsContent != null && rewardsContent.getVisibility() == View.VISIBLE 
	            && rewardsDetail != null && rewardsDetail.getVisibility() == View.VISIBLE){
	        rewardsDetail.setVisibility(View.GONE);
	        rewardsList.setVisibility(View.VISIBLE);
	        Misc.fadeIn(this, rewardsList);
	    }else if(validatedTripsContent != null && validatedTripsContent.getVisibility() == View.VISIBLE 
	                && validatedTripsDetail != null && validatedTripsDetail.getVisibility() == View.VISIBLE){
            validatedTripsDetail.setVisibility(View.GONE);
            validatedTripsList.setVisibility(View.VISIBLE);
            Misc.fadeIn(this, validatedTripsList);
	    }else if(awardsContent != null && awardsContent.getVisibility() == View.VISIBLE 
                && awardsDetail != null && awardsDetail.getVisibility() == View.VISIBLE){
	        awardsDetail.setVisibility(View.GONE);
	        awardsList.setVisibility(View.VISIBLE);
	        Misc.fadeIn(this, awardsList);
	    }else{
	        super.onBackPressed();
	    }
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
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater mi = getSupportMenuInflater();
        //mi.inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        MainMenu.onMenuItemSelected(this, featureId, item);
        
        return super.onMenuItemSelected(featureId, item);
    }
	
}