package com.smartrek.ui.overlays;

import org.osmdroid.views.overlay.OverlayItem;

public interface RouteOverlayCallback {
	boolean onBalloonTap(int index, OverlayItem item);
	boolean onTap(int index);
	boolean onClose();
}
