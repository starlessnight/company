package com.metropia.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.localytics.android.Localytics;
import com.metropia.ContactListService;
import com.metropia.SmarTrekApplication;
import com.metropia.SmarTrekApplication.TrackerName;
import com.metropia.dialogs.CancelableProgressDialog;
import com.metropia.dialogs.NotificationDialog;
import com.metropia.models.Contact;
import com.metropia.ui.animation.ClickAnimation;
import com.metropia.ui.animation.ClickAnimation.ClickAnimationEndCallback;
import com.metropia.utils.Dimension;
import com.metropia.utils.Font;
import com.metropia.utils.Misc;

public class ContactsSelectActivity extends FragmentActivity {
	
	public static final String SELECTED_EMAILS = "SELECTED_EMAILS";
	public static final String SELECTED_PHONES = "SELECTED_PHONES";
    
	private Typeface boldFont;
	private EditText searchTextView;
	private ListView contactListView;
	private View addButton;
	private ArrayAdapter<Contact> contactListAdapter;
	private Set<String> selectedContactEmails = new HashSet<String>();
	private Set<String> selectedContactPhones = new HashSet<String>();
	private Set<String> manualInputEmail = new HashSet<String>();
	private Set<String> manualInputPhone = new HashSet<String>();
	private List<Contact> contactList = new ArrayList<Contact>();
	private JSONObject frequencyContacts;
	private JSONObject topFiveFrequencyAndSelectedContacts;
	
