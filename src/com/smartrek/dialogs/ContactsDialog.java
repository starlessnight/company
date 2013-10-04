package com.smartrek.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;

public class ContactsDialog extends Dialog implements TextWatcher {
	
	public interface ActionListener {
		void onClickPositiveButton(List<String> emails);
		void onClickNegativeButton();
	}
	
	private ExceptionHandlingService ehs = new ExceptionHandlingService(getContext());
	
	private ActionListener listener;
	private String title;
	private ViewGroup dialogView;
	private EditText editTextEmail;
	private ProgressBar progressBar;
	private Activity ctx;
	
	public ContactsDialog(Activity ctx) {
		super(ctx, R.style.PopUpDialog);
		this.title = title;
		this.ctx = ctx;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.contacts_dialog, null);
		
		TextView titleView = (TextView) dialogView.findViewById(R.id.title);
		titleView.setText("Enter Email");
		
		editTextEmail = (EditText) dialogView.findViewById(R.id.editTextEmail);
		editTextEmail.addTextChangedListener(this);
		
		progressBar = (ProgressBar) dialogView.findViewById(R.id.progressBar);
		
		setContentView(dialogView);
				
		Button confirmButton = (Button) dialogView.findViewById(R.id.confirm_button);
		confirmButton.setEnabled(false);
		confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    String email = editTextEmail.getText().toString();
                    listener.onClickPositiveButton(Collections.singletonList(email));
                }
                dismiss();
                /*
                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build());
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, email)
                        .build());
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                        .build());
                try {
                    ctx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                    Toast.makeText(ctx, "Email added", Toast.LENGTH_SHORT).show();
                    editTextEmail.setText("");
                } catch (Exception e) {
                }
                */
            }
        });
		
		Button contactsButton = (Button) dialogView.findViewById(R.id.contacts_button);
		contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<Contact> contacts = new ArrayList<Contact>();
                Map<String, Contact> contactsMap = new LinkedHashMap<String, Contact>();
                List<String> ids = new ArrayList<String>(); 
                Cursor people = ctx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
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
                Cursor emails = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, 
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " in (" + StringUtils.join(ids, ",") + ")", null, null); 
                while (emails.moveToNext()) {
                    String id = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID));
                    String email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    Contact contact = contactsMap.get(id);
                    if(contact != null && contact.email == null){
                        contact.email = email;
                    }
                }
                emails.close();
                for(Contact contact : contactsMap.values()){
                    if(contact.email != null){
                        contacts.add(contact);
                    }
                }
                Collections.sort(contacts, new Comparator<Contact>() {
                    @Override
                    public int compare(Contact lhs, Contact rhs) {
                        return lhs.name.compareTo(rhs.name);
                    }
                });
                int len = contacts.size();
                final boolean[] checkedItems = new boolean[len];
                CharSequence[] items = new CharSequence[len];
                for(int i=0; i<len; i++){
                    Contact contact = contacts.get(i);
                    items[i] = contact.name + "\n" + contact.email;
                }
                AlertDialog dialog = new AlertDialog.Builder(ctx)
                    .setMultiChoiceItems(items, null, new OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            checkedItems[which] = isChecked;
                            int checkedCnt = 0;
                            for(boolean c : checkedItems){
                                if(c){
                                    checkedCnt++;
                                }
                            }
                            ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(checkedCnt > 0);
                        }
                    })
                    .setPositiveButton(string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            List<String> emails = new ArrayList<String>();
                            for(int i=0; i<checkedItems.length; i++){
                                if(checkedItems[i]){
                                    emails.add(contacts.get(i).email);
                                }
                            }
                            if (listener != null) {
                                listener.onClickPositiveButton(emails);
                            }
                            dismiss();
                        }
                    })
                    .setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            
                        }
                    })
                    .create();
                    dialog.setOnShowListener(new OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }
                    });
                    dialog.getListView().setFastScrollEnabled(true);
                    dialog.show();
            }
        });
		
		View closeIcon = dialogView.findViewById(R.id.close_icon);
		closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onClickNegativeButton();
                }
            }
        });
		
		AssetManager assets = getContext().getAssets();
		Font.setTypeface(Font.getBold(assets), titleView, contactsButton, confirmButton);
		Font.setTypeface(Font.getLight(assets), editTextEmail);
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
		
	}
	
	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}
	
    @Override
    public void afterTextChanged(Editable s) {
        
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(editTextEmail != null){
            Button btn = (Button) dialogView.findViewById(R.id.confirm_button);
            btn.setEnabled(StringUtils.isNotBlank(editTextEmail.getText()));
        }
    }
    
    private static class Contact {
        
        String id;
        
        String name;
        
        String email;
        
    }
	
}
