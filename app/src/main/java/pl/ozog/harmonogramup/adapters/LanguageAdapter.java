package pl.ozog.harmonogramup.adapters;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class LanguageAdapter extends BaseAdapter {

    private final ArrayList<LinkedHashMap.Entry<String, String>> mData;
    private static final String TAG = "LanguageAdapter";
    public LanguageAdapter(LinkedHashMap<String, String> map){

        this.mData = new ArrayList<>();
        this.mData.addAll(map.entrySet());
    }
    @Override
    public int getCount() {
        return this.mData.size();
    }

    @Override
    public LinkedHashMap.Entry<String, String> getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;

        if(convertView == null){
            result = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);

        }
        else{
            result = convertView;
        }
        LinkedHashMap.Entry<String, String> item = getItem(position);
        TextView itemTextView = result.findViewById(android.R.id.text1);
        itemTextView.setTextSize(20);
        itemTextView.setText(item.getValue());
//        ((TextView) result.findViewById(android.R.id.text1)).setText(item.getValue());
        return result;
    }
}
