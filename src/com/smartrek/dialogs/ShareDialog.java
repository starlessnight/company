package com.smartrek.dialogs;

import java.util.Arrays;

import twitter4j.TwitterException;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.google.android.gms.plus.GooglePlusUtil;
import com.google.android.gms.plus.PlusShare;
import com.smartrek.activities.R;
import com.smartrek.utils.Font;
import com.smartrek.utils.Misc;
import com.twitter.android.TwitterApp;
import com.twitter.android.TwitterApp.TwDialogListener;

public class ShareDialog extends DialogFragment {
	
    private static final String FB_PERMISSIONS = "publish_actions";
    
    private static final int GOOGLE_PLUS_REQ = 7; 
    
    private static String TITLE = "TITLE";
    
    private static String SHARE_TEXT = "SHARE_TEXT";
    
	private ViewGroup dialogView;
	
	private UiLifecycleHelper uiHelper;
	
	private String shareText;
	
	private boolean fbPending;
	
	private Session.StatusCallback fbCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
	
	private TwitterApp mTwitter;
	
    public static ShareDialog newInstance(String title, String shareText) {
        ShareDialog dialog = new ShareDialog();
        dialog.setStyle(0, R.style.PopUpDialog);
        
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(SHARE_TEXT, shareText);
        dialog.setArguments(args);

        return dialog;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, 
	        Bundle savedInstanceState) {
	    
	    Bundle args = getArguments();
	    
	    shareText = args.getString(SHARE_TEXT); 
	    
        dialogView = (ViewGroup) inflater.inflate(R.layout.share_dialog, null);
        
        TextView titleView = (TextView) dialogView.findViewById(R.id.title);
        titleView.setText(args.getString(TITLE));
        
        TextView facebookButton = (TextView) dialogView.findViewById(R.id.facebook_button);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNotLoading()){
                    Session session = Session.getActiveSession();
                    if (session != null && session.isOpened()) {
                        publishFB();
                    }else{
                        fbPending = true;
                        fbLogin();
                    }
                }
            }
        });
        
        mTwitter = new TwitterApp(getActivity(), 
                getString(R.string.twitter_key), getString(R.string.twitter_secret));
        mTwitter.setListener( new TwDialogListener() {
            @Override
            public void onError(String value) {
                Log.w("onError", value);
                if(!"Error getting access token".equals(value)){
                    mTwitter.resetAccessToken();
                    mTwitter.authorize();
                    Toast.makeText(getActivity(), "wrong username and/or password", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onComplete(String value) {
                updateTwitterStatus();
            }
        });
        TextView twitterButton = (TextView) dialogView.findViewById(R.id.twitter_button);
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNotLoading()){
                    updateTwitterStatus();
                }
            }
        });
        
        TextView googlePlusButton = (TextView) dialogView.findViewById(R.id.google_plus_button);
        googlePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNotLoading()){
                    int errorCode = GooglePlusUtil.checkGooglePlusApp(getActivity());
                    if (errorCode != GooglePlusUtil.SUCCESS) {
                      GooglePlusUtil.getErrorDialog(errorCode, getActivity(), 0).show();
                    }else{
                        Intent shareIntent = new PlusShare.Builder(getActivity())
                            .setType("text/plain")
                            .setText(shareText)
                            .getIntent();
                        startActivityForResult(shareIntent, GOOGLE_PLUS_REQ);
                    }
                }
            }
        });
        
        View closeIcon = dialogView.findViewById(R.id.close_icon);
        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissQuietly();
            }
        });
        
        AssetManager assets = getActivity().getAssets();
        Font.setTypeface(Font.getBold(assets), titleView, facebookButton, 
            twitterButton, googlePlusButton);
        
        uiHelper = new UiLifecycleHelper(getActivity(), fbCallback);
        uiHelper.onCreate(savedInstanceState);
	    
	    return dialogView;
	}
	
	private void fbLogin(){
	    try{
    	    Session.openActiveSession(getActivity(), ShareDialog.this, true, fbCallback);
	    } catch(Throwable t){}
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    Log.i("onSessionStateChange", session + "");
	    if (state == SessionState.OPENED_TOKEN_UPDATED) {
	        if(hasPublishPermission()){
	            publishFB();
	        }else{
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, Arrays.asList(FB_PERMISSIONS)));
            }    
	    }else if(state == SessionState.OPENED && (fbPending || hasPublishPermission())){
	        fbPending = false;
	        if(hasPublishPermission()){
	            publishFB();
	        }else{
	            session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, Arrays.asList(FB_PERMISSIONS)));
	        }
        }else if(state == SessionState.CLOSED && fbPending){
            fbLogin();
        }
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	    if(requestCode == GOOGLE_PLUS_REQ && resultCode == Activity.RESULT_OK){
	        dismissQuietly();
	    }
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains(FB_PERMISSIONS);
    }

    private void publishFB() {
        final Session session = Session.getActiveSession();
        if (session != null) {
            updateFBStatus();
        }
    }
    
    private void updateFBStatus(){
        if(getActivity() != null) {
            final Session session = Session.getActiveSession();
            final View loading = getView().findViewById(R.id.loading);
            Request request = Request
                    .newStatusUpdateRequest(session, shareText, new Request.Callback() {
                        @Override
                        public void onCompleted(Response response) {
                            loading.setVisibility(View.GONE);
                            FacebookRequestError error = response.getError();
                            Log.i("onCompleted", error != null?response.getError().toString():"");
                            if(error != null && error.getErrorCode() != 506){
                                fbPending = true;
                                session.closeAndClearTokenInformation();
                            }else{
                                dismissQuietly();
                                displaySharedNotification();
                            }
                        }
                    });
            request.executeAsync();
            loading.setVisibility(View.VISIBLE);
        }
    }
    
    private void displaySharedNotification(){
        if(getActivity() != null){
            Toast.makeText(getActivity(), "shared", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateTwitterStatus() {
        if (mTwitter.hasAccessToken() == true) {
            final View loading = getView().findViewById(R.id.loading);
            AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    boolean success;
                    try {
                        mTwitter.updateStatus(shareText);
                        success = true;
                    }
                    catch (Exception e) {
                        success = false;
                        if(e instanceof TwitterException){
                            TwitterException te = (TwitterException) e;
                            if(te.getErrorCode() == 187){
                                success = true;
                            }
                        }
                        Log.w("updateTwitterStatus", Log.getStackTraceString(e));
                    }
                    return success;
                }
                @Override
                protected void onPostExecute(Boolean success) {
                    loading.setVisibility(View.GONE);
                    if(success){
                        dismissQuietly();
                        displaySharedNotification();
                    }else{
                        mTwitter.resetAccessToken();
                        mTwitter.authorize();
                    }
                }
            };
            Misc.parallelExecute(task);
            loading.setVisibility(View.VISIBLE);
        } else {
            mTwitter.authorize();
        }
    }
    
    private boolean isNotLoading(){
        return getView().findViewById(R.id.loading).getVisibility() != View.VISIBLE;
    }
    
    private void dismissQuietly(){
        if(getActivity() != null){
            try{
                dismiss();
            }catch(Throwable t){
            }
        }
    }
    
}