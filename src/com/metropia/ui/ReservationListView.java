package com.metropia.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.actionbarsherlock.internal.nineoldandroids.animation.AnimatorSet;
import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
import com.metropia.activities.ContactsSelectActivity;
import com.metropia.activities.DebugOptionsActivity;
import com.metropia.activities.LandingActivity2;
import com.metropia.activities.MapDisplayActivity;
import com.metropia.activities.R;
import com.metropia.activities.RouteActivity;
import com.metropia.activities.ValidationActivity;
import com.metropia.activities.LandingActivity2.BalloonModel;
import com.metropia.dialogs.CancelableProgressDialog;
import com.metropia.dialogs.NotificationDialog2;
import com.metropia.dialogs.NotificationDialog2.ActionListener;
import com.metropia.models.PoiOverlayInfo;
import com.metropia.models.Reservation;
import com.metropia.models.ReservationTollHovInfo;
import com.metropia.models.Route;
import com.metropia.models.User;
import com.metropia.requests.Request;
import com.metropia.requests.ReservationDeleteRequest;
import com.metropia.requests.ReservationListFetchRequest;
import com.metropia.requests.ReservationRequest;
import com.metropia.requests.RouteFetchRequest;
import com.metropia.tasks.ICallback;
import com.metropia.ui.animation.ClickAnimation;
import com.metropia.ui.animation.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.ui.timelayout.AdjustableTime;
import com.metropia.utils.Dimension;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.Geocoding;
import com.metropia.utils.Misc;
import com.metropia.utils.SystemService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ReservationListView extends FrameLayout implements OnClickListener {
	
	public static final int ON_MY_WAY = Integer.valueOf(100);
	

	public Long dismissReservId = Long.valueOf(-1);
    private Boolean swipeRight = Boolean.FALSE;
    private List<Long> removedReservIds = new ArrayList<Long>();
    public AtomicBoolean disableRefreshTripInfo = new AtomicBoolean(false);
    public AtomicBoolean closeIfEmpty = new AtomicBoolean(true);
    private ICallback refreshCallback;
    
    private LinearLayout reservationListPanel;
    private View newUserTipView;

	int[] clickableAnimated = {R.id.reservation_list_menu, R.id.reservation_head_add, R.id.add_new_reservation_panel, R.id.tip_close};
	
	public ReservationListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		LayoutInflater.from(context).inflate(R.layout.reservations_list, this);
		
		ClickAnimation.OnClickListener onClickListener = new ClickAnimation.OnClickListener(this);
        for (int i=0 ; i<clickableAnimated.length ; i++) findViewById(clickableAnimated[i]).setOnClickListener(onClickListener);
        
        initReservationListView();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.reservation_list_menu:
				DrawerLayout mDrawerLayout = (DrawerLayout) LandingActivity2.getInstance().findViewById(R.id.drawer_layout);
				LandingActivity2.getInstance().toggleMenu();
			break;
			case R.id.reservation_head_add:
			case R.id.add_new_reservation_panel:
				dismissReservId = getFirstReservation() != null ? getFirstReservation().getRid() : -1;
				closeIfEmpty.set(true);
				hideTripInfoPanel();
				LandingActivity2.getInstance().centerMap();
			break;
			case R.id.tip_close:
				newUserTipView.setVisibility(View.GONE);
				DebugOptionsActivity.userCloseTip(getContext());
				int reservCount = 0;
				for(int i = 0 ; i < reservationListPanel.getChildCount() ; i++) {
					View child = reservationListPanel.getChildAt(i);
					if(child.getTag() != null && child.getTag() instanceof Reservation) {
						reservCount++;
					}
				}
				
				if(reservCount < 2) findViewById(R.id.add_new_trip_background).setVisibility(View.VISIBLE);
			break;
		}
	}
	
	public void setRefreshCallback(ICallback refreshCallback) {
		this.refreshCallback = refreshCallback;
	}
    
    private void initReservationListView() {
    	reservationListPanel = (LinearLayout) findViewById(R.id.reservation_list);
    	
    	newUserTipView = findViewById(R.id.new_user_tip);
    	newUserTipView.setVisibility(DebugOptionsActivity.isUserCloseTip(getContext())?View.GONE:View.VISIBLE);
    	
    	ImageView tipCloseView = (ImageView) findViewById(R.id.tip_close);
    	if(!DebugOptionsActivity.isUserCloseTip(getContext())) {
    		tipCloseView.setImageBitmap(Misc.getBitmap(getContext(), R.drawable.tip_close, 1));
    	}
    	
    	
    	SwipeDeleteTouchListener touchListener =
                new SwipeDeleteTouchListener(reservationListPanel, 
                        new SwipeDeleteTouchListener.OnDismissCallback() {
                            @Override
                            public void onDismiss(View tripInfoView, final List<Reservation> reservs) {
                                final Reservation reserv = (Reservation) tripInfoView.getTag();
                                final Long removeReservId = reserv.getRid();
                                AsyncTask<Void, Void, Boolean> delTask = new AsyncTask<Void, Void, Boolean>(){
                                	@Override
                                	protected void onPreExecute() {
                                		removedReservIds.add(removeReservId);
                                		closeIfEmpty.set(true);
                                		refreshReservationList(reservs);
                                	}
                                	
            		                @Override
            		                protected Boolean doInBackground(Void... params) {
            		                    ReservationDeleteRequest request = new ReservationDeleteRequest(
            		                        User.getCurrentUser(getContext()), reserv.getRid());
            		                    Boolean success = Boolean.TRUE;
            		                    try {
            		                        request.execute(getContext());
            		                    }
            		                    catch (Exception e) {
            		                      	success = Boolean.FALSE;
            		                    }
            		                    return success;
            		                }
            		                    
            		                @Override
            		                protected void onPostExecute(Boolean success) {
            		                 	if(success) {
            		                   		DebugOptionsActivity.removeReservRecipients(getContext(), reserv.getRid());
            		                   		removedReservIds.remove(removeReservId);
            		                   	}
            		                }
            		            };
            		            Misc.parallelExecute(delTask);
                            }

							@Override
							public void onDismissRight() {}

                         });
    	reservationListPanel.setOnTouchListener(touchListener);
    }
    
    
    private View createReservationInfoView(final Reservation reserv, boolean isFirst) {
    	View reservInfo = LayoutInflater.from(getContext()).inflate(R.layout.reservation_trip_info, reservationListPanel, false);
    	
    	String arrivalTime = StringUtils.replace(formatTime(reserv.getArrivalTimeUtc(), reserv.getRoute().getTimezoneOffset(), false), " ", "");
    	String durationTimeDesc = "";
    	String arrivalTimeDesc = "";
    	int backgroundColor = R.color.metropia_orange;
    	int startTimeVisible = View.VISIBLE;
    	int durationTimeVisible = View.VISIBLE;
    	int startButtonResourceId = R.drawable.reservation_start_trip_transparent;
    	long departureTimeUtc = reserv.getDepartureTimeUtc();
    	String nextTripStartTime = "";
        long timeUntilDepart = departureTimeUtc - System.currentTimeMillis();
        long durationTime = reserv.getDuration();  //sec
        String originDesc = StringUtils.isNotBlank(reserv.getOriginName()) ? reserv.getOriginName() : reserv.getOriginAddress();
        String destDesc = StringUtils.isNotBlank(reserv.getDestinationName()) ? reserv.getDestinationName() : reserv.getDestinationAddress();
        final boolean lessThanOneMinite = timeUntilDepart < 1 * 60 * 1000;
        if(reserv.isEligibleTrip()){
        	startTimeVisible = lessThanOneMinite && isFirst ? View.GONE : View.VISIBLE;
        	durationTimeVisible = lessThanOneMinite ? View.VISIBLE : View.GONE;
            backgroundColor = isFirst ? (lessThanOneMinite ? R.color.metropia_green : R.color.metropia_orange) : R.color.metropia_blue;
            if(isFirst && !lessThanOneMinite) {
            	int countDownMins = Double.valueOf(Math.ceil(Double.valueOf(timeUntilDepart)/Double.valueOf(60*1000))).intValue();
            	nextTripStartTime = String.format("%d\nMINS", countDownMins);
            }
            else {
            	nextTripStartTime = StringUtils.replace(formatTime(departureTimeUtc, reserv.getRoute().getTimezoneOffset(), !isFirst), " ", "\n");
            }
            arrivalTime = lessThanOneMinite ? formatTime(System.currentTimeMillis() + durationTime*1000, reserv.getRoute().getTimezoneOffset(), false) : arrivalTime;
            arrivalTimeDesc = "Arrival: " + arrivalTime;
            durationTimeDesc = "Duration: " + getFormattedDuration(Long.valueOf(durationTime).intValue());
            startButtonResourceId = isFirst ? (lessThanOneMinite ? R.drawable.reservation_start_trip : R.drawable.reservation_start_trip_transparent) : R.drawable.reservation_start_trip_disable;
        }else {
        	startTimeVisible = View.VISIBLE;
        	durationTimeVisible = View.GONE;
            nextTripStartTime = StringUtils.replace(formatTime(departureTimeUtc, reserv.getRoute().getTimezoneOffset(), !isFirst), " ", "\n");
            backgroundColor = isFirst ? R.color.metropia_orange : R.color.metropia_blue;
            arrivalTimeDesc = "Arrival: " + arrivalTime;
            durationTimeDesc = "Duration: " + getFormattedDuration(Long.valueOf(durationTime).intValue());
            startButtonResourceId = isFirst ? R.drawable.reservation_start_trip_transparent : R.drawable.reservation_start_trip_disable;
        }
        
        reservInfo.findViewById(R.id.trip_info_desc).setVisibility(isFirst?View.VISIBLE:View.GONE);
        
        TextView tripInfoFromAddressView =  (TextView) reservInfo.findViewById(R.id.trip_info_from_address);
    	tripInfoFromAddressView.setText(originDesc);
    	TextView tripInfoToAddressView =  (TextView) reservInfo.findViewById(R.id.trip_info_to_address);
    	tripInfoToAddressView.setText(destDesc);
    	TextView timeToGo = (TextView) reservInfo.findViewById(R.id.time_to_go_desc);
    	timeToGo.setText(startTimeVisible == View.GONE ? "It's Time to Go!" : (reserv.isEligibleTrip() ? "WILL START IN:" : ""));
    	timeToGo.setVisibility(isFirst?View.VISIBLE:View.GONE);
        reservInfo.setBackgroundColor(getResources().getColor(backgroundColor));
        reservInfo.setTag(reserv);
        TextView tripDurationTimeView = (TextView) reservInfo.findViewById(R.id.reservation_duration_time);
        tripDurationTimeView.setVisibility(durationTimeVisible);
        tripDurationTimeView.setText(formatTripTime(durationTimeDesc));
        TextView tripArrivalTimeView = (TextView) reservInfo.findViewById(R.id.reservation_arrive_time);
        tripArrivalTimeView.setVisibility(View.VISIBLE);
        tripArrivalTimeView.setText(formatTripTime(arrivalTimeDesc));
        TextView tripStartTimeView = (TextView) reservInfo.findViewById(R.id.reservation_start_time);
        tripStartTimeView.setVisibility(startTimeVisible);
        if(isFirst && reserv.isEligibleTrip()) {
        	tripStartTimeView.setText(formatCountDownTime(nextTripStartTime));
        	tripStartTimeView.setPadding(0, Dimension.dpToPx(8, getResources().getDisplayMetrics()), 0, 0);
        }
        else {
        	tripStartTimeView.setText(formatStartTripTime(nextTripStartTime));
        	tripStartTimeView.setPadding(0, 0, 0, 0);
        }
        ImageView startButton = (ImageView) reservInfo.findViewById(R.id.reservation_start_button);
        startButton.setImageBitmap(Misc.getBitmap(getContext(), startButtonResourceId, 1));
//        startButton.setImageResource(startButtonResourceId);
        reservInfo.findViewById(R.id.reservation_trip_times).setVisibility(isFirst?View.VISIBLE:View.GONE);
//        reservInfo.findViewById(R.id.leave_label).setVisibility((isFirst && !reserv.isEligibleTrip())?View.VISIBLE:View.GONE);
        reservInfo.findViewById(R.id.center_line).setVisibility(isFirst? View.GONE : View.VISIBLE);
        
        if(isFirst) {
        	startButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					ClickAnimation clickAnimation = new ClickAnimation(getContext(), v);
					clickAnimation.startAnimation(new ClickAnimationEndCallback() {
						@Override
						public void onAnimationEnd() {
							if(reserv.isEligibleTrip()) {
								LandingActivity2.getInstance().startValidationActivity(reserv);
							}
							else {
								NotificationDialog2 dialog = new NotificationDialog2(getContext(), "Would you like to start your trip early?");
								dialog.setVerticalOrientation(false);
								dialog.setTitle("");
								dialog.setPositiveButtonText("Yes");
								dialog.setPositiveActionListener(new ActionListener() {
									@Override
									public void onClick() {
									    GeoPoint origin = null;
									    final ReservationTollHovInfo reservationInfo = MapDisplayActivity.getReservationTollHovInfo(getContext(), reserv.getRid());
									    if(LandingActivity2.getInstance().myPoint != null){
									        origin = new GeoPoint(LandingActivity2.getInstance().myPoint.getLatitude(), LandingActivity2.getInstance().myPoint.getLongitude());
									    }
										RescheduleTripTask rescheduleTask = new RescheduleTripTask(getContext(), origin, null, reserv.getDestinationAddress(), reserv.getRid(), "", new ExceptionHandlingService(getContext()), reservationInfo);
										rescheduleTask.callback = new RescheduleTripTask.Callback() {
				                            @Override
				                            public void run(Reservation reservation) {
				                            	Log.d("LandingActivity2", "Reschedule trip start");
				                            	reservationInfo.setReservationId(reservation.getRid());
				                            	MapDisplayActivity.addReservationTollHovInfo(getContext(), reservationInfo);
				                            	LandingActivity2.getInstance().startValidationActivity(reservation);
				                            }
				                        };
				                        Misc.parallelExecute(rescheduleTask);
									}
								});
								dialog.setNegativeButtonText("No");
								dialog.setNegativeActionListener(new ActionListener() {
									@Override
									public void onClick() {
										//do nothing
									}
								});
								dialog.show();
							}
						}
					});
				}
        	});
        }
        
        TextView reservationOnMyWay = (TextView) reservInfo.findViewById(R.id.reservation_on_my_way);
        reservationOnMyWay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation clickAnimation = new ClickAnimation(getContext(), v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						v.setClickable(true);
						Intent contactSelect = new Intent(getContext(), ContactsSelectActivity.class);
					    JSONObject reservRecipients = DebugOptionsActivity.getReservRecipients(getContext(), reserv.getRid());
					    if(reservRecipients != null) {
						    contactSelect.putExtra(ContactsSelectActivity.SELECTED_EMAILS, reservRecipients.optString(ValidationActivity.EMAILS, ""));
						    contactSelect.putExtra(ContactsSelectActivity.SELECTED_PHONES, reservRecipients.optString(ValidationActivity.PHONES, ""));
					    }
						((FragmentActivity) getContext()).startActivityForResult(contactSelect, ON_MY_WAY);
					}
				});
			}
        });
        
        View reservReschedule = reservInfo.findViewById(R.id.reschedule_panel);
        reservReschedule.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation clickAnimation = new ClickAnimation(getContext(), v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						try {
			                Intent intent = new Intent(getContext(), RouteActivity.class);
			                Bundle extras = new Bundle();
			                extras.putLong(RouteActivity.RESCHEDULE_RESERVATION_ID, reserv.getRid());
			                extras.putString("originAddr", reserv.getOriginAddress());
			                extras.putParcelable(RouteActivity.ORIGIN_COORD, reserv.getStartGpFromNavLink());
			                extras.putString(RouteActivity.ORIGIN_COORD_PROVIDER, null);
			                extras.putLong(RouteActivity.ORIGIN_COORD_TIME, 0);
			                extras.putParcelable(RouteActivity.ORIGIN_OVERLAY_INFO, getPoiInfo(reserv.getStartGpFromNavLink().getLatitude(), reserv.getStartGpFromNavLink().getLongitude(), reserv.getOriginAddress(), reserv.getOriginName()));
			                extras.putString("destAddr", reserv.getDestinationAddress());
			                extras.putParcelable(RouteActivity.DEST_COORD, reserv.getEndGpFromNavLink());
			                extras.putLong(RouteActivity.RESCHEDULE_DEPARTURE_TIME, reserv.getDepartureTimeUtc());
			                extras.putParcelable(RouteActivity.DEST_OVERLAY_INFO, getPoiInfo(reserv.getEndGpFromNavLink().getLatitude(), reserv.getEndGpFromNavLink().getLongitude(), reserv.getDestinationAddress(), reserv.getDestinationName()));
			                intent.putExtras(extras);
			                getContext().startActivity(intent);
			                ((Activity) getContext()).finish();
						}
						catch(Exception e) {}
						v.setClickable(true);
					}
				});
			}
        });
        
        reservInfo.findViewById(R.id.trip_od_detail).setVisibility(isFirst?View.GONE:View.VISIBLE);
        reservInfo.findViewById(R.id.reservation_on_my_way).setVisibility(isFirst?View.VISIBLE:View.GONE);
        
        TextView fromAddressView = (TextView) reservInfo.findViewById(R.id.od_from_address);
        fromAddressView.setText(originDesc);
        TextView toAddressView = (TextView) reservInfo.findViewById(R.id.od_to_address);
        toAddressView.setText(destDesc);
        
        Font.setTypeface(Font.getRobotoBold(getContext().getAssets()), timeToGo, tripDurationTimeView, tripArrivalTimeView, tripStartTimeView, tripInfoFromAddressView, tripInfoToAddressView);
        Font.setTypeface(Font.getRobotoLight(getContext().getAssets()), reservationOnMyWay, (TextView) reservInfo.findViewById(R.id.reschedule_desc));
        
        
        return reservInfo;
    }
    
    private View createEmptyReservationInfoView() {
    	FrameLayout emptyReservInfo = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.reservation_trip_info, reservationListPanel, false);
    	int startButtonResourceId = R.drawable.reservation_start_trip_transparent;
        emptyReservInfo.findViewById(R.id.trip_info_desc).setVisibility(View.GONE);
    	TextView timeToGo = (TextView) emptyReservInfo.findViewById(R.id.time_to_go_desc);
    	timeToGo.setVisibility(View.GONE);
        TextView tripDurationTimeView = (TextView) emptyReservInfo.findViewById(R.id.reservation_duration_time);
        tripDurationTimeView.setVisibility(View.GONE);
        TextView tripArrivalTimeView = (TextView) emptyReservInfo.findViewById(R.id.reservation_arrive_time);
        tripArrivalTimeView.setVisibility(View.INVISIBLE);
        TextView tripStartTimeView = (TextView) emptyReservInfo.findViewById(R.id.reservation_start_time);
        tripStartTimeView.setVisibility(View.INVISIBLE);
        ImageView startButton = (ImageView) emptyReservInfo.findViewById(R.id.reservation_start_button);
        startButton.setImageBitmap(Misc.getBitmap(getContext(), startButtonResourceId, 1));
