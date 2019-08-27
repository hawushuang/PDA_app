package com.microtechmd.pda_app.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.SubscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.aa.cdemo.JNIUtils;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.gson.Gson;
import com.microtechmd.pda.library.entity.monitor.DateTime;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ApplicationPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.activity.SettingActivity;
import com.microtechmd.pda_app.adapter.BaseViewHolder;
import com.microtechmd.pda_app.adapter.CommonAdapter;
import com.microtechmd.pda_app.entity.DbHistory;
import com.microtechmd.pda_app.entity.DbHistory_;
import com.microtechmd.pda_app.entity.TestDemo;
import com.microtechmd.pda_app.entity.TrendsEntity;
import com.microtechmd.pda_app.util.FileUtil;
import com.microtechmd.pda_app.util.excelUtil.ZzExcelCreator;
import com.microtechmd.pda_app.util.excelUtil.ZzFormatCreator;
import com.microtechmd.pda_app.widget.MainActionBar;
import com.microtechmd.pda_app.widget.expandLayout.ExpandableLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE;
import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE_RECOMMEND_CAL;
import static com.microtechmd.pda_app.ActivityPDA.HIGHGLUCOSE_SAVED;
import static com.microtechmd.pda_app.ActivityPDA.HIGH_DEFAULT;
import static com.microtechmd.pda_app.ActivityPDA.LOWGLUCOSE_SAVED;
import static com.microtechmd.pda_app.ActivityPDA.LOW_DEFAULT;


public class FragmentTrends extends FragmentBase {
    public static final float IGV = 6.6f;
    private View mRootView;
    private MainActionBar mainActionBar;
    private RelativeLayout rl_time_space;
    private ImageView iv_select;
    private TextView time_begin;
    private TextView time_end;
    private RadioGroup radioGroup;
    private TextView tv_glucose_average;
    private PieChart mChart;
    private Date date_lastWeek;
    private Date date_last14Days;
    private Date date_lastMonth;
    private Date date_today;
    private TextView display_hi_event, display_hi_duration, display_low_event, display_low_duration;
    private LinearLayout ll_range_statistics;
    private LinearLayout ll_trends;
    private LinearLayout ll_scroll;
    private TextView text1;
    private TextView text2;
    private NestedScrollView scrollview;
    private LineChartView lineChart;
    private LineChartData data;
    private TextView tv_more;
    private ExpandableLayout expandableLayout;
    private RecyclerView recycleview;
    private CommonAdapter<TrendsEntity> adapter;
    private List<TrendsEntity> mDatas;
    private ApplicationPDA applicationPDA;
    private int high;
    private int low;
    private Handler handler;

    private JNIUtils jniUtils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_trends, container,
                false);
        applicationPDA = (ApplicationPDA) Objects.requireNonNull(getActivity()).getApplication();
        handler = new Handler();
        jniUtils = JNIUtils.getInstance();
        initViewId();
        initActionBar();
        initRadioGroup();
        initPieChart();
        initLineChart();
        initRecyclerView();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time_next_zero = calendar.getTimeInMillis();

        date_today = new Date();
        date_today.setTime(time_next_zero);


        date_lastWeek = new Date();
        date_lastWeek.setTime(time_next_zero - 60 * 60 * 24 * 7 * 1000L);

        date_last14Days = new Date();
        date_last14Days.setTime(time_next_zero - 60 * 60 * 24 * 14 * 1000L);

        date_lastMonth = new Date();
        date_lastMonth.setTime(time_next_zero - 60 * 60 * 24 * 30 * 1000L);

        initTimeSpace(date_lastWeek, date_today, 7);

        initClick();

        iv_select.setVisibility(View.GONE);
        rl_time_space.setClickable(false);
        return mRootView;
    }

    private void initViewId() {
        mainActionBar = mRootView.findViewById(R.id.toolbar);
        rl_time_space = mRootView.findViewById(R.id.rl_date_space);
        iv_select = mRootView.findViewById(R.id.iv_select);
        time_begin = mRootView.findViewById(R.id.time_begin);
        time_end = mRootView.findViewById(R.id.time_end);
        radioGroup = mRootView.findViewById(R.id.radio_group);
        tv_glucose_average = mRootView.findViewById(R.id.tv_glucose_average);
        mChart = mRootView.findViewById(R.id.pie_chart);
        display_hi_event = mRootView.findViewById(R.id.display_hi_events);
        display_hi_duration = mRootView.findViewById(R.id.display_hi_duration);
        display_low_event = mRootView.findViewById(R.id.display_low_events);
        display_low_duration = mRootView.findViewById(R.id.display_low_duration);
        ll_range_statistics = mRootView.findViewById(R.id.ll_range_statistics);
        ll_trends = mRootView.findViewById(R.id.ll_trends);
        ll_scroll = mRootView.findViewById(R.id.ll_scroll);
        text1 = mRootView.findViewById(R.id.text1_1);
        text2 = mRootView.findViewById(R.id.text2_2);
        lineChart = mRootView.findViewById(R.id.chart);
        tv_more = mRootView.findViewById(R.id.tv_more);
        expandableLayout = mRootView.findViewById(R.id.expandable_layout);
        recycleview = mRootView.findViewById(R.id.recycleview);
        scrollview = mRootView.findViewById(R.id.scrollview);
    }

    private void initActionBar() {
        mainActionBar.setTitleText(R.string.activity_main_tab_trends);
        mainActionBar.setRightButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });
    }

    private void initRadioGroup() {
        radioGroup.check(R.id.time_7);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.time_7:
                        initTimeSpace(date_lastWeek, date_today, 7);
                        iv_select.setVisibility(View.GONE);
                        rl_time_space.setClickable(false);
                        break;
                    case R.id.time_14:
                        initTimeSpace(date_last14Days, date_today, 14);
                        iv_select.setVisibility(View.GONE);
                        rl_time_space.setClickable(false);
                        break;
                    case R.id.time_30:
                        initTimeSpace(date_lastMonth, date_today, 30);
                        iv_select.setVisibility(View.GONE);
                        rl_time_space.setClickable(false);
                        break;
                    case R.id.time_custom:
                        showDateRangePicker();
                        iv_select.setVisibility(View.VISIBLE);
                        rl_time_space.setClickable(true);
                        break;
                    default:

                        break;
                }
            }
        });
    }

    private void initPieChart() {
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);

        // enable rotation of the chart by touch
        mChart.setRotationEnabled(false);
        mChart.setHighlightPerTapEnabled(true);
        mChart.setDrawHoleEnabled(false);

        mChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);

        Legend l = mChart.getLegend();
