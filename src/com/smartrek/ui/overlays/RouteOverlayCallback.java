package com.smartrek.ui.overlays;

import com.google.android.maps.OverlayItem;

public interface RouteOverlayCallback {
	boolean onBalloonTap(int index, OverlayItem item);
	boolean onTap(int index);
}
