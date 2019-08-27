package com.microtechmd.pda_app.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.style.FontStyle;
import com.bin.david.form.data.table.PageTableData;
import com.bin.david.form.data.table.TableData;
import com.bin.david.form.utils.DensityUtils;
import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.pagination.Pagination;
import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.entity.ParameterMonitor;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.ApplicationPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.activity.LoginActivity;
import com.microtechmd.pda_app.activity.MainActivity;
import com.microtechmd.pda_app.activity.SettingActivity;
import com.microtechmd.pda_app.activity.WelcomeActivity;
import com.microtechmd.pda_app.constant.StringConstant;
import com.microtechmd.pda_app.entity.DbRawHistory;
import com.microtechmd.pda_app.entity.DbRawHistoryDisplay;
import com.microtechmd.pda_app.entity.DbRawHistory_;
import com.microtechmd.pda_app.util.UploadRawDataUtil;
import com.microtechmd.pda_app.widget.MainActionBar;
import com.microtechmd.pda_app.widget.tableview.TableViewAdapter;
import com.microtechmd.pda_app.widget.tableview.model.Cell;
import com.microtechmd.pda_app.widget.tableview.model.ColumnHeader;
import com.microtechmd.pda_app.widget.tableview.model.RowHeader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import io.objectbox.query.QueryBuilder;

import static com.microtechmd.pda_app.ActivityPDA.IMPENDANCE;
import static com.microtechmd.pda_app.ActivityPDA.SETTING_RF_ADDRESS;
import static com.microtechmd.pda_app.util.RfAddressUtil.getAddress;


public class FragmentShop extends FragmentBase implements EntityMessage.Listener {
    public int COLUMN_SIZE;
    public int ROW_SIZE;
    private View mRootView;

    private static final int UPLOADCOUNT = 20;
    private List<RowHeader> mRowHeaderList;
    private List<ColumnHeader> mColumnHeaderList;
    private List<List<Cell>> mCellList;
    private AbstractTableAdapter mTableViewAdapter;

    private ImageButton previousButton, nextButton;
    private TextView pageNumberField;

    //    private SwipeRefreshLayout refresh;
    private TableView mTableView;
    //    private ActionButton actionButton;
    private Button synchronize_raw, upload, refresh;
    private Pagination mPagination;
    private String[] items_raw_data;
    private ApplicationPDA applicationPDA;
    private CountDownTimer timer;


    private SmartTable<DbRawHistoryDisplay> table;
    private List<Column> columns;
    private PageTableData<DbRawHistoryDisplay> tableData;
    private boolean waite = true;
    private List<DbRawHistoryDisplay> displayArrayList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_shop, container, false);
        applicationPDA = (ApplicationPDA) getActivity().getApplication();
        items_raw_data = getActivity().getResources().getStringArray(R.array.items_raw_data);
        COLUMN_SIZE = items_raw_data.length;
        mRowHeaderList = new ArrayList<>();
        mColumnHeaderList = new ArrayList<>();
        mCellList = new ArrayList<>();

        FontStyle.setDefaultTextSize(DensityUtils.sp2px(getActivity(), 15));
        FontStyle.setDefaultTextColor(getResources().getColor(R.color.text_white));
        table = (SmartTable<DbRawHistoryDisplay>) mRootView.findViewById(R.id.table);
        table.getConfig().setShowTableTitle(false);
        table.getConfig().setShowXSequence(false);
        table.getConfig().setShowYSequence(false);
        table.getConfig().setFixedCountRow(false);
        table.setZoom(true, 1, 0.2f);


        initActionBar();
//        Spinner itemsPerPage = mRootView.findViewById(R.id.items_per_page_spinner);
//        previousButton = mRootView.findViewById(R.id.previous_button);
//        nextButton = mRootView.findViewById(R.id.next_button);
//        pageNumberField = mRootView.findViewById(R.id.page_number_text);
////        refresh = mRootView.findViewById(R.id.refresh);
//
//        RelativeLayout fragment_container = mRootView.findViewById(R.id.fragment_container);
//        // Create Table view
//        mTableView = createTableView();
//        fragment_container.addView(mTableView);
//
//        mPagination = new Pagination(mTableView);
//        mPagination.setOnTableViewPageTurnedListener(onTableViewPageTurnedListener);
//
//        itemsPerPage.setOnItemSelectedListener(onItemsPerPageSelectedListener);
//
//        previousButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mPagination.previousPage();
//            }
//        });
//        nextButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mPagination.nextPage();
//            }
//        });

        displayArrayList = new ArrayList<>();
