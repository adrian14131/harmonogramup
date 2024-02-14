package pl.ozog.harmonogramup.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.util.ArrayList;

import pl.ozog.harmonogramup.R;
import pl.ozog.harmonogramup.items.RangeItem;

public class RangeSpinnerAdapter extends BaseAdapter {
    private final ArrayList<RangeItem> mData;
    private static final String TAG = "RangeSpinnerAdapter";

    public RangeSpinnerAdapter(ArrayList<RangeItem> items) {
        mData = items;
    }


    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public RangeItem getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final View result = view ==null? LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spinner_row_range, viewGroup, false): view;
//        if (view == null){
//            result = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spinner_row_range, viewGroup, false);
//        }
//        else
//        {
//            result = view;
//        }
        RangeItem item = getItem(i);
        CheckedTextView ctv = result.findViewById(R.id.spinnerRowText2);
        ctv.setText(item.getText());

        return result;
    }
}
