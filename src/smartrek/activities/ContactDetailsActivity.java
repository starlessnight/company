package smartrek.activities;

import smartrek.models.User;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public final class ContactDetailsActivity extends Activity {
	/**
	 * Model instance
	 */
	private User user;
	
	private TextView textViewName;
	
	private TextView textViewUsername;
	
	private TextView textViewEmail;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_details);
		
		Bundle extras = getIntent().getExtras();
		user = (User) extras.getParcelable("user");
		
		textViewName = (TextView) findViewById(R.id.textViewName);
		textViewName.setText(user.getName());
		
		textViewUsername = (TextView) findViewById(R.id.textViewUsername);
		textViewUsername.setText(user.getUsername());
		
		textViewEmail = (TextView) findViewById(R.id.textViewEmail);
		textViewEmail.setText(user.getEmail());
	}
}
