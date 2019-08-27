/**
 *
 */
package com.microtechmd.pda_app.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.entity.DbHistory;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import static com.microtechmd.pda_app.ActivityPDA.BLOOD_GLUCOSE;
import static com.microtechmd.pda_app.ActivityPDA.CALIBRATION;
import static com.microtechmd.pda_app.ActivityPDA.HYPER;
import static com.microtechmd.pda_app.ActivityPDA.HYPO;
import static com.microtechmd.pda_app.ActivityPDA.SENSOR_ERROR;
import static com.microtechmd.pda_app.ActivityPDA.SENSOR_EXPIRATION;

public class TimelineAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer {
    private List<DbHistory> mDatas;

    public void setmDatas(List<DbHistory> mDatas) {
        this.mDatas = mDatas;
    }

    private int[] mSectionIndices;
    private String[] mSectionLetters;
    private final Context mContext;
    private LayoutInflater mInflater;

    public TimelineAdapter(Context context, List<DbHistory> mDatas) {
        this.mDatas = mDatas;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        if (mDatas.size() > 0) {
            mSectionIndices = getSectionIndices();
            mSectionLetters = getSectionLetters();
        }
    }

    private int[] getSectionIndices() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//        String date = format.format(new Date(mDatas.get(position).getDate_time()));
        ArrayList<Integer> sectionIndices = new ArrayList<>();
//        String t = String.valueOf(mDatas.get(0).getDate_time());
//        String date = t.substring(0, 8);
        String date = format.format(new Date(mDatas.get(0).getDate_time()));
        sectionIndices.add(0);
        for (int i = 1; i < mDatas.size(); i++) {
            if (!format.format(new Date(mDatas.get(i).getDate_time())).equals(date)) {
                date = format.format(new Date(mDatas.get(i).getDate_time()));
                sectionIndices.add(i);
            }
        }
        int[] sections = new int[sectionIndices.size()];
        for (int i = 0; i < sectionIndices.size(); i++) {
            sections[i] = sectionIndices.get(i);
        }
        return sections;
    }

    private String[] getSectionLetters() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String[] letters = new String[mSectionIndices.length];
        for (int i = 0; i < mSectionIndices.length; i++) {
            letters[i] = format.format(new Date(mDatas.get(i).getDate_time()));
        }
        return letters;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.time_line_item, parent, false);
            holder.time = (TextView) convertView.findViewById(R.id.time_tv);
            holder.content = (TextView) convertView.findViewById(R.id.content_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

//        String t = String.valueOf(mDatas.get(position).getDate_time());
////        String time = t.substring(0, 4) + "-" + t.substring(4, 6) + "-" + t.substring(6, 8) + " " +
////                t.substring(8, 10) + ":" + t.substring(10, 12) + ":" + t.substring(12, 14);
//        String time = t.substring(8, 10) + ":" + t.substring(10, 12) + ":" + t.substring(12, 14);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String time = format.format(new Date(mDatas.get(position).getDate_time()));
        holder.time.setText(time);
        String comment = getEventContent(mDatas.get(position).getEvent_type());
        holder.content.setText(comment);
        return convertView;
    }

    private String getEventContent(int event) {
        String content = "";

        switch (event) {
            case SENSOR_ERROR:
                content = mContext.getString(R.string.alarm_sensor_error);
                break;

            case SENSOR_EXPIRATION:
                content = mContext.getString(R.string.alarm_expiration);
                break;

            case HYPO:
                content = mContext.getString(R.string.alarm_hypo);
                break;

            case HYPER:
                content = mContext.getString(R.string.alarm_hyper);
                break;

            case BLOOD_GLUCOSE:
                content = mContext.getString(R.string.blood_glucose);
                break;

            case CALIBRATION:
                content = mContext.getString(R.string.calibrate_blood);
                break;
            default:
                break;
        }
        return content;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.time_line_header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.day_title);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        // set header text as first char in name
//        String t = String.valueOf(mDatas.get(position).getDate_time());
//        String date = t.substring(0, 4) + "-" + t.substring(4, 6) + "-" + t.substring(6, 8);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = format.format(new Date(mDatas.get(position).getDate_time()));
        holder.text.setText(date);
        return convertView;
    }

    /**
     * Remember that these have to be static, postion=1 should always return
     * the same Id that is.
     */
    @Override
    public long getHeaderId(int position) {
        // return the first character of the country as ID because this is what
        // headers are based upon
//        return mCountries[position].subSequence(0, 1).charAt(0);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String date = format.format(new Date(mDatas.get(position).getDate_time()));
        return Long.valueOf(date);
    }

    @Override
    public int getPositionForSection(int section) {
        if (mSectionIndices.length == 0) {
            return 0;
        }

        if (section >= mSectionIndices.length) {
            section = mSectionIndices.length - 1;
        } else if (section < 0) {
            section = 0;
        }
        return mSectionIndices[section];
    }

    @Override
    public int getSectionForPosition(int position) {
        for (int i = 0; i < mSectionIndices.length; i++) {
            if (position < mSectionIndices[i]) {
                return i - 1;
            }
        }
        return mSectionIndices.length - 1;
    }

    @Override
    public Object[] getSections() {
        return mSectionLetters;
    }

    public void clear() {
        mDatas.clear();
        mSectionIndices = new int[0];
        mSectionLetters = new String[0];
        notifyDataSetChanged();
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        TextView time;
        TextView content;
    }


}
