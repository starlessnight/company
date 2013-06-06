package com.smartrek.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.smartrek.requests.AwardsFetchRequest.Award;



public final class AwardsFetchRequest extends FetchRequest<List<Award>> {

    public static class Award {
        
        public enum Type { 
            
            beta {
                 @Override 
                 public String description(long trips){
                     return "beta tester";
                 }
            }, 
            trips {
                @Override
                public String description(long trips) {
                    return "complete " + trips + " trip" + (trips > 1?"s":"");
                }
            },
            points {
                @Override
                public String description(long lifeTimePoints) {
                    return "earn " + lifeTimePoints + " trekpoint" 
                        + (lifeTimePoints > 1?"s":"");
                }
            };
            
            public static Type of(String type){
                Type typeEnum = null;
                for(Type t : values()){
                    if(t.name().equals(type)){
                        typeEnum = t;
                        break;
                    }
                }
                return typeEnum;
            }
            
            public abstract String description(long threshold);
            
        }
        
        public long id;
        
        public String name;
        
        public String type;
        
        public String picture;
        
        public long threshold;
        
        public String headerLabel;
        
        public boolean hideSeparator;
        
        public boolean completed;
        
    }
    
    
	public AwardsFetchRequest() {
		super(String.format("%s/achievements", NEW_HOST));
	}
	
	@Override
	public List<Award> execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		JSONArray jsons  = new JSONArray(response);
		List<Award> awards = new ArrayList<Award>();
		for(int i=0; i<jsons.length(); i++){
		    JSONObject json = jsons.getJSONObject(i);
		    Award award = new Award();
		    award.id = json.getLong("id");
		    award.name = json.getString("name");
		    award.type = json.getString("type");
		    award.threshold = json.getLong("threshold");
		    award.picture = json.getString("picture");
		    awards.add(award);
		}
		return awards;
	}
	

}
