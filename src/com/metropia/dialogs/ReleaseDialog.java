package com.metropia.dialogs;

import com.metropia.activities.LandingActivity2;
import com.metropia.activities.R;
import com.metropia.utils.Preferences;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.widget.TextView;

public class ReleaseDialog extends Dialog implements android.view.View.OnClickListener {
	
	String version;

	public ReleaseDialog(Context context) {
		super(context, R.style.FullScreenDialog);
		setContentView(R.layout.release_dialog);
		
		getWindow().getAttributes().height = -1;
		setCanceledOnTouchOutside(false);
		
		findViewById(R.id.dialogOK).setOnClickListener(this);
		
		try {
			version = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {}
		((TextView)findViewById(R.id.dialogTitle)).setText(getContext().getString(R.string.releaseTitle, version));
	}

	@Override
	public void onClick(View v) {
		dismiss();
	}
	
	@Override
	public void show() {
		
		
		SharedPreferences prefs = Preferences.getGlobalPreferences(getContext());
		boolean releaseNoteShown = prefs.getBoolean(version, false);
        if (releaseNoteShown) return;
        
        super.show();
        prefs.edit().putBoolean(version, true).commit();
	}

}
