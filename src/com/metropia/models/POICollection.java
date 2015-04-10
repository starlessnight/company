package com.metropia.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.metropia.SkobblerUtils;
import com.metropia.activities.LandingActivity2.PoiOverlayInfo;

public class POICollection {

	private Integer idStartWith;
	private Integer idEndWith;
	private Map<Integer, PoiOverlayInfo> idPoiMap = new HashMap<Integer, PoiOverlayInfo>();
	private Map<String, PoiOverlayInfo> addressPoiMap = new HashMap<String, PoiOverlayInfo>();
	private Map<Integer, Integer> locationIdMap = new HashMap<Integer, Integer>();

	public POICollection(Integer idStartWith, Integer idEndWith) {
		this.idStartWith = idStartWith;
		this.idEndWith = idEndWith;
	}

	public PoiOverlayInfo getExistedPOIByLocation(double lat, double lon) {
		if(locationIdMap.containsKey(SkobblerUtils.getUniqueId(lat, lon))) {
			return idPoiMap.get(locationIdMap.get(SkobblerUtils.getUniqueId(lat, lon)));
		}
		return null;
	}

	public PoiOverlayInfo getExistedPOIByUniqueId(Integer uniqueId) {
		return idPoiMap.get(uniqueId);
	}

	public PoiOverlayInfo getExistedPOIByAddress(String addr) {
		return addressPoiMap.get(addr);
	}

	public Integer addPOIToMap(PoiOverlayInfo info) {
		Integer uniqueId = getRandomUniqueId();
		locationIdMap.put(SkobblerUtils.getUniqueId(info.lat, info.lon), uniqueId);
		info.uniqueId = uniqueId;
		idPoiMap.put(uniqueId, info);
		addressPoiMap.put(info.address, info);
		return uniqueId;
	}

	public void removePOI(PoiOverlayInfo info) {
		Integer unique = locationIdMap.get(SkobblerUtils.getUniqueId(info.lat, info.lon));
		if(unique != null) {
			idPoiMap.remove(unique);
		}
		locationIdMap.remove(SkobblerUtils.getUniqueId(info.lat, info.lon));
		addressPoiMap.remove(info.address);
	}

	public void removeAll() {
		locationIdMap.clear();
		idPoiMap.clear();
		addressPoiMap.clear();
	}
	
	public Set<Integer> getUniqueIdSet() {
		Set<Integer> idSet = new HashSet<Integer>();
		idSet.addAll(idPoiMap.keySet());
		return idSet;
	}

	private Integer getRandomUniqueId() {
		Random r = new Random();
		Integer uniqueId = r.nextInt(idEndWith - idStartWith) + idStartWith;
		while (idPoiMap.containsKey(uniqueId)) {
			uniqueId = r.nextInt(idEndWith - idStartWith) + idStartWith;
		}
		return uniqueId;
	}
	
}