//        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
//        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
//        l.setOrientation(Legend.LegendOrientation.VERTICAL);
//        l.setDrawInside(true);
//        l.setTextColor(Color.WHITE);
        l.setEnabled(false);

        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
        mChart.setEntryLabelTextSize(14f);
    }

    private void initLineChart() {
        lineChart.setInteractive(true);
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setZoomEnabled(false);
    }

    private void initRecyclerView() {
        mDatas = new ArrayList<>();
        adapter = new CommonAdapter<TrendsEntity>(getActivity(), R.layout.item_more, mDatas) {
            @Override
            public void convert(BaseViewHolder holder, int position) {
                TextView tv_name = holder.getView(R.id.item_name);
                tv_name.setText(mDatas.get(position).getName());
                holder.setText(R.id.item_normal, mDatas.get(position).getNormal());
                holder.setText(R.id.item_value, mDatas.get(position).getValue() + "");
            }
        };
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recycleview.setLayoutManager(layoutManager);
        //解决数据加载不完的问题
        recycleview.setNestedScrollingEnabled(false);
        recycleview.setHasFixedSize(true);
        //解决数据加载完成后, 没有停留在顶部的问题
        recycleview.setFocusable(false);
        //添加Android自带的分割线
//        recycleview.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recycleview.setAdapter(adapter);
    }

    private void initClick() {
        rl_time_space.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateRangePicker();
            }
        });

        tv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (expandableLayout.isExpanded()) {
                    expandableLayout.collapse();
                    tv_more.setText("更多");
//                    saveBitmapAndExcel();

                } else {
                    expandableLayout.expand();
                    tv_more.setText("收起");
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
////                            scrollview.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
//
//                            scrollview.scrollTo(0, 1000);
//                        }
//                    }, 310);

                }
            }
        });
    }

    private void saveBitmapAndExcel() {
        try {
            Bitmap bitmap1 = FileUtil.getViewBitmap(ll_range_statistics);
            Bitmap bitmap2 = FileUtil.getViewBitmap(ll_trends);
            FileUtil.saveBitmapToFile("D1_1.png", bitmap1);
            FileUtil.saveBitmapToFile("D1_2.png", bitmap2);
            ZzExcelCreator zzExcelCreator = ZzExcelCreator.getInstance()
                    .createExcel(FileUtil.PATH, "D1Params")
                    .createSheet("params");
            WritableCellFormat cellFormat = ZzFormatCreator
                    .getInstance()
                    .createCellFont(WritableFont.ARIAL)
                    .setWrapContent(true, 120)
                    .getCellFormat();
            for (int i = 0; i < mDatas.size(); i++) {
                TrendsEntity trendsEntity = mDatas.get(i);
                zzExcelCreator
                        .fillContent(0, i, trendsEntity.getName().toString(), cellFormat);
                zzExcelCreator
                        .fillContent(1, i, trendsEntity.getNormal(), cellFormat);
                zzExcelCreator
                        .fillContent(2, i, trendsEntity.getValue(), cellFormat);
            }
            zzExcelCreator.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setData(long count_normal, long count_high, long count_low, long count_all) {

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.basal_color10));
        colors.add(getResources().getColor(R.color.red));
        colors.add(getResources().getColor(R.color.yellow));

        ArrayList<Integer> textColors = new ArrayList<>();
        textColors.add(getResources().getColor(R.color.white));
        textColors.add(getResources().getColor(R.color.white));
        textColors.add(getResources().getColor(R.color.white));

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (count_normal == 0 && count_high == 0 && count_low == 0) {
            mChart.setDrawCenterText(true);
            mChart.setCenterTextColor(Color.WHITE);
            mChart.setCenterText(getResources().getString(R.string.no_data));
            entries.add(new PieEntry(100));
            colors.set(0, getResources().getColor(R.color.gray_light));
            textColors.set(0, getResources().getColor(R.color.transparent));
        } else {
            mChart.setDrawCenterText(false);
            if (count_normal != 0) {
                long normal_percent = (count_normal * 100) / count_all;
                if (normal_percent > 5) {
                    entries.add(new PieEntry(count_normal, getResources().getString(R.string.normal)));
                } else {
                    entries.add(new PieEntry(count_normal, getResources().getString(R.string.normal), false));
                }
            } else {
                entries.add(new PieEntry(0));
                textColors.set(0, getResources().getColor(R.color.transparent));
            }

            if (count_high != 0) {
                long high_percent = (count_high * 100) / count_all;
                if (high_percent > 10) {
                    entries.add(new PieEntry(count_high, getResources().getString(R.string.hyper)));
                } else {
                    entries.add(new PieEntry(count_high, getResources().getString(R.string.hyper), false));
                }
            } else {
                entries.add(new PieEntry(0));
                textColors.set(1, getResources().getColor(R.color.transparent));
            }

            if (count_low != 0) {
                long low_percent = (count_low * 100) / count_all;
                if (low_percent > 10) {
                    entries.add(new PieEntry(count_low, getResources().getString(R.string.hypo)));
                } else {
                    entries.add(new PieEntry(count_low, getResources().getString(R.string.hypo), false));
                }
            }
        }


        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setSliceSpace(1f);
        dataSet.setSelectionShift(5f);

        dataSet.setColors(colors);

//        dataSet.setValueLinePart1OffsetPercentage(80.f);
//        dataSet.setValueLinePart1Length(0.2f);
//        dataSet.setValueLinePart2Length(0.8f);
//        dataSet.setValueLineColor(Color.WHITE);
////        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
//        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColors(textColors);

        mChart.setData(data);
        mChart.invalidate();
    }

    private void showDateRangePicker() {
        final FragmentCalendarView fragmentCalendarView = new FragmentCalendarView();
        FragmentDialog fragmentDialog = new FragmentDialog();
        fragmentDialog.setTitle("选择日期");
        fragmentDialog.setContent(fragmentCalendarView);
        fragmentDialog.setListener(new FragmentDialog.ListenerDialog() {
            @Override
            public boolean onButtonClick(int buttonID, Fragment content) {
                switch (buttonID) {
                    case FragmentDialog.BUTTON_ID_POSITIVE:
                        List<Date> dataList = fragmentCalendarView.getSelectedDate();
                        if (dataList.size() < 2) {
                            Toast.makeText(getActivity(), "请选择起止日期", Toast.LENGTH_SHORT).show();
                            return false;
                        } else {
                            Date date_begin = dataList.get(0);
                            Date date_end = dataList.get(dataList.size() - 1);
                            initTimeSpace(date_begin, date_end, dataList.size() - 1);
                        }
                        break;
                    default:
                        break;
                }

                return true;
            }
        });
        fragmentDialog.show(getChildFragmentManager(), null);
    }

    private void initTimeSpace(Date dateBegin, Date dateEnd, int dayCount) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/M/d", Locale.getDefault());
        time_begin.setText(formatter.format(dateBegin));
        time_end.setText(formatter.format(dateEnd));


