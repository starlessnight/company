package com.metropia.utils;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import com.metropia.requests.Request;
import com.metropia.requests.Request.Setting;

/**
 * This is a tile source for a map view.
 *
 */
final public class SmartrekTileProvider extends XYTileSource {

    private static final String osmHost = "http://tile.openstreetmap.org/";
    
    private static final String osmImgFilenameEnding = ".png"; 
    
	public SmartrekTileProvider() {
		super("Custom", null, 3, 18, 512, "", new String[]{"http://tile.smartrekmobile.com/osm/"});
	}
	
	@Override
    public String getTileURLString(final MapTile aTile) {
	    int zoomLevel = aTile.getZoomLevel();
	    String baseUrl = getBaseUrl();
	    String imageFilenameEnding = mImageFilenameEnding;
	    String url = ((String)Request.getSetting(Setting.tile))
            .replaceAll("\\{x\\}", String.valueOf(aTile.getX()))
            .replaceAll("\\{y\\}", String.valueOf(aTile.getY()))
            .replaceAll("\\{z\\}", String.valueOf(zoomLevel));
	    
	    // deCarta images tiles
	    // url = "http://api.decarta.com/v1/814192d44ada190313e7639881bf7226/tile/"
        //    + aTile.getX() + "/" + aTile.getY() + "/" + zoomLevel + ".png";
	    
	    // Skobbler images tiles
	    //url = "http://tiles1.api.skobbler.net/tiles/" + zoomLevel + "/" 
        //    + aTile.getX() + "/" + aTile.getY() + ".png?api_key=97c7a512253c388d252fa4a141aba82b";
	     
	    // fallback
        /*try {
            HTTP http = new HTTP(url);
            http.setMethod(Method.HEAD);
            http.setTimeout(7500);
            http.setReferer("http://www.smartrekmobile.com");
            int responseCode = http.getResponseCode();
            if(!(200 <= responseCode && responseCode <= 399)){
                baseUrl = osmHost;
                imageFilenameEnding = osmImgFilenameEnding;
                url = baseUrl + zoomLevel + "/" + aTile.getX() + "/" + aTile.getY() + imageFilenameEnding;
            }
        }
        catch (IOException e) {
        }*/
	    
        return url;
    }

}
