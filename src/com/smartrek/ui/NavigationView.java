package com.smartrek.ui;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import android.content.Context;
import android.graphics.Typeface;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.models.Route;
import com.smartrek.utils.Font;
import com.smartrek.utils.RouteLink;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.StringUtil;
import com.smartrek.utils.ValidationParameters;

public final class NavigationView extends LinearLayout {
    
    public enum Status {
        WaitingForGPS, OutOfRoute, InRoute
    }
    
    private Status status;
	
	private TextView textViewNavigation;
	private TextView textViewWaiting;
	private TextView textViewGenericMessage;
	
	private CheckPointListener listener;
	
	private boolean everInRoute;

	public NavigationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.navigation_view, this, true);
		
		textViewNavigation = (TextView) findViewById(R.id.text_view_navigation);
		textViewWaiting = (TextView) findViewById(R.id.text_view_waiting);
		textViewGenericMessage = (TextView) findViewById(R.id.text_view_generic_message);
        
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
	        setBackgroundResource(R.color.transparent_gray);
	    }
	    else if (Status.OutOfRoute.equals(status)) {
            textViewWaiting.setVisibility(View.GONE);
            textViewGenericMessage.setVisibility(View.VISIBLE);
            textViewNavigation.setVisibility(View.GONE);
            setBackgroundResource(R.color.transparent_light_red);
        }
	    else if (Status.InRoute.equals(status)) {
            textViewWaiting.setVisibility(View.GONE);
            textViewGenericMessage.setVisibility(View.GONE);
            textViewNavigation.setVisibility(View.VISIBLE);
            setBackgroundResource(R.color.transparent_light_green);
        }
	    else {
	        Log.e(getClass().toString(), "setStatus(): Should not reach here.");
	    }
	}
	
	public static String getDirection(RouteNode node, String distance){
        String roadName = node.getRoadName();
        String dir = WordUtils.capitalize(node.getMessage()) 
            + (StringUtils.isEmpty(distance)?"":(" in " + distance)) 
            + (StringUtils.isBlank(roadName) || StringUtils.equalsIgnoreCase(roadName, "null")
                ?"":(" on " + roadName));
        return dir;
    }
	
	public static String getDirection(RouteNode node, double distance){
        String distancePresentation = StringUtil.formatImperialDistance(distance);
        return getDirection(node, distancePresentation);
	}
	
	private static double metersToFeet(double meters){
	    return meters * 3.28084;
	}
	
	private static double metersToMiles(double meters){
        return meters * 0.000621371;
    }
	
	public Status update(final Route route, final Location location, final RouteNode node) {
	    Status rtnStatus;
	    
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
		
		double distance = route.getDistanceToNextTurn(latitude, longitude);
        double distanceInMile = metersToMiles(distance);
        double distanceInFoot = metersToFeet(distance);
        
        ValidationParameters params = ValidationParameters.getInstance();
        RouteLink nearestLink = route.getNearestLink(latitude, longitude);
        if (nearestLink.distanceTo(latitude, longitude) <= params.getInRouteDistanceThreshold()) {
            setStatus(rtnStatus = Status.InRoute);
            
            textViewNavigation.setText(getDirection(node, distance));
            
            // FIXME: Temporary
            if (node.hasMetadata()) {
                double linkDistance = 0;
                RouteNode end = nearestLink.getEndNode();
                while((end = end.getPrevNode()) != null){
                    linkDistance += end.getDistance();
                    if (end.getFlag() != 0) {
                        break;
                    }
                }
                
                double linkDistanceInMile = metersToMiles(linkDistance);
                RouteNode.Metadata metadata = node.getMetadata();
            
                String checkpointDistance = null;
                
                if (!metadata.pingFlags[0] && linkDistanceInMile >= 0.094697 && distanceInMile <= 0.094697) {
                    metadata.pingFlags[0] = true;
                    metadata.pingFlags[1] = true;
                    metadata.pingFlags[2] = true;
                    checkpointDistance = "0.1 miles";
                }
                else if (!metadata.pingFlags[1] && linkDistanceInMile >= 1.0 && distanceInMile <= 1.0) {
                    metadata.pingFlags[1] = true;
                    metadata.pingFlags[2] = true;
                    checkpointDistance = "1 mile";
                }
                else if (!metadata.pingFlags[2] && linkDistanceInMile >= 2.0 && distanceInMile <= 2.0) {
                    metadata.pingFlags[2] = true;
                    checkpointDistance = "2 miles";
                }
                
                if(listener != null && checkpointDistance != null){
                    listener.onCheckPoint(getDirection(node, checkpointDistance));
                }
            }
        }
        else {
            setStatus(rtnStatus = Status.OutOfRoute);
            textViewGenericMessage.setText(everInRoute?"Out of route. Please go back to route.":"Please start from the highlighted route.");
        }
        
        return rtnStatus;
	}
	
	public static interface CheckPointListener {
	    
	    void onCheckPoint(String navText);
	    
	}

    public CheckPointListener getListener() {
        return listener;
    }

    public void setListener(CheckPointListener cpListener) {
        this.listener = cpListener;
    }
    
    public void setTypeface(Typeface font){
        Font.setTypeface(font, textViewGenericMessage, textViewNavigation,
            textViewWaiting);
    }

    public boolean isEverInRoute() {
        return everInRoute;
    }

    public void setEverInRoute(boolean everInRoute) {
        this.everInRoute = everInRoute;
    }

}
