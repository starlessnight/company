package com.metropia.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.metropia.requests.RewardsFetchRequest.Reward;

public final class RewardsFetchRequest extends FetchRequest<List<Reward>> {

    public static class Reward {
        
        public long id;
        
        public String name;
        
        public String description;
        
        public String picture;
        
        public Long trekpoints;
        
    }
    
	public RewardsFetchRequest() {
		super(buildUrl());
	}
	
	@Override
	public List<Reward> execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		JSONArray jsons;
		if(NEW_API){
            jsons = new JSONObject(response).getJSONArray("data");
        }else{
            jsons = new JSONArray(response);
        }
		List<Reward> rewards = new ArrayList<Reward>();
		for(int i=0; i<jsons.length(); i++){
		    JSONObject json = jsons.getJSONObject(i);
		    Reward reward = new Reward();
		    reward.id = json.getLong("id");
		    reward.name = json.getString("name");
		    reward.description = json.getString("description");
		    reward.picture = json.getString("picture");
		    if(!json.isNull("trekpoints")){
		        reward.trekpoints = json.getLong("trekpoints");
		    }
		    rewards.add(reward);
		}
		return rewards;
	}
	
	private static String buildUrl(){
	    String url;
	    if(NEW_API){
	        url = getLinkUrl(Link.reward);
	    }else{
	        url = String.format("%s/rewards", NEW_HOST);
	    }
	    return url;
	}
	

}
