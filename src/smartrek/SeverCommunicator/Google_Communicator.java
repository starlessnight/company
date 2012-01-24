package smartrek.SeverCommunicator;

import java.util.ArrayList;

import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class Google_Communicator extends Server_Communicator{

	GeoPoint start;
	GeoPoint end;
	
	
	public Google_Communicator(GeoPoint gp1, GeoPoint gp2){
		super();
		this.start = gp1;
		this.end = gp2;
	}
	
	public ArrayList<GeoPoint> getPoints(MapView mapView) {
		String gurl = appendToUrl();
		String google_response = DownloadText(gurl);
		Log.d("************",google_response);
		
		
		return null;
	}

	@Override
	protected String appendToUrl() {
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.google.com/maps?f=d&hl=en");
        urlString.append("&saddr=");//from
        urlString.append( start.getLatitudeE6()*0.000001);
        urlString.append(",");
        urlString.append( start.getLongitudeE6()*0.000001);
        urlString.append("&daddr=");//to
        urlString.append( end.getLatitudeE6()*0.000001);
        urlString.append(",");
        urlString.append( end.getLongitudeE6()*0.000001);
        urlString.append("&ie=UTF8&0&om=0&output=kml");
		return urlString.toString();
	}	
}