//        Calendar cal_begin = Calendar.getInstance();
//        cal_begin.setTime(dateBegin);
//        DateTime dateTime_begin = new DateTime(cal_begin);
        long beginTime = dateBegin.getTime();

//        Calendar cal_end = Calendar.getInstance();
//        cal_end.setTime(dateEnd);
//        DateTime dateTime_end = new DateTime(cal_end);
        long endTime = dateEnd.getTime();

        QueryBuilder<DbHistory> builder = applicationPDA.getBoxStore().boxFor(DbHistory.class).query();
        Query<DbHistory> glucoseAllQuery = builder.equal(DbHistory_.event_type, GLUCOSE)
                .or()
                .equal(DbHistory_.event_type, GLUCOSE_RECOMMEND_CAL)
                .between(DbHistory_.date_time, beginTime, endTime)
                .between(DbHistory_.value, 0, 300)
                .build();
        List<DbHistory> glucoseAll = glucoseAllQuery.find();

//        double glucose_avg = glucoseAllQuery.property(DbHistory_.value).avg() / 10;
//        tv_glucose_average.setText(new DecimalFormat("0.00").format(glucose_avg));
        tv_glucose_average.setText(new DecimalFormat("0.0").format(0));

//        long count_all = glucoseAllQuery.count();
        int[] valueAll = glucoseAllQuery.property(DbHistory_.value).findInts();

        high = (int) SPUtils.get(Objects.requireNonNull(getActivity()), HIGHGLUCOSE_SAVED, HIGH_DEFAULT);
        low = (int) SPUtils.get(Objects.requireNonNull(getActivity()), LOWGLUCOSE_SAVED, LOW_DEFAULT);

        long count_all = glucoseAllQuery.count();
        long count_normal = glucoseAllQuery.setParameters(DbHistory_.value, low + 1, high - 1).count();
        long count_high = glucoseAllQuery.setParameters(DbHistory_.value, high, 300).count();
        long count_low = glucoseAllQuery.setParameters(DbHistory_.value, 0, low).count();

//        setData(count_normal, count_high, count_low, count_all);
        mChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);
        mChart.invalidate();

        display_hi_event.setText(String.valueOf(0));
        display_low_event.setText(String.valueOf(0));
        display_low_duration.setText("0" + getResources().getString(R.string.minutes));
        display_hi_duration.setText("0" + getResources().getString(R.string.minutes));
//        if (count_high != 0) {
//            displayDuration(valueAll, high, true);
//        } else {
//            display_hi_duration.setText("0" + getResources().getString(R.string.minutes));
//        }
//
//        if (count_low != 0) {
//            displayDuration(valueAll, low, false);
//        } else {
//            display_low_duration.setText("0" + getResources().getString(R.string.minutes));
//        }


        double[][] glucoseArray = new double[dayCount][288];
        for (DbHistory dbHistory : glucoseAll) {
            long time = dbHistory.getDate_time();
            int x = (int) ((time - beginTime) / (3600 * 1000 * 24));
            int y = (int) ((time - beginTime) / (300 * 1000)) - x * 288;
            glucoseArray[x][y] = (double) (dbHistory.getValue() / 10f);
        }

