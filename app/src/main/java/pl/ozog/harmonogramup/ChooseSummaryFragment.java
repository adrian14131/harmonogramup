package pl.ozog.harmonogramup;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import pl.ozog.harmonogramup.R;


public class ChooseSummaryFragment extends Fragment {

    public ChooseSummaryFragment() {
        // Required empty public constructor
    }
    TextView info;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_choose_summary, container, false);

        info = view.findViewById(R.id.summaryTextView);

        return view;
    }

    public void setSummary(ArrayList<String> infos){
        StringBuilder sb = new StringBuilder();
        for(String info: infos){
            sb.append(info);
        }
        info.setText(sb.toString());
    }


}
