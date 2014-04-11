package com.smartrek.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.smartrek.requests.ServiceDiscoveryRequest.Result;
import com.smartrek.utils.Cache;

public final class DebugOptionsActivity extends Activity {
    
    /**
     * Name of the shared preference file
     */
    public static final String DEBUG_PREFS = "debug_prefs";
    
    public static final String DEBUG_MODE = "DebugMode";
    public static final String GPS_MODE = "GPSMode";
    
    public static final int GPS_MODE_REAL = 1;
    public static final int GPS_MODE_PRERECORDED = 2;
    public static final int GPS_MODE_PRERECORDED_LA = 3;
    public static final int GPS_MODE_PRERECORDED_LA2 = 5;
    public static final int GPS_MODE_PRERECORDED_LA3 = 6;
    public static final int GPS_MODE_PRERECORDED_LA4 = 7;
    public static final int GPS_MODE_LONG_PRESS = 4;
    public static final int GPS_MODE_DEFAULT = GPS_MODE_REAL;
    
    public static final String GPS_UPDATE_INTERVAL = "GPS_UPDATE_INTERVAL";
    
    private static final String CURRENT_LOCATION = "CURRENT_LOCATION";
    
    private static final String ENTRYPOINT = "ENTRYPOINT";
    
    public static final String GOOGLE_GEOCODING_PATCHED = "GOOGLE_GEOCODING_PATCHED";
    
    private static final int GOOGLE_GEOCODING_PATCH_NO = 1;
    
    private static final String LAST_USER_LAT_LON = "LAST_USER_LAT_LON";
    
    private static final String LAST_USER_LAT_SENT = "LAST_USER_LAT_SENT";
    
    private static final String EULA_ETAG = "EULA_ETAG";
    
    private static final String fakeRoutes = "fakeRouteIds";
    
    private static final int fakeRouteSize = 10;
    
    private static final String navLinks = "navLinks";
    
    private static final int navLinksSize = 100;
    
    private static final String recentAddresses = "recentAddresses";
    
    private static final int recentAddressesSize = 30;
    
    private static final String osmdroidCacheDir = "osmdroid";
    
    private static final int defaultUpdateInterval = 1000;
    
    private static final String REROUTING_NOTIFICATION_SOUND = "REROUTING_NOTIFICATION_SOUND";
    
    private static final String REROUTING_DEBUG_MSG = "REROUTING_DEBUG_MSG";
    
    private SharedPreferences prefs;
    
    private RadioButton radioRealGPS;
    private RadioButton radioPrerecordedGPS;
    private RadioButton radioPrerecordedGPS_LA;
    private RadioButton radioPrerecordedGPS_LA2;
    private RadioButton radioPrerecordedGPS_LA3;
    private RadioButton radioPrerecordedGPS_LA4;
    private RadioButton radioLongPress;
    
    private Button buttonClearCache;
    private Button buttonCrash;
    
