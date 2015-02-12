package com.metropia.activities;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.activities.FavoriteOperationActivity.FavoriteSlideFragment.ClickCallback;
import com.metropia.activities.LandingActivity2.PoiOverlayInfo;
import com.metropia.models.FavoriteIcon;
import com.metropia.models.User;
import com.metropia.requests.AddressLinkRequest;
import com.metropia.requests.FavoriteAddressAddRequest;
import com.metropia.requests.FavoriteAddressDeleteRequest;
import com.metropia.requests.FavoriteAddressUpdateRequest;
import com.metropia.requests.Request;
import com.metropia.ui.ClickAnimation;
import com.metropia.ui.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.utils.Dimension;
import com.metropia.utils.ExceptionHandlingService;
import com.metropia.utils.Font;
import com.metropia.utils.GeoPoint;
import com.metropia.utils.Geocoding;
import com.metropia.utils.Geocoding.Address;
import com.metropia.utils.Misc;

public class FavoriteOperationActivity extends FragmentActivity {
	
	public static final Integer FAVORITE_OPT = Integer.valueOf(3345);
	public static final String FAVORITE_OPT_TYPE = "favoriteOptType";
	public static final String FAVORITE_POI_INFO = "favoritePoi";
	public static final String FAVORITE_DELETE = "favoriteDel";
	public static final String FAVORITE_UPDATE = "favoriteUpdate";
	public static final String FAVORITE_POI_ID = "favoritePoiId";
	
	private ExceptionHandlingService ehs = new ExceptionHandlingService(this);

