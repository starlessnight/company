package com.smartrek.requests;

import java.util.EnumMap;

import org.json.JSONObject;

import android.content.Context;

import com.smartrek.requests.ServiceDiscoveryRequest.Result;

public final class ServiceDiscoveryRequest extends FetchRequest<Result> {
    
    public static class Result {
        
        public EnumMap<Link, String> links = new EnumMap<Link, String>(Link.class);
        
        public EnumMap<Page, String> pages = new EnumMap<Page, String>(Page.class);
        
    }
    
    public ServiceDiscoveryRequest(String url) {
        super(url);
        skipLinkUrlCheck = true;
    }
    
	public ServiceDiscoveryRequest() {
		this(ENTRYPOINT_URL);
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
		return rs;
	}

}