    private AsyncTask<Void, Void, Result> initApiLinksTask;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_options);
        
        prefs = getSharedPreferences(DEBUG_PREFS, MODE_PRIVATE);
        
        radioRealGPS = (RadioButton) findViewById(R.id.radio_real_gps);
        radioPrerecordedGPS = (RadioButton) findViewById(R.id.radio_prerecorded_gps);
        radioPrerecordedGPS_LA = (RadioButton) findViewById(R.id.radio_prerecorded_gps_la);
        radioPrerecordedGPS_LA2 = (RadioButton) findViewById(R.id.radio_prerecorded_gps_la_2);
        radioPrerecordedGPS_LA3 = (RadioButton) findViewById(R.id.radio_prerecorded_gps_la_3);
        radioPrerecordedGPS_LA4 = (RadioButton) findViewById(R.id.radio_prerecorded_gps_la_4);
        radioLongPress = (RadioButton) findViewById(R.id.radio_long_press);
        
        radioRealGPS.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(GPS_MODE, GPS_MODE_REAL);
                editor.commit();
            }
        });
        
        radioPrerecordedGPS.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(GPS_MODE, GPS_MODE_PRERECORDED);
                editor.commit();
            }
        });
        
        radioPrerecordedGPS_LA.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(GPS_MODE, GPS_MODE_PRERECORDED_LA);
                editor.commit();
            }
        });
        
        radioPrerecordedGPS_LA2.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(GPS_MODE, GPS_MODE_PRERECORDED_LA2);
                editor.commit();
            }
        });
        
        radioPrerecordedGPS_LA3.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(GPS_MODE, GPS_MODE_PRERECORDED_LA3);
                editor.commit();
            }
        });
        
        radioPrerecordedGPS_LA4.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(GPS_MODE, GPS_MODE_PRERECORDED_LA4);
                editor.commit();
            }
        });
        
        radioLongPress.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(GPS_MODE, GPS_MODE_LONG_PRESS);
                editor.commit();
            }
        });
        
        buttonClearCache = (Button) findViewById(R.id.button_clear_cache);
        buttonCrash = (Button) findViewById(R.id.button_crash);
        
        buttonClearCache.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {
                    
                    ProgressDialog dialog;
                    
                    protected void onPreExecute() {
                        dialog = new ProgressDialog(DebugOptionsActivity.this);
                        dialog.setMessage("Clearing cache...");
                        dialog.setIndeterminate(true);
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    }
                    @Override
                    protected Void doInBackground(Void... params) {
                        Cache.getInstance(DebugOptionsActivity.this).clear();
                        FileUtils.deleteQuietly(new File(
                            Environment.getExternalStorageDirectory(), osmdroidCacheDir));
                        return null;
                    }
                    protected void onPostExecute(Void result) {
                        dialog.cancel();
                        Toast toast = Toast.makeText(
                                DebugOptionsActivity.this,
                                "Cache has been cleared.",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }.execute();
            }

        });
        
        buttonCrash.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ((String) null).length();
            }
            
        });
        
        EditText updateIntervalView = (EditText) findViewById(R.id.update_interval);
        updateIntervalView.setText(String.valueOf(prefs.getInt(GPS_UPDATE_INTERVAL, defaultUpdateInterval)));
        updateIntervalView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    prefs.edit()
                        .putInt(GPS_UPDATE_INTERVAL, Integer.parseInt(s.toString()))
                        .commit();
                }
            }
        });
        
        EditText curLocView = (EditText) findViewById(R.id.current_location);
        curLocView.setText(String.valueOf(prefs.getString(CURRENT_LOCATION, "")));
        curLocView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                prefs.edit()
                    .putString(CURRENT_LOCATION, s.toString())
                    .commit();
            }
        });
        
        EditText entrypointView = (EditText) findViewById(R.id.entry_point);
        entrypointView.setText(String.valueOf(prefs.getString(ENTRYPOINT, "")));
        entrypointView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                String entrypoint = s.toString();
                prefs.edit()
                    .putString(ENTRYPOINT, entrypoint)
                    .commit();
                if(initApiLinksTask != null){
                    initApiLinksTask.cancel(true);
                }
                initApiLinksTask = MainActivity.initApiLinks(DebugOptionsActivity.this, entrypoint, 
                    null, null);
            }
        });
        
        CheckBox reroutingNotificationSound = (CheckBox) findViewById(R.id.rerouting_notification_sound);
        reroutingNotificationSound.setChecked(isReroutingNotificationSoundEnabled(this));
        reroutingNotificationSound.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setReroutingNotificationSoundEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
        
        CheckBox reroutingDebugMsg = (CheckBox) findViewById(R.id.rerouting_debug_msg);
        reroutingDebugMsg.setChecked(isReroutingDebugMsgEnabled(this));
        reroutingDebugMsg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setReroutingDebugMsgEnabled(DebugOptionsActivity.this, isChecked);
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        int gpsMode = prefs.getInt(GPS_MODE, GPS_MODE_DEFAULT);
        
        switch (gpsMode) {
        case GPS_MODE_REAL:
            radioRealGPS.setChecked(true);
            break;
            
        case GPS_MODE_PRERECORDED:
            radioPrerecordedGPS.setChecked(true);
            break;
            
        case GPS_MODE_PRERECORDED_LA:
            radioPrerecordedGPS_LA.setChecked(true);
            break;
            
        case GPS_MODE_PRERECORDED_LA2:
            radioPrerecordedGPS_LA2.setChecked(true);
            break;
            
        case GPS_MODE_PRERECORDED_LA3:
            radioPrerecordedGPS_LA3.setChecked(true);
            break;
            
        case GPS_MODE_PRERECORDED_LA4:
            radioPrerecordedGPS_LA4.setChecked(true);
            break;
            
        case GPS_MODE_LONG_PRESS:
            radioLongPress.setChecked(true);
            break;
            
        default:
            Log.e("DebugOptionsActivity", "Should not reach here.");
        }
    }
    
    private static SharedPreferences getPrefs(Context ctx){
        return ctx.getSharedPreferences(DebugOptionsActivity.DEBUG_PREFS, 
                MODE_PRIVATE);
    }
    
    public static int getGpsUpdateInterval(Context ctx){
        return getPrefs(ctx).getInt(GPS_UPDATE_INTERVAL, defaultUpdateInterval);
    }
    
    public static String getCurrentLocation(Context ctx){
        return getPrefs(ctx).getString(CURRENT_LOCATION, "");
    }
    
    public static String getEntrypoint(Context ctx){
        return getPrefs(ctx).getString(ENTRYPOINT, "");
    }
    
    public static boolean isGoogleGeocodingPatched(Context ctx){
        boolean patched;
        try{
            patched = getPrefs(ctx).getInt(GOOGLE_GEOCODING_PATCHED, 0) == GOOGLE_GEOCODING_PATCH_NO;
        }catch(Throwable t){
            patched = false;
        }
        return patched;
    }
    
    public static void setGoogleGeocodingPatched(Context ctx, boolean patched){
        getPrefs(ctx).edit()
            .putInt(GOOGLE_GEOCODING_PATCHED, patched?GOOGLE_GEOCODING_PATCH_NO:0)
            .commit();
    }
    
    public static void setLastUserLatLonSent(Context ctx,long time){
        getPrefs(ctx).edit()
            .putLong(LAST_USER_LAT_SENT, time)
            .commit();
    }
    
    public static long getLastUserLatLonSent(Context ctx){
        return getPrefs(ctx).getLong(LAST_USER_LAT_SENT, 0);
    }
    
    public static void setEulaEtag(Context ctx, String eTag){
        getPrefs(ctx).edit()
            .putString(EULA_ETAG, eTag)
            .commit();
    }
    
    public static String getEulaEtag(Context ctx){
        return getPrefs(ctx).getString(EULA_ETAG, "");
    }
    
    public static LatLon getLastUserLatLon(Context ctx){
        LatLon rs = null;
        try{
            String latLonStr = getPrefs(ctx).getString(LAST_USER_LAT_LON, "");
            if(StringUtils.isNotBlank(latLonStr)){
                LatLon latLon = new LatLon();
                String[] toks = latLonStr.split(",");
                latLon.lat = Float.parseFloat(toks[0]);
                latLon.lon = Float.parseFloat(toks[1]);
                rs = latLon;
            }
        }catch(Throwable t){
        }
        return rs;
    }
    
    public static void setLastUserLatLon(Context ctx, float lat, float lon){
        getPrefs(ctx).edit()
            .putString(LAST_USER_LAT_LON, lat + "," + lon)
            .commit();
    }
    
    public static class LatLon {
        
        public float lat;
        
        public float lon;
        
    }
    
    private static JSONArray getFakeRoutes(Context ctx){
        JSONArray ids = null;
        try {
            ids = new JSONArray(getPrefs(ctx).getString(fakeRoutes, "[]"));
        }
        catch (JSONException e) {
            ids = new JSONArray();
        }
        return ids;
    }
    
    private static void saveFakeRoutes(Context ctx, JSONArray routes){
        SharedPreferences.Editor editor = getPrefs(ctx).edit();
        editor.putString(fakeRoutes, routes.toString());
        editor.commit();
    }
    
    public static void addFakeRoute(Context ctx, FakeRoute route){
        JSONArray newRoutes;
        JSONArray oldRoutes = getFakeRoutes(ctx);
        if(oldRoutes.length() > fakeRouteSize - 1){
            newRoutes = new JSONArray();
            for(int i=oldRoutes.length() - fakeRouteSize + 1; i<oldRoutes.length(); i++){
                newRoutes.put(oldRoutes.optString(i));
            }
        }else{
            newRoutes = oldRoutes;
        }
        saveFakeRoutes(ctx, newRoutes.put(route.toString()));
    }
    
    public static FakeRoute getFakeRoute(Context ctx, long id){
        FakeRoute route = null;
        JSONArray routes = getFakeRoutes(ctx);
        for(int i=0; i<routes.length(); i++){
            FakeRoute r = FakeRoute.fromString(routes.optString(i));
            if(r.id == id){
                route = r;
                break;
            }
        }
        return route;
    }
    
    public static class FakeRoute {
        
        long id;
        
        int seq;
        
        @Override
        public String toString() {
            return id + "," + seq;
        }
        
        public static FakeRoute fromString(String val){
            String[] vals = val.split(",");
            FakeRoute r = new FakeRoute();
            r.id = Long.parseLong(vals[0]);
            r.seq = Integer.parseInt(vals[1]);
            return r;
        }
        
    }
    
    private static SortedMap<Long, NavigationLink> getNavLinks(Context ctx){
        SortedMap<Long, NavigationLink> rs = new TreeMap<Long, NavigationLink>();
        JSONArray array = null;
        try {
            array = new JSONArray(getPrefs(ctx).getString(navLinks, "[]"));
        }
        catch (Throwable t) {
            array = new JSONArray();
        }
        for (int i=0; i < array.length(); i++) {
            JSONObject json = array.optJSONObject(i);
            if(json != null){
                NavigationLink l = NavigationLink.fromJSON(json);
                rs.put(l.id, l);
            }
        }
        return rs;
    }
    
    private static void saveNavLinks(Context ctx, Map<Long, NavigationLink> links){
        JSONArray array = new JSONArray();
        for(NavigationLink l:links.values()){
            try {
                array.put(l.toJSON());
            }
            catch (JSONException e) { }
        }
        SharedPreferences.Editor editor = getPrefs(ctx).edit();
        editor.putString(navLinks, array.toString());
        editor.commit();
    }
    
    public static void addNavLink(Context ctx, NavigationLink l){
        SortedMap<Long, NavigationLink> links = getNavLinks(ctx);
        while(links.size() > navLinksSize - 1){
            links.remove(links.firstKey());
        }
        links.put(l.id, l);
        saveNavLinks(ctx, links);
    }
    
    public static NavigationLink getNavLink(Context ctx, long id){
        return getNavLinks(ctx).get(id);
    }
    
    public static class NavigationLink {
        
        private static String ID = "ID";
        
        private static String URL = "URL";
        
        public long id;
        
        public String url;
        
        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put(ID, id);
            json.put(URL, url);
            return json;
        }
        
        public static NavigationLink fromJSON(JSONObject json){
            NavigationLink l = new NavigationLink();
            l.id = json.optLong(ID);
            l.url = json.optString(URL);
            return l;
        }
        
    }
    
    private static void saveRecentAddresses(Context ctx, List<String> addrs){
        JSONArray array = new JSONArray();
        for(String a : addrs){
            array.put(a);
        }
        SharedPreferences.Editor editor = getPrefs(ctx).edit();
        editor.putString(recentAddresses, array.toString());
        editor.commit();
    }
    
    public static void addRecentAddress(Context ctx, String addr){
        List<String> list = getRecentAddresses(ctx);
        while(list.size() > recentAddressesSize - 1){
            list.remove(list.size() - 1);
        }
        list.add(0, addr);
        saveRecentAddresses(ctx, list);
    }
    
    public static List<String> getRecentAddresses(Context ctx){
        List<String> list = new ArrayList<String>();
        JSONArray array = null;
        try {
            array = new JSONArray(getPrefs(ctx).getString(recentAddresses, "[]"));
        }
        catch (Throwable t) {
            array = new JSONArray();
        }
        for (int i=0; i < array.length(); i++) {
            String addr = array.optString(i);
            if(StringUtils.isNotBlank(addr)){
                list.add(addr);
            }
        }
        return list;
    }
    
    public static boolean isReroutingNotificationSoundEnabled(Context ctx){
        boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(REROUTING_NOTIFICATION_SOUND, false);
        }catch(Throwable t){
            enabled = false;
        }
        return enabled;
    }
    
    public static void setReroutingNotificationSoundEnabled(Context ctx, boolean enabled){
        getPrefs(ctx).edit()
            .putBoolean(REROUTING_NOTIFICATION_SOUND, enabled)
            .commit();
    }
    
    public static boolean isReroutingDebugMsgEnabled(Context ctx){
        boolean enabled;
        try{
            enabled = getPrefs(ctx).getBoolean(REROUTING_DEBUG_MSG, false);
        }catch(Throwable t){
            enabled = false;
        }
        return enabled;
    }
    
    public static void setReroutingDebugMsgEnabled(Context ctx, boolean enabled){
        getPrefs(ctx).edit()
            .putBoolean(REROUTING_DEBUG_MSG, enabled)
            .commit();
    }
    
}
