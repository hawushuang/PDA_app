package com.microtechmd.pda_app.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.widget.rangedate.CalendarPickerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FragmentCalendarView extends FragmentBase {

    private CalendarPickerView range_date_picker;

    public FragmentCalendarView() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.date_picker_content_1, container, false);
        range_date_picker = view.findViewById(R.id.range_date_picker);
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.DAY_OF_MONTH, 1);
        Calendar lastYear = Calendar.getInstance();
        lastYear.add(Calendar.YEAR, -1);
        range_date_picker.init(lastYear.getTime(), nextYear.getTime(), new SimpleDateFormat("MMMM, yyyy", Locale.getDefault())) //
                .inMode(CalendarPickerView.SelectionMode.RANGE);
        range_date_picker.scrollToDate(new Date());
        return view;
    }

    public List<Date> getSelectedDate() {
        return range_date_picker.getSelectedDates();
    }
}
