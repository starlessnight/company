package com.metropia.ui.overlays;

import org.osmdroid.views.overlay.OverlayItem;

public interface OverlayCallback {
	boolean onBalloonTap(int index, OverlayItem item);
	boolean onTap(int index);
	boolean onClose();
	void onChange();
	boolean onLongPress(int index, OverlayItem item);
}