	private View favOptPanel;
	private ImageView labelIcon;
	private TextView favSearchBox;
	private GeoPoint lastLocation;
	private View loadingPanel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorite_operation);

		// Integrate Localytics
		Localytics.integrate(this);
		
		favOptPanel = findViewById(R.id.fav_opt);
		favSearchBox = (TextView) favOptPanel.findViewById(R.id.favorite_search_box);
		labelIcon = (ImageView) findViewById(R.id.label_icon);
		
		Bundle extras = getIntent().getExtras();
		PoiOverlayInfo info = extras.getParcelable(FAVORITE_POI_INFO);
		favOptPanel.setTag(info);
		writeInfo2FavoritePanel(info);

		TextView favSave = (TextView)favOptPanel.findViewById(R.id.fav_save);

		final EditText labelInput = (EditText) favOptPanel.findViewById(R.id.label_input);

		favOptPanel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				labelInput.clearFocus();
			}
		});
		
		loadingPanel = findViewById(R.id.loading_panel);
		loadingPanel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// do nothing
			}
		});

		final View labelInputClear = favOptPanel.findViewById(R.id.label_clear);
		labelInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (StringUtils.isNotBlank(s.toString())) {
					labelInputClear.setVisibility(View.VISIBLE);
				} else {
					labelInputClear.setVisibility(View.GONE);
				}
			}
		});

		labelInput.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
			}
		});

		labelInputClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				labelInput.setText("");
			}
		});

		findViewById(R.id.fav_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(
						FavoriteOperationActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Intent result = new Intent();
						result.putExtra(FAVORITE_OPT_TYPE, "");
						setResult(Activity.RESULT_OK, result);
						finish();
					}
				});
			}
		});

		favSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				v.setClickable(false);
				ClickAnimation clickAnimation = new ClickAnimation(
						FavoriteOperationActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						if (isFavoriteOptComplete()) {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
							PoiOverlayInfo info = (PoiOverlayInfo) favOptPanel.getTag();
							final PoiOverlayInfo _info = info == null ? new PoiOverlayInfo() : info;
							String label = ((EditText) favOptPanel.findViewById(R.id.label_input)).getText().toString();
							if (StringUtils.isBlank(label)) {
								label = "Favorite";
							}
							final String lbl = label;
							final String addr = ((TextView) favOptPanel.findViewById(R.id.favorite_search_box)).getText().toString();
							final FavoriteIcon icon = (FavoriteIcon) favOptPanel.findViewById(R.id.icon).getTag();
							AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
								@Override
								protected void onPreExecute() {
									loadingPanel.setVisibility(View.VISIBLE);
									if (_info.lat == 0 && _info.lon == 0) {
										List<Address> result = Collections.emptyList();
										try {
											if (lastLocation != null) {
												result = Geocoding.searchPoi(FavoriteOperationActivity.this, addr, lastLocation.getLatitude(),
														lastLocation.getLongitude());
											} else {
												result = Geocoding.searchPoi(FavoriteOperationActivity.this, addr);
											}
										} catch (Exception e) {
											ehs.registerException(e, e.getMessage());
										}
										if (result.isEmpty()) {
											ehs.registerException(new RuntimeException(), "Address [" + addr + "] not found!");
										} else {
											Address found = result.get(0);
											_info.address = found.getAddress();
											_info.lat = found.getLatitude();
											_info.lon = found.getLongitude();
											_info.geopoint = found.getGeoPoint();
										}
									} else {
										_info.address = addr;
									}
								}

								@Override
								protected Integer doInBackground(Void... params) {
									Integer id = 0;
									Request req = null;
									FavoriteIcon favIcon = icon != null ? icon : FavoriteIcon.star;
									User user = User.getCurrentUser(FavoriteOperationActivity.this);
									_info.iconName = favIcon.name();
									_info.label = lbl;
									_info.marker = favIcon.getResourceId(FavoriteOperationActivity.this);
									_info.markerWithShadow = favIcon.getShadowResourceId(FavoriteOperationActivity.this);
									try {
										if (_info.id == 0) {
											FavoriteAddressAddRequest request = new FavoriteAddressAddRequest(
													user, lbl, _info.address, favIcon.name(), _info.lat, _info.lon);
											req = request;
											id = request.execute(FavoriteOperationActivity.this);
										} else {
											FavoriteAddressUpdateRequest request = new FavoriteAddressUpdateRequest(new AddressLinkRequest(user).execute(FavoriteOperationActivity.this),
													_info.id, user, lbl, addr, favIcon.name(), _info.lat,	_info.lon);
											req = request;
											request.execute(FavoriteOperationActivity.this);
										}
									} catch (Exception e) {
										ehs.registerException(e, "[" + (req == null ? "" : req.getUrl()) + "]\n" + e.getMessage());
									}
									return id;
								}

								protected void onPostExecute(Integer id) {
									loadingPanel.setVisibility(View.GONE);
									if (ehs.hasExceptions()) {
										ehs.reportExceptions();
									} else {
										_info.id = _info.id != 0 ? _info.id	: id;
										Intent result = new Intent();
										result.putExtra(FAVORITE_OPT_TYPE, FAVORITE_UPDATE);
										result.putExtra(FAVORITE_POI_INFO, _info);
										setResult(Activity.RESULT_OK, result);
										finish();
									}
								}
							};
							Misc.parallelExecute(task);
						}
						v.setClickable(true);
					}
				});
			}
		});

		findViewById(R.id.fav_del).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(
						FavoriteOperationActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						findViewById(R.id.confirm_panel).setVisibility(
								View.VISIBLE);
					}
				});
			}
		});

		findViewById(R.id.confirm_panel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// do nothing
			}
		});

		findViewById(R.id.confirm_del).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						ClickAnimation clickAnimation = new ClickAnimation(FavoriteOperationActivity.this, v);
						clickAnimation.startAnimation(new ClickAnimationEndCallback() {
								@Override
								public void onAnimationEnd() {
									findViewById(R.id.confirm_panel).setVisibility(View.GONE);
									final PoiOverlayInfo info = (PoiOverlayInfo) favOptPanel.getTag();
									final int oldId = info.id;
									AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
										@Override
										protected Void doInBackground(Void... params) {
											loadingPanel.setVisibility(View.VISIBLE);
											Request req = null;
											User user = User.getCurrentUser(FavoriteOperationActivity.this);
											try {
												FavoriteAddressDeleteRequest request = new FavoriteAddressDeleteRequest(
														new AddressLinkRequest(user).execute(FavoriteOperationActivity.this), user, oldId);
												req = request;
												request.execute(FavoriteOperationActivity.this);
											} catch (Exception e) {
												ehs.registerException(e, "[" + (req == null ? "" : req.getUrl()) + "]\n" + e.getMessage());
											}
											return null;
										}

										protected void onPostExecute(Void param) {
											loadingPanel.setVisibility(View.GONE);
											if (ehs.hasExceptions()) {
												ehs.reportExceptions();
											}else {
												favOptPanel.setTag(null);
												Intent result = new Intent();
												result.putExtra(FAVORITE_OPT_TYPE, FAVORITE_DELETE);
												result.putExtra(FAVORITE_POI_ID, oldId);
												setResult(Activity.RESULT_OK, result);
												finish();
											}
										}
									};
									Misc.parallelExecute(task);
								}
							});
					}
				});

		findViewById(R.id.confirm_cancel).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						ClickAnimation clickAnimation = new ClickAnimation(
								FavoriteOperationActivity.this, v);
						clickAnimation.startAnimation(new ClickAnimationEndCallback() {
							@Override
							public void onAnimationEnd() {
								findViewById(R.id.confirm_panel).setVisibility(View.GONE);
							}
						});
					}
				});

		initFavoritePage();

		AssetManager assets = getAssets();
		Font.setTypeface(Font.getMedium(assets), favSearchBox, labelInput, (TextView) findViewById(R.id.favorite_address_desc),
				favSave, (TextView) findViewById(R.id.label), (TextView)findViewById(R.id.fav_cancel), (TextView)findViewById(R.id.header), 
				(TextView)findViewById(R.id.icon));
		
		((SmarTrekApplication)getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	private void writeInfo2FavoritePanel(PoiOverlayInfo info) {
    	if(info != null) {
	    	favOptPanel.setTag(info);
	    	((EditText) favOptPanel.findViewById(R.id.label_input)).setText(info.label);
	    	FavoriteIcon icon = FavoriteIcon.fromName(info.iconName, FavoriteIcon.star);
	    	if(icon != null) {
	    		favOptPanel.findViewById(R.id.icon).setTag(icon);
	    		labelIcon.setVisibility(View.VISIBLE);
	    		labelIcon.setImageResource(icon.getResourceId(FavoriteOperationActivity.this));
	    	}
	    	TextView favSearchBox = (TextView) favOptPanel.findViewById(R.id.favorite_search_box); 
	    	favSearchBox.setText(info.address);
	    	favSearchBox.setTextIsSelectable(true);
	    	favOptPanel.findViewById(R.id.fav_save).setVisibility(StringUtils.isNotBlank(info.address) ? View.VISIBLE : View.GONE);
	    	favOptPanel.findViewById(R.id.fav_del_panel).setVisibility(info.id!=0 ? View.VISIBLE : View.GONE);
	    	((TextView)favOptPanel.findViewById(R.id.header)).setText(info.id!=0 ? "Edit Favorite" : "Save Favorite");
    	}
    }

	private void initFavoritePage() {
		ViewPager favoriteIconPager = (ViewPager) findViewById(R.id.favorite_icons_pager);
		favoriteIconPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
				favOptPanel.findViewById(R.id.label_input).clearFocus();
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				favOptPanel.findViewById(R.id.label_input).clearFocus();
			}

			@Override
			public void onPageSelected(int pos) {
				LinearLayout indicators = (LinearLayout) findViewById(R.id.indicators);
				for (int i = 0; i < indicators.getChildCount(); i++) {
					indicators.getChildAt(i).setEnabled(i == pos);
				}
			}
		});

		FavoriteSlideAdapter slideAdapter = new FavoriteSlideAdapter(
				getSupportFragmentManager(), new ClickCallback() {
					@Override
					public void onClick(FavoriteIcon icon) {
						favOptPanel.findViewById(R.id.icon).setTag(icon);
						favOptPanel.findViewById(R.id.label_input).clearFocus();
						labelIcon.setImageResource(icon
								.getResourceId(FavoriteOperationActivity.this));
						labelIcon.setVisibility(View.VISIBLE);
					}
				});

		favoriteIconPager.setAdapter(slideAdapter);

		LinearLayout indicators = (LinearLayout) findViewById(R.id.indicators);
		for (int i = 0; i < slideAdapter.getCount(); i++) {
			View indicator = getLayoutInflater().inflate(
					R.layout.onboard_indicator, indicators, false);
			if (i == 0) {
				((LinearLayout.LayoutParams) indicator.getLayoutParams()).leftMargin = 0;
			} else {
				indicator.setEnabled(false);
			}
			indicators.addView(indicator);
		}
	}

	public static class FavoriteSlideFragment extends Fragment {

		static final String ICONS_PAGE_NO = "icons_page_no";

		private FavoriteIcon[][] icons;
		private ClickCallback clickCallback;

		public interface ClickCallback {
			public void onClick(FavoriteIcon icon);
		}

		static FavoriteSlideFragment of(Integer pageNo,
				ClickCallback _clickCallback) {
			FavoriteSlideFragment f = new FavoriteSlideFragment(_clickCallback);
			Bundle args = new Bundle();
			args.putInt(ICONS_PAGE_NO, pageNo);
			f.setArguments(args);
			return f;
		}

		public FavoriteSlideFragment() {
		}

		private FavoriteSlideFragment(ClickCallback clickCallback) {
			this.clickCallback = clickCallback;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Bundle args = getArguments();
			this.icons = FavoriteIcon.getIcons(args.getInt(ICONS_PAGE_NO));
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			final LinearLayout view = (LinearLayout) inflater.inflate(
					R.layout.favorite_icon_slide, container, false);
			DisplayMetrics dm = view.getContext().getResources()
					.getDisplayMetrics();
			int iconMargin = Dimension.dpToPx(10, dm);
			for (FavoriteIcon[] rowIcons : icons) {
				LinearLayout row = new LinearLayout(view.getContext());
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, 0);
				lp.weight = 1;
				row.setLayoutParams(lp);
				row.setWeightSum(rowIcons.length);
				for (FavoriteIcon icon : rowIcons) {
					ImageView iconView = new ImageView(row.getContext());
					LinearLayout.LayoutParams imageLp = new android.widget.LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.MATCH_PARENT);
					imageLp.weight = 1;
					imageLp.bottomMargin = iconMargin;
					imageLp.leftMargin = iconMargin;
					imageLp.rightMargin = iconMargin;
					imageLp.topMargin = iconMargin;
					imageLp.gravity = Gravity.CENTER;
					iconView.setLayoutParams(imageLp);
					iconView.setTag(icon);
					iconView.setImageBitmap(BitmapFactory
							.decodeStream(getResources().openRawResource(
									icon.getFavoritePageResourceId(view
											.getContext()))));
					iconView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(final View v) {
							ClickAnimation clickAnimation = new ClickAnimation(
									view.getContext(), v);
							clickAnimation
									.startAnimation(new ClickAnimationEndCallback() {
										@Override
										public void onAnimationEnd() {
											if (clickCallback != null) {
												clickCallback
														.onClick((FavoriteIcon) v
																.getTag());
											}
										}
									});
						}
					});
					row.addView(iconView);
				}
				view.addView(row);
			}
			return view;
		}

	}

	public static class FavoriteSlideAdapter extends FragmentPagerAdapter {

		private static Integer[] slides = { FavoriteIcon.FIRST_PAGE,
				FavoriteIcon.SECOND_PAGE };

		private ClickCallback clickCallback;

		public FavoriteSlideAdapter(FragmentManager fm, ClickCallback callback) {
			super(fm);
			this.clickCallback = callback;
		}

		@Override
		public int getCount() {
			return slides.length;
		}

		@Override
		public Fragment getItem(int position) {
			return FavoriteSlideFragment.of(slides[position], clickCallback);
		}

	}

	private boolean isFavoriteOptComplete() {
		String favAddr = ((TextView) favOptPanel.findViewById(R.id.favorite_search_box)).getText().toString();
		return StringUtils.isNotBlank(favAddr);
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    Localytics.openSession();
	    Localytics.upload();
	    Localytics.setInAppMessageDisplayActivity(this);
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
	}
	
	@Override
	public void onPause() {
	    Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
	    super.onPause();
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
        Misc.tripInfoPanelOnActivityStop(this);
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();
	    Misc.tripInfoPanelOnActivityRestart(this);
	}

}
