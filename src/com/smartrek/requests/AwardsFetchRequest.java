package com.smartrek.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.requests.AwardsFetchRequest.Award;



public final class AwardsFetchRequest extends FetchRequest<List<Award>> {

    public static class Award {
        
        public long id;
        
        public String name;
        
        public String picture;
        
        public String task;
        
        public String description;
        
        public int percent;
        
        public boolean complete;
        
        public String headerLabel;
        
        public boolean hideSeparator;
        
    }
    
    
	public AwardsFetchRequest(long uid) {
		super(String.format("%s/achievements/full/%d", NEW_HOST, uid));
	}
	
	@Override
	public List<Award> execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		JSONArray jsons  = new JSONArray(response);
		List<Award> awards = new ArrayList<Award>();
		for(int i=0; i<jsons.length(); i++){
		    JSONArray list = jsons.getJSONArray(i);
		    for(int j=0; j<list.length(); j++){
    		    JSONObject json = list.getJSONObject(j);
    		    Award award = new Award();
    		    award.id = json.getLong("id");
    		    award.name = json.getString("name");
    		    award.picture = json.getString("picture");
    		    award.task = json.getString("task");
    		    award.description = json.getString("description");
    		    award.percent = json.getInt("percent");
    		    award.complete = json.getBoolean("complete");
    		    awards.add(award);
		    }
		}
		return awards;
	}
	

}
