package com.smartrek.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.smartrek.activities.R;
import com.smartrek.models.Trip;
import com.smartrek.utils.ExceptionHandlingService;

public class TripListDialog extends AlertDialog {
	
	/**
	 * Dialog action listener
	 *
	 */
	public interface ActionListener {
		void onClickNegativeButton();
		void onClickListItem(Trip trip);
	}
	
	private ExceptionHandlingService ehs = new ExceptionHandlingService(getContext());
	
	private DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (listener != null) {
				listener.onClickNegativeButton();
			}
		}
		
	};
	
	private ActionListener listener;
	private ViewGroup dialogView;
	private ListView listViewTrip;
	private TextView textViewEmpty;
	
	public TripListDialog(Context context) {
		super(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = (ViewGroup) inflater.inflate(R.layout.trip_list, null);
		
		setView(dialogView);
		setTitle("Trip List");
		
		setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", onClickListener);
		
		initTripList();
		
		// This has to be called after all overriding code, otherwise it won't
		// look like a dialog.
		super.onCreate(savedInstanceState);
	}
	
	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}
	
	private void initTripList() {
		listViewTrip = (ListView) dialogView.findViewById(R.id.list_view_trip);
		textViewEmpty = (TextView) dialogView.findViewById(R.id.text_view_empty);
		
		// FIXME: Temporary
		listViewTrip.setVisibility(View.INVISIBLE);
		textViewEmpty.setVisibility(View.VISIBLE);
		setButton(DialogInterface.BUTTON_NEGATIVE, "Dismiss", onClickListener);
	}

}