//        String jsonStr = readAssertResource(getActivity(), "D004.txt");
//        TestDemo testDemo = new Gson().fromJson(jsonStr, TestDemo.class);
//        List<Integer> integerList = new ArrayList<>();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
//        List<TestDemo.ListBean> testList = testDemo.getList();
//        if (testList.size() > 72) {
//            for (int i = 0; i < testList.size(); i++) {
//                TestDemo.ListBean listBean = testList.get(i);
//                long time = listBean.getTime() * 1000 - 62167334400000L;
//                long time_begin = dateBegin.getTime();
//                long time_end = dateEnd.getTime();
//                if (i == 0) {
//                    mLog.Error(getClass(), simpleDateFormat.format(new Date(time)));
//                }
//                if (time >= time_begin && time <= time_end) {
//                    int first = (int) (((time - time_begin) / (24 * 60 * 60 * 1000)) % dayCount);
//                    int second = (int) (((time - time_begin) / 300000) % 288);
//                    glucoseArray[first][second] = listBean.getSG();
//                    if (listBean.getSG() != 0) {
//                        integerList.add((int) (listBean.getSG() * 10));
//                    }
//                }
//            }
//        }
//        int[] gAll = new int[integerList.size()];
//        long normal_c = 0;
//        long low_c = 0;
//        long high_c = 0;
//        long all_c = integerList.size();
//        for (int i = 0; i < integerList.size(); i++) {
//            gAll[i] = integerList.get(i);
//            if (integerList.get(i) >= high) {
//                high_c++;
//            } else if (integerList.get(i) > low) {
//                normal_c++;
//            } else {
//                low_c++;
//            }
//        }
        setData(0, 0, 0, 0);
