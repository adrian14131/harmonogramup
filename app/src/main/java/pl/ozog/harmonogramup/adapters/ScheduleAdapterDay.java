package pl.ozog.harmonogramup.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import pl.ozog.harmonogramup.items.CourseItem;
import pl.ozog.harmonogramup.R;

public class ScheduleAdapterDay extends BaseAdapter {
    private final ArrayList<CourseItem> mData;
    private static final String TAG = "ScheduleAdapterDay";
    private final boolean showData;
    private final Context context;
    public ScheduleAdapterDay(ArrayList<CourseItem> courseItems, boolean showData, Context context) {

        this.mData = courseItems;
        this.showData = showData;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public CourseItem getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        String timeFormat = "yyyy-MM-dd HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, context.getResources().getConfiguration().getLocales().get(0));
        Date currentTime = Calendar.getInstance().getTime();
        final View result = view==null? LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.schedule_row_day, viewGroup, false): view;

//        if (view == null){
//            result = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.schedule_row_day, viewGroup, false);
//        }
//        else
//        {
//            result = view;
//        }
        CourseItem item = mData.get(i);
        String time;
        ((TextView) result.findViewById(R.id.sdName)).setText(item.getName());
        Date d = null;
        try {
            d = sdf.parse(item.getDate()+" "+item.getToTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(d != null){
            if(currentTime.getTime()>d.getTime()){
                result.setBackgroundColor(result.getResources().getColor(R.color.gray, context.getTheme()));
            }
            else{
                result.setBackgroundColor(result.getResources().getColor(android.R.color.transparent, context.getTheme()));
            }
        }
        time = item.getFromTime()+"-"+item.getToTime();

        if(showData){
            sdf.applyPattern("yyyy-MM-dd EEE");
            time += "\n"+sdf.format(d);

        }
//        else{
//            time = item.getFromTime()+"-"+item.getToTime();
//        }
        ((TextView) result.findViewById(R.id.sdTime)).setText(time);
        ((TextView) result.findViewById(R.id.sdClassRoom)).setText(item.getClassRoom());
        ((TextView) result.findViewById(R.id.sdGroup)).setText(String.format("%s %s", context.getResources().getString(R.string.group), item.getGroup()));
        ((TextView) result.findViewById(R.id.sdTeacher)).setText(item.getTeacher());
        return result;
    }
}
