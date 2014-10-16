package com.smartrek;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.smartrek.activities.ContactsSelectActivity;
import com.smartrek.models.Contact;

public class ContactListService extends IntentService {
    
    public ContactListService() {
        super(ContactListService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Contact contact : ContactsSelectActivity.loadContactList(this)) {
                jsonArray.put(contact.toJSON());
            }
            FileUtils.writeStringToFile(getFile(this), jsonArray.toString());
        }
        catch (Throwable t) {
            Log.d("ContactListService", Log.getStackTraceString(t));
        }
    }
    
    private static File getFile(Context ctx){
        return new File(ctx.getExternalFilesDir(null), "contact_list");
    }
    
    public static void schedule(Context ctx) {
        PendingIntent saveContactList = PendingIntent.getService(ctx, 0,
                new Intent(ctx, ContactListService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        long interval = 3600000L;
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
            SystemClock.elapsedRealtime() + interval, interval, saveContactList);
    }
    
    public static ArrayList<Contact> getSyncedContactList(Context ctx) {
        ArrayList<Contact> list = new ArrayList<Contact>();
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(FileUtils.readFileToString(getFile(ctx)));
            for (int i=0; i<jsonArray.length(); i++) {
                try{
                    list.add(Contact.fromJSON(jsonArray.getJSONObject(i)));
                }catch (Throwable t) {}
            }
        }
        catch (Throwable t) {}
        return list;
    }

}
