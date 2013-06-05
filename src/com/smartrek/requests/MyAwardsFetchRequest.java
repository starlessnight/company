package com.smartrek.requests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;

public class MyAwardsFetchRequest extends FetchRequest<List<String>> {
	
	public MyAwardsFetchRequest(int uid) {
		super(String.format("%s/achievements/%d", NEW_HOST, uid));
	}

	@Override
	public List<String> execute(Context ctx) throws Exception {
	    String response = executeFetchRequest(getURL(), ctx);
        JSONArray jsons  = new JSONArray(response);
        List<String> awards = new ArrayList<String>();
        for(int i=0; i<jsons.length(); i++){
            awards.add(jsons.getString(i));
        }
        return awards;
	}

}
