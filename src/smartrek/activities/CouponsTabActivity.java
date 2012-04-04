package smartrek.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class CouponsTabActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.coupon_inbox);
		TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
		tabHost.setup();

		TabSpec spec1 = tabHost.newTabSpec("Tab 1");
		spec1.setContent(R.id.tab1);
		spec1.setIndicator("My Coupons");

		TabSpec spec2 = tabHost.newTabSpec("Tab 2");
		spec2.setIndicator("Received");
		spec2.setContent(R.id.tab2);

		TabSpec spec3 = tabHost.newTabSpec("Tab 3");
		spec3.setIndicator("Sent");
		spec3.setContent(R.id.tab3);

		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		tabHost.addTab(spec3);

	}
}
