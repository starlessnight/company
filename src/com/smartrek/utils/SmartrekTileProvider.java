package com.smartrek.utils;

import org.osmdroid.tileprovider.tilesource.XYTileSource;

/**
 * This is a tile source for a map view.
 *
 */
final public class SmartrekTileProvider extends XYTileSource {

	public SmartrekTileProvider() {
		super("Custom", null, 3, 18, 256, "", "http://tile.smartrekmobile.com/osm/");
	}

}
