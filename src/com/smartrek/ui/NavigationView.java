package com.smartrek.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.graphics.Typeface;
import android.location.Location;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    
    private static final double checkPointFeetOffset = 250;
    
    private static final double checkPointMilesOffset = checkPointFeetOffset * 0.000189394;
    
    public enum Status {
        WaitingForGPS, OutOfRoute, InRoute
    }
    
    private Status status;
	
    private ViewGroup navigationDisplay;
    private ImageView imgViewDirection;
    private TextView textViewDistance;
    private TextView textViewRoad;
	private TextView textViewWaiting;
	private TextView textViewGenericMessage;
	
	private CheckPointListener listener;
	
	private boolean everInRoute;
	
	private RouteNode lastEnd;

	public NavigationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.navigation_view, this, true);
		
		navigationDisplay = (ViewGroup) findViewById(R.id.navigation_display);
		imgViewDirection = (ImageView) findViewById(R.id.img_view_direction);
		textViewDistance = (TextView) findViewById(R.id.text_view_distance);
		textViewRoad = (TextView) findViewById(R.id.text_view_road);
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
	        navigationDisplay.setVisibility(View.GONE);
	        setBackgroundResource(R.color.transparent_gray);
	    }
	    else if (Status.OutOfRoute.equals(status)) {
            textViewWaiting.setVisibility(View.GONE);
            textViewGenericMessage.setVisibility(View.VISIBLE);
            navigationDisplay.setVisibility(View.GONE);
            setBackgroundResource(R.color.transparent_light_red);
        }
	    else if (Status.InRoute.equals(status)) {
            textViewWaiting.setVisibility(View.GONE);
            textViewGenericMessage.setVisibility(View.GONE);
            navigationDisplay.setVisibility(View.VISIBLE);
            setBackgroundResource(android.R.color.transparent);
        }
	    else {
	        Log.e(getClass().toString(), "setStatus(): Should not reach here.");
	    }
	}
	
	public static String getDirection(RouteNode node, String distance, boolean actionOnly){
        String roadName = node.getRoadName();
        String msg = node.getMessage();
        String dir1 = StringUtils.substringBeforeLast(msg, " ");
        String dir2 = StringUtils.substringAfterLast(msg, " ");
        String dir;
        if(actionOnly){
            dir = dir1;
        }else{
            dir = (StringUtils.isEmpty(distance)?"":("In " + distance + ", ")) 
                + dir1 
                + (StringUtils.isBlank(dir2)?"":(" " + dir2)) 
                + (StringUtils.isBlank(roadName) || StringUtils.equalsIgnoreCase(roadName, "null")
                    ?"":(" " + roadName));
        }
        return dir;
    }
	
	public static CharSequence getFormattedDirection(RouteNode node, String distance, int largeFontSize){
	    String roadName = node.getRoadName();
        String msg = node.getMessage();
        String[] distToks = distance.split(" ");
        String dir1 = StringUtils.substringBeforeLast(msg, " ");
        String dir2 = StringUtils.substringAfterLast(msg, " ");
        SpannableStringBuilder dir = new SpannableStringBuilder();
        if(StringUtils.isNotEmpty(distance)){
            dir.append("In ")
                .append(spannable(distToks[0], new AbsoluteSizeSpan(largeFontSize)))
                .append(" " + distToks[1] + ", ");
        }
        dir.append(hightlight(dir1, "left right", largeFontSize))
            .append(StringUtils.isBlank(dir2)?"":(" " + dir2));
        if(StringUtils.isNotBlank(roadName) && !StringUtils.equalsIgnoreCase(roadName, "null")){
            dir.append(" ")
                .append(spannable(roadName, new AbsoluteSizeSpan(largeFontSize)));
        }
        return dir;
    }
	
	private static Spannable hightlight(String str, String substr, int largeFontSize){
        SpannableString spannable = SpannableString.valueOf(str);
        for(String s : substr.split(" ")){
            int len = s.length();
            for (int i : indexesOf(str, s)) {
                spannable.setSpan(new AbsoluteSizeSpan(largeFontSize), i, i + len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannable;
    }
	
	private static List<Integer> indexesOf(String str, String substr){
        List<Integer> indexes = new ArrayList<Integer>();
        for (int index = str.indexOf(substr); index >= 0; index = str.indexOf(substr, index + 1)){
            indexes.add(index);
        }
        return indexes;
    }
	
	private static Spannable spannable(CharSequence source, Object... span){
        SpannableString spannable = SpannableString.valueOf(source);
        int length = source.length();
        for (Object s : span) {
            if(s != null){
                spannable.setSpan(s, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannable;
    }
	
	public static CharSequence getFormattedDirection(RouteNode node, double distance, int largeFontSize){
        return getFormattedDirection(node, StringUtil.formatImperialDistance(distance), largeFontSize);
    }
	
	public static String getContinueDirection(RouteNode node, String distance){
	    String roadName = node.getRoadName();
        String dir = "Continue"
            + (StringUtils.isBlank(roadName) || StringUtils.equalsIgnoreCase(roadName, "null")
                    ?"":(" on " + roadName))
            + (StringUtils.isEmpty(distance)?"":(" for " + distance));
        return dir;
    }
	
	private static double metersToFeet(double meters){
	    return meters * 3.28084;
	}
	
	private static double metersToMiles(double meters){
        return meters * 0.000621371;
    }
	
	public void update(final Route route, final Location location, final RouteNode node) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
		
		double distance = route.getDistanceToNextTurn(latitude, longitude);
        double distanceInMile = metersToMiles(distance);
        double distanceInFoot = metersToFeet(distance);
        
        ValidationParameters params = ValidationParameters.getInstance();
        RouteLink nearestLink = route.getNearestLink(latitude, longitude);
        if (nearestLink.distanceTo(latitude, longitude) <= params.getInRouteDistanceThreshold()) {
            setStatus(Status.InRoute);
            
            String formattedDist = StringUtil.formatImperialDistance(distance);
            
            int dirDrawableId = getDirectionDrawableId(node.getDirection());
            if(dirDrawableId == 0){
                imgViewDirection.setVisibility(View.INVISIBLE);
            }else{
                imgViewDirection.setImageResource(dirDrawableId);
                imgViewDirection.setVisibility(View.VISIBLE);
            }
            textViewDistance.setText(StringUtil.formatImperialDistance(distance, true));
            String roadName = node.getRoadName();
            textViewRoad.setText((StringUtils.isBlank(roadName) || StringUtils.equalsIgnoreCase(roadName, "null"))?"":roadName);
            
            // FIXME: Temporary
            if (node.hasMetadata()) {
                RouteNode end = nearestLink.getEndNode();
                while(end.getFlag() == 0 && end.getNextNode() != null){
                    end = end.getNextNode(); 
                }
                boolean continueDir = false;    
                if(!everInRoute){
                    everInRoute = true;
                    continueDir = true;
                }else if(end != lastEnd){
                    continueDir = true;
                }
                lastEnd = end;
                
                if(continueDir){
                    if(listener != null){
                        RouteNode prevNode = end.getPrevNode();
                        if(prevNode == null){
                            prevNode = end;
                        }
                        listener.onCheckPoint(getContinueDirection(prevNode, formattedDist));
                    }
                }else{
                    double linkDistance = 0;
                    while((end = end.getPrevNode()) != null){
                        linkDistance += end.getDistance();
                        if (end.getFlag() != 0) {
                            break;
                        }
                    }
                    
                    double linkDistanceInMile = metersToMiles(linkDistance);
                    RouteNode.Metadata metadata = node.getMetadata();
                
                    String checkpointDistance = null;
                    boolean actionOnly = false;
                    
                    if (!metadata.pingFlags[0] && distanceInFoot <= 100 + checkPointFeetOffset) {
                        metadata.pingFlags[0] = true;
                        metadata.pingFlags[1] = true;
                        metadata.pingFlags[2] = true;
                        metadata.pingFlags[3] = true;
                        metadata.pingFlags[4] = true;
                        checkpointDistance = "";
                        actionOnly = true;
                    }
                    else if (!metadata.pingFlags[1] && distanceInMile <= 0.2 + checkPointMilesOffset) {
                        metadata.pingFlags[1] = true;
                        metadata.pingFlags[2] = true;
                        metadata.pingFlags[3] = true;
                        metadata.pingFlags[4] = true;
                        checkpointDistance = linkDistanceInMile >= 0.2?"0.2 miles":formattedDist;
                    }
                    else if (!metadata.pingFlags[2] && distanceInMile <= 0.5 + checkPointMilesOffset) {
                        metadata.pingFlags[2] = true;
                        metadata.pingFlags[3] = true;
                        metadata.pingFlags[4] = true;
                        checkpointDistance = linkDistanceInMile >= 0.5?"0.5 miles":formattedDist;
                    }
                    else if (!metadata.pingFlags[3] && distanceInMile <= 1.0 + checkPointMilesOffset) {
                        metadata.pingFlags[3] = true;
                        metadata.pingFlags[4] = true;
                        checkpointDistance = linkDistanceInMile >= 1.0?"1 mile":formattedDist;
                    }
                    else if (!metadata.pingFlags[4] && distanceInMile <= 2.0 + checkPointMilesOffset) {
                        metadata.pingFlags[4] = true;
                        checkpointDistance = "2 miles";
                        checkpointDistance = linkDistanceInMile >= 2.0?"2 miles":formattedDist;
                    }
                    
                    if(listener != null && checkpointDistance != null){
                        listener.onCheckPoint(getDirection(node, checkpointDistance, actionOnly));
                    }
                }
            }
        }
        else {
            setStatus(Status.OutOfRoute);
            textViewGenericMessage.setText(everInRoute?"Out of route. Please go back to route.":"Please start from the highlighted route.");
        }
	}
	
	public static int getDirectionDrawableId(String direction){
	    int id;
	    if(StringUtils.equalsIgnoreCase("slightly left", direction)){
	        id = R.drawable.slightly_left;
	    }else if(StringUtils.equalsIgnoreCase("curve left", direction)){
	        id = R.drawable.curve_left;
	    }else if(StringUtils.equalsIgnoreCase("turn left", direction)){
	        id = R.drawable.turn_left;
	    }else if(StringUtils.equalsIgnoreCase("slightly right", direction)){
	        id = R.drawable.slightly_right;
	    }else if(StringUtils.equalsIgnoreCase("curve right", direction)){
	        id = R.drawable.curve_right;
	    }else if(StringUtils.equalsIgnoreCase("turn right", direction)){
	        id = R.drawable.turn_right;
	    }else if(StringUtils.equalsIgnoreCase("make a u turn", direction)){
	        id = R.drawable.make_a_u_turn;
	    }else if(StringUtils.equalsIgnoreCase("go straight", direction)){
	        id = R.drawable.go_straight;
	    }else{
	        id = 0; 
	    }
	    return id;
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
        Font.setTypeface(font, textViewGenericMessage, textViewWaiting,
            textViewDistance, textViewRoad);
    }

}
