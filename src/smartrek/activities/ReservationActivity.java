package smartrek.activities;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class ReservationActivity extends ListActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.reservation);
        
        String[] items = new String[] { "alpha", "beta", "charlie" };
        
        setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, items));
	}
}
