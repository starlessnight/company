package com.smartrek.requests;

import java.util.EnumMap;

import org.json.JSONObject;

import android.content.Context;

import com.smartrek.requests.Request.Link;

public final class ServiceDiscoveryRequest extends FetchRequest<EnumMap<Link, String>> {
    
    public static class Trekpoint {
        
        public long credit;
        
        public long lifeTimeCredit;
        
    }
    
	public ServiceDiscoveryRequest() {
		super(ENTRYPOINT_URL);
	}
	
	@Override
	public EnumMap<Link, String> execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		JSONObject json  = new JSONObject(response).getJSONObject("links");
		EnumMap<Link, String> rs = new EnumMap<Link, String>(Link.class);
		for(Link l : Link.values()){
		    String name = l.name();
            if(json.has(name)){
                rs.put(l, json.getString(name));
            }
		}
		return rs;
	}

}
