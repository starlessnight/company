package com.smartrek.dialogs;

import org.apache.commons.lang3.StringUtils;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.models.Address;
import com.smartrek.models.User;
import com.smartrek.ui.EditAddress;
import com.smartrek.utils.ExceptionHandlingService;
import com.smartrek.utils.Font;

public class ShortcutAddressDialog extends Dialog implements TextWatcher {
	
	public interface ActionListener {
		void onClickPositiveButton();
		void onClickNegativeButton();
	}
	
	private ExceptionHandlingService ehs = new ExceptionHandlingService(getContext());
	
	private ActionListener listener;
	private String title;
	private ViewGroup dialogView;
	private EditAddress editTextAddress;
	private ProgressBar progressBar;
	
	public ShortcutAddressDialog(Context context) {
		this(context, null);
	}
	
	public ShortcutAddressDialog(Context context, String title) {
		super(context, R.style.PopUpDialog);
		this.title = title;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.shortcut_address_dialog, null);
		
		TextView titleView = (TextView) dialogView.findViewById(R.id.title);
		titleView.setText(title);
		
		editTextAddress = (EditAddress) dialogView.findViewById(R.id.editTextAddress);
		editTextAddress.addTextChangedListener(this);
		
		progressBar = (ProgressBar) dialogView.findViewById(R.id.progressBar);
		
		setContentView(dialogView);
		
		ImageButton favAddrBtn = (ImageButton) dialogView.findViewById(R.id.fav_addr_btn);
		favAddrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavoriteAddressListDialog dialog = new FavoriteAddressListDialog(getContext());
                dialog.setActionListener(new FavoriteAddressListDialog.ActionListener() {
                    
                    @Override
                    public void onClickNegativeButton() {
                    }
                    
                    @Override
                    public void onClickNeutralButton() {
                        final FavoriteAddressEditDialog dialog = new FavoriteAddressEditDialog(getContext());
                        dialog.setAddress(new Address(0, User.getCurrentUser(getContext()).getId(), "", editTextAddress.getText().toString().trim(), 0, 0));
                        dialog.setActionListener(new FavoriteAddressEditDialog.ActionListener() {
                            
                            @Override
                            public void onClickPositiveButton() {
                                editTextAddress.setAddress(dialog.getAddressObject());
                            }
                            
                            @Override
                            public void onClickNegativeButton() {
                            }
                        });
                        dialog.show();
                    }
                    
                    @Override
                    public void onClickListItem(Address item, int position) {
                        editTextAddress.unsetAddress();
                        editTextAddress.setAddress(item);
                    }
                });
                dialog.show();
            }
        });
		
		Button confirmButton = (Button) dialogView.findViewById(R.id.confirm_button);
		confirmButton.setEnabled(false);
		confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClickPositiveButton();
                }
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
		Font.setTypeface(Font.getBold(assets), titleView, confirmButton);
		Font.setTypeface(Font.getLight(assets), editTextAddress);
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
		
	}
	
	public String getAddress() {
		return editTextAddress.getText().toString().trim();
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
        if(editTextAddress != null){
            Button btn = (Button) dialogView.findViewById(R.id.confirm_button);
            btn.setEnabled(StringUtils.isNotBlank(editTextAddress.getText()));
        }
    }
	
}
