package com.metropia.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.metropia.models.User;
import com.metropia.requests.AwardsFetchRequest.Award;
import com.metropia.activities.R;



public final class AwardsFetchRequest extends FetchRequest<List<Award>> {

    public static class Award {
        
        public long id;
        
        public String name;
        
        public String picture;
        
        public String task;
        
        public String description;
        
        public String type;
        
        public int percent;
        
        public boolean complete;
        
        public String headerLabel;
        
        public boolean hideSeparator;
        
        public int getShareHintResId(){
            int id = 0;
            if("points".equals(type)){
                id = R.string.points_award_share_hint;
            }else if("trips".equals(type)){
                id = R.string.trips_award_share_hint;
            }else if("environment".equals(type)){
                id = R.string.environment_award_share_hint;
            }else if("donated".equals(type)){
                id = R.string.donated_award_share_hint;
            }else if("redemptions".equals(type)){
                id = R.string.redemptions_award_share_hint;
            }
            return id;
        }
        
    }
    
    
	public AwardsFetchRequest(long uid) {
		super(String.format("%s/achievements/full/%d", NEW_HOST, uid));
	}
	
	public AwardsFetchRequest(User user) {
        super(getLinkUrl(Link.achievement).replaceAll("\\{user_id\\}", String.valueOf(user.getId())));
        this.username = user.getUsername();
        this.password = user.getPassword();
    }
	
	@Override
	public List<Award> execute(Context ctx) throws Exception {
		String response = executeFetchRequest(getURL(), ctx);
		List<Award> awards = new ArrayList<Award>();
		if(NEW_API){
		    JSONArray list = new JSONObject(response).getJSONArray("data");
            for(int j=0; j<list.length(); j++){
                JSONObject json = list.getJSONObject(j);
                Award award = new Award();
                award.id = json.getLong("id");
                award.name = json.getString("name");
                award.picture = json.getString("picture");
                award.task = json.getString("task");
                award.description = json.getString("description");
                award.type = json.getString("type");
                award.complete = json.getBoolean("complete");
                award.percent = json.optInt("percent", award.complete?100:0);
                awards.add(award);
            }
        }else{
            JSONArray jsons = new JSONArray(response);
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
                    award.type = json.getString("type");
                    award.percent = json.getInt("percent");
                    award.complete = json.getBoolean("complete");
                    awards.add(award);
                }
            }
        }
		return awards;
	}
	

}
