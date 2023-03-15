package pl.ozog.harmonogramup.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.util.ArrayList;
import java.util.Map;

import pl.ozog.harmonogramup.R;

public class MapSpinnerAdapter extends BaseAdapter {
    private final ArrayList<Map.Entry<String, String>> mData;
    private static final String TAG = "MapSpinnerAdapter";
    public MapSpinnerAdapter(Map<String, String> map) {

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
        final View result;
        if (view == null){
            result = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spinner_row, viewGroup, false);
        }
        else
        {
            result = view;
        }
        Map.Entry<String, String> item = getItem(i);

        ((CheckedTextView) result.findViewById(R.id.spinnerRowText1)).setText(item.getValue());

        return result;
    }
}
