package com.smartrek.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartrek.activities.DebugOptionsActivity;
import com.smartrek.activities.MainActivity;
import com.smartrek.activities.R;
import com.smartrek.models.Route;
import com.smartrek.models.Trajectory;
import com.smartrek.requests.Request;
import com.smartrek.requests.Request.Setting;
import com.smartrek.utils.Dimension;
import com.smartrek.utils.Font;
import com.smartrek.utils.Misc;
import com.smartrek.utils.RouteLink;
import com.smartrek.utils.RouteNode;
import com.smartrek.utils.RouteNode.Metadata;
import com.smartrek.utils.StringUtil;
import com.smartrek.utils.ValidationParameters;

public class NavigationView extends LinearLayout {

	public static class DirectionItem {

		public int drawableId;

		public double distance;

		public String roadName;
		
		public int smallDrawableId;
		
		public String direction;
		
		public DirectionItem(String direction, double distance, String roadName) {
			this.direction = direction;
			this.drawableId = getDirectionDrawableId(direction, false);
			this.distance = distance;
			this.roadName = roadName;
			this.smallDrawableId = getDirectionDrawableId(direction, true);
		}
		
		private int getDirectionDrawableId(String direction, boolean smallOne) {
			int id;
			if (StringUtils.equalsIgnoreCase("keep left", direction)) {
				id = smallOne?R.drawable.small_slightly_left:R.drawable.slightly_left;
			} else if (StringUtils.equalsIgnoreCase("slightly left", direction)) {
				id = smallOne?R.drawable.small_curve_left:R.drawable.curve_left;
			} else if (StringUtils.equalsIgnoreCase("turn left", direction)) {
				id = smallOne?R.drawable.small_turn_left:R.drawable.turn_left;
			} else if (StringUtils.equalsIgnoreCase("keep right", direction)) {
				id = smallOne?R.drawable.small_slightly_right:R.drawable.slightly_right;
			} else if (StringUtils.equalsIgnoreCase("slightly right", direction)) {
				id = smallOne?R.drawable.small_curve_right:R.drawable.curve_right;
			} else if (StringUtils.equalsIgnoreCase("turn right", direction)) {
				id = smallOne?R.drawable.small_turn_right:R.drawable.turn_right;
			} else if (StringUtils.equalsIgnoreCase("make a u turn", direction)) {
				id = smallOne?R.drawable.small_make_a_u_turn:R.drawable.make_a_u_turn;
			} else if (StringUtils.equalsIgnoreCase("go straight", direction)) {
				id = smallOne?R.drawable.small_go_straight:R.drawable.go_straight;
			} else {
				id = smallOne?R.drawable.small_go_straight:R.drawable.go_straight;
			}
			return id;
		}

	}

	private static final double checkPointFeetOffset = 250;

	private static final double checkPointMilesOffset = checkPointFeetOffset * 0.000189394;

	public enum Status {
		WaitingForGPS, OutOfRoute, InRoute
	}

	private Status status;

	private ViewGroup navigationDisplay;
	private ImageView imgViewDirection;
	private TextView textViewDistance;
//	private TextView textViewRoad;
	private TextView textViewWaiting;
	private TextView textViewGenericMessage;
	private ImageView btnPrevItem;
	private ImageView btnNextItem;

	private ImageView imgViewNextDirection;
	
	private LinearLayout nextDirectionPanel;

	private CheckPointListener listener;

	private boolean everInRoute;
	
	private boolean rerouting;

	private boolean lastRerouting;
	
	private RouteNode lastEnd;

	private List<DirectionItem> items = Collections.emptyList();

	private int currentItemIdx;
	
	private double lastCheckPointDistanceInMile;
	
	private String destinationAddress;
	
	private boolean hasVoice;
	
	private String notifiedMsg = "";
	
	private boolean needNotification; 
	
	private int mStartingY = -1000;
	
	private boolean move = false;
	
