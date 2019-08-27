package com.microtechmd.pda_app.activity;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microtechmd.pda.library.entity.monitor.DateTime;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.entity.BaseMsgEntity;
import com.microtechmd.pda_app.entity.Friend;
import com.microtechmd.pda_app.entity.FriendDetail;
import com.microtechmd.pda_app.fragment.FragmentHelloChartsGraph;
import com.microtechmd.pda_app.network.NetPostUtil;
import com.microtechmd.pda_app.network.okhttp_util.callback.MyStringCallback;
import com.microtechmd.pda_app.widget.SwitchMultiButton;
import com.vise.xsnow.http.callback.ACallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.Call;

import static com.microtechmd.pda_app.constant.StringConstant.FRIUSERNO;
import static com.microtechmd.pda_app.fragment.FragmentCombinedGraph.pointSpace;


public class FriendsDetailActivity extends ActivityPDA {
    private static final long millisecond_1 = 60 * 60 * 1000;
    private static final long millisecond_2 = millisecond_1 * 2;
    private static final long millisecond_4 = millisecond_1 * 4;

    private String accessId;
    private String signKey;

    private SwitchMultiButton radio_group;
    private LineChartView chart;
    private LineChartData data;
    private TextView tv_time_line_left;
    private TextView tv_time_line_right;
    private float minY = 0f;//Y轴坐标最小值
    private float maxY = 25f;//Y轴坐标最大值

    private long step = millisecond_1;
    private long zero;
    private long maxLimit;
    private long minLimit;
    private float displayLeft;
    private float displayRight;

    private List<PointValue> valuesAll;
    private List<AxisValue> mAxisYValues = new ArrayList<>();//Y轴坐标值
    private List<AxisValue> mAxisYValues_2 = new ArrayList<>();//Y_2轴坐标值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);
        accessId = (String) SPUtils.get(this, StringConstant.ACCESSID, "");
        signKey = (String) SPUtils.get(this, StringConstant.SIGNKEY, "");
        String friuserno = getIntent().getStringExtra(FRIUSERNO);
        valuesAll = new ArrayList<>();
        int y = 0;
        for (float i = minY; i <= maxY; i += 5) {
            mAxisYValues.add(new AxisValue(i).setLabel((int) i + ""));
            mAxisYValues_2.add(new AxisValue(i).setLabel(y + ""));
            y += 2;
        }

        chart = (LineChartView) findViewById(R.id.chart);
        tv_time_line_left = (TextView) findViewById(R.id.tv_time_line_left);
        tv_time_line_right = (TextView) findViewById(R.id.tv_time_line_right);

