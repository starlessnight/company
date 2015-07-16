package com.metropia.models;

import java.util.HashSet;
import java.util.Set;

import com.metropia.activities.LandingActivity2.PoiOverlayInfo;
import com.metropia.activities.R;

public class POIContainer {
	
	private static final Integer SIZE = Integer.valueOf(2000000);
	private static final Integer BALLOON_SIZE = Integer.valueOf(200);
	private static final Integer BULB_POI_START_ID = Integer.MAX_VALUE - SIZE;
	private static final Integer OTHER_POI_START_ID = BULB_POI_START_ID - SIZE;
	private static final Integer WORK_POI_START_ID = OTHER_POI_START_ID - SIZE;
	private static final Integer HOME_POI_START_ID = WORK_POI_START_ID - SIZE;
	
	private Object mutex = new Object();
	private POICollection bulbPois = new POICollection(BULB_POI_START_ID, Integer.MAX_VALUE - BALLOON_SIZE);
	private POICollection otherPois = new POICollection(OTHER_POI_START_ID, BULB_POI_START_ID - 1);
	private POICollection workPois = new POICollection(WORK_POI_START_ID, OTHER_POI_START_ID - 1);
	private POICollection homePois = new POICollection(HOME_POI_START_ID, WORK_POI_START_ID - 1);
	
	private POICollection[] poiCollections = {homePois, workPois, otherPois, bulbPois};
	private POICollection[] hasIdPoiCollections = {homePois, workPois, otherPois};
	
	public POIContainer() {}
	
    public PoiOverlayInfo getExistedPOIByLocation(double lat, double lon) {
    	synchronized(mutex) {
	    	PoiOverlayInfo result = null;
	    	for(int i = 0 ; i < poiCollections.length && result == null ; i++) {
	    		result = poiCollections[i].getExistedPOIByLocation(lat, lon);
	    	}
	    	return result;
    	}
    }
    
    public PoiOverlayInfo getExistedPOIByUniqueId(Integer uniqueId) {
    	synchronized(mutex) {
    		PoiOverlayInfo result = null;
	    	for(int i = 0 ; i < poiCollections.length && result == null ; i++) {
	    		result = poiCollections[i].getExistedPOIByUniqueId(uniqueId);
	    	}
	    	return result;
    	}
    }
    
    public PoiOverlayInfo getExistedPOIByAddress(String addr) {
    	synchronized(mutex) {
    		PoiOverlayInfo result = null;
	    	for(int i = 0 ; i < poiCollections.length && result == null ; i++) {
	    		result = poiCollections[i].getExistedPOIByAddress(addr);
	    	}
	    	return result;
    	}
    }
    
    public PoiOverlayInfo getExistedPOIByPoiId(Integer poiId) {
    	synchronized(mutex) {
    		PoiOverlayInfo result = null;
	    	for(int i = 0 ; i < hasIdPoiCollections.length && result == null ; i++) {
	    		result = hasIdPoiCollections[i].getExistedPOIByPoiId(poiId);
	    	}
	    	return result;
    	}
    }
    
    public void updateExistedPOIByPoiId(Integer poiId, PoiOverlayInfo info) {
    	synchronized(mutex) {
	    	for(int i = 0 ; i < hasIdPoiCollections.length ; i++) {
	    		hasIdPoiCollections[i].updateExistedPOIByPoiId(poiId, info);
	    	}
    	}
    }
    
    public Integer addPOIToMap(PoiOverlayInfo info) {
    	synchronized(mutex) {
    		switch(info.markerWithShadow) {
    			case R.drawable.home_with_shadow :
    				return homePois.addPOIToMap(info);
    			case R.drawable.work_with_shadow : 
    				return workPois.addPOIToMap(info);
    			case R.drawable.bulb_poi_with_shadow : 
    				return bulbPois.addPOIToMap(info);
    			default :
    				return otherPois.addPOIToMap(info);
    		}
    	}
    }
    
    public void removePOI(PoiOverlayInfo info) {
    	synchronized(mutex) {
       		switch(info.markerWithShadow) {
       			case R.drawable.home_with_shadow :
       				homePois.removePOI(info);
       				break;
       			case R.drawable.work_with_shadow : 
       				workPois.removePOI(info);
       				break;
       			case R.drawable.bulb_poi_with_shadow : 
       				bulbPois.removePOI(info);
       				break;
       			default :
       				otherPois.removePOI(info);
       		}
    	}
    }
    
    public void removeAll() {
    	synchronized(mutex) {
    		for(int i = 0 ; i < poiCollections.length ; i++) {
	    		poiCollections[i].removeAll();
	    	}
    	}
    }
    
    public Set<Integer> getBulbUniqueIdSet() {
    	synchronized(mutex) {
    		return bulbPois.getUniqueIdSet();
    	}
    }
    
    public Set<Integer> getStarUniqueIdSet() {
    	synchronized(mutex) {
	    	Set<Integer> starUniqueIdSet = new HashSet<Integer>();
	    	starUniqueIdSet.addAll(homePois.getUniqueIdSet());
	    	starUniqueIdSet.addAll(workPois.getUniqueIdSet());
	    	starUniqueIdSet.addAll(otherPois.getUniqueIdSet());
	    	return starUniqueIdSet;
    	}
    }
    
    public void cleanStarPois() {
    	synchronized(mutex) {
    		homePois.removeAll();
    		workPois.removeAll();
    		otherPois.removeAll();
    	}
    }
    
    public void cleanBulbPois() {
    	synchronized(mutex) {
    		bulbPois.removeAll();
    	}
    }

}