//        setData(normal_c, low_c, high_c, all_c);
//        displayDuration(gAll, high, true);
//        displayDuration(gAll, low, false);

        setLineData(glucoseArray);
        setMore(glucoseArray);

    }

    public static String readAssertResource(Context context, String strAssertFileName) {
        AssetManager assetManager = context.getAssets();
        String strResponse = "";
        try {
            InputStream ims = assetManager.open(strAssertFileName);
            strResponse = getStringFromInputStream(ims);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResponse;
    }

    private static String getStringFromInputStream(InputStream a_is) {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(a_is))) {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException ignored) {
        }
        return sb.toString();
    }

    //double四舍五入保留小数
    public double round(Double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        try {
            BigDecimal b = null == v ? new BigDecimal("0.0") : new BigDecimal(Double.toString(v));
            BigDecimal one = new BigDecimal("1");
            return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (Exception e) {
            return 0.00;
        }

    }

    @SuppressLint("SetTextI18n")
    private void setMore(double[][] glucoseArray) {
//        if (glucoseArray.length < 13) {
//            return;
//        }
        jniUtils.init(glucoseArray, low / 10d, high / 10d);

        mDatas.clear();
        mDatas.add(new TrendsEntity(new SpannableString("项目"), "正常参考值", "测量值"));

        double[] lbgd = jniUtils.getLBGD();
        double[] hbgd = jniUtils.getHBGD();
        double[] pt = jniUtils.getPT();
        double mbg = jniUtils.getMBG();
        double mvalue = jniUtils.getMValue();
        double sdbg = jniUtils.getSDBG();
        double cv = jniUtils.getCV();
        double jindex = jniUtils.getJIndex();
        double iqr = jniUtils.getIQR();
        double aac = jniUtils.getAAC();
        double auc = jniUtils.getAUC();
        double lbgi = jniUtils.getLBGI();
        double hbgi = jniUtils.getHBGI();
        double adrr = jniUtils.getADRR();
        double[] grade = jniUtils.getGRADE();
        double lage = jniUtils.getLAGE();
        double[] mage = jniUtils.getMAGE();
        double mag = jniUtils.getMAG();
        double modd = jniUtils.getMODD();
        double conga = jniUtils.getCONGA();

        tv_glucose_average.setText(String.valueOf(round(mbg, 2)));
        display_low_event.setText(String.valueOf(lbgd[0]));
        display_low_duration.setText(new DecimalFormat("0.0").format(round(lbgd[1], 2) * 60) +
                getResources().getString(R.string.minutes));
        display_hi_event.setText(String.valueOf(hbgd[0]));
        display_hi_duration.setText(new DecimalFormat("0.0").format(round(hbgd[1], 2) * 60) +
                getResources().getString(R.string.minutes));
        setData((long) (round(pt[1], 1) * 10), (long) (round(pt[2], 1) * 10), (long) (round(pt[0], 1) * 10), 1000L);
        text1.setText("(" + round(pt[2], 1) + "%)");
        text2.setText("(" + round(pt[0], 1) + "%)");

        mDatas.add(new TrendsEntity(new SpannableString("MBG(mmol/L)\n平均血糖"), "4.3 ~ 6.6 ", String.valueOf(round(mbg, 2))));
        mDatas.add(new TrendsEntity(new SpannableString("M-value\nM值"), "控制良好（0-18）、控制一般（18-32）、控制不佳（>32）", String.valueOf(round(mvalue, 2))));
        mDatas.add(new TrendsEntity(new SpannableString("SDBG(mmol/L)\n血糖标准差"), "0.5 ~ 1.4", String.valueOf(round(sdbg, 2))));
        mDatas.add(new TrendsEntity(new SpannableString("%CV\n变异系数"), "18 ~ 36", String.valueOf(round(cv, 2))));
        mDatas.add(new TrendsEntity(new SpannableString("J-index\nJ指数"), "控制理想（10-20）、控制良好（20-30）、控制不佳（30-40）、缺乏控制（>40）", String.valueOf(round(jindex, 2))));
        mDatas.add(new TrendsEntity(new SpannableString("IQR(mmol/L)\n四分位距"), "0.7 ~ 1.6", String.valueOf(round(iqr, 2))));

        SpannableString spannableStringAAC = new SpannableString("AAC5.6(day•mmol/L)\n葡萄糖≤5.6的曲线上面积");
        spannableStringAAC.setSpan(new SubscriptSpan(), 3, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        mDatas.add(new TrendsEntity(spannableStringAAC, "", String.valueOf(round(aac, 2))));

        SpannableString spannableStringAUC = new SpannableString("AUC5.6(day•mmol/L)\n葡萄糖≥5.6的曲线下面积");
        spannableStringAUC.setSpan(new SubscriptSpan(), 3, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        mDatas.add(new TrendsEntity(spannableStringAUC, " < 0.9 ", String.valueOf(round(auc, 2))));

//        mDatas.add(new TrendsEntity("AUC(day•mmol/L)\n" + "血糖曲线下面积", "", String.valueOf(round(auc, 2))));
        mDatas.add(new TrendsEntity(new SpannableString("LBGI\n低血糖指数"), "", String.valueOf(round(lbgi, 2))));
        mDatas.add(new TrendsEntity(new SpannableString("HBGI\n高血糖指数"), "", String.valueOf(round(hbgi, 2))));
        mDatas.add(new TrendsEntity(new SpannableString("ADRR\n日平均危险范围"), "低风险（<20）、中等风险（20-40）、高风险（>40）", String.valueOf(round(adrr, 2))));
        mDatas.add(new TrendsEntity(new SpannableString("GRADE\n血糖风险评估"), "< 5", String.valueOf(round(grade[0], 2))));
        mDatas.add(new TrendsEntity(new SpannableString("LAGE(mmol/L)\n最大血糖波动幅度"), "< 5.7", String.valueOf(round(lage, 2))));
        mDatas.add(new TrendsEntity(new SpannableString("MAGE(mmol/L)\n平均血糖波动幅度"), "< 3.9", String.valueOf(round(Math.abs(mage[0]) / 2 + Math.abs(mage[1]) / 2, 2))));
        mDatas.add(new TrendsEntity(new SpannableString("MAG(mmol/L/hour)\n每隔1小时的葡萄糖平均绝对差"), "", String.valueOf(round(mag, 2))));
        mDatas.add(new TrendsEntity(new SpannableString("MODD(mmol/L)\n日间血糖平均绝对差"), "< 1.4", String.valueOf(round(modd, 2))));

        SpannableString spannableStringCONGA = new SpannableString("CONGA4(mmol/L)\n连续每隔4小时的血糖净作用");
        spannableStringCONGA.setSpan(new SubscriptSpan(), 5, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        mDatas.add(new TrendsEntity(spannableStringCONGA, "", String.valueOf(round(conga, 2))));

        adapter.notifyDataSetChanged();
        jniUtils.destroy();
//        for (float[] floats : glucoseArray) {
//            float mbg = ArrayUtil.getAverageNoZero(floats);
//            double sdbg = ArrayUtil.getStandardDeviation(floats);
//            float[] arrayDelZero = ArrayUtil.getArrayDelZero(floats);
//            Arrays.sort(arrayDelZero);
//            float lage = arrayDelZero[arrayDelZero.length - 1] - arrayDelZero[0];
//            double cv = sdbg * 100 / mbg;
//            double j_index = 0.324 * Math.pow((mbg + sdbg), 2);
//            double sum = 0;
//            double sum_low = 0;
//            double sum_high = 0;
//            for (float v : arrayDelZero) {
//                sum += Math.pow(Math.abs(10 * Math.log10(v / IGV)), 3);
//                double tbg = 1.794 * (Math.pow(Math.log(v), 1.026) - 1.861);
//                double rl;
//                double rh;
//                if (tbg > 0) {
//                    rl = 0;
//                    rh = 10 * Math.pow(tbg, 2);
//                } else {
//                    rl = 10 * Math.pow(tbg, 2);
//                    rh = 0;
//                }
//                sum_low += rl;
//                sum_high += rh;
//            }
//            double m_value = sum / arrayDelZero.length;
//            double lbgi = sum_low / arrayDelZero.length;
//            double hbgi = sum_high / arrayDelZero.length;
//
//        }
    }

    private void setLineData(double[][] glucoseArray) {
        List<PointValue> valuesAverage = new ArrayList<>();
        List<PointValue> values25 = new ArrayList<>();
        List<PointValue> values75 = new ArrayList<>();

        for (int i = 5; i <= 24 * 60; i += 5) {
            int index = (i - 5) / 5;
            float sum = 0;
            List<Float> glucoseList = new ArrayList<>();
            for (double[] doubles : glucoseArray) {
                sum += doubles[index];
                if (doubles[index] != 0) {
                    glucoseList.add((float) doubles[index]);
                }
            }
            if (glucoseList.size() >= 4) {
                valuesAverage.add(new PointValue(i, sum / glucoseList.size()));
                Collections.sort(glucoseList);
                double aa = glucoseList.get(glucoseList.size() / 4);
                values25.add(new PointValue(i, (float) aa));
                double bb = glucoseList.get(glucoseList.size() * 3 / 4);
                values75.add(new PointValue(i, (float) bb));
            }

        }
        Line lineAverage = new Line(valuesAverage);
        Line line25 = new Line(values25);
        Line line75 = new Line(values75);

        setLineStyle(lineAverage);
        setLineStyle(line25);
        setLineStyle(line75);

        lineAverage.setColor(Color.parseColor("#FF00DEFF"));
        line25.setStrokeWidth(1);
//        line25.setColor(getResources().getColor(R.color.yellow));
        line75.setStrokeWidth(1);
//        line75.setColor(getResources().getColor(R.color.yellow));

        line75.setFillLine(line25);
//        line75.setLineFillColor(getResources().getColor(R.color.yellow));

        List<Line> lines = new ArrayList<>();
        lines.add(lineAverage);
        lines.add(line25);
        lines.add(line75);

        List<PointValue> valuesLow = new ArrayList<>();
        valuesLow.add(new PointValue(0, low / 10f));
        valuesLow.add(new PointValue(24 * 60 + 1, low / 10f));
        Line lineLow = new Line(valuesLow);
        lineLow.setPointRadius(1);
        lineLow.setStrokeWidth(1);
        lineLow.setColor(Color.GRAY);
        lines.add(lineLow);

        List<PointValue> valuesHigh = new ArrayList<>();
        valuesHigh.add(new PointValue(0, high / 10f));
        valuesHigh.add(new PointValue(24 * 60 + 1, high / 10f));
        Line lineHigh = new Line(valuesHigh);
        lineHigh.setPointRadius(1);
        lineHigh.setStrokeWidth(1);
        lineHigh.setColor(Color.GRAY);
        lines.add(lineHigh);

        data = new LineChartData(lines);

        List<AxisValue> mAxisXValues = new ArrayList<>();//X轴坐标值
        for (float i = 0; i < 24 * 60; i += 60 * 4) {
            mAxisXValues.add(new AxisValue(i).setLabel((int) (i / 60) + ""));
        }
        mAxisXValues.add(new AxisValue(24 * 60).setLabel("0"));
        Axis axisX = new Axis().setHasLines(true);
        axisX.setLineColor(Color.GRAY);
        axisX.setValues(mAxisXValues);

        List<AxisValue> mAxisYValues = new ArrayList<>();//Y轴坐标值
        for (float i = 0; i <= 25; i += 5) {
            mAxisYValues.add(new AxisValue(i).setLabel((int) i + ""));
        }
        Axis axisY = new Axis();
        axisY.setHasLines(false);
        axisY.setInside(true);
        axisY.setValues(mAxisYValues);

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        lineChart.setLineChartData(data);

        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.bottom = 0;
        v.top = 26f;
        //固定Y轴的范围,如果没有这个,Y轴的范围会根据数据的最大值和最小值决定
        lineChart.setMaximumViewport(v);
        v.left = 0;
        v.right = 24 * 60;
        lineChart.setCurrentViewport(v);
    }

    private void setLineStyle(Line line) {
        line.setHasPoints(false);
        line.setStrokeWidth(3);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