        chart.setInteractive(true);
        chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        chart.setZoomEnabled(false);
        chart.setMaxZoom(Float.MAX_VALUE);
        chart.setViewportChangeListener(new ViewportChangeListener() {
            @Override
            public void onViewportChanged(Viewport viewport) {
                long timeLineLeft = ((long) viewport.left) * pointSpace + zero;
                long timeLineRight = ((long) viewport.right) * pointSpace + zero;
                SimpleDateFormat format = new SimpleDateFormat("MM-dd", Locale.getDefault());
                String left = format.format(new Date(timeLineLeft));
                String right = format.format(new Date(timeLineRight));
                tv_time_line_left.setText(left);
                tv_time_line_right.setText(right);
                if (TextUtils.equals(left, right)) {
                    tv_time_line_right.setVisibility(View.GONE);
                } else {
                    tv_time_line_right.setVisibility(View.VISIBLE);
                }
            }
        });
        radio_group = (SwitchMultiButton) findViewById(R.id.radio_group);
        String[] tabTexts = {getResources().getString(R.string.six_hour),
                getResources().getString(R.string.half_hour),
                getResources().getString(R.string.all_hour)};
        radio_group.setText(tabTexts).setSelectedTab(0).setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                switch (position) {
                    case 0:
                        step = millisecond_1;
                        break;
                    case 1:
                        step = millisecond_2;
                        break;
                    case 2:
                        step = millisecond_4;
                        break;
                    default:
                        break;
                }
                setZoom();
            }
        });
        generateData(new ArrayList<FriendDetail.ContentBean.DatalistBean>());
        loadData(friuserno);
    }

    private void loadData(String friuserno) {
        if (TextUtils.isEmpty(friuserno)) {
            return;
        }
        Map<String, Object> reqcontent = new LinkedHashMap<>();
        Map<String, Object> datab = new LinkedHashMap<>();

        datab.put("recordid", "0");
//		datab.put("recordtime", "20180101010101");
//		datab.put("devicetime", "20180101010101");
//		datab.put("parameter1", "1");
//		datab.put("parameter2", "1");
//		datab.put("parameter3", "1");
//		datab.put("parameter4", "1");
//		datab.put("eventindex", "1");
//		datab.put("eventport", "1");
//		datab.put("eventtype", "1");
//		datab.put("eventlevel", "1");
//		datab.put("eventdata", "1");

        Map<String, Object> datae = new LinkedHashMap<>();
        datae.put("recordid", "999999999");
//		datae.put("recordtime", "20190101101010");
//		datae.put("devicetime", "20190101101010");
//		datae.put("parameter1", "9999");
//		datae.put("parameter2", "9999");
//		datae.put("parameter3", "9999");
//		datae.put("parameter4", "9999");
//		datae.put("eventindex", "9999");
//		datae.put("eventport", "9999");
//		datae.put("eventtype", "9999");
//		datae.put("eventlevel", "9999");
//		datae.put("eventdata", "9999");

        String ordersort = "DESC";
        String pagesize = "";
        String currentpage = "";

        reqcontent.put("devicetype", "1");
        reqcontent.put("friuserno", friuserno);
        reqcontent.put("deviceid", "");
        reqcontent.put("datab", datab);
        reqcontent.put("datae", datae);
        reqcontent.put("ordersort", ordersort);
        reqcontent.put("pagesize", pagesize);
        reqcontent.put("currentpage", currentpage);

        NetPostUtil.doAction(accessId, signKey, reqcontent, "userdataQuery",
                new MyStringCallback(FriendsDetailActivity.this) {
                    @Override
                    public void onResponse(String response, int id) {
                        super.onResponse(response, id);
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }
                        FriendDetail authorModel = new Gson().fromJson(response, FriendDetail.class);
                        if (authorModel == null) {
                            return;
                        }
                        if (authorModel.getContent() != null) {
                            if (authorModel.getContent().getDatalist() != null) {
                                generateData(authorModel.getContent().getDatalist());
                            }
                        }
                    }
                }
        );
    }

    private void generateData(List<FriendDetail.ContentBean.DatalistBean> dataList) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        //当前整点时间
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        long now_zero = calendar.getTimeInMillis();
        //今天0点
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        //以当天0点位基准 如果数据相隔一天则所有点重新换基准
        zero = calendar.getTimeInMillis();
        maxLimit = now_zero - zero + 5 * 60 * 60 * 1000;
        minLimit = now_zero - zero - ((24 + hour % 4) * 60 * 60 * 1000);

        List<Line> lines = new ArrayList<>();

        if (dataList.size() > 0) {
            valuesAll.addAll(dbToPoint(dataList));
        }
        if (valuesAll.size() > 0) {
            //设置曲线最左端点
            setMinPoint();
            Line glucoseLine = getGlucoseLine(valuesAll);
            lines.add(glucoseLine);

        } else {
            lines.add(new Line());
        }

        //添加阈值线
        Line lowLine = getLimitLine("low");
        Line highLine = getLimitLine("high");

        lines.add(lowLine);
        lines.add(highLine);

        data = new LineChartData(lines);
        data.setValueLabelBackgroundEnabled(false);
        data.setValueLabelsTextColor(Color.RED);
        setZoom();
    }

    private List<PointValue> dbToPoint(List<FriendDetail.ContentBean.DatalistBean> dataList) {
        List<PointValue> values = new ArrayList<>();
        long current = System.currentTimeMillis();
        for (FriendDetail.ContentBean.DatalistBean dbHistory : dataList) {
            if (dbHistory.getEventlevel() == 0) {
                if (dbHistory.getEventtype() == GLUCOSE ||
                        dbHistory.getEventtype() == GLUCOSE_RECOMMEND_CAL) {
                    DateTime dateTime = new DateTime();
                    dateTime.setBCD(dbHistory.getDevicetime());
                    long t = dateTime.getCalendar().getTimeInMillis();
                    if (t < current + 60 * 60 * 1000) {
                        float value_display = dbHistory.getEventdata() / 10f;
                        switch ((int) value_display) {
                            case 0:
                                value_display = 2f;
                                break;
                            case 255:
                                value_display = 25f;
                                break;
                            default:

                                break;
                        }
                        long time = t - zero;
                        values.add(new PointValue(time / pointSpace, value_display));
                    }
                }
            }
        }
        return values;
    }

    private void setMinPoint() {
        PointValue minPoint = Collections.min(valuesAll, new Comparator<PointValue>() {
            @Override
            public int compare(PointValue pointValue, PointValue t1) {
                Float f1 = pointValue.getX();
                Float f2 = t1.getX();
                return f1.compareTo(f2);
            }
        });
        if (minPoint.getX() * pointSpace < minLimit) {
            Calendar calendarMin = Calendar.getInstance();
            calendarMin.setTimeInMillis(((long) minPoint.getX()) * pointSpace + zero);
            calendarMin.set(Calendar.MINUTE, 0);
            calendarMin.set(Calendar.SECOND, 0);
            calendarMin.set(Calendar.MILLISECOND, 0);
            minLimit = calendarMin.getTimeInMillis() -
                    (calendarMin.get(Calendar.HOUR_OF_DAY) % 4) * millisecond_1 - zero;
        }
    }

    private void setZoom() {
        Axis axisX = new Axis().setHasLines(true);
        axisX.setLineColor(Color.GRAY);
        List<AxisValue> xValues = new ArrayList<>();
        for (long value = minLimit; value < maxLimit; value += step) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            AxisValue axisValue = new AxisValue(value / pointSpace);
            axisValue.setLabel("" + format.format(new Date(value + zero)));
            xValues.add(axisValue);
        }
        axisX.setValues(xValues);
        axisX.setMaxLabelChars(0);
        axisX.setTextSize(13);

        Axis axisY = new Axis();
        axisY.setHasLines(false);
        axisY.setInside(true);
        axisY.setValues(mAxisYValues);

        Axis axisY_2 = new Axis();
        axisY_2.setHasLines(false);
        axisY_2.setInside(true);
        axisY_2.setValues(mAxisYValues_2);


        data.setAxisXBottom(axisX);
        data.setAxisYRight(axisY);
        data.setAxisYLeft(axisY_2);
        chart.setLineChartData(data);

        setDisplayArea();
    }

    private void setDisplayArea() {
        Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = minY;
        v.top = maxY + 4f;
        //固定Y轴的范围,如果没有这个,Y轴的范围会根据数据的最大值和最小值决定
        chart.setMaximumViewport(v);
        //这2个属性的设置一定要在lineChart.setMaximumViewport(v);这个方法之后,不然显示的坐标数据是不能左右滑动查看更多数据的

        long now = System.currentTimeMillis();
        displayLeft = (now - zero + 60 * 60 * 1000 - 6 * step) / pointSpace;
        displayRight = (now - zero + 60 * 60 * 1000) / pointSpace;
        if (valuesAll.size() > 0) {
            PointValue maxPoint = Collections.max(valuesAll, new MyComparator());
            float l = maxPoint.getX() - (step * 5) / pointSpace;
            float r = maxPoint.getX() + step / pointSpace;
            if (l < minLimit / pointSpace) {
                displayLeft = minLimit / pointSpace;
                displayRight = (minLimit + 6 * step) / pointSpace;
            } else {
                displayLeft = l;
                displayRight = r;
            }
        }
        v.left = displayLeft;
        v.right = displayRight;

        chart.setCurrentViewport(v);
    }

    public class MyComparator implements Comparator<PointValue> {
        @Override
        public int compare(PointValue pointValue, PointValue t1) {
            Float f1 = pointValue.getX();
            Float f2 = t1.getX();
            return f1.compareTo(f2);
        }
    }

    @NonNull
    private Line getGlucoseLine(List<PointValue> values) {
        Line line = new Line(values);
        line.setPointColor(Color.parseColor("#FF00DEFF"));
        line.setPointRadius(2);
        line.setHasLines(false);
        return line;
    }

    private Line getLimitLine(String limit) {
        int high = (int) SPUtils.get(Objects.requireNonNull(this), HIGHGLUCOSE_SAVED, HIGH_DEFAULT);
        int low = (int) SPUtils.get(this, LOWGLUCOSE_SAVED, LOW_DEFAULT);

        Line line = new Line();
        List<PointValue> valuesLimit = new ArrayList<>();
        line.setHasPoints(false);
        line.setHasLines(true);
        line.setColor(Color.parseColor("#FF348C6C"));
        line.setAreaTransparency(90);
        line.setStrokeWidth(0);
        switch (limit) {
            case "low":
                valuesLimit.add(new PointValue(minLimit / pointSpace, low / 10f));
                valuesLimit.add(new PointValue(maxLimit / pointSpace, low / 10f));
                line.setFilled(false);
                break;
            case "high":
                line.setBaseArea(low / 10f);
                line.setFilled(true);
                valuesLimit.add(new PointValue(minLimit / pointSpace, high / 10f));
                valuesLimit.add(new PointValue(maxLimit / pointSpace, high / 10f));
                break;
            default:

                break;
        }
        line.setValues(valuesLimit);
        return line;
    }

    @Override
    protected void onClickView(View v) {
        super.onClickView(v);
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            default:
                break;
        }
    }

}
