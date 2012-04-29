package smartrek.activities;

import smartrek.models.Route;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public final class RouteReserveActivity extends Activity {
	
	private Route route;
	
	private TextView textViewOrigin;
	private TextView textViewDestination;
	private TextView textViewDepartureTime;
	private TextView textViewArrivalTime;
	private TextView textViewCredits;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_reserve);

        Bundle extras = getIntent().getExtras();
        route = extras.getParcelable("route");
        
        textViewOrigin = (TextView) findViewById(R.id.textViewOrigin);
        textViewOrigin.setText(route.getOrigin());
        
        textViewDestination = (TextView) findViewById(R.id.textViewDestination);
        textViewDestination.setText(route.getDestination());
        
        textViewDepartureTime = (TextView) findViewById(R.id.textViewDepartureTime);
        textViewDepartureTime.setText(route.getDepartureTime().format2445());
        
        textViewArrivalTime = (TextView) findViewById(R.id.textViewArrivalTime);
        textViewArrivalTime.setText(route.getArrivalTime().format2445());
	}
}