	private Set<String> earlySelectedEmails;
	private Set<String> earlySelectedPhones;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_select);
		
		Localytics.integrate(this);
		
		AssetManager assets = getAssets();
		boldFont = Font.getBold(assets);
		
		Bundle extras = getIntent().getExtras();
		earlySelectedEmails = toSet(extras.getString(SELECTED_EMAILS));
		earlySelectedPhones = toSet(extras.getString(SELECTED_PHONES));
		
		// pre-select
		selectedContactEmails.addAll(earlySelectedEmails);
		selectedContactPhones.addAll(earlySelectedPhones);
		
		TextView backButton = (TextView) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(ContactsSelectActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						setResult(Activity.RESULT_CANCELED);
						finish();
					}
				});
			}
		});
		
		TextView doneButton = (TextView) findViewById(R.id.done);
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClickAnimation clickAnimation = new ClickAnimation(ContactsSelectActivity.this, v);
				clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						Intent resultIntent = new Intent();
						resultIntent.putExtra(ValidationActivity.EMAILS, StringUtils.join(selectedContactEmails, ","));
						resultIntent.putExtra(ValidationActivity.PHONES, StringUtils.join(selectedContactPhones, ","));
						Set<String> allSelectedContacts = new HashSet<String>();
						allSelectedContacts.addAll(selectedContactEmails);
						allSelectedContacts.addAll(selectedContactPhones);
						saveSelectedContacts(allSelectedContacts);
						setResult(Activity.RESULT_OK, resultIntent);
						finish();
					}
				});
			}
		});
		
		addButton = findViewById(R.id.add_button);
		addButton.setEnabled(true);
        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickAnimation clickAnimation = new ClickAnimation(ContactsSelectActivity.this, v);
                clickAnimation.startAnimation(new ClickAnimationEndCallback() {
					@Override
					public void onAnimationEnd() {
						String newInput = searchTextView.getText().toString();
		                if(StringUtils.isNotBlank(newInput)) {
		                    if(emailFormatIsGood(newInput)) {
		                        manualInputEmail.add(newInput);
		                        selectedContactEmails.add(newInput);
		                        searchTextView.setText("");
		                        try {
		                        	topFiveFrequencyAndSelectedContacts.put(newInput, Integer.MAX_VALUE);
		                        }
		                        catch(Throwable ignore) {}
		                        updateContactList("");
		                    }
		                    else if(phoneFormatIsGood(newInput)) {
		                    	manualInputPhone.add(newInput);
		                        selectedContactPhones.add(newInput);
		                        searchTextView.setText("");
		                        try {
		                        	topFiveFrequencyAndSelectedContacts.put(newInput, Integer.MAX_VALUE - 1);
		                        }
		                        catch(Throwable ignore) {}
		                        updateContactList("");
		                    }
		                    else {
		                        NotificationDialog dialog = new NotificationDialog(ContactsSelectActivity.this, 
		                                "Error email format!");
		                        dialog.show();
		                    }
		                }
					}
				});
            }
        });

        final View searchBoxClear = findViewById(R.id.search_box_clear);
		searchTextView = (EditText) findViewById(R.id.search);
		searchTextView.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				String filter = s.toString();
				searchBoxClear.setVisibility(StringUtils.isBlank(filter)?View.GONE:View.VISIBLE); 
				updateContactList(filter);
				boolean showAdd = emailFormatIsGood(filter) || phoneFormatIsGood(filter);
				int searchPanelPadding = Dimension.dpToPx(10, getResources().getDisplayMetrics());
				findViewById(R.id.search_panel).setPadding(searchPanelPadding, searchPanelPadding, showAdd ? 0 : searchPanelPadding, searchPanelPadding);
				addButton.setVisibility(showAdd ?View.VISIBLE:View.GONE);
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
		
		searchBoxClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTextView.setText("");
            }
        });
		
		searchTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = addButton.getVisibility() == View.VISIBLE;
                if(handled){
                    addButton.performClick();
                }
                return handled;
            }
        });
		
		contactListAdapter = new ContactsAdapter();
		contactListView = (ListView) findViewById(R.id.contacts_list);
		contactListView.setAdapter(contactListAdapter);
	    contactListView.setFastScrollEnabled(true);
	    Misc.setFastScrollAlwaysVisible(contactListView);
	    
	    initFrequencyContacts();
	    
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
                            	Misc.doQuietly(new Runnable() {
    								@Override
    								public void run() {
    									loadingDialog.show();
    								}
                            	});
                            }
                        }
                        @Override
                        protected List<Contact> doInBackground(Void... params) {
                            return loadContactList(ContactsSelectActivity.this);
                        }
                        @Override
                        protected void onPostExecute(List<Contact> result) {
                        	Misc.doQuietly(new Runnable() {
								@Override
								public void run() {
									loadingDialog.cancel();
								}
                        	});
                            contactList = result;
                            updateContactList(null);
                        }
                    };
                    Misc.parallelExecute(loadContactList);
                }
            }
        };
        Misc.parallelExecute(task);
        //init Tracker
      	((SmarTrekApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
	}
	
	private void initFrequencyContacts() {
		try {
			frequencyContacts = new JSONObject(FileUtils.readFileToString(getFile(ContactsSelectActivity.this)));
			Iterator<String> contacts = frequencyContacts.keys();
			List<FrequencyContact> freqContacts = new ArrayList<FrequencyContact>();
			while(contacts.hasNext()) {
				String contact = contacts.next();
				FrequencyContact fc = new FrequencyContact();
				fc.value = contact;
				fc.count = frequencyContacts.getInt(contact);
				freqContacts.add(fc);
			}
			Collections.sort(freqContacts, new Comparator<FrequencyContact>() {
				@Override
				public int compare(FrequencyContact lhs, FrequencyContact rhs) {
					return Integer.valueOf(rhs.count).compareTo(Integer.valueOf(lhs.count));
				}
			});
			//get top 5 frequency contacts
			topFiveFrequencyAndSelectedContacts = new JSONObject();
			for(int i = 0 ; i < freqContacts.size() && i < 5 ; i++) {
				FrequencyContact contact = freqContacts.get(i);
				topFiveFrequencyAndSelectedContacts.put(contact.value, contact.count);
			}
			
			//add selected contacts
			for(String selectedEmail : earlySelectedEmails) {
				topFiveFrequencyAndSelectedContacts.put(selectedEmail, Integer.MAX_VALUE);
			}
			for(String selectedPhone : earlySelectedPhones) {
				topFiveFrequencyAndSelectedContacts.put(selectedPhone, Integer.MAX_VALUE - 1);
			}
			
		} catch (Throwable t) {
			Log.d("FrequencyContactIO", Log.getStackTraceString(t));
			frequencyContacts = new JSONObject();
			topFiveFrequencyAndSelectedContacts = new JSONObject();
		} 
	}
	
	private Set<String> toSet(String infos) {
		String[] infoArray = StringUtils.split(infos, ",");
		Set<String> infoSet = new HashSet<String>();
		if(infoArray != null) {
			for(String info : infoArray) {
				infoSet.add(StringUtils.trimToEmpty(info));
			}
		}
		return infoSet;
	}
	
	private String listToString(Collection<String> list) {
		StringBuffer string = new StringBuffer();
		for(String email : list) {
			string.append(email).append(", ");
		}
		return string.toString();
	}
	
	private void saveSelectedContacts(Set<String> selectedContacts) {
		try {
			for(String contact : selectedContacts) {
				if(!earlySelectedEmails.contains(contact) && !earlySelectedPhones.contains(contact)) {
					if(frequencyContacts.has(contact)) {
						int count = frequencyContacts.getInt(contact);
						frequencyContacts.put(contact, count + 1);
					}
					else {
						frequencyContacts.put(contact, 1);
					}
				}
			}
			FileUtils.writeStringToFile(getFile(this), frequencyContacts.toString());
		}
		catch(Throwable t) {
			 Log.d("FrequencyContactIO", Log.getStackTraceString(t));
		}
	}
	
	private static File getFile(Context ctx){
        return new File(ctx.getExternalFilesDir(null), "frequency_contact_list");
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
	        
	        Set<String> inputEmails = new HashSet<String>(getPreSelectedManualEmail());
	        Set<String> inputPhones = new HashSet<String>(getPreSelectedManualPhone());
	        
	        @Override
	        protected void onPreExecute() {
	            for(String email : manualInputEmail) {
	                inputEmails.add(email);
	            }
	            
	            for(String phone : manualInputPhone) {
	            	inputPhones.add(phone);
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
                if(!inputPhones.isEmpty()) {
                	for(String phone : inputPhones) {
                        Contact manual = new Contact();
                        manual.name = phone;
                        manual.phone = phone;
                        manual.lastnameInitial = phone;
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
                    	try {
	                    	if(topFiveFrequencyAndSelectedContacts.has(lhs.getContactString()) && topFiveFrequencyAndSelectedContacts.has(rhs.getContactString())) {
	                    		int result = Integer.valueOf(topFiveFrequencyAndSelectedContacts.getInt(rhs.getContactString())).compareTo(Integer.valueOf(topFiveFrequencyAndSelectedContacts.getInt(lhs.getContactString())));
	                    		if(result==0) {
	                    			result = (lhs.lastnameInitial + " " + lhs.name).compareTo(rhs.lastnameInitial + " " + rhs.name);
	                    		}
	                    		return result;
	                    	}
	                    	else if(topFiveFrequencyAndSelectedContacts.has(lhs.getContactString())){
	                    		return -1;
	                    	}
	                    	else if(topFiveFrequencyAndSelectedContacts.has(rhs.getContactString())) {
	                    		return 1;
	                    	}
                    	}
                    	catch(Throwable t) {
                    		Log.e("FrequencyContact", Log.getStackTraceString(t));
                    	}
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
	
	private Set<String> getPreSelectedManualEmail() {
		Set<String> earlyManualInputEmail = new HashSet<String>();
		for(String email : earlySelectedEmails) {
			if(!containInContactList(email)) {
				earlyManualInputEmail.add(email);
			}
		}
		return earlyManualInputEmail;
	}
	
	private Set<String> getPreSelectedManualPhone() {
		Set<String> earlyManualInputPhone = new HashSet<String>();
		for(String phone : earlySelectedPhones) {
			if(!containInContactList(phone)) {
				earlyManualInputPhone.add(phone);
			}
		}
		return earlyManualInputPhone;
	}
	
	private boolean containInContactList(String input) {
		for(Contact contact : contactList) {
			if(StringUtils.equalsIgnoreCase(contact.email, input) || StringUtils.equalsIgnoreCase(contact.phone, input)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean emailFormatIsGood(String email) {
		return Pattern.matches("^[\\w-\\+]+(\\.[\\w-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", email);
	}
	
	private boolean phoneFormatIsGood(String phone) {
		return Pattern.matches("^\\+?\\d{10,12}", phone);
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
	public void onResume() {
		super.onResume();
	    Localytics.openSession();
	    Localytics.tagScreen(this.getClass().getSimpleName());
	    Localytics.upload();
	    Localytics.setInAppMessageDisplayActivity(this);
	    Localytics.handleTestMode(getIntent());
	    Localytics.handlePushNotificationOpened(getIntent());
	}
	
	@Override
	protected void onPause() {
		Localytics.dismissCurrentInAppMessage();
	    Localytics.clearInAppMessageDisplayActivity();
	    Localytics.closeSession();
	    Localytics.upload();
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
    
    private class FrequencyContact {
    	String name;
    	String value;
    	Integer count;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
    
    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

}
