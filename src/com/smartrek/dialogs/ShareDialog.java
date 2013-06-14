package com.smartrek.dialogs;

import java.util.Arrays;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.smartrek.activities.R;
import com.smartrek.utils.Font;

public class ShareDialog extends DialogFragment {
	
    private static final String FB_PERMISSIONS = "publish_actions";

    public static final String FB_APP_ID = "202039786615562";
    
    private static String TITLE = "TITLE";
    
    private static String SHARE_TEXT = "SHARE_TEXT";
    
	private ViewGroup dialogView;
	
	private UiLifecycleHelper uiHelper;
	
	private GraphUser fbUser;
	
	private boolean fbPending;
	
	private String shareText;
	
	private LoginButton internalFBButton;
	
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
        internalFBButton = (LoginButton) dialogView.findViewById(R.id.internal_facebook_button);
        internalFBButton.setFragment(this);
        internalFBButton.setPublishPermissions(Arrays.asList(FB_PERMISSIONS));
        internalFBButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                fbUser= user;
                if(fbPending){
                    updateFBStatus();
                }
            }
        });
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session session = Session.getActiveSession();
                if (session != null && session.isOpened() && fbUser != null) {
                    publishFB();
                }else{
                    fbPending = true;
                    internalFBButton.performClick();
                }
            }
        });
        
        TextView twitterButton = (TextView) dialogView.findViewById(R.id.twitter_button);
        
        TextView googlePlusButton = (TextView) dialogView.findViewById(R.id.google_plus_button);
        
        View closeIcon = dialogView.findViewById(R.id.close_icon);
        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        
        AssetManager assets = getActivity().getAssets();
        Font.setTypeface(Font.getBold(assets), titleView, facebookButton, 
            twitterButton, googlePlusButton);
        
        uiHelper = new UiLifecycleHelper(getActivity(), new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (state == SessionState.OPENED_TOKEN_UPDATED) {
                    updateFBStatus();
                }
            }
        });
        uiHelper.onCreate(savedInstanceState);
	    
	    return dialogView;
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
            fbPending = true;
            if (hasPublishPermission()) {
                updateFBStatus();
            } else {
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, Arrays.asList(FB_PERMISSIONS)));
            }
        }
    }
    
    private void updateFBStatus(){
        if (fbUser != null && hasPublishPermission()) {
            final String message = shareText;
            final Session session = Session.getActiveSession();
            final View loading = getView().findViewById(R.id.loading);
            Request request = Request
                    .newStatusUpdateRequest(session, message, new Request.Callback() {
                        @Override
                        public void onCompleted(Response response) {
                            loading.setVisibility(View.GONE);
                            if(response.getError() != null){
                                fbPending = true;
                                session.closeAndClearTokenInformation();
                                internalFBButton.performClick();
                            }else{
                                dismiss();
                            }
                        }
                    });
            request.executeAsync();
            loading.setVisibility(View.VISIBLE);
            fbPending = false;
        }
    }
    
}
