package com.smartrek.utils;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

/**
 * This is a tile source for a map view.
 *
 */
final public class SmartrekTileProvider extends XYTileSource {

    private static final String osmHost = "http://tile.openstreetmap.org/";
    
    private static final String osmImgFilenameEnding = ".png"; 
    
    private static final int debugZoomLimit = 18;
    
    private boolean debug;
    
	public SmartrekTileProvider(boolean debug) {
		super("Custom", null, 3, debug?debugZoomLimit:18, 256, "", "http://tile.smartrekmobile.com/osm/");
		this.debug = debug;
	}
	
	@Override
    public String getTileURLString(final MapTile aTile) {
	    int zoomLevel = aTile.getZoomLevel();
	    String baseUrl;
	    String imageFilenameEnding;
	    if(debug && zoomLevel > debugZoomLimit){
	        baseUrl = osmHost;
	        imageFilenameEnding = osmImgFilenameEnding;
	    }else{
	        baseUrl = getBaseUrl();
	        imageFilenameEnding = mImageFilenameEnding;
	    }
        return baseUrl + zoomLevel + "/" + aTile.getX() + "/" + aTile.getY()
                + imageFilenameEnding;
    }

}
