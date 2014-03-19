package com.smartrek.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.smartrek.dialogs.NotificationDialog;
import com.smartrek.utils.Font;
import com.smartrek.utils.Misc;

public class ContactsSelectActivity extends FragmentActivity {
	
	private Typeface boldFont;
	private EditText searchTextView;
	private ListView contactListView;
	private ImageView addButton;
	private ArrayAdapter<Contact> contactListAdapter;
	private Set<String> selectedContactEmails = new HashSet<String>();
	private Set<String> manualInputEmail = new HashSet<String>();
	
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
		updateContactList(null);
		contactListView = (ListView) findViewById(R.id.contacts_list);
		contactListView.setAdapter(contactListAdapter);
	    contactListView.setFastScrollEnabled(true);
	    Misc.setFastScrollAlwaysVisible(contactListView);
	}
	
	private String listToString(Collection<String> list) {
		StringBuffer string = new StringBuffer();
		for(String email : list) {
			string.append(email).append(", ");
		}
		return string.toString();
	}
	
	private void updateContactList(final String filter) {
	    AsyncTask<Void, Void, List<Contact>> task = new AsyncTask<Void, Void, List<Contact>>() {
	        
	        Set<String> inputEmails = new HashSet<String>();
	        
	        @Override
	        protected void onPreExecute() {
	            for(String email : manualInputEmail) {
	                inputEmails.add(email);
	            }
	        }
	        
            @Override
            protected List<Contact> doInBackground(Void... params) {
                List<Contact> contacts = new ArrayList<Contact>();
                Cursor people = ContactsSelectActivity.this.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, 
                    ContactsContract.Data.MIMETYPE + " = ?", 
                    new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE }, null); 
                while (people.moveToNext()) {
                    String firstname = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                    String lastname = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                    String email = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    if(email != null){
                        Contact contact = new Contact();
                        contact.name = StringUtils.defaultString(firstname) 
                            + " " + StringUtils.defaultString(lastname);
                        contact.lastnameInitial = StringUtils.defaultString(
                            StringUtils.capitalize(StringUtils.substring(lastname, 0, 1)));
                        contact.email = email;
                        if(StringUtils.isBlank(contact.name)) {
                            contact.name = email;
                            contact.lastnameInitial = StringUtils.defaultString(
                                StringUtils.capitalize(StringUtils.substring(email, 0, 1)));
                        }
                        contacts.add(contact);
                    }
                }
                people.close();
                
                if(!inputEmails.isEmpty()) {
                    for(String email : inputEmails) {
                        Contact manual = new Contact();
                        manual.name = email;
                        manual.email = email;
                        manual.lastnameInitial = StringUtils.defaultString(
                            StringUtils.capitalize(StringUtils.substring(email, 0, 1)));
                        contacts.add(manual);
                    }
                }
                List<Contact> filteredList = new ArrayList<ContactsSelectActivity.Contact>();
                for(Contact contact : contacts) {
                    if(StringUtils.isBlank(filter) || StringUtils.containsIgnoreCase(contact.name, filter) 
                            || StringUtils.containsIgnoreCase(contact.email, filter)) {
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
        Misc.parallelExecute(task);
	}
	
	private boolean emailFormatIsGood(String email) {
		return Pattern.matches("^[\\w-\\+]+(\\.[\\w-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", email);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		setResult(Activity.RESULT_CANCELED);
		finish();
	}
	
    private static class Contact {
        
        String name;
        
        String lastnameInitial;
        
        String email;
        
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
            ToggleButton selectButton = (ToggleButton) view.findViewById(R.id.contact_select_button);
            selectButton.setTag(item.email);
            if(selectedContactEmails.contains(item.email)) {
                selectButton.setChecked(true);
                Log.d("SelectedEmail", listToString(selectedContactEmails));
            }
            else {
                selectButton.setChecked(false);
            }
            selectButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                        boolean isChecked) {
                    String email = (String) buttonView.getTag();
                    if(isChecked) {
                        selectedContactEmails.add(email);
                    }
                    else {
                        selectedContactEmails.remove(email);
                    }
                }
            });
            
            selectButton.requestLayout();
            contactInfo.setText(item.name);
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
