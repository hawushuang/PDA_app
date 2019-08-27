package com.microtechmd.pda_app.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.microtechmd.pda.library.entity.DataList;
import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.entity.ParameterGlucose;
import com.microtechmd.pda.library.entity.ParameterMonitor;
import com.microtechmd.pda.library.entity.monitor.DateTime;
import com.microtechmd.pda.library.entity.monitor.History;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.ApplicationPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.entity.CalibrationHistory;
import com.microtechmd.pda_app.entity.DbHistory;
import com.microtechmd.pda_app.entity.DbHistory_;
import com.microtechmd.pda_app.util.SpObjectListSaveUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.objectbox.query.QueryBuilder;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.OnTouchPointListener;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

import static com.microtechmd.pda_app.ActivityPDA.CALIBRATION_HISTORY;
import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE;
import static com.microtechmd.pda_app.ActivityPDA.GLUCOSE_RECOMMEND_CAL;
import static com.microtechmd.pda_app.ActivityPDA.HIGHGLUCOSE_SAVED;
import static com.microtechmd.pda_app.ActivityPDA.HIGH_DEFAULT;
import static com.microtechmd.pda_app.ActivityPDA.HYPER;
import static com.microtechmd.pda_app.ActivityPDA.HYPO;
import static com.microtechmd.pda_app.ActivityPDA.LOWGLUCOSE_SAVED;
import static com.microtechmd.pda_app.ActivityPDA.LOW_DEFAULT;
import static com.microtechmd.pda_app.ActivityPDA.PDA_ERROR;
import static com.microtechmd.pda_app.ActivityPDA.SENSOR_ERROR;
import static com.microtechmd.pda_app.ActivityPDA.SENSOR_EXPIRATION;


public class FragmentHelloChartsGraph extends FragmentBase implements EntityMessage.Listener {
    private static final long millisecond_30min = 30 * 60 * 1000;
    private static final long millisecond_1 = 60 * 60 * 1000;
    private static final long millisecond_2 = millisecond_1 * 2;
    private static final long millisecond_4 = millisecond_1 * 4;
    private View mRootView;

    private ApplicationPDA applicationPDA;
    private boolean dateChange;
    private PowerManager.WakeLock wakeLock = null;
    private LineChartView chart;
    private LineChartData data;
    private float minY = 0f;//Y轴坐标最小值
    private float maxY = 25f;//Y轴坐标最大值
    private List<AxisValue> mAxisYValues = new ArrayList<>();//Y轴坐标值
    private List<AxisValue> mAxisYValues_2 = new ArrayList<>();//Y_2轴坐标值
    private long step = millisecond_1;
    private long maxLimit;
    private long minLimit;
    private float displayLeft;
    private float displayRight;
    private float displayDragRight;
    private long zero;
    private static int pointSpace = 30 * 1000;

    private boolean mIsHistoryQuerying = false;
    private List<DbHistory> dbList;
    private List<DbHistory> dataListAll;
    private List<DbHistory> dataListCash;
    private List<PointValue> valuesAll;
    private Handler mHandler;
    private Runnable mRunnable;
    private CountDownTimer refreshGraphCountdownTimer;

    private TextView tv_time_line_left;
    private TextView tv_time_line_right;
    private TextView display_value;
    private TextView display_week;
    private TextView display_time;
    private TextView tv_unit_bolus;


    private ExecutorService fixedThreadPool;

    public void setTimeData(int timeData) {
        switch (timeData) {
            case FragmentHome.TIME_DATA_6:
                step = millisecond_1;
                break;
            case FragmentHome.TIME_DATA_12:
                step = millisecond_2;
                break;
            case FragmentHome.TIME_DATA_24:
                step = millisecond_4;
                break;
            default:
                break;
        }
        setZoom();
    }

    @Override
    public void onReceive(EntityMessage message) {
        switch (message.getOperation()) {
            case EntityMessage.OPERATION_SET:
                handleSet(message);
                break;

            case EntityMessage.OPERATION_GET:
                break;

            case EntityMessage.OPERATION_EVENT:
                break;

            case EntityMessage.OPERATION_NOTIFY:
                handleNotification(message);
                break;

            case EntityMessage.OPERATION_ACKNOWLEDGE:
                break;

            default:
                break;
        }
    }

    private void handleSet(EntityMessage message) {
        switch (message.getParameter()) {
            case ParameterGlucose.PARAM_FILL_LIMIT:
            case ParameterGlucose.PARAM_BG_LIMIT:
//                generateData();
                break;
            case ParameterComm.RESET_DATA:
                switch (message.getData()[0]) {
                    case 0:
//                        mDataSetHistory.cleanDatabases();
                        dataListAll.clear();
                        valuesAll.clear();
                        generateData(new ArrayList<DbHistory>());
                        break;
                    case 1:
                        generateData(new ArrayList<DbHistory>());
                        break;
                    case 2:
                        dateChange = true;
                        break;
                    default:

                        break;
                }
                break;
            default:
                break;
        }
    }

