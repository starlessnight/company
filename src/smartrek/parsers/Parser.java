package smartrek.parsers;

import java.util.ArrayList;

import org.json.JSONException;

import smartrek.util.Coupon;
import smartrek.util.Route;
import smartrek.util.User;


/****************************************************************************************************
 * 
 * 
 * @author timothyolivas
 *
 ****************************************************************************************************/
public class Parser {
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public static ArrayList<Coupon> parse_Coupon_List(String str) throws JSONException {
//		int i = 0;
//		int j = 0;
//		ArrayList<Coupon> cp_array = new ArrayList<Coupon>();
//		while(i != -1){
//			i = str.indexOf("{",j);
//			j = str.indexOf("}",j);
//			String temp = str.substring(i,j);
//			Coupon cp = Parse_Item.parse_coupon(temp);
//			cp_array.add(cp);
//		}
//		return cp_array;	
		return Parse_Item.parse_coupons(str);
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public static ArrayList<Route> parse_Routes(String str) throws JSONException {
		return Parse_Item.parse_routes(str);
	}
	
	/*****************************************************************************************
	 * 
	 *
	 *****************************************************************************************/
	public static User parse_User(String name, String str) throws JSONException {
		return Parse_Item.parse_user(name,str);
	}
	
}
