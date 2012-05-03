package smartrek.activities;

import java.io.IOException;

import smartrek.mappers.ReservationMapper;
import smartrek.models.Route;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public final class ReservationConfirmationActivity extends Activity {
	
	private Route route;
	
	private TextView textViewOrigin;
	private TextView textViewDestination;
	private TextView textViewDepartureTime;
	private TextView textViewArrivalTime;
	private TextView textViewCredits;
	
	private Button buttonReserve;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reservation_confirmation);

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

        textViewCredits = (TextView) findViewById(R.id.textViewCredits);
        
        buttonReserve = (Button) findViewById(R.id.buttonReserve);
        buttonReserve.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO: This must be executed in a separate thread
				ReservationMapper mapper = new ReservationMapper();
				try {
					mapper.reserveRoute(route);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
        });
	}
}
