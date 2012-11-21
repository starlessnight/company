package com.smartrek.ui;

import java.io.IOException;

import android.content.Context;
import android.location.Location;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.models.Route;
import com.smartrek.utils.RouteLink;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.StringUtil;
import com.smartrek.utils.ValidationParameters;

public final class NavigationView extends LinearLayout {
    
    public enum Status {
        WaitingForGPS, OutOfRoute, InRoute
    }
    
    private Status status;
	
	private ViewGroup textViewNavigation;
	private TextView textViewWaiting;
	private TextView textViewGenericMessage;
	private TextView textViewMessage;
	private TextView textViewDistance;
	private TextView textViewRoadname;
	
	/**
	 * Media player to play a ping sound
	 */
	private MediaPlayer mediaPlayer;

	public NavigationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.navigation_view, this, true);
		
		textViewNavigation = (ViewGroup) findViewById(R.id.text_view_navigation);
		textViewWaiting = (TextView) findViewById(R.id.text_view_waiting);
		textViewGenericMessage = (TextView) findViewById(R.id.text_view_generic_message);
        textViewMessage = (TextView) findViewById(R.id.text_view_message);
        textViewDistance = (TextView) findViewById(R.id.text_view_distance);
        textViewRoadname = (TextView) findViewById(R.id.text_view_roadname);
        
        setStatus(Status.WaitingForGPS);
	}
	
	public Status getStatus() {
	    return status;
	}
	
	public void setStatus(Status status) {
	    if (Status.WaitingForGPS.equals(status)) {
	        textViewWaiting.setVisibility(View.VISIBLE);
	        textViewGenericMessage.setVisibility(View.GONE);
	        textViewNavigation.setVisibility(View.GONE);
	        
	        setBackgroundColor(0xC0666666);
	    }
	    else if (Status.OutOfRoute.equals(status)) {
            textViewWaiting.setVisibility(View.GONE);
            textViewGenericMessage.setVisibility(View.VISIBLE);
            textViewNavigation.setVisibility(View.GONE);
            
            setBackgroundColor(0xC0FF3300);
        }
	    else if (Status.InRoute.equals(status)) {
            textViewWaiting.setVisibility(View.GONE);
            textViewGenericMessage.setVisibility(View.GONE);
            textViewNavigation.setVisibility(View.VISIBLE);
            
            setBackgroundColor(0xC0293A17);
        }
	    else {
	        Log.e(getClass().toString(), "setStatus(): Should not reach here.");
	    }
	}
	
	public void update(final Route route, final Location location, final RouteNode node) {
		
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
		
		double distance = route.getDistanceToNextTurn(latitude, longitude);
        double distanceInMile = distance * 0.000621371;
        double distanceInFoot = distance * 3.28084;
        
        ValidationParameters params = ValidationParameters.getInstance();
        RouteLink nearestLink = route.getNearestLink(latitude, longitude);
        if (nearestLink.distanceTo(latitude, longitude) <= params.getInRouteDistanceThreshold()) {
            setStatus(Status.InRoute);
            
            String distancePresentation = StringUtil.formatImperialDistance(distance);
            textViewMessage.setText(StringUtil.capitalizeFirstLetter(node.getMessage()));
            textViewDistance.setText(distancePresentation);
            textViewRoadname.setText(node.getRoadName());
            
            // FIXME: Temporary
            if (node.hasMetadata()) {
                RouteNode.Metadata metadata = node.getMetadata();
            
                if (!metadata.pingFlags[0] && distanceInFoot <= 500) {
                    metadata.pingFlags[0] = true;
                    playPingSound();
                }
                else if (metadata.pingFlags[1] && distanceInMile <= 1.0) {
                    metadata.pingFlags[1] = true;
                    playPingSound();
                }
                else if (!metadata.pingFlags[2] && distanceInMile <= 2.0) {
                    metadata.pingFlags[2] = true;
                    playPingSound();
                }
            }
        }
        else {
            setStatus(Status.OutOfRoute);
            textViewGenericMessage.setText("Out of route. Please go back to route.");
        }
        

	}
	
	/**
	 * This function causes Activity.setContentView() to hang on Android 3.1.
	 */
    public void preparePingSound() {
        mediaPlayer = new MediaPlayer();
        
        try {
        	//mediaPlayer.setDataSource("file:///android_asset/ping.mp3");
            mediaPlayer.setDataSource(getResources().getAssets().openFd("ping.mp3").getFileDescriptor());
			mediaPlayer.prepare();
		}
		catch (IllegalStateException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
        catch (Exception e) {
            e.printStackTrace();
        }
    }
	
    // FIXME: Temporary
    private void playPingSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

}
