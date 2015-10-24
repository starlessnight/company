package com.metropia.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.metropia.SkobblerUtils;

public class POICollection {

	private Integer idStartWith;
	private Integer idEndWith;
	private Map<Integer, PoiOverlayInfo> idPoiMap = new HashMap<Integer, PoiOverlayInfo>();
	private Map<String, PoiOverlayInfo> addressPoiMap = new HashMap<String, PoiOverlayInfo>();
	private Map<Integer, Integer> locationIdMap = new HashMap<Integer, Integer>();
	private Map<Integer, PoiOverlayInfo> poiIdPoiMap = new HashMap<Integer, PoiOverlayInfo>();

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
	
	public PoiOverlayInfo getExistedPOIByPoiId(Integer poiId) {
		return poiIdPoiMap.get(poiId);
	}

	public Integer addPOIToMap(PoiOverlayInfo info) {
		Integer uniqueId = getRandomUniqueId();
		locationIdMap.put(SkobblerUtils.getUniqueId(info.lat, info.lon), uniqueId);
		info.uniqueId = uniqueId;
		idPoiMap.put(uniqueId, info);
		addressPoiMap.put(info.address, info);
		if(info.id > 0) {
			poiIdPoiMap.put(info.id, info);
		}
		return uniqueId;
	}

	public void removePOI(PoiOverlayInfo info) {
		Integer unique = locationIdMap.get(SkobblerUtils.getUniqueId(info.lat, info.lon));
		if(unique != null) {
			idPoiMap.remove(unique);
		}
		locationIdMap.remove(SkobblerUtils.getUniqueId(info.lat, info.lon));
		addressPoiMap.remove(info.address);
		poiIdPoiMap.remove(info.id);
	}
	
	public void updateExistedPOIByPoiId(Integer poiId, PoiOverlayInfo newInfo) {
		PoiOverlayInfo oldPoi = getExistedPOIByPoiId(poiId);
		if(oldPoi != null) {
			try {
				Integer oldLocationId = SkobblerUtils.getUniqueId(oldPoi.lat, oldPoi.lon);
				Integer uniqueId = Integer.valueOf(locationIdMap.get(oldLocationId));
				newInfo.uniqueId = uniqueId;
				locationIdMap.remove(oldLocationId);
				addressPoiMap.remove(oldPoi.address);
				idPoiMap.put(uniqueId, newInfo);
				addressPoiMap.put(newInfo.address, newInfo);
				locationIdMap.put(SkobblerUtils.getUniqueId(newInfo.lat, newInfo.lon), uniqueId);
				poiIdPoiMap.put(poiId, newInfo);
			}
			catch(Throwable ignore){}
		}
	}

	public void removeAll() {
		locationIdMap.clear();
		idPoiMap.clear();
		addressPoiMap.clear();
		poiIdPoiMap.clear();
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
