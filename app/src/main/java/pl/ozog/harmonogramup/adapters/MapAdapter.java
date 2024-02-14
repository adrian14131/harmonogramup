package pl.ozog.harmonogramup.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import pl.ozog.harmonogramup.R;

public class MapAdapter extends BaseAdapter {
    private final ArrayList<Map.Entry<String, String>> mData;
    private static final String TAG = "MapAdapter";
    public MapAdapter(Map<String, String> map) {
        mData = new ArrayList<>();
        mData.addAll(map.entrySet());
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, String> getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final View result = view==null? LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listview_item_white, viewGroup, false): view;
//        if (view == null){
//            result = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listview_item_white, viewGroup, false);
//        }
//        else
//        {
//            result = view;
//        }
        Map.Entry<String, String> item = getItem(i);

        ((TextView) result.findViewById(R.id.tv)).setText(item.getValue());

        return result;
    }
}
