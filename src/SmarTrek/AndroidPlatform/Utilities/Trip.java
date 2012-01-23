package SmarTrek.AndroidPlatform.Utilities;

public class Trip {

	private String origin;
	private String destination;
	private Route route;
	private Coupon coupon;
	private boolean validated;
	
	public Trip(String origin, String destination, Route route, Coupon coupon){
		this.origin = origin;
		this.destination = destination;
		this.route = route;
		this.coupon = coupon;
		this.validated = false;
	}
	
	public String getOrigin(){
		return origin;
	}
	
	public String getDestination(){
		return destination;
	}
	
	public Route getRoute(){
		return route;
	}
	
	public Coupon getCoupon(){
		return coupon;
	}
	
	public boolean isValidated(){
		return validated;
	}	
}
