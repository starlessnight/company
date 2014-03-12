package com.smartrek.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
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
import android.widget.TextView;
import android.widget.ToggleButton;

import com.smartrek.utils.Font;

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
		
//		addButton = (ImageView) findViewById(R.id.add_button);
		searchTextView = (EditText) findViewById(R.id.search);
		searchTextView.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				String filter = s.toString();
				int num = updateContactList(filter);
				if(num == 0) {
					/*
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
									searchTextView.setText("");
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
					*/
				}
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
		
		
		contactListAdapter = new ArrayAdapter<Contact>(this,
				R.layout.contact_select_item, R.id.contactInfo) {
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
		};
		updateContactList(null);
		contactListView = (ListView) findViewById(R.id.contacts_list);
		contactListView.setAdapter(contactListAdapter);
	}
	
	private String listToString(Collection<String> list) {
		StringBuffer string = new StringBuffer();
		for(String email : list) {
			string.append(email).append(", ");
		}
		return string.toString();
	}
	
	private int updateContactList(String filter) {
		List<Contact> contacts = new ArrayList<Contact>();
		Map<String, Contact> contactsMap = new LinkedHashMap<String, Contact>();
        List<String> ids = new ArrayList<String>(); 
        Cursor people = ContactsSelectActivity.this.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while(people.moveToNext()) {
           int nameFieldColumnIndex = people.getColumnIndex(PhoneLookup.DISPLAY_NAME);
           String name = people.getString(nameFieldColumnIndex);
           int idFieldColumnIndex = people.getColumnIndex(PhoneLookup._ID);
           String contactId = people.getString(idFieldColumnIndex);
           Contact contact = new Contact();
           contact.id = contactId;
           contact.name = name;
           contactsMap.put(contactId, contact);
           ids.add(contactId);
        }
        people.close();
        Cursor emails = ContactsSelectActivity.this.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, 
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " in (" + StringUtils.join(ids, ",") + ")", null, null); 
        while (emails.moveToNext()) {
            String id = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID));
            String email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            Contact contact = contactsMap.get(id);
            if(contact != null && contact.email == null){
                contact.email = email;
                if(StringUtils.isBlank(contact.name)) {
                	contact.name = email;
                }
            }
        }
        emails.close();
        for(Contact contact : contactsMap.values()){
            if(contact.email != null){
                contacts.add(contact);
            }
        }
        if(!manualInputEmail.isEmpty()) {
        	for(String email : manualInputEmail) {
	        	Contact manual = new Contact();
	        	manual.name = email;
	        	manual.email = email;
	        	contacts.add(manual);
        	}
        }
        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                return lhs.name.toLowerCase().compareTo(rhs.name.toLowerCase());
            }
        });
        
        contactListAdapter.clear();
        int filtedCnt = 0;
        for(Contact contact : contacts) {
        	if(StringUtils.isBlank(filter) || StringUtils.containsIgnoreCase(contact.name, filter) 
        			|| StringUtils.containsIgnoreCase(contact.email, filter)) {
        		contactListAdapter.add(contact);
        		filtedCnt++;
        	}
        }
        contactListAdapter.notifyDataSetChanged();
        return filtedCnt;
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
        
        String id;
        
        String name;
        
        String email;
        
    }

}