//        columns = getColumns();
//        tableData = new PageTableData<>("测试", displayArrayList, columns);
        table.setData(displayArrayList);
        loadData();

        synchronize_raw = mRootView.findViewById(R.id.synchronize_raw);
        upload = mRootView.findViewById(R.id.upload);
        refresh = mRootView.findViewById(R.id.refresh);

        synchronize_raw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mRFAddress = getAddress(SPUtils.getbytes(getActivity(), SETTING_RF_ADDRESS));
                if (TextUtils.isEmpty(mRFAddress)) {
                    Toast.makeText(getActivity(), R.string.null_pair, Toast.LENGTH_SHORT).show();
                    return;
                }
                getRawdata();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = (String) SPUtils.get(getActivity(), StringConstant.USERNAME, "");
                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(applicationPDA, R.string.unlogin, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getActivity(), WelcomeActivity.class));
                    return;
                }
                uploadRawData();
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });
        return mRootView;
    }

    private List<String> generateRowData() {
        List<String> rowDataList = new ArrayList<>();
        Collections.addAll(rowDataList, items_raw_data);
        return rowDataList;
    }

    private void getRawdata() {
        if (getActivity() != null) {
            ((ActivityPDA) getActivity()).handleMessage(
                    new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_VIEW,
                            ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                            ParameterGlobal.PORT_MONITOR,
                            ParameterGlobal.PORT_MONITOR,
                            EntityMessage.OPERATION_SET,
                            ParameterComm.SYNCHRONIZE_RAW_DATA,
                            null));
        }
    }

    private void uploadRawData() {
        QueryBuilder<DbRawHistory> builder = applicationPDA.getBoxStore().boxFor(DbRawHistory.class).query();
        builder.isNull(DbRawHistory_.record_no);
        final long count = builder.build().count();
        if (count == 0) {
            Toast.makeText(applicationPDA, "已上传完毕", Toast.LENGTH_SHORT).show();
            return;
        }
        if (timer == null) {
            timer = new CountDownTimer(((count / UPLOADCOUNT) + 1) * 2 * 1000, 2 * 1000) {
                @Override
                public void onTick(long l) {
                    QueryBuilder<DbRawHistory> builder_1 = applicationPDA.getBoxStore().boxFor(DbRawHistory.class).query();
                    builder_1.isNull(DbRawHistory_.record_no)
                            .order(DbRawHistory_.id);
                    List<DbRawHistory> list = builder_1.build().find(0, UPLOADCOUNT);
                    if (list.size() == 0) {
                        return;
                    }
                    UploadRawDataUtil.addUserData(getActivity(), list);

                    long countL = applicationPDA.getBoxStore().boxFor(DbRawHistory.class).query().isNull(DbRawHistory_.record_no).build().count();
                    upload.setText((count - countL) + " / " + count);
                }

                @Override
                public void onFinish() {
                    QueryBuilder<DbRawHistory> builder_1 = applicationPDA.getBoxStore().boxFor(DbRawHistory.class).query();
                    builder_1.isNull(DbRawHistory_.record_no)
                            .order(DbRawHistory_.id);
                    List<DbRawHistory> list = builder_1.build().find(0, UPLOADCOUNT);
                    if (list.size() == 0) {
                        return;
                    }
                    UploadRawDataUtil.addUserData(getActivity(), list);
                    upload.setText(R.string.upload);
                }
            }.start();
        } else {
            timer.cancel();
            timer = null;
            upload.setText(R.string.upload);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void initActionBar() {
        MainActionBar mainActionBar = mRootView.findViewById(R.id.toolbar);
        mainActionBar.setTitleText(R.string.test);
        mainActionBar.setRightButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });
    }

    private TableView createTableView() {
        TableView tableView = new TableView(getContext());
//         Set adapter
        mTableViewAdapter = new TableViewAdapter(getContext());
        tableView.setAdapter(mTableViewAdapter);

        // Set layout params
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams
                .MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        tableView.setLayoutParams(tlp);
        return tableView;
    }

    private void loadData() {
        try {
            if (waite) {
                waite = false;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        waite = true;
                    }
                }, 2000);
                List<DbRawHistory> list = applicationPDA.getBoxStore().boxFor(DbRawHistory.class).getAll();
                if (list.size() > 0) {
                    displayArrayList.clear();
                    ROW_SIZE = list.size();
                    TextView tablePaginationDetails = mRootView.findViewById(R.id.table_details);
                    tablePaginationDetails.setText("总数：" + ROW_SIZE);
                    for (DbRawHistory dbRawHistory : list) {
                        DbRawHistoryDisplay dbRawHistoryDisplay = new DbRawHistoryDisplay();
                        dbRawHistoryDisplay.setDevice_id(dbRawHistory.getDevice_id());
                        dbRawHistoryDisplay.setSensorIndex(dbRawHistory.getSensorIndex());
                        String t = String.valueOf(dbRawHistory.getDate_time());
                        String time = t.substring(4, 6) + "-" + t.substring(6, 8) + " " +
                                t.substring(8, 10) + ":" + t.substring(10, 12) + ":" + t.substring(12, 14);
                        dbRawHistoryDisplay.setDate_time(time);
                        dbRawHistoryDisplay.setEvent_index(dbRawHistory.getEvent_index());
                        dbRawHistoryDisplay.setEvent_type(dbRawHistory.getEvent_type());
                        dbRawHistoryDisplay.setSplit(dbRawHistory.getSplit());
                        if (dbRawHistory.getSplit() == 0) {
                            dbRawHistoryDisplay.setValue(dbRawHistory.getValue() / 10f);
                        } else {
                            dbRawHistoryDisplay.setValue(dbRawHistory.getValue() / 100f);
                        }
                        dbRawHistoryDisplay.setP1(dbRawHistory.getP1() / 100f);
                        if (dbRawHistory.getEvent_type() == IMPENDANCE) {
                            dbRawHistoryDisplay.setP2(((int) ((short) dbRawHistory.getP2())) / 100f);
                            dbRawHistoryDisplay.setP3(((int) ((short) dbRawHistory.getP3())) / 100f);
                        } else {
                            dbRawHistoryDisplay.setP2(dbRawHistory.getP2() / 100f);
                            dbRawHistoryDisplay.setP3(dbRawHistory.getP3() / 100f);
                        }

                        dbRawHistoryDisplay.setP4(dbRawHistory.getP4() / 100f);
                        displayArrayList.add(dbRawHistoryDisplay);
                    }
//                    tableData.setT(displayArrayList);
                    table.setData(displayArrayList);
//                    table.notifyDataChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        List<RowHeader> rowHeaders = getRowHeaderList();
//        List<ColumnHeader> columnHeaders = getColumnHeaderList();
//        List<List<Cell>> cellList = getCellListForSortingTest(list);
//
//
//        mRowHeaderList.addAll(rowHeaders);
//        mCellList = cellList;
//        // Load all data
//        mColumnHeaderList.addAll(columnHeaders);
//        mTableViewAdapter.setAllItems(mColumnHeaderList, mRowHeaderList, mCellList);
//        refresh.setRefreshing(false);
    }

    @NonNull
    private List<Column> getColumns() {
        List<Column> columns = new ArrayList<>();
        for (int i = 0; i < generateRowData().size(); i++) {
            switch (i) {
                case 0:
                    columns.add(new Column<>(generateRowData().get(i), "device_id"));
                    break;
                case 1:
                    columns.add(new Column<>(generateRowData().get(i), "sensorIndex"));
                    break;
                case 2:
                    columns.add(new Column<>(generateRowData().get(i), "date_time"));
                    break;
                case 3:
                    columns.add(new Column<>(generateRowData().get(i), "event_index"));
                    break;
                case 4:
                    columns.add(new Column<>(generateRowData().get(i), "event_type"));
                    break;
                case 5:
                    columns.add(new Column<>(generateRowData().get(i), "split"));
                    break;
                case 6:
                    columns.add(new Column<>(generateRowData().get(i), "value"));
                    break;
                case 7:
                    columns.add(new Column<>(generateRowData().get(i), "p1"));
                    break;
                case 8:
                    columns.add(new Column<>(generateRowData().get(i), "p2"));
                    break;
                case 9:
                    columns.add(new Column<>(generateRowData().get(i), "p3"));
                    break;
                case 10:
                    columns.add(new Column<>(generateRowData().get(i), "p4"));
                    break;
                default:
                    break;
            }
        }
        return columns;
    }

    private List<RowHeader> getRowHeaderList() {
        List<RowHeader> list = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            String rh = "" + i;
            RowHeader header = new RowHeader(String.valueOf(i), rh);
            list.add(header);
        }
        return list;
    }

    private List<ColumnHeader> getColumnHeaderList() {
        List<ColumnHeader> list = new ArrayList<>();
        for (int i = 0; i < items_raw_data.length; i++) {
            ColumnHeader header = new ColumnHeader(String.valueOf(i), items_raw_data[i]);
            list.add(header);
        }
        return list;
    }

    private List<List<Cell>> getCellListForSortingTest(List<DbRawHistory> rawHistoryList) {
        List<List<Cell>> list = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            List<Cell> cellList = new ArrayList<>();
            DbRawHistory dbRawHistory = rawHistoryList.get(i);
//            if (dbRawHistory.getRecord_no() == null) {
//                mLog.Error(getClass(), "记录为： Null");
//            } else if (dbRawHistory.getRecord_no().equals("")) {
//                mLog.Error(getClass(), "记录为： 空");
//            } else {
//                mLog.Error(getClass(), dbRawHistory.getRecord_no());
//            }
            for (int j = 0; j < COLUMN_SIZE; j++) {
                Object text = "";
                switch (j) {
                    case 0:
                        text = dbRawHistory.getDevice_id();
                        break;
                    case 1:
                        text = dbRawHistory.getSensorIndex();
                        break;
                    case 2:
                        text = dbRawHistory.getDate_time();
                        break;
                    case 3:
                        text = dbRawHistory.getEvent_index();
                        break;
                    case 4:
                        text = dbRawHistory.getEvent_type();
                        break;
                    case 5:
                        text = dbRawHistory.getValue();
                        break;
                    case 6:
                        text = dbRawHistory.getP1();
                        break;
                    case 7:
                        text = dbRawHistory.getP2();
                        break;
                    case 8:
                        text = dbRawHistory.getP3();
                        break;
                    case 9:
                        text = dbRawHistory.getP4();
                        break;
//                    case 10:
//                        text = dbRawHistory.getP5();
//                        break;
//                    case 11:
//                        text = dbRawHistory.getP6();
//                        break;
//                    case 12:
//                        text = dbRawHistory.getP7();
//                        break;
//                    case 13:
//                        text = dbRawHistory.getP8();
//                        break;
//                    case 14:
//                        text = dbRawHistory.getP9();
//                        break;
                    default:
                        break;
                }
                // Create dummy id.
                String id = j + "-" + i;
                Cell cell = new Cell(id, text);
                cellList.add(cell);
            }
            list.add(cellList);
        }

        return list;
    }

    private Pagination.OnTableViewPageTurnedListener onTableViewPageTurnedListener =
            new Pagination.OnTableViewPageTurnedListener() {
                @Override
                public void onPageTurned(int numItems, int itemsStart, int itemsEnd) {
                    int currentPage = mPagination.getCurrentPage();
                    int pageCount = mPagination.getPageCount();
                    previousButton.setVisibility(View.VISIBLE);
                    nextButton.setVisibility(View.VISIBLE);

                    if (currentPage == 1 && pageCount == 1) {
                        previousButton.setVisibility(View.INVISIBLE);
                        nextButton.setVisibility(View.INVISIBLE);
                    }
                    if (currentPage == 1) {
                        previousButton.setVisibility(View.INVISIBLE);
                    }

                    if (currentPage == pageCount) {
                        nextButton.setVisibility(View.INVISIBLE);
                    }

                    pageNumberField.setText(currentPage + " / " + pageCount);

                }
            };
    private AdapterView.OnItemSelectedListener onItemsPerPageSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int itemsPerPage;
            switch (parent.getItemAtPosition(position).toString()) {
                case "All":
                    itemsPerPage = 0;
                    break;
                default:
                    itemsPerPage = Integer.valueOf(parent.getItemAtPosition(position).toString());
            }
            mPagination.setItemsPerPage(itemsPerPage);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((ApplicationPDA) getActivity().getApplication())
                .registerMessageListener(ParameterGlobal.PORT_MONITOR, this);
    }

    @Override
    public void onDestroyView() {
        ((ApplicationPDA) getActivity().getApplication())
                .unregisterMessageListener(ParameterGlobal.PORT_MONITOR, this);
        super.onDestroyView();
    }

    @Override
    public void onReceive(EntityMessage message) {
        switch (message.getOperation()) {
            case EntityMessage.OPERATION_SET:
                handlerSet(message);
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

    private void handlerSet(EntityMessage message) {
        if (message.getParameter() == ParameterMonitor.GLUCOSE_DISPLAY) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            if (hour == 0 && min == 0) {
                uploadRawData();
            }
        }
    }

    private void handleNotification(final EntityMessage message) {
        if ((message.getSourcePort() == ParameterGlobal.PORT_MONITOR) &&
                (message.getParameter() == ParameterMonitor.RAW_SAVED)) {
            loadData();
        }
    }

}