	private Runnable openDirectionViewEvent;
	private boolean portraitMode = true;
	
	private ViewPager roadPager;
	
	public NavigationView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater layoutInflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.navigation_view, this, true);

		navigationDisplay = (ViewGroup) findViewById(R.id.navigation_display);
		imgViewDirection = (ImageView) findViewById(R.id.img_view_direction);
		textViewDistance = (TextView) findViewById(R.id.text_view_distance);
//		textViewRoad = (TextView) findViewById(R.id.text_view_road);
		textViewWaiting = (TextView) findViewById(R.id.text_view_waiting);
		textViewGenericMessage = (TextView) findViewById(R.id.text_view_generic_message);
		imgViewNextDirection = (ImageView) findViewById(R.id.img_view_next_direction);
		nextDirectionPanel = (LinearLayout) findViewById(R.id.next_direction_panel);
		nextDirectionPanel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(openDirectionViewEvent!=null) {
					openDirectionViewEvent.run();
				}
			}
		});
		findViewById(R.id.direction_infos).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(openDirectionViewEvent!=null) {
					openDirectionViewEvent.run();
				}
			}
		});
		btnPrevItem = (ImageView) findViewById(R.id.btn_prev_item);
		btnPrevItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				currentItemIdx = Math.max(currentItemIdx - 1, 0);
//				refresh(false);
			}
		});
		btnNextItem = (ImageView) findViewById(R.id.btn_next_item);
		btnNextItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				currentItemIdx = Math.min(currentItemIdx + 1, items.size() - 1);
//				refresh(false);
			}
		});
		
		roadPager = (ViewPager) findViewById(R.id.text_view_road_pager);
		roadPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int pos) {
				currentItemIdx = pos;
	        	refresh(false);
			}
		});
		SlideAdapter slideAdapter = new SlideAdapter();
		roadPager.setAdapter(slideAdapter);
		roadPager.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				final int Y = (int) event.getY();
			    switch (event.getAction() & MotionEvent.ACTION_MASK) {
				    case MotionEvent.ACTION_DOWN:
				        mStartingY = Y;
				        break;
				    case MotionEvent.ACTION_MOVE:
				    	move = true;
				    	break;
				    case MotionEvent.ACTION_UP:
				        if(mStartingY != -1000 && move && Y-mStartingY > 20 && openDirectionViewEvent!=null){
				        	openDirectionViewEvent.run();
				        }
				        mStartingY = -1000;
				        move = false;
				        break;
			    }
				return false;
			}
		});
		
		LinearLayout roadPanel = (LinearLayout) findViewById(R.id.road_panel);
		
