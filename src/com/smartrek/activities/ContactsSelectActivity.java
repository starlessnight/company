package com.smartrek.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.smartrek.ContactListService;
import com.smartrek.dialogs.CancelableProgressDialog;
import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.models.Contact;
import com.smartrek.utils.Font;
import com.smartrek.utils.Misc;

public class ContactsSelectActivity extends FragmentActivity {
    
	private Typeface boldFont;
	private EditText searchTextView;
	private ListView contactListView;
	private ImageView addButton;
	private ArrayAdapter<Contact> contactListAdapter;
	private Set<String> selectedContactEmails = new HashSet<String>();
	private Set<String> selectedContactPhones = new HashSet<String>();
	private Set<String> manualInputEmail = new HashSet<String>();
	private List<Contact> contactList = new ArrayList<Contact>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_select);
		
		AssetManager assets = getAssets();
		boldFont = Font.getBold(assets);
		
		TextView backButton = (TextView) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});
		
		TextView doneButton = (TextView) findViewById(R.id.done);
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra(ValidationActivity.EMAILS, StringUtils.join(selectedContactEmails, ","));
				resultIntent.putExtra(ValidationActivity.PHONES, StringUtils.join(selectedContactPhones, ","));
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
		});
		
		addButton = (ImageView) findViewById(R.id.add_button);
		addButton.setEnabled(true);
        addButton.setBackgroundResource(R.drawable.icon_add);
        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ClickAdd", "true");
                String newEmail = searchTextView.getText().toString();
                if(StringUtils.isNotBlank(newEmail)) {
                    if(emailFormatIsGood(newEmail)) {
                        manualInputEmail.add(newEmail);
                        selectedContactEmails.add(newEmail);
                        updateContactList(newEmail);
                    }
                    else {
                        NotificationDialog dialog = new NotificationDialog(ContactsSelectActivity.this, 
                                "Error email format!");
                        dialog.show();
                    }
                }
            }
        });

		searchTextView = (EditText) findViewById(R.id.search);
		searchTextView.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				String filter = s.toString();
				updateContactList(filter);
				addButton.setVisibility(emailFormatIsGood(filter)?View.VISIBLE:View.GONE);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
			
		});
		
		contactListAdapter = new ContactsAdapter();
		contactListView = (ListView) findViewById(R.id.contacts_list);
		contactListView.setAdapter(contactListAdapter);
	    contactListView.setFastScrollEnabled(true);
	    Misc.setFastScrollAlwaysVisible(contactListView);
	    
	    AsyncTask<Void, Void, ArrayList<Contact>> task = new AsyncTask<Void, Void, ArrayList<Contact>>(){
            @Override
            protected ArrayList<Contact> doInBackground(Void... params) {
                return ContactListService.getSyncedContactList(ContactsSelectActivity.this);
            }
            @Override
            protected void onPostExecute(ArrayList<Contact> extraContactList) {
                if(extraContactList != null && !extraContactList.isEmpty()){
                    Log.i("ContactsSelectActivity", "cached");
                    contactList = extraContactList;
                    updateContactList(null);
                }else{
                    Log.i("ContactsSelectActivity", "not cached");
                    AsyncTask<Void, Void, List<Contact>> loadContactList = new AsyncTask<Void, Void, List<Contact>>(){
                        
                        CancelableProgressDialog loadingDialog;
                        
                        @Override
                        protected void onPreExecute() {
                            loadingDialog = new CancelableProgressDialog(ContactsSelectActivity.this, "Loading...");
                            if(!loadingDialog.isShowing()){
                                loadingDialog.show();
                            }
                        }
                        @Override
                        protected List<Contact> doInBackground(Void... params) {
                            return loadContactList(ContactsSelectActivity.this);
                        }
                        @Override
                        protected void onPostExecute(List<Contact> result) {
                            loadingDialog.cancel();
                            contactList = result;
                            updateContactList(null);
                        }
                    };
                    Misc.parallelExecute(loadContactList);
                }
            }
        };
        Misc.parallelExecute(task);
	}
	
	private String listToString(Collection<String> list) {
		StringBuffer string = new StringBuffer();
		for(String email : list) {
			string.append(email).append(", ");
		}
		return string.toString();
	}
	
	public static ArrayList<Contact> loadContactList(Context ctx){
	    ArrayList<Contact> contacts = new ArrayList<Contact>();
        Map<String, Contact> contactsIdMap = new HashMap<String, Contact>();
        List<String> ids = new ArrayList<String>(); 
        Cursor people = ctx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while(people.moveToNext()) {
           int idFieldColumnIndex = people.getColumnIndex(PhoneLookup._ID);
           String contactId = people.getString(idFieldColumnIndex);
           Contact contact = new Contact();
           contactsIdMap.put(contactId, contact);
           ids.add(contactId);
        }
        people.close();
        Cursor names = ctx.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, 
            ContactsContract.Data.MIMETYPE + " = ? and " + ContactsContract.Data.CONTACT_ID + " in (" + StringUtils.join(ids, ",") + ")", 
            new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE }, null); 
        while (names.moveToNext()) {
            String id = names.getString(names.getColumnIndex(ContactsContract.Data.CONTACT_ID));
            String firstname = names.getString(names.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            String lastname = names.getString(names.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
            Contact contact = contactsIdMap.get(id);
            if(contact != null){
                contact.lastnameInitial = StringUtils.defaultString(
                    StringUtils.capitalize(StringUtils.substring(lastname, 0, 1)));
                contact.name = StringUtils.defaultString(firstname) + " " + StringUtils.defaultString(lastname); 
            }
        }
        names.close();
        Cursor emails = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, 
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " in (" + StringUtils.join(ids, ",") + ")", null, null);
        List<Contact> emailContacts = new ArrayList<Contact>();
        while (emails.moveToNext()) {
            String id = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID));
            String email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            Contact contact = contactsIdMap.get(id).clone();
            if(StringUtils.isNotBlank(email)){
                contact.email = email;
                if(StringUtils.isBlank(contact.name)) {
                    contact.name = email;
                }
            }
            emailContacts.add(contact);
        }
        emails.close();
        Cursor phones = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
        		ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " in (" + StringUtils.join(ids, ",") + ") " 
                + " and " + ContactsContract.CommonDataKinds.Phone.TYPE + "=" + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                , null, null);
        List<Contact> phoneContacts = new ArrayList<Contact>();
        while (phones.moveToNext()) {
        	String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
        	String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        	Contact contact = contactsIdMap.get(id).clone();
            if(StringUtils.isNotBlank(phone)){
                contact.phone = phone;
                if(StringUtils.isBlank(contact.name)) {
                	contact.name = phone;
                }
                phoneContacts.add(contact);
            }
        }
        phones.close();
        Map<String, Contact> contactsEmailMap = new HashMap<String, Contact>();
        for(Contact contact : emailContacts){
            if(contact.email != null){
                contactsEmailMap.put(contact.email, contact);
            }
        }
        contacts.addAll(contactsEmailMap.values());
        Map<String, Contact> contactsPhoneMap = new HashMap<String, Contact>();
        for(Contact contact : phoneContacts) {
        	if(contact.phone != null) {
        		contactsPhoneMap.put(contact.phone, contact);
        	}
        }
        contacts.addAll(contactsPhoneMap.values());
        return contacts;
	}
	
	AsyncTask<Void, Void, List<Contact>> updateTask;
	
	private void updateContactList(final String filter) {
	    if(updateTask != null){
	        updateTask.cancel(true);
	    }
	    updateTask = new AsyncTask<Void, Void, List<Contact>>() {
	        
	        Set<String> inputEmails = new HashSet<String>();
	        
	        @Override
	        protected void onPreExecute() {
	            for(String email : manualInputEmail) {
	                inputEmails.add(email);
	            }
	        }
	        
            @Override
            protected List<Contact> doInBackground(Void... params) {
                List<Contact> filteredList = new ArrayList<Contact>();
                if(!inputEmails.isEmpty()) {
                    for(String email : inputEmails) {
                        Contact manual = new Contact();
                        manual.name = email;
                        manual.email = email;
                        manual.lastnameInitial = StringUtils.defaultString(
                            StringUtils.capitalize(StringUtils.substring(email, 0, 1)));
                        filteredList.add(manual);
                    }
                }
                for(Contact contact : contactList) {
                    if(StringUtils.isBlank(filter) || StringUtils.containsIgnoreCase(contact.name, filter) 
                            || StringUtils.containsIgnoreCase(contact.email, filter) 
                            || StringUtils.containsIgnoreCase(contact.phone,  filter)) {
                        filteredList.add(contact);
                    }
                }
                Collections.sort(filteredList, new Comparator<Contact>() {
                    @Override
                    public int compare(Contact lhs, Contact rhs) {
                        return (lhs.lastnameInitial + " " + lhs.name).compareTo(
                            rhs.lastnameInitial + " " + rhs.name);
                    }
                });
                return filteredList;
            }
	        @Override
	        protected void onPostExecute(List<Contact> contacts) {
	            contactListAdapter.clear();
	            for(Contact contact : contacts) {
                    contactListAdapter.add(contact);
	            }
	            contactListAdapter.notifyDataSetChanged();
	        }
        };
        Misc.parallelExecute(updateTask);
	}
	
	private boolean emailFormatIsGood(String email) {
		return Pattern.matches("^[\\w-\\+]+(\\.[\\w-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", email);
	}
	
	private CharSequence formatContactInfo(String name, String email, String phone) {
		String contactInfo = name + "\n" + (StringUtils.isBlank(email)? phone : email);
		int indexOfNewline = contactInfo.indexOf("\n");
		SpannableString contactInfoSpan = SpannableString.valueOf(contactInfo);
		contactInfoSpan.setSpan(new AbsoluteSizeSpan(ContactsSelectActivity.this.getResources()
				.getDimensionPixelSize(R.dimen.smaller_font)), indexOfNewline, contactInfo.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		contactInfoSpan.setSpan(new ForegroundColorSpan(ContactsSelectActivity.this.getResources()
				.getColor(R.color.light_gray)), indexOfNewline, contactInfo.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return contactInfoSpan;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		setResult(Activity.RESULT_CANCELED);
		finish();
	}
	
    private class ContactsAdapter extends ArrayAdapter<Contact> implements SectionIndexer {
        
        ContactsAdapter(){
            super(ContactsSelectActivity.this, R.layout.contact_select_item, R.id.contactInfo);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView contactInfo = (TextView) view
                    .findViewById(R.id.contactInfo);
            Font.setTypeface(boldFont, contactInfo);
            Contact item = getItem(position);
            final ToggleButton selectButton = (ToggleButton) view.findViewById(R.id.contact_select_button);
            selectButton.setTag(StringUtils.isBlank(item.email)?item.phone:item.email);
            if(selectedContactEmails.contains(item.email) || selectedContactPhones.contains(item.phone)) {
                selectButton.setChecked(true);
                Log.d("SelectedEmail", listToString(selectedContactEmails));
                Log.d("SelectedPhone", listToString(selectedContactPhones));
            }
            else {
                selectButton.setChecked(false);
            }
            selectButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String contactInfo = (String) buttonView.getTag();
                    if(isChecked) {
                    	if(StringUtils.contains(contactInfo, "@")) {
                    		selectedContactEmails.add(contactInfo);
                    	}
                    	else {
                    		selectedContactPhones.add(contactInfo);
                    	}
                    }
                    else {
                        selectedContactEmails.remove(contactInfo);
                        selectedContactPhones.remove(contactInfo);
                    }
                }
            });
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectButton.setChecked(!selectButton.isChecked());
                }
            });
            
            selectButton.requestLayout();
            contactInfo.setText(formatContactInfo(item.name, item.email, item.phone));
            contactInfo.requestLayout();
            return view;
        }
        
        private Map<String, Integer> sections = new LinkedHashMap<String, Integer>(); 
        
        @Override
        public Object[] getSections() {
            Map<String, Integer> map = new LinkedHashMap<String, Integer>();
            for(int i=0; i<getCount(); i++){
                String s = getItem(i).lastnameInitial;
                if(!map.containsKey(s)){
                    map.put(s, i);
                }
            }
            sections = map;
            return sections.keySet().toArray();
        }
        
        @Override
        public int getPositionForSection(int section) {
            String[] keys = sections.keySet().toArray(new String[0]);
            return keys.length > 0?sections.get(keys[Math.min(section, keys.length - 1)]):0;
        }
        
        @Override
        public int getSectionForPosition(int position) {
            int section = 0;
            for (Entry<String, Integer> e : sections.entrySet()) {
                int s = e.getValue();
                if(position >= s){
                    section = s;
                    break;
                }
            }
            return section;
        }
        
    }

}
