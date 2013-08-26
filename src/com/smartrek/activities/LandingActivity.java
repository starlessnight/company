package com.smartrek.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class LandingActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing);
        findViewById(R.id.plan_trip_btn).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandingActivity.this, HomeActivity.class);
                intent.putExtra(HomeActivity.INIT, true);
                startActivity(intent);
            }
        });
    }
    
}
