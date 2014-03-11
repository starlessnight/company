package com.smartrek.requests;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.format.Time;

import com.smartrek.activities.DebugOptionsActivity.NavigationLink;
import com.smartrek.exceptions.RouteNotFoundException;
import com.smartrek.models.Route;
import com.smartrek.models.User;
import com.smartrek.utils.GeoPoint;

public class RouteFetchRequest extends FetchRequest<List<Route>> {
	
	private long departureTime;
	
	private boolean fake;
	
	private boolean hasNavUrl;
	
	private int duration;
	
	private static String buildUrl(GeoPoint origin, GeoPoint destination, 
	        long departureTime, double speed, float course, String originAddr, 
            String destAddr) {
		String url;
		double startlat = origin.getLatitude();
		double startlon = origin.getLongitude();
		double endlat = destination.getLatitude();
		double endlon = destination.getLongitude();
        if(NEW_API){
            SimpleDateFormat dateFormatUtc = new SimpleDateFormat("yyyyMMddHHmm");
            dateFormatUtc.setTimeZone(TimeZone.getTimeZone(UTC_TIMEZONE));
		    url = getLinkUrl(Link.route)
                .replaceAll("\\{startlat\\}", String.format("%.7f", startlat))
                .replaceAll("\\{startlon\\}", String.format("%.7f", startlon))
                .replaceAll("\\{endlat\\}", String.format("%.7f", endlat))
                .replaceAll("\\{endlon\\}", String.format("%.7f", endlon))
                .replaceAll("\\{departtime\\}", dateFormatUtc.format(new Date(departureTime)))
                .replaceAll("\\{speed\\}", String.valueOf(speed))
                .replaceAll("\\{course\\}", String.valueOf(course))
                .replaceAll("\\{origin\\}", StringUtils.defaultString(originAddr))
                .replaceAll("\\{destination\\}", StringUtils.defaultString(destAddr));
		}else{
		    Time t = new Time();
	        t.set(departureTime);
	        t.switchTimezone(getTimeZone());
            url = String.format("%s/getroutes/startlat=%.7f%%20startlon=%.7f%%20endlat=%.7f%%20endlon=%.7f%%20departtime=%d:%02d",
	                HOST, startlat, startlon, endlat, endlon, t.hour, t.minute); 
		}
		return url;
	}
	
	public RouteFetchRequest(User user, GeoPoint origin, GeoPoint destination, 
	        long departureTime, double speed, float course, String originAddr, 
	        String destAddr) {
		super(buildUrl(origin, destination, departureTime, speed, course, 
	        originAddr, destAddr));
		this.departureTime = departureTime;
		if(NEW_API){
		    this.username = user.getUsername();
		    this.password = user.getPassword();
        }
	}
	
	/**
	 * Enables debug mode
	 * 
	 * @param departureTime
	 */
	public RouteFetchRequest(long departureTime) {
		//super(HOST + "/getroutesTucson/fake");
		super(HOST + "/getroutesTucsonNavigation/fake");
		//super("http://static.suminb.com/smartrek/fake-routes.html");
		
		this.departureTime = departureTime;
		fake = true;
	}
	
	private static String buildUrl(String rawUrl, double speedInMph, float bearing){
        String speedInMphStr = String.valueOf(speedInMph);
        String courseAngleClockwise = String.valueOf(bearing);
        return rawUrl.replaceAll("\\{speed_in_mph\\}", speedInMphStr)
                .replaceAll("\\{course_angle_clockwise\\}", courseAngleClockwise)
                .replaceAll("\\[speed_in_mph\\]", speedInMphStr)
                .replaceAll("\\[course_angle_clockwise\\]", courseAngleClockwise);
    }
	
    public RouteFetchRequest(String url, long departureTime, int duration, 
            double speedInMph, float bearing) {
        super(buildUrl(url, speedInMph, bearing));
        this.departureTime = departureTime;
        this.duration = duration;
        hasNavUrl = true;
    }
	
	public List<Route> execute(Context ctx) throws IOException, JSONException, RouteNotFoundException, InterruptedException {
		String response = null;
		try{
		    response = executeFetchRequest(url, ctx);
		}catch(IOException e){
		    String msg = null;
		    if(responseCode == 400){
		        msg = "The requested route is out of service area.";
		    }else if(responseCode == 500){
		        msg = "The server is busy.";
		    }
		    if(msg == null){
		        throw e;
		    }else{
		        throw new IOException(msg);
		    }
		}
		
		// Begin parsing the server response
		List<Route> routes = new ArrayList<Route>();

		JSONArray array;
		NavigationLink link = null;
		if(NEW_API){
		    array = new JSONArray();
            JSONObject jsonRes = new JSONObject(response);
		    if(hasNavUrl){
		        array.put(jsonRes.getJSONArray("data"));
		    }else{
	            array.put(jsonRes.getJSONObject("data"));
	            String linksAttr = "links";
	            if(jsonRes.has(linksAttr)){
	                link = new NavigationLink();
	                link.url = jsonRes.getJSONObject(linksAttr).getString("navigation");
	            }
		    }
		}else{
		    array = new JSONArray(response);
		}
		
		for(int i = 0; i < array.length(); i++) {
		    Object obj = array.get(i);
            Route route;
            if(obj instanceof JSONObject){
                JSONObject jsonObj = (JSONObject) obj;
                route = Route.parse(jsonObj, departureTime);
                route.setRawJSON(jsonObj.toString());
            }else{
                route = Route.parse((JSONArray) obj, departureTime, duration);
            }
            
            route.setFake(fake);
            route.setSeq(i);
            if(!route.getNodes().isEmpty()){
                routes.add(route);
            }
            if(NEW_API && i == 0){
                route.setLink(link);
            }
        }
		
		if (routes.size() == 0) {
			//throw new RouteNotFoundException("Could not find a route.");
		}
		
		return routes;
	}
	

}
