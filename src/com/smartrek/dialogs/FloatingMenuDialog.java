package com.smartrek.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.smartrek.activities.R;
import com.smartrek.ui.menu.MainMenu;

public class FloatingMenuDialog extends Dialog {
	
	public interface ActionListener {
		void onClickPositiveButton();
		void onClickNegativeButton();
	}
	
	private ActionListener listener;
	private ViewGroup dialogView;
	private Activity context;
	
	/**
	 * When an instance of Address is given, this dialog will trigger an edit mode.
	 * 
	 * @param context
	 */
	public FloatingMenuDialog(Activity context) {
		super(context, R.style.PopUpDialog);
		this.context = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.floating_menu_dialog, null);
		
		int[] menuIds = {R.id.route, R.id.dashboard, R.id.reservation, 
	        R.id.map_display_options, R.id.home, R.id.logout_option};
		for(int id : menuIds){
		    final View vMenu = dialogView.findViewById(id);
		    vMenu.post(new Runnable() {
                @Override
                public void run() {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) vMenu.getLayoutParams();
                    layoutParams.height = vMenu.getWidth() * 5 / 6;
                }
            });
            vMenu.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                dismiss();
	                MainMenu.onMenuItemSelected(context, 0, v.getId());
	            }
	        });
		}
		
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
		
		setContentView(dialogView);
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
		
	}
	
}