    private void handleNotification(final EntityMessage message) {
        if ((message.getSourcePort() == ParameterGlobal.PORT_MONITOR) &&
                (message.getParameter() == ParameterMonitor.PARAM_HISTORY)) {
            switch (message.getSourceAddress()) {
                case ParameterGlobal.ADDRESS_LOCAL_CONTROL:
                case ParameterGlobal.ADDRESS_REMOTE_MASTER:
                    handleFromControl(message);
                    break;
                default:

                    break;
            }
        }
    }

    //数据库返回的数据
    private void handleMsgFromModel() {
        mIsHistoryQuerying = false;
        //取消屏幕常亮
        wakeLock.release();
        dismissDialogProgress();
        mLog.Error(FragmentHelloChartsGraph.this.getClass(), "UI数据库数量" + dbList.size());
        generateData(dbList);
    }

    //广播包及同步的数据
    private void handleFromControl(EntityMessage message) {
        final DataList dataList = new DataList(message.getData());
        for (int i = 0; i < dataList.getCount(); i++) {
            History history_convert = new History(dataList.getData(i));
            DbHistory history = new DbHistory();
            history.setDate_time(history_convert.getDateTime().getDateTimeLong());
            history.setEvent_type(history_convert.getEvent().getEvent());
            history.setValue(history_convert.getStatus().getShortValue1());
//            switch (history.getValue()) {
//                case 0:
//                    history.setValue(20);
//                    break;
//                case 255:
//                    history.setValue(250);
//                    break;
//                default:
//                    break;
//            }
            switch (history.getEvent_type()) {
                case GLUCOSE:
                case GLUCOSE_RECOMMEND_CAL:
                    dataListAll.add(history);
                    dataListCash.add(history);
                    break;
                case SENSOR_ERROR:
                case SENSOR_EXPIRATION:
                case HYPO:
                case HYPER:
                case PDA_ERROR:
//                case BLOOD_GLUCOSE:
//                case CALIBRATION:
//                    if (app != null) {
//                        dataErrListAll = app.getDataErrListAll();
//                        dataErrListAll.add(history);
//                        app.setDataErrListAll(dataErrListAll);
//                    }
                    break;
                default:

                    break;
            }
        }

        if (dataListCash.size() < 3 || dataListCash.size() > 500) {
            generateData(dataListCash);
            dataListCash.clear();
        }

        if (refreshGraphCountdownTimer != null) {
            refreshGraphCountdownTimer.cancel();
        }
        refreshGraphCountdownTimer = new CountDownTimer(3000, 3000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                if (dataListCash.size() > 0) {
                    generateData(dataListCash);
                    dataListCash.clear();
                }

            }
        }.start();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_hellochartsgraph, container, false);
        applicationPDA = (ApplicationPDA) getActivity().getApplication();
        chart = mRootView.findViewById(R.id.chart);
        tv_time_line_left = mRootView.findViewById(R.id.tv_time_line_left);
        tv_time_line_right = mRootView.findViewById(R.id.tv_time_line_right);
        display_value = mRootView.findViewById(R.id.display_value);
        display_week = mRootView.findViewById(R.id.display_week);
        display_time = mRootView.findViewById(R.id.display_time);
        tv_unit_bolus = mRootView.findViewById(R.id.tv_unit_bolus);

        tv_unit_bolus.setVisibility(View.INVISIBLE);

        chart.setInteractive(true);