//        startButton.setImageResource(startButtonResourceId);
        startButton.setVisibility(View.INVISIBLE);
        emptyReservInfo.findViewById(R.id.reservation_trip_times).setVisibility(View.GONE);
//        emptyReservInfo.findViewById(R.id.leave_label).setVisibility(View.GONE);
        emptyReservInfo.findViewById(R.id.center_line).setVisibility(View.INVISIBLE);
        emptyReservInfo.findViewById(R.id.reservation_on_my_way).setVisibility(View.INVISIBLE);
        emptyReservInfo.findViewById(R.id.reschedule_panel).setVisibility(View.INVISIBLE);
        emptyReservInfo.findViewById(R.id.center_line).setVisibility(View.VISIBLE);;
        return emptyReservInfo;
    }
    
    public void refreshReservationList(List<Reservation> reservations) {
    	reservationListPanel.removeAllViews();
    	Reservation notifyReserv = null;
    	int curReservIdx = -1;
    	boolean cont = true;
    	while(curReservIdx < (reservations.size()-1) && cont) {
    		curReservIdx++;
    		Reservation tempReserv = reservations.get(curReservIdx);
    		if(!tempReserv.hasExpired()) {
    			notifyReserv = tempReserv;
    			cont = false;
    		}
    	}
    	
    	if (refreshCallback!=null) refreshCallback.run(notifyReserv);
    	
    	
    	if(!cont) {
    		TextView nextDesc = new TextView(getContext());
    		LinearLayout.LayoutParams nextDescLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    		nextDesc.setLayoutParams(nextDescLp);
    		nextDesc.setGravity(Gravity.CENTER);
    		nextDesc.setText("NEXT");
    		nextDesc.setTextColor(getResources().getColor(android.R.color.white));
    		nextDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
    		nextDesc.setBackgroundColor(getResources().getColor(android.R.color.black));
    		reservationListPanel.addView(nextDesc);
    		
    		for(int i = curReservIdx ; i < reservations.size() ; i++) {
    			Reservation reserv = reservations.get(i);
    			if(!removedReservIds.contains(reserv.getRid())) {
    				boolean isFirst = i == curReservIdx;
    				View reservInfoView = createReservationInfoView(reserv, isFirst);
    				if(isFirst) {
    					MapDisplayActivity.cleanReservationTollHovInfoBeforeId(getContext(), reserv.getRid());
    				}
    				reservationListPanel.addView(reservInfoView);
    				if(isFirst) {
    					TextView scheduledDesc = new TextView(getContext());
    		    		LinearLayout.LayoutParams scheduledDescLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    		    		scheduledDesc.setLayoutParams(scheduledDescLp);
    		    		scheduledDesc.setGravity(Gravity.CENTER);
    		    		scheduledDesc.setText("SCHEDULED");
    		    		scheduledDesc.setTextColor(getResources().getColor(android.R.color.white));
    		    		scheduledDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
    		    		scheduledDesc.setBackgroundColor(getResources().getColor(android.R.color.black));
    		    		reservationListPanel.addView(scheduledDesc);
    				}
    				else if(i != reservations.size() - 1) {
    					View spliter = new View(getContext());
    					LinearLayout.LayoutParams spliterLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Dimension.dpToPx(1, getResources().getDisplayMetrics()));
    					spliter.setLayoutParams(spliterLp);
    					spliter.setBackgroundColor(getResources().getColor(R.color.light_gray));
    					reservationListPanel.addView(spliter);
    				}
    				
    			}
    		}
    		
    		if (!disableRefreshTripInfo.get()) showTripInfoPanel(false, false);
    	}
    	else {
    		dismissReservId = -1L;
    		if(closeIfEmpty.get()) {
    			hideTripInfoPanel();
    		}
    	}
 
    	int reservCount = curReservIdx == -1 ? 0 : reservations.size() - curReservIdx;
    	if((reservCount == 1 && !DebugOptionsActivity.isUserCloseTip(getContext())) || reservCount > 1) {
			findViewById(R.id.add_new_trip_background).setVisibility(View.GONE);
			if(reservCount > 1) {
				newUserTipView.setVisibility(View.GONE);
				if(reservCount == 2) {
					View emptyView = createEmptyReservationInfoView();
					reservationListPanel.addView(emptyView);
				}
			}
			else {
				newUserTipView.setVisibility(DebugOptionsActivity.isUserCloseTip(getContext())?View.GONE:View.VISIBLE);
			}
		}
		else {
			findViewById(R.id.add_new_trip_background).setVisibility(View.VISIBLE);
			newUserTipView.setVisibility(DebugOptionsActivity.isUserCloseTip(getContext())?View.GONE:View.VISIBLE);
		}
    }
    
    
    public void showTripInfoPanel(boolean force, boolean animation) {
    	View tripInfoPanel = findViewById(R.id.reservationList);
    	if((force && hasReservTrip()) || 
    			(tripInfoPanel.getVisibility() != View.VISIBLE && hasReservTrip() && !dismissReservId.equals(getFirstReservation().getRid()))) {
    		tripInfoPanel.setVisibility(View.VISIBLE);
    		if(animation) {
    			float fromX = swipeRight?tripInfoPanel.getWidth():-1*tripInfoPanel.getWidth();
	    		ObjectAnimator slideAnimator = ObjectAnimator.ofFloat(tripInfoPanel, "translationX", fromX, 0f);
	    		slideAnimator.setDuration(500);
	    		slideAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
	    		ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(tripInfoPanel, "alpha", 0f, 1f);
	    		alphaAnimator.setDuration(500);
	    		alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
	    		AnimatorSet animatorSet = new AnimatorSet();
	    		animatorSet.play(slideAnimator).with(alphaAnimator);
	    		animatorSet.start();
    		}
    	}
    }
    
    public void hideTripInfoPanel() {
    	findViewById(R.id.reservationList).setVisibility(View.GONE);
    }
    
    public boolean hasReservTrip() {
    	int childCount = reservationListPanel.getChildCount();
    	for(int i = 0 ; i < childCount ; i++) {
    		View child = reservationListPanel.getChildAt(i);
    		if(child.getTag() != null && child.getTag() instanceof Reservation) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public Reservation getFirstReservation() {
    	int childCount = reservationListPanel.getChildCount();
    	for(int i = 0 ; i < childCount ; i++) {
    		View child = reservationListPanel.getChildAt(i);
    		if(child.getTag() != null && child.getTag() instanceof Reservation && child.findViewById(R.id.reservation_on_my_way).getVisibility() == View.VISIBLE) {
    			return (Reservation) child.getTag();
    		}
    	}
    	return null;
    }
    
    public PoiOverlayInfo getPoiInfo(double lat, double lon, String address, String label) {
    	PoiOverlayInfo poiInfo = null;
    	
    	poiInfo = LandingActivity2.getInstance().poiContainer.getExistedPOIByLocation(lat, lon);
    	if (poiInfo==null) {
            BalloonModel model = new BalloonModel();
            model.lat = lat;
            model.lon = lon;
            model.address = address;
            model.label = label;
            model.geopoint = new GeoPoint(lat, lon);;
            poiInfo = PoiOverlayInfo.fromBalloonModel(model);
    	}
    	
    	return poiInfo;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private String formatTime(long time, int timzoneOffset, boolean showDate){
    	String format = showDate ? "EEEE h:mm a" : "h:mm a";
	    SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone(Request.getTimeZone(timzoneOffset)));
        return dateFormat.format(new Date(time));
	}
    
    private String getFormattedDuration(int duration){
	    return String.format("%dmin", duration/60);
	}
    
    private SpannableString formatTripTime(String startTime) {
    	int firstNumberIdx = getFirstNumberIndex(startTime);
		SpannableString startTimeSpan = SpannableString.valueOf(startTime);
		if(firstNumberIdx != -1) {
			startTimeSpan.setSpan(new AbsoluteSizeSpan(getResources()
					.getDimensionPixelSize(R.dimen.smaller_font)), 0, firstNumberIdx,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			startTimeSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.transparent_white)), 0 , firstNumberIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		int lastNumberIdx = getLastNumberIndex(startTime);
		if(lastNumberIdx != -1) {
			startTimeSpan.setSpan(new AbsoluteSizeSpan(getResources()
					.getDimensionPixelSize(R.dimen.smaller_font)), lastNumberIdx + 1, startTime.length(), 
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return startTimeSpan;
    }
    
    private SpannableString formatStartTripTime(String startTripTime) {
    	int firstNumberIdx = getFirstNumberIndex(startTripTime);
		SpannableString startTimeSpan = SpannableString.valueOf(startTripTime);
		if(firstNumberIdx != -1) {
			startTimeSpan.setSpan(new AbsoluteSizeSpan(getResources()
					.getDimensionPixelSize(R.dimen.micro_font)), 0, firstNumberIdx,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		int lastNumberIdx = getLastNumberIndex(startTripTime);
		if(lastNumberIdx != -1) {
			startTimeSpan.setSpan(new AbsoluteSizeSpan(getResources()
					.getDimensionPixelSize(R.dimen.smaller_font)), lastNumberIdx + 1, startTripTime.length(), 
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			startTimeSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.transparent_black)), lastNumberIdx + 1, startTripTime.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return startTimeSpan;
    }
    
    private SpannableString formatCountDownTime(String countDownDesc) {
    	int indexOfChange = countDownDesc.indexOf("\n");
    	SpannableString countDownSpan = SpannableString.valueOf(countDownDesc);
		countDownSpan.setSpan(new AbsoluteSizeSpan(Dimension.dpToPx(50, getResources().getDisplayMetrics())), 0, indexOfChange,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		countDownSpan.setSpan(new AbsoluteSizeSpan(getResources()
					.getDimensionPixelSize(R.dimen.smallest_font)), indexOfChange + 1, countDownDesc.length(), 
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		countDownSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.transparent_black)), indexOfChange + 1, countDownDesc.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return countDownSpan;
    }
	
    private int getLastNumberIndex(String str) {
    	char[] strChars = str.toCharArray();
    	for(int i = strChars.length - 1 ; i >= 0 ; i--) {
    		char c = strChars[i];
    		if(CharUtils.isAsciiNumeric(c)) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    private int getFirstNumberIndex(String str) {
    	char[] strChars = str.toCharArray();
    	for(int i = 0 ; i < strChars.length ; i++) {
    		char c = strChars[i];
    		if(CharUtils.isAsciiNumeric(c)) {
    			return i;
    		}
    	}
    	return -1;
    }
	
    
    
    
    
    
    
    
    
    
static class RescheduleTripTask extends AsyncTask<Void, Void, Void> {
        
        interface Callback {
            
            void run(Reservation reserv);
            
        }
        
        CancelableProgressDialog dialog;
        
        String originAddress;
        
        String address;
        
        Context ctx;
        
        GeoPoint origin;
        
        GeoPoint dest;
        
        ExceptionHandlingService ehs;
        
        Route _route;
        
        boolean startedMakingReserv;
        
        String versionNumber = "";
        
        ReservationTollHovInfo reservInfo;
        
        Callback callback = new Callback() {
            @Override
            public void run(Reservation reserv) {
                Intent intent = new Intent(ctx, ValidationActivity.class);
                intent.putExtra("route", reserv.getRoute());
                intent.putExtra("reservation", reserv);
                ctx.startActivity(intent);
            }
        };
        
        long id;
        
        RescheduleTripTask(Context ctx, GeoPoint origin, String originAddress, String destAddress, long rescheduleId, String versionNumber, ExceptionHandlingService ehs, ReservationTollHovInfo info){
            this.ehs = ehs;
            this.ctx = ctx;
            this.origin = origin;
            this.originAddress = originAddress;
            this.address = destAddress;
            this.id = rescheduleId;
            dialog = new CancelableProgressDialog(ctx, "Loading...");
            this.versionNumber = versionNumber;
            this.reservInfo = info;
        }
        
        @Override
        protected void onPreExecute() {
            dialog.show();
            if(this._route == null && origin == null){
                final LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    SystemService.alertNoGPS(ctx, true, new SystemService.Callback() {
                        @Override
                        public void onNo() {
                            if (dialog.isShowing()) {
                                dialog.cancel();
                            }
                        }
                    });
                }
                android.location.LocationListener locationListener = new android.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        try{
                            locationManager.removeUpdates(this);
                            dialog.dismiss();
                            origin = new GeoPoint(location.getLatitude(), location.getLongitude());
                        }catch(Throwable t){}
                    }
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override
                    public void onProviderEnabled(String provider) {}
                    @Override
                    public void onProviderDisabled(String provider) {}
                };
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            if(this._route == null && dest == null){
                try {
                	dest = Geocoding.lookup(ctx, address, origin.getLatitude(), origin.getLongitude()).get(0).getGeoPoint();
                    GeoPoint curLoc = DebugOptionsActivity.getCurrentLocationLatLon(ctx);
                    if(curLoc != null){
                        origin = curLoc;
                    }
                }
                catch (Exception e) {
                    ehs.registerException(e);
                }
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
        	if (((Activity)ctx).isFinishing()) return;
            if (ehs.hasExceptions()) {
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
                ehs.reportExceptions();
            }else{
                makeReservation();
            }
        }
        
        void cancelTask(){
            if (dialog.isShowing()) {
                dialog.cancel();
            }
            cancel(true);
        }
        
        void makeReservation(){
            if(!startedMakingReserv && ((origin != null && dest != null) || _route != null)){
                startedMakingReserv = true;
                Misc.parallelExecute(new AsyncTask<Void, Void, Reservation>(){
                    @Override
                    protected Reservation doInBackground(Void... params) {
                        Reservation reserv = null;
                        AdjustableTime departureTime = new AdjustableTime();
                        departureTime.setToNow();
                        User user = User.getCurrentUser(ctx);
                        try {
                            Route route;
                            if(_route == null){
                                RouteFetchRequest routeReq = new RouteFetchRequest(user, 
                                    origin, dest, departureTime.initTime().toMillis(false),
                                    0, 0, originAddress, address, reservInfo.isIncludeToll(), versionNumber, reservInfo.isHov());
                                route = routeReq.execute(ctx).get(0);
                                route.setAddresses(originAddress, address);
                                route.setUserId(user.getId());
                            }else{
                               route = _route; 
                            }
                            ReservationRequest reservReq = new ReservationRequest(user, 
                                route, ctx.getString(R.string.distribution_date), id);
                            reservReq.execute(ctx);
                            ReservationListFetchRequest reservListReq = new ReservationListFetchRequest(user);
                            reservListReq.invalidateCache(ctx);
                            List<Reservation> reservs = reservListReq.execute(ctx);
                            for (Reservation r : reservs) {
                                if(((Long)r.getRid()).equals(route.getId())){
                                    reserv = r;
                                }
                            }
                        }
                        catch(Exception e) {
                            ehs.registerException(e);
                        }
                        return reserv;
                    }
                    protected void onPostExecute(Reservation reserv) {
                    	if (((Activity)ctx).isFinishing()) return;
                        if (dialog.isShowing()) {
                            dialog.cancel();
                        }
                        if (ehs.hasExceptions()) {
                            ehs.reportExceptions();
                        }else if(reserv != null && callback != null){
                            callback.run(reserv);
                        }
                    }
                });
            }
        }
    }

}
