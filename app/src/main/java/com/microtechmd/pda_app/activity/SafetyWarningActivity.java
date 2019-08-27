package com.microtechmd.pda_app.activity;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.widget.StateButton;


public class SafetyWarningActivity extends ActivityPDA {
    private ImageButton back;
    private StateButton next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_warning);

        back = findViewById(R.id.ibt_back);
        next = findViewById(R.id.button_next);
        initClick();
    }

    private void initClick() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startactivity(LowGlucoseAlertActivity.class);
            }
        });
    }
}
