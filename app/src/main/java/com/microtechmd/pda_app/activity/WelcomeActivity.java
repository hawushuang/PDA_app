package com.microtechmd.pda_app.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.constant.StringConstant;


public class WelcomeActivity extends ActivityPDA {
    private Button register;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        register = findViewById(R.id.button_register);
        login = findViewById(R.id.button_login);

        initClick();
    }

    private void initClick() {
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this,
                        RegistrationPhoneActivity.class);
//                intent.putExtra(StringConstant.REGISTFLAG, StringConstant.REGISTRATION);
                startActivity(intent);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this,
                        LoginPhoneActivity.class);
                startActivity(intent);
            }
        });
    }
}
