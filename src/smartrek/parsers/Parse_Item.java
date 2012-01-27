package smartrek.parsers;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import smartrek.models.User;
import smartrek.util.Coupon;
import smartrek.util.Route;
import smartrek.util.RouteNode;
import android.util.Log;


/*****************************************************************************************
 * 
 * @author Timothy Olivas
 *
 *****************************************************************************************/
public class Parse_Item {

	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public static ArrayList<Coupon> parse_coupons(String str) throws JSONException{		
		
		ArrayList<Coupon> coupon_array = new ArrayList<Coupon>();
		
		JSONArray jsonCouponArray = new JSONArray(str);
		Log.d("Parse_Item","Got " + jsonCouponArray.length() + " JSON OBjects");
		JSONObject couponInfo;
		
		for(int i = 0;i < jsonCouponArray.length(); i++) {
			Log.d("Parse_Item","Parsing Coupon: " + i);
		    couponInfo = (JSONObject) jsonCouponArray.getJSONObject(i);	
//			int did = couponInfo.getInt("DID");
//			String vname = couponInfo.getString("VENDOR");
//			String desc = couponInfo.getString("DESCRIPTION");
//			String vdate = couponInfo.getString("VALID_DATE");
//			String imageurl = couponInfo.getString("IMAGE_URL");
//			coupon_array.add(new Coupon(did,vname,desc,vdate,imageurl));
		    Coupon coupon = parse_coupon(couponInfo);
		    if(!coupon.getVendorName().equals("Barnes & Noble")){
		    	coupon_array.add(coupon);
		    }
		}
	    
		return coupon_array;
	}
	
	/*****************************************************************************************
	 * 
	 * 
	 *****************************************************************************************/
	private static Coupon parse_coupon(JSONObject json) throws JSONException{		
		Log.d("Parse_Item","Begin Parsing Coupon");
		int did = json.getInt("DID");
		String vname = json.getString("VENDOR");
		Log.d("Parse_Item","Vendor = " + vname);
		String desc = json.getString("DESCRIPTION");
		Log.d("Parse_Item","Description = " + desc);
		String vdate = json.getString("VALID_DATE");
		Log.d("Parse_Item","Valid Date = " + vdate);
	    String imageurl = json.getString("IMAGE_URL");
	    Log.d("Parse_Item","Image URL = " + imageurl);
		return new Coupon(did,vname,desc,vdate,imageurl);
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public static User parse_user(String name, String str) throws JSONException{	
		JSONObject json = new JSONObject(str);  
		int id = json.getInt("uid");
		return new User(id, name);
	}
		
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public static ArrayList<Route> parse_routes(String str) throws JSONException{		
		Log.d("Parse_Item","Begin Parsing Routes");
		ArrayList<Route> routearray = new ArrayList<Route>();
		Log.d("Parse_Item","JSON Expression \n" + str);
		JSONArray jsonroutearray = new JSONArray(str);
		Log.d("Parse_Item","Got " + jsonroutearray.length() + " JSON OBjects");
		JSONObject routeinfo;
		int i = 0;
		int j = 0;
		while(i < jsonroutearray.length()) {
			j = 0;
		    routeinfo = (JSONObject) jsonroutearray.getJSONObject(i);	
			JSONArray loclist = routeinfo.getJSONArray("route");
			ArrayList<RouteNode> route_nodes = new ArrayList<RouteNode>();
			Log.d("Parse_Item","Got Array with " + loclist.length() + " values");
			while(j < loclist.length()){
				JSONObject latlong = ((JSONObject) loclist.get(j));
				float latitude = Float.valueOf(latlong.getString("LATITUDE"));
				float longitude = Float.valueOf(latlong.getString("LONGITUDE"));
				Log.d("Parse_Item","Got latitude " + latitude);
				Log.d("Parse_Item","Got longitude " + longitude);
				RouteNode rn = new RouteNode(latitude,longitude,i,j);
				route_nodes.add(rn);
				j++;
			}  
			Log.d("Parse_Item","Parsing lable ");
			//Float lable = Float.valueOf((routeinfo.getString("label")));
			//Log.d("Parse_Item","Lable = " + lable);
			
			Log.d("Parse_Item","Parsing RID");
			int rid = Integer.valueOf((routeinfo.getString("rid")));
			Log.d("Parse_Item","RID = " + rid);
			
			Log.d("Parse_Item","Parsing time ");
			Float time = Float.valueOf(routeinfo.getString("estimated_travel_time"));	
			Log.d("Parse_Item","Time = " + time);
			
//			Log.d("Parse_Item","Parsing Coupon");
//			Log.d("Parse_Item",routeinfo.getJSONObject("discount").toString());
//			Coupon cp = parse_coupon(routeinfo.getJSONObject("discount"));
			
//			Coupon cp = new Coupon("", "", "", "");
			routearray.add(new Route(route_nodes,rid,time));
			Log.d("Parse_Item","Finished Parsing Route: " + i);
			i++;
		}
		return routearray;
	}
}