//        chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        chart.setZoomEnabled(false);
        chart.setMaxZoom(Float.MAX_VALUE);
        chart.setViewportChangeListener(new ViewportChangeListener() {
            @Override
            public void onViewportChanged(Viewport viewport) {
                displayDragRight = viewport.right;
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

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                setHideAnimation(display_value);
                setHideAnimation(display_week);
                setHideAnimation(display_time);
            }
        };
        chart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
//                display_value.getBackground().setAlpha(255);
//                display_week.getBackground().setAlpha(255);
//                display_time.getBackground().setAlpha(255);
                pointVisible(value, handler, runnable);

                chart.setScrollEnabled(false);
            }

            @Override
            public void onValueDeselected() {

            }
        });
        chart.setOnTouchPointListener(new OnTouchPointListener() {
            @Override
            public void onPointTouch(PointValue point) {
                pointVisible(point, handler, runnable);
            }
        });
        int y = 0;
        for (float i = minY; i <= maxY; i += 5) {
            mAxisYValues.add(new AxisValue(i).setLabel((int) i + ""));
//            mAxisYValues_2.add(new AxisValue(i).setLabel(y + ""));
            y += 2;
        }
        mAxisYValues_2.add(new AxisValue(0).setLabel(5 + ""));
        mAxisYValues_2.add(new AxisValue(26).setLabel(26 + ""));

        dataListAll = new ArrayList<>();
        dataListCash = new ArrayList<>();
        valuesAll = new ArrayList<>();
        dbList = new ArrayList<>();

        generateData(dataListAll);
        return mRootView;
    }

    private void pointVisible(PointValue value, Handler handler, Runnable runnable) {
        display_value.setVisibility(View.VISIBLE);
        display_week.setVisibility(View.VISIBLE);
        display_time.setVisibility(View.VISIBLE);
        handler.removeCallbacks(runnable);
        handler.post(runnable);

        long time_x = (long) (value.getX() * pointSpace + zero);
        SimpleDateFormat format_week = new SimpleDateFormat("EE", Locale.getDefault());
        SimpleDateFormat format_time = new SimpleDateFormat("HH:mm", Locale.getDefault());

        display_value.setText(value.getY() + "");
        display_week.setText(format_week.format(new Date(time_x)));
        display_time.setText(format_time.format(new Date(time_x)));
    }

    private void setHideAnimation(final View view) {
        if (null == view) {
            return;
        }
        final AlphaAnimation mHideAnimation = new AlphaAnimation(1.0f, 0f);
        mHideAnimation.setDuration(1000);
        mHideAnimation.setFillAfter(true);
        mHideAnimation.setStartOffset(2000);
        view.startAnimation(mHideAnimation);
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((ApplicationPDA) Objects.requireNonNull(getActivity()).getApplication())
                .registerMessageListener(ParameterGlobal.PORT_MONITOR, this);

        //定长线程池，可控制线程最大并发数，超出的线程会在队列中等待
        fixedThreadPool = Executors.newFixedThreadPool(6);
        PowerManager powerManager = (PowerManager) Objects.requireNonNull(getActivity()).getSystemService(Service.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Lock");
            wakeLock.setReferenceCounted(false);
        }

        if (dataListAll == null) {
            dataListAll = new ArrayList<>();
        }
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                queryHistory();
            }
        };
        if (dataListAll.size() == 0) {
            mHandler.postDelayed(mRunnable, 100);
        }
    }

    @Override
    public void onDestroyView() {
        mHandler.removeCallbacks(mRunnable);
        ((ApplicationPDA) getActivity().getApplication())
                .unregisterMessageListener(ParameterGlobal.PORT_MONITOR, this);
        super.onDestroyView();
    }

    protected Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    private void queryHistory() {
        if (mIsHistoryQuerying) {
            return;
        }
        //请求屏幕常亮
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        showDialogProgress();
        mIsHistoryQuerying = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                QueryBuilder<DbHistory> builder = applicationPDA.getBoxStore().boxFor(DbHistory.class).query();
                builder.equal(DbHistory_.event_type, GLUCOSE)
                        .or()
                        .equal(DbHistory_.event_type, GLUCOSE_RECOMMEND_CAL);
                dbList = builder.build().find();
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleMsgFromModel();
                    }
                });
            }
        }).start();
    }

    private void showDialogProgress() {
        ((ActivityPDA) getActivity()).showDialogLoading();
    }

    private void dismissDialogProgress() {
        ((ActivityPDA) getActivity()).dismissDialogLoading();
    }

    private void generateData(List<DbHistory> dataList) {
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
        long offset = zero - calendar.getTimeInMillis();
        if ((offset != 0) && (zero != 0)) {
            for (int i = 0; i < valuesAll.size(); i++) {
                PointValue value = valuesAll.get(i);
                valuesAll.set(i, new PointValue(value.getX() + offset / pointSpace, value.getY()));
            }
        }
        zero = calendar.getTimeInMillis();

        if (offset > 0) {
            maxLimit = now_zero - zero + 5 * 60 * 60 * 1000 + offset;
        } else {
            maxLimit = now_zero - zero + 5 * 60 * 60 * 1000;
        }

        minLimit = now_zero - zero - ((24 + hour % 4) * 60 * 60 * 1000);

        List<Line> lines = new ArrayList<>();

        if (dataList.size() > 0) {
            valuesAll.addAll(dbToPoint(dataList));
        }
        if (valuesAll.size() > 0) {
            //设置曲线最左端点
            setMinPoint();
            //添加血糖线
            Line glucoseLine = getGlucoseLine(valuesAll);
            lines.add(glucoseLine);

            //添加血糖时间最大值的点
//            PointValue maxPoint;
//            History historyMax = MainActivity.getStatus();
//            if (historyMax != null) {
//                Status status = historyMax.getStatus();
//                float value_display = status.getShortValue1() / 10f;
//                long time = historyMax.getDateTime().getCalendar().getTimeInMillis() - zero;
//                maxPoint = new PointValue(time / pointSpace, value_display);
//            } else {
//                maxPoint = Collections.max(valuesAll, new MyComparator());
//            }
//
//            Line maxPointLine = new Line();
//            List<PointValue> maxPointValue = new ArrayList<>();
//            maxPointValue.add(maxPoint);
//            maxPointLine.setValues(maxPointValue);
//            maxPointLine.setPointColor(Color.GREEN);
//            maxPointLine.setPointRadius(4);
//            maxPointLine.setHasLines(false);
//            lines.add(maxPointLine);
        } else {
            lines.add(new Line());
        }

        //添加血糖校准线
        List<CalibrationHistory> list = (List<CalibrationHistory>) SpObjectListSaveUtil.get(getActivity(), CALIBRATION_HISTORY);
        if (list != null) {
            if (list.size() > 0) {
                Line calibrationLine = getCalibrationLine(list);
                lines.add(calibrationLine);
            }
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

    private void setMinPoint() {
        PointValue minPoint = Collections.min(valuesAll, new MyComparator());
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

    //血糖校准线
    private Line getCalibrationLine(List<CalibrationHistory> list) {
        Line line = new Line();
        line.setPointColor(Color.RED);
        line.setPointRadius(1);
        line.setHasLabels(true);
        line.setHasLines(false);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 2;
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.trends_glucose);
        line.setLabelBitmap(setImgSize(bmp, 26, 30));
        List<PointValue> values = new ArrayList<>();
        for (CalibrationHistory calibrationHistory : list) {
            long time = calibrationHistory.getTime() - zero;
            if (rangeInDefined(time, minLimit, maxLimit)) {
                float value = calibrationHistory.getValue();
                PointValue pointValue = new PointValue(time / pointSpace, value);
                pointValue.setLabel(value + "");
                values.add(pointValue);
            }
        }
        line.setValues(values);
        return line;
    }

    private boolean rangeInDefined(long current, long min, long max) {
        return Math.max(min, current) == Math.min(current, max);
    }

    public Bitmap setImgSize(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高.
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片.
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    private List<PointValue> dbToPoint(List<DbHistory> dataList) {
        List<PointValue> values = new ArrayList<>();
        long current = System.currentTimeMillis();
        for (DbHistory dbHistory : dataList) {
            if (dbHistory.getEvent_type() == GLUCOSE ||
                    dbHistory.getEvent_type() == GLUCOSE_RECOMMEND_CAL) {
//                DateTime dateTime = new DateTime();
//                dateTime.setBCD(dbHistory.getDate_time());
                long t = dbHistory.getDate_time();
                if (t < current + 60 * 60 * 1000) {
                    float value_display = dbHistory.getValue() / 10f;
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
        return values;
    }
//    private List<PointValue> dbToPoint(List<DbHistory> dataList) {
//        List<PointValue> values = new ArrayList<>();
//        long current = System.currentTimeMillis();
//        for (DbHistory dbHistory : dataList) {
//            if (dbHistory.getEvent_type() == GLUCOSE ||
//                    dbHistory.getEvent_type() == GLUCOSE_RECOMMEND_CAL) {
//                long t = dbHistory.getDate_time();
//                if (t < current + 60 * 60 * 1000) {
//                    float value_display = dbHistory.getValue() / 10f;
//                    long time = t - zero;
//                    values.add(new PointValue(time / pointSpace, value_display));
//                }
//            }
//        }
//        return values;
//    }

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
//        tv_unit_bolus.setVisibility(View.VISIBLE);
        chart.setLineChartData(data);

        setDisplayArea();
    }

    private void setDisplayArea() {
        Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = minY;
        v.top = maxY + 0.5f;
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


    @NonNull
    private Line getGlucoseLine(List<PointValue> values) {
        Line line = new Line(values);
        line.setPointColor(Color.parseColor("#FF00DEFF"));
        line.setPointRadius(2);
        line.setHasLines(false);
        return line;
    }

    private Line getLimitLine(String limit) {
        int high = (int) SPUtils.get(Objects.requireNonNull(getActivity()), HIGHGLUCOSE_SAVED, HIGH_DEFAULT);
        int low = (int) SPUtils.get(getActivity(), LOWGLUCOSE_SAVED, LOW_DEFAULT);

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


    public class MyComparator implements Comparator<PointValue> {
        @Override
        public int compare(PointValue pointValue, PointValue t1) {
            Float f1 = pointValue.getX();
            Float f2 = t1.getX();
            return f1.compareTo(f2);
        }
    }


    public String getAddress(byte[] addressByte) {
        if (addressByte != null) {
            for (int i = 0; i < addressByte.length; i++) {
                if (addressByte[i] < 10) {
                    addressByte[i] += '0';
                } else {
                    addressByte[i] -= 10;
                    addressByte[i] += 'A';
                }
            }

            return new String(addressByte);
        } else {
            return "";
        }
    }

}
