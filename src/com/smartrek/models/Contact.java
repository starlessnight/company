package com.smartrek.models;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {
    
    public String name;
    
    public String lastnameInitial;
    
    public String email;

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
    
    public Contact() {}
    
    private Contact(Parcel in) {
        name = in.readString();
        lastnameInitial = in.readString();
        email = in.readString();
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(lastnameInitial);
        dest.writeString(email);
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("lastnameInitial", lastnameInitial);
        json.put("email", email);
        return json;
    }
    
    public static Contact fromJSON(JSONObject json) throws JSONException {
        Contact c = new Contact();
        c.name = json.getString("name");
        c.lastnameInitial = json.getString("lastnameInitial");
        c.email = json.getString("email");
        return c;
    }
    
}