package com.microtechmd.pda_app.activity;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.widget.ScrollSelecedView.BitmapScrollPicker;
import com.microtechmd.pda_app.widget.ScrollSelecedView.ScrollPickerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class ExerciseStateActivity extends ActivityPDA {
    private RelativeLayout ll_content;
    private ImageButton back;
    private TextView exercise_name;
    private TextView start_time, end_time;

    private List<String> names;
    private CopyOnWriteArrayList<Bitmap> bitmaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_state);

        initData();

        ll_content = findViewById(R.id.ll_content);
        back = findViewById(R.id.ibt_back);
        exercise_name = findViewById(R.id.exercise_name);
        start_time = findViewById(R.id.start_time);
        end_time = findViewById(R.id.end_time);

        BitmapScrollPicker bitmapScrollPicker = findViewById(R.id.bitmapScrollPicker);
        bitmapScrollPicker.setData(bitmaps);
        bitmapScrollPicker.setOnSelectedListener(new ScrollPickerView.OnSelectedListener() {
            @Override
            public void onSelected(ScrollPickerView scrollPickerView, int position) {
                exercise_name.setText(names.get(position));
            }
        });

        //获取控件高度
        ViewTreeObserver vto = ll_content.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ll_content.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) exercise_name.getLayoutParams(); //取控件textView当前的布局参数 linearParams.height = 20;// 控件的高强制设成20
                linearParams.height = ll_content.getHeight() / 5;// 控件的高强制
                exercise_name.setLayoutParams(linearParams); //使设置好的布局参数应用到控件
            }
        });
        initClick();
    }

    private void initData() {
        bitmaps = new CopyOnWriteArrayList<>();
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.state_1));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.state_2));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.state_3));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.state_4));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.state_5));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.state_6));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.state_7));

        names = new ArrayList<>();
        names.add("Sleep");
        names.add("Illness");
        names.add("Stress");
        names.add("Feeling BG High");
        names.add("Feeling BG Low");
        names.add("Menstral Cycle");
        names.add("Alcohol");
    }

    private void initClick() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
