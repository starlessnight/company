package com.smartrek.requests;

import java.util.EnumMap;

import org.json.JSONObject;

import android.content.Context;

import com.smartrek.requests.ServiceDiscoveryRequest.Result;

public final class ServiceDiscoveryRequest extends FetchRequest<Result> {
    
    public static class Result {
        
        public EnumMap<Link, String> links = new EnumMap<Link, String>(Link.class);
        
        public EnumMap<Page, String> pages = new EnumMap<Page, String>(Page.class);
        
        public EnumMap<Setting, Object> settings = new EnumMap<Setting, Object>(Setting.class);
        
    }
    
    public ServiceDiscoveryRequest(String url) {
        super(url);
    }
	
	@Override
	public Result execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		JSONObject links = new JSONObject(response).getJSONObject("links");
		Result rs = new Result();
		for(Link l : Link.values()){
		    String name = l.name();
            if(links.has(name)){
                rs.links.put(l, links.getString(name));
            }
		}
		JSONObject pages = new JSONObject(response).getJSONObject("pages");
		for(Page p : Page.values()){
            String name = p.name();
            if(pages.has(name)){
                rs.pages.put(p, pages.getString(name));
            }
        }
		JSONObject settings = new JSONObject(response).getJSONObject("settings");
        for(Setting s : Setting.values()){
            String name = s.name();
            if(settings.has(name)){
                rs.settings.put(s, settings.get(name));
            }
        }
		return rs;
	}

}
