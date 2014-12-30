package com.metropia.models;

import java.util.List;
import java.util.Vector;

public class RouteGroup {

	private List<Route> routes = new Vector<Route>();
	
	public RouteGroup(List<Route> routes) {
		this.routes = routes;
	}
	
	public void addRoute(Route route) {
		routes.add(route);
	}
	
	public List<Route> getRoutes() {
		return routes;
	}
	
	public int getMinimumTravelTime() {
		int min = routes.size() > 0 ? routes.get(0).getDuration() : 0;
		
		for (Route route : routes) {
			int duration = route.getDuration();
			if (duration < min) min = duration;
		}
		
		return min;
	}
	
	public int getMaximumTravelTime() {
		int max = 0;
		
		for (Route route : routes) {
			int duration = route.getDuration();
			if (duration > max) max = duration;
		}
		
		return max;
	}
}
