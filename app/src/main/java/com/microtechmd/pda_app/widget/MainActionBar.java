package com.microtechmd.pda_app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.microtechmd.pda_app.R;

/**
 * Created by Administrator on 2018/3/29.
 */

public class MainActionBar extends RelativeLayout {
    private TextView title;
    private ImageButton setting;
    private ImageButton left;

    public MainActionBar(Context context) {
        super(context);
    }

    public MainActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.widget_main_actionbar, this);

        title = findViewById(R.id.text_view_title_bar);
        setting = findViewById(R.id.setting);
        left = findViewById(R.id.left);
    }

    public MainActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTitleText(String text) {
        title.setText(text);
    }

    public void setTitleText(int resourceId) {
        title.setText(resourceId);
    }


    public void setRightButtonListener(OnClickListener l) {
        setting.setOnClickListener(l);
    }

    public void setLeftButtonVisible(boolean isVisible) {
        if (isVisible) {
            left.setVisibility(VISIBLE);
        } else {
            left.setVisibility(GONE);
        }
    }

    public void setLeftButtonListener(OnClickListener l) {
        left.setOnClickListener(l);
    }
}