//		roadPanel.setOnTouchListener(new OnTouchListener(){
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				final int Y = (int) event.getY();
//			    switch (event.getAction() & MotionEvent.ACTION_MASK) {
//				    case MotionEvent.ACTION_DOWN:
//				        mStartingY = Y;
//				        break;
//				    case MotionEvent.ACTION_MOVE:
//				    	move = true;
//				    	break;
//				    case MotionEvent.ACTION_UP:
//				        if(mStartingY != -1000 && move && Y-mStartingY > 20 && openDirectionViewEvent!=null){
//				        	openDirectionViewEvent.run();
//				        }
//				        mStartingY = -1000;
//				        move = false;
//				        break;
//			    }
//				return true;
//			}
//		});

		setStatus(Status.WaitingForGPS);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		if (Status.WaitingForGPS.equals(status)) {
			currentItemIdx = 0;
			textViewWaiting.setVisibility(View.VISIBLE);
			textViewGenericMessage.setVisibility(View.GONE);
			navigationDisplay.setVisibility(View.GONE);
			setBackgroundResource(R.color.transparent_gray);
			this.status = status;
		} else if (Status.OutOfRoute.equals(status)) {
		    if(everInRoute && !rerouting){
		        textViewWaiting.setVisibility(View.GONE);
	            textViewGenericMessage.setVisibility(View.GONE);
	            navigationDisplay.setVisibility(View.VISIBLE);
	            setBackgroundResource(android.R.color.transparent);
		    }else{
    			currentItemIdx = 0;
    			textViewWaiting.setVisibility(View.GONE);
    			textViewGenericMessage.setVisibility(View.VISIBLE);
    			navigationDisplay.setVisibility(View.GONE);
    			setBackgroundResource(R.color.transparent_light_red);
		    }
			this.status = status;
		} else if (Status.InRoute.equals(status)) {
			textViewWaiting.setVisibility(View.GONE);
			textViewGenericMessage.setVisibility(View.GONE);
			navigationDisplay.setVisibility(View.VISIBLE);
			setBackgroundResource(android.R.color.transparent);
			this.status = status;
		} else {
			Log.e(getClass().toString(), "setStatus(): Should not reach here.");
		}
	}

	public static String getDirection(RouteNode node, String distance,
			boolean actionOnly, boolean skipDistance) {
		String roadName = node.getRoadName();
		if (roadName != null) {
			roadName = roadName.replaceAll("-", "");
		}
		String msg = node.getMessage();
		String dir1 = StringUtils.substringBeforeLast(msg, " ");
		String dir2 = StringUtils.substringAfterLast(msg, " ");
		String dir;
		if (actionOnly) {
			dir = dir1;
		} else {
			dir = (skipDistance? "Then ": (StringUtils.isEmpty(distance) ? "" : ("In " + distance + ", ")))
					+ dir1
					+ (StringUtils.isBlank(dir2) ? "" : (" " + dir2))
					+ (StringUtils.isBlank(roadName)
							|| StringUtils.equalsIgnoreCase(roadName, "null") ? ""
							: (" " + roadName));
		}
		return dir;
	}

	public static CharSequence getFormattedDirection(RouteNode node,
			String distance, int largeFontSize) {
		String roadName = node.getRoadName();
		String msg = node.getMessage();
		String[] distToks = distance.split(" ");
		String dir1 = StringUtils.substringBeforeLast(msg, " ");
		String dir2 = StringUtils.substringAfterLast(msg, " ");
		SpannableStringBuilder dir = new SpannableStringBuilder();
		if (StringUtils.isNotEmpty(distance)) {
			dir.append("In ")
					.append(spannable(distToks[0], new AbsoluteSizeSpan(
							largeFontSize))).append(" " + distToks[1] + ", ");
		}
		dir.append(hightlight(dir1, "left right", largeFontSize)).append(
				StringUtils.isBlank(dir2) ? "" : (" " + dir2));
		if (StringUtils.isNotBlank(roadName)
				&& !StringUtils.equalsIgnoreCase(roadName, "null")) {
			dir.append(" ").append(
					spannable(roadName, new AbsoluteSizeSpan(largeFontSize)));
		}
		return dir;
	}

	private static Spannable hightlight(String str, String substr,
			int largeFontSize) {
		SpannableString spannable = SpannableString.valueOf(str);
		for (String s : substr.split(" ")) {
			int len = s.length();
			for (int i : indexesOf(str, s)) {
				spannable.setSpan(new AbsoluteSizeSpan(largeFontSize), i, i
						+ len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return spannable;
	}

	private static List<Integer> indexesOf(String str, String substr) {
		List<Integer> indexes = new ArrayList<Integer>();
		for (int index = str.indexOf(substr); index >= 0; index = str.indexOf(
				substr, index + 1)) {
			indexes.add(index);
		}
		return indexes;
	}

	private static Spannable spannable(CharSequence source, Object... span) {
		SpannableString spannable = SpannableString.valueOf(source);
		int length = source.length();
		for (Object s : span) {
			if (s != null) {
				spannable.setSpan(s, 0, length,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return spannable;
	}

	public static CharSequence getFormattedDirection(RouteNode node,
			double distance, int largeFontSize) {
		return getFormattedDirection(node,
				StringUtil.formatImperialDistance(distance), largeFontSize);
	}

	public static String getContinueDirection(RouteNode node, String distance) {
		String roadName = node.getRoadName();
		if (roadName != null) {
			roadName = roadName.replaceAll("-", "");
		}
		String dir = "Continue"
				+ (StringUtils.isBlank(roadName)
						|| StringUtils.equalsIgnoreCase(roadName, "null") ? ""
						: (" on " + roadName))
				+ (StringUtils.isEmpty(distance) ? "" : (" for " + distance));
		return dir;
	}

	public static double metersToFeet(double meters) {
		return meters * 3.28084;
	}

	public static double metersToMiles(double meters) {
		return meters * 0.000621371;
	}
	
	private static final Integer PORTRAIT_WIDTH = Integer.valueOf(100);
	private static final Integer LANDSCAPE_WIDTH = Integer.valueOf(180);

	private void refresh(boolean forceNoti) {
	    if(items.isEmpty()){
	        return;
	    }
	    
		DirectionItem item = items.get(currentItemIdx);
		
		if (item.drawableId == 0) {
			imgViewDirection.setVisibility(View.INVISIBLE);
		} else {
			imgViewDirection.setImageResource(item.drawableId);
			imgViewDirection.setVisibility(View.VISIBLE);
		}

		int nextItemIdx = currentItemIdx + 1 > items.size() - 1 ? -1
				: currentItemIdx + 1;
		DirectionItem nextItem = null;
		if (nextItemIdx > 0) {
			nextItem = items.get(nextItemIdx);
		}
		
		if(nextItem == null) {
			nextDirectionPanel.setVisibility(View.INVISIBLE);
		}
		else {
			nextDirectionPanel.setVisibility(View.VISIBLE);
			if (nextItem.drawableId == 0) {
				imgViewNextDirection.setVisibility(View.INVISIBLE);
			} else {
				imgViewNextDirection.setImageResource(nextItem.smallDrawableId);
				imgViewNextDirection.setVisibility(View.VISIBLE);
			}
		}

		String distance = StringUtil
				.formatImperialDistance(item.distance, !portraitMode);
		distance = distance.replaceFirst(" ", portraitMode?"\n":"");
		textViewDistance.setText(formatDistance(distance));
		
		int itemSize = items.size();
		boolean isLastItem = itemSize == 1 || currentItemIdx == itemSize - 1;
		
		btnPrevItem.setVisibility(currentItemIdx == 0 ? View.INVISIBLE
				: View.VISIBLE);
		btnNextItem.setVisibility(isLastItem? View.INVISIBLE:View.VISIBLE);
		
		if(nextItem != null) {
			CharSequence roadText = (StringUtils.isBlank(item.roadName) 
		            || StringUtils.equalsIgnoreCase(item.roadName, "null")) ? "" 
		            :(StringUtils.capitalize(item.roadName.substring(0, 1)) 
		            + item.roadName.substring(1));
			notifyIfNecessary(item.direction + ", " + distance + ", " + roadText.toString(), forceNoti);
		}
		
	}
	
	private void refreshDimension() {
		LinearLayout directionInfos = (LinearLayout) findViewById(R.id.direction_infos);
		directionInfos.setOrientation(portraitMode?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
		ViewGroup.LayoutParams directionInfosLp = directionInfos.getLayoutParams();
		directionInfosLp.width = Dimension.dpToPx(portraitMode?PORTRAIT_WIDTH:LANDSCAPE_WIDTH, 
				getContext().getResources().getDisplayMetrics());
		directionInfos.setLayoutParams(directionInfosLp);
		ViewGroup.LayoutParams nextDirectionPanelLp = nextDirectionPanel.getLayoutParams();
		nextDirectionPanelLp.width = Dimension.dpToPx(portraitMode?PORTRAIT_WIDTH:LANDSCAPE_WIDTH, 
				getContext().getResources().getDisplayMetrics());
		nextDirectionPanel.setLayoutParams(nextDirectionPanelLp);
		String distance = textViewDistance.getText().toString();
		if(distance.endsWith("mi")) {
			distance = distance.replaceAll("mi", portraitMode?"\nmiles":"mi");
		}
		else if(distance.endsWith("\nmiles")){
			distance = distance.replaceAll("\nmiles", portraitMode?"\nmiles":"mi");
		}
		else {
			distance = distance.replaceAll("\nmile", portraitMode?"\nmile":"mi");
		}
		textViewDistance.setText(formatDistance(distance));
	}

	public static SpannableString adjustDistanceFontSize(Context ctx,
			String distance) {
		String mi = "mi";
		SpannableString distanceSpan = SpannableString.valueOf(distance);
		int indexOfMi = distance.indexOf(mi);
		distanceSpan.setSpan(new AbsoluteSizeSpan(ctx.getResources()
				.getDimensionPixelSize(R.dimen.smallest_font)), indexOfMi,
				indexOfMi + mi.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return distanceSpan;
	}
	
	private SpannableString formatDistance(String distance) {
		int indexOfMi = distance.indexOf("mi");
		SpannableString distSpan = SpannableString.valueOf(distance);
		distSpan.setSpan(new AbsoluteSizeSpan(getContext().getResources()
				.getDimensionPixelSize(R.dimen.smaller_font)), indexOfMi,
				distance.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return distSpan;
	}

	private List<RouteNode> pendingVoiceForLinkNodes = new ArrayList<RouteNode>();
	
	public void update(final Route route, final Location location,
			final RouteNode node, List<DirectionItem> dirItems) {
		items = dirItems;
		updateViewPager(dirItems);
		currentItemIdx = Math.min(currentItemIdx, items.size() - 1);
		roadPager.setCurrentItem(currentItemIdx);
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		double distance = route.getDistanceToNextTurn(latitude, longitude);
		double distanceInMile = metersToMiles(distance);
		double distanceInFoot = metersToFeet(distance);
		ValidationParameters params = ValidationParameters.getInstance();
        RouteLink nearestLink = route.getNearestLink(latitude, longitude);
        if (nearestLink.distanceTo(latitude, longitude) <= params
                .getInRouteDistanceThreshold()) {
			setStatus(Status.InRoute);

			refresh(false);

			if (node.hasMetadata()) {
				RouteNode end = nearestLink.getEndNode();
				while (end.getFlag() == 0 && end.getNextNode() != null) {
					end = end.getNextNode();
				}
				boolean continueDir = false;
				if (!everInRoute) {
					everInRoute = true;
					continueDir = true;
				} else if (end != lastEnd) {
					continueDir = true;
				}
				lastEnd = end;

				if(hasVoice){
				    RouteNode firstNode = route.getFirstNode();
				    Metadata firstMetadata = firstNode.getMetadata();
				    double firstNodeDist = metersToFeet(firstNode.distanceTo(latitude, longitude));
				    if(!firstMetadata.pingFlags[1]
				            && firstNodeDist <= firstNode.getVoiceRadius()){
				        firstMetadata.pingFlags[1] = true;
                        String text = firstNode.getVoice();
                        if(listener != null && StringUtils.isNotBlank(text)){
                            listener.onCheckPoint(text, false, false);
                        }
				    }
				    for(RouteNode vflNode : pendingVoiceForLinkNodes){
				        Number radiusObj = (Number)Request.getSetting(Setting.intersection_radius_in_meter);
				        double radius = radiusObj == null?0:radiusObj.doubleValue();
				        if(vflNode.distanceTo(latitude, longitude) >= radius){
				            pendingVoiceForLinkNodes.remove(vflNode);
				        }
				    }
				    
				    double speedMph = Trajectory.msToMph(location.getSpeed());
		            float bearing = location.getBearing();
		            float accuracy = location.getAccuracy();
		            double distanceLimit = ((Number)Request.getSetting(Setting.reroute_trigger_distance_in_meter)).doubleValue() + accuracy;
		            List<RouteLink> nearbyLinks = route.getNearbyLinks(latitude, longitude, distanceLimit);
		            List<RouteLink> sameDirLinks = route.getSameDirectionLinks(nearbyLinks, speedMph, bearing);
				    if(!Route.isPending(nearbyLinks, sameDirLinks) && sameDirLinks.size() > 0){
				        RouteLink nearestLinkVoiceForLink = Route.getClosestLink(sameDirLinks, latitude, longitude);
				        RouteNode startNode = nearestLinkVoiceForLink.getStartNode();
		                RouteNode.Metadata startMetadata = startNode.getMetadata();
		                if (!startMetadata.pingFlags[0]) {
	                        startMetadata.pingFlags[0] = true;
	                        String text = startNode.getVoiceForLink();
	                        if(listener != null && StringUtils.isNotBlank(text)){
	                            pendingVoiceForLinkNodes.add(startNode);
	                            listener.onCheckPoint(text, false, false);
	                        }
	                    }
				    }
				    RouteNode endNode = nearestLink.getEndNode();
				    while(StringUtils.isBlank(endNode.getVoice()) && endNode.getNextNode() != null){
				        endNode = endNode.getNextNode();
				    }
				    RouteNode.Metadata endMetadata = endNode.getMetadata();
				    double dist = metersToFeet(endNode.distanceTo(latitude, longitude));
                    if (!endMetadata.pingFlags[1]
                            && dist <= endNode.getVoiceRadius()) {
                        endMetadata.pingFlags[1] = true;
                        String text = endNode.getVoice();
                        if(listener != null && StringUtils.isNotBlank(text)){
                            listener.onCheckPoint(text, false, false);
                        }
                    }
				}else{
    				String formattedDist = StringUtil
    						.formatImperialDistance(distance);
    
    				if (continueDir) {
    					if (listener != null) {
    						RouteNode prevNode = end.getPrevNode();
    						if (prevNode == null) {
    							prevNode = end;
    						}
    						listener.onCheckPoint(getContinueDirection(prevNode, formattedDist) 
    					        + ". " + getDirection(node, null, false, true), false, true);
    					}
    					lastCheckPointDistanceInMile = distanceInMile;
    				} else {
    					double linkDistance = 0;
    					while ((end = end.getPrevNode()) != null) {
    						linkDistance += end.getDistance();
    						if (end.getFlag() != 0) {
    							break;
    						}
    					}
    
    					double linkDistanceInMile = metersToMiles(linkDistance);
    					RouteNode.Metadata metadata = node.getMetadata();
    
    					String checkpointDistance = null;
    					boolean actionOnly = false;
    					if (!metadata.pingFlags[0]
    							&& distanceInFoot <= 100 + checkPointFeetOffset) {
    						metadata.pingFlags[0] = true;
    						metadata.pingFlags[1] = true;
    						metadata.pingFlags[2] = true;
    						metadata.pingFlags[3] = true;
    						metadata.pingFlags[4] = true;
    						checkpointDistance = "";
    						actionOnly = true;
    					} else if (!metadata.pingFlags[1]
    							&& distanceInMile <= 0.2 + checkPointMilesOffset) {
    						metadata.pingFlags[1] = true;
    						metadata.pingFlags[2] = true;
    						metadata.pingFlags[3] = true;
    						metadata.pingFlags[4] = true;
    						if(lastCheckPointDistanceInMile - distanceInMile >= 0.5){
        						checkpointDistance = linkDistanceInMile >= 0.2 ? "0.2 miles"
        								: formattedDist;
    						}
    					} else if (!metadata.pingFlags[2]
    							&& distanceInMile <= 0.5 + checkPointMilesOffset) {
    						metadata.pingFlags[2] = true;
    						metadata.pingFlags[3] = true;
    						metadata.pingFlags[4] = true;
    						if(lastCheckPointDistanceInMile - distanceInMile >= 1.0){
        						checkpointDistance = linkDistanceInMile >= 0.5 ? "0.5 miles"
        								: formattedDist;
    						}
    					} else if (!metadata.pingFlags[3]
    							&& distanceInMile <= 1.0 + checkPointMilesOffset) {
    						metadata.pingFlags[3] = true;
    						metadata.pingFlags[4] = true;
    						if(lastCheckPointDistanceInMile - distanceInMile >= 1.0){
        						checkpointDistance = linkDistanceInMile >= 1.0 ? "1 mile"
        								: formattedDist;
    						}
    					} else if (!metadata.pingFlags[4]
    							&& distanceInMile <= 2.0 + checkPointMilesOffset) {
    						metadata.pingFlags[4] = true;
    						if(lastCheckPointDistanceInMile - distanceInMile >= 1.0){
        						checkpointDistance = linkDistanceInMile >= 2.0 ? "2 miles"
        								: formattedDist;
    						}
    					}
    
    					if (listener != null && checkpointDistance != null) {
    						listener.onCheckPoint(getDirection(node, checkpointDistance, 
    					        actionOnly, false), false, true);
    						lastCheckPointDistanceInMile = distanceInMile;
    					}
    				}
				}
			}
		} else {
			String routeMsg = rerouting?"Rerouting":"Out of route. Please go back to route."; 
			if(rerouting) {
				notifyIfNecessary(routeMsg, false);
			}
			String startFromRouteMsg = "Proceed to";
			RouteNode roaddNode = route.getFirstNode();
			while (roaddNode != null) {
				String roadName = roaddNode.getRoadName();
				if (StringUtils.isNotBlank(roadName)) {
					startFromRouteMsg += " " + roadName;
					break;
				} 
				roaddNode = roaddNode.getNextNode();
			}
			boolean speakRerouting = rerouting && !lastRerouting;
			if (everInRoute && (status != Status.OutOfRoute || speakRerouting) 
			        && listener != null) {
			    if(speakRerouting){
			        listener.onCheckPoint("", speakRerouting, true);
			    }
				if(speakRerouting && DebugOptionsActivity.isReroutingNotificationSoundEnabled(getContext())){
                    Misc.playDefaultNotificationSound(getContext());
                }
			} else if (!everInRoute
					&& (status == null || status == Status.WaitingForGPS)
					&& listener != null) {
				listener.onCheckPoint(startFromRouteMsg, true, true);
			}
			lastRerouting = rerouting;
			setStatus(Status.OutOfRoute);
			textViewGenericMessage.setText(everInRoute?routeMsg:startFromRouteMsg);
		}
	}
	
	private static final Integer ID = 123451;
	
	private void notifyIfNecessary(String message, boolean force) {
		if(force || !notifiedMsg.equalsIgnoreCase(message) && needNotification) {
			notifiedMsg = message;
			Intent validationIntent = new Intent(getContext(), MainActivity.class);
			validationIntent.setAction(Intent.ACTION_MAIN);
			validationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	        PendingIntent sender = PendingIntent.getActivity(getContext(), ID, validationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.drawable.icon_small, "Metropia", System.currentTimeMillis());
            notification.setLatestEventInfo(getContext(), "Metropia", message, sender);
            notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;            
            notificationManager.notify(ID, notification);
		}
	}
	
	public static void removeNotification(Context ctx) {
		NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(ID);
	}
	
	public void startNotification() {
		this.needNotification = true;
		refresh(true);
	}
	
	public void stopNotification() {
		this.needNotification = false;
		removeNotification(getContext());
	}

	public static interface CheckPointListener {

		void onCheckPoint(String navText, boolean flush, boolean delay);

	}

	public CheckPointListener getListener() {
		return listener;
	}

	public void setListener(CheckPointListener cpListener) {
		this.listener = cpListener;
	}

	public void setTypeface(Typeface font) {
		Font.setTypeface(font, textViewGenericMessage, textViewWaiting,
				textViewDistance);
	}

    public boolean isRerouting() {
        return rerouting;
    }

    public void setRerouting(boolean rerouting) {
        this.rerouting = rerouting;
        pendingVoiceForLinkNodes.clear();
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public boolean isHasVoice() {
        return hasVoice;
    }

    public void setHasVoice(boolean hasVoice) {
        this.hasVoice = hasVoice;
    }
    
    public void setOpenDirectionViewEvent(Runnable event) {
    	this.openDirectionViewEvent = event;
    }

	public void setLandscapMode() {
		this.portraitMode = false;
		refreshDimension();
	}
	
	public void setPortraitMode() {
		this.portraitMode = true;
		refreshDimension();
	}
	
	public void setToCurrentDireciton(){
	    currentItemIdx = 0;
        refresh(false);
	}

	public void setTextViewWaiting(String text){
	    textViewWaiting.setText(text);
	}

	private void updateViewPager(List<DirectionItem> items) {
	    SlideAdapter adapter = new SlideAdapter();
		for(int i = 0 ; i < items.size() ; i++) {
			DirectionItem item = items.get(i);
			CharSequence roadText = (StringUtils.isBlank(item.roadName) 
		            || StringUtils.equalsIgnoreCase(item.roadName, "null")) ? "" 
		            :(StringUtils.capitalize(item.roadName.substring(0, 1)) 
		            + item.roadName.substring(1));
			int itemSize = items.size();
		    boolean isLastItem = itemSize == 1 || i == itemSize - 1;
			if(isLastItem){
			    SpannableStringBuilder roadTextSpan = new SpannableStringBuilder()
			        .append(roadText + "\n")
			        .append(spannable(destinationAddress, 
			            new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.smaller_font))));
			    roadText = roadTextSpan;
			}
			
			LinearLayout layout = new LinearLayout(getContext());
			layout.setVisibility(VISIBLE);

			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.road_slide, layout);
			TextView roadTextView = (TextView)v.findViewById(R.id.road);
			roadTextView.setText(roadText);
			adapter.addView(v);
		}
		roadPager.setAdapter(adapter);
	}
	
	public static class SlideAdapter extends PagerAdapter {
        
		  private ArrayList<View> views = new ArrayList<View>();

		  @Override
		  public int getItemPosition(Object object) {
			  int index = views.indexOf (object);
			  if (index == -1) 
			      return POSITION_NONE;
			  else
			      return index;
		  }

		  @Override
		  public Object instantiateItem(ViewGroup container, int position) {
		      View v = views.get (position);
		      container.addView (v);
		      return v;
		  }

		  @Override
		  public void destroyItem(ViewGroup container, int position, Object object) {
		      container.removeView(views.get(position));
		  }

		  @Override
		  public int getCount() {
		      return views.size();
		  }

		  @Override
		  public boolean isViewFromObject(View view, Object object) {
		      return view == object;
		  }

		  public int addView(View v) {
		      return addView(v, views.size());
		  }

		  public int addView(View v, int position) {
		      views.add(position, v);
		      notifyDataSetChanged();
		      return position;
		  }

		  public int removeView(ViewPager pager, View v) {
		      return removeView(pager, views.indexOf (v));
		  }

		  public int removeView (ViewPager pager, int position) {
		      pager.setAdapter (null);
		      views.remove (position);
		      pager.setAdapter (this);
		      notifyDataSetChanged();
		      return position;
		  }

		  public View getView (int position) {
		      return views.get (position);
		  }
		  
		  public void removeAllView(ViewPager pager) {
			  pager.setAdapter(null);
		      views.clear();
		      pager.setAdapter(this);
		      notifyDataSetChanged();
		  }
    }

}
