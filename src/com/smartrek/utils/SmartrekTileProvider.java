package com.smartrek.utils;

import java.io.IOException;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

/**
 * This is a tile source for a map view.
 *
 */
final public class SmartrekTileProvider extends XYTileSource {

    private static final String osmHost = "http://tile.openstreetmap.org/";
    
    private static final String osmImgFilenameEnding = ".png"; 
    
	public SmartrekTileProvider() {
		super("Custom", null, 3, 18, 256, "", "http://tile.smartrekmobile.com/osm/");
	}
	
	@Override
    public String getTileURLString(final MapTile aTile) {
	    int zoomLevel = aTile.getZoomLevel();
	    String baseUrl = getBaseUrl();
	    String imageFilenameEnding = mImageFilenameEnding;
	    String url = baseUrl + zoomLevel + "/" + aTile.getX() + "/" + aTile.getY() + imageFilenameEnding;	    
        try {
            HTTP http = new HTTP(url);
            http.connect();
            if(http.getResponseCode() == 404){
                baseUrl = osmHost;
                imageFilenameEnding = osmImgFilenameEnding;
                url = baseUrl + zoomLevel + "/" + aTile.getX() + "/" + aTile.getY() + imageFilenameEnding;
            }
        }
        catch (IOException e) {
        }
        return url;
    }

}
