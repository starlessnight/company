package smartrek.adapters;

import java.util.List;

import smartrek.activities.R;
import smartrek.models.User;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ContactItemAdapter extends ArrayAdapter<User> {
	
	private int textViewResourceId;
	private List<User> objects;
	
	public ContactItemAdapter(Context context, int textViewResourceId,
			List<User> objects) {
		super(context, textViewResourceId, objects);
		
		this.textViewResourceId = textViewResourceId;
		this.objects = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		User u = objects.get(position);
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(textViewResourceId, parent, false);
		
		TextView textView1 = (TextView)view.findViewById(R.id.textViewName);
		// FIXME: Need to consider i18n
		textView1.setText(String.format("%s %s (%s)", u.getFirstname(), u.getLastname(), u.getUsername()));

		return view;
	}
}