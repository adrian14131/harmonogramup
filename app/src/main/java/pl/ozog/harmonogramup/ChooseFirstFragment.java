package pl.ozog.harmonogramup;


import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import pl.ozog.harmonogramup.adapters.MapAdapter;


public class ChooseFirstFragment extends FirstSettingsFragment {

//    private final String key = "faculity";
    private final String mainPage = "https://harmonogram.up.krakow.pl";
    private TextView textView, error;
    private ListView listView;
    private String action = "get_form", data = "faculity";
    private MapAdapter adapter;
    private final boolean skip = false;
    LinkedHashMap<String, String> options;
    public ChooseFirstFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_choose_first, container, false);

        textView = view.findViewById(R.id.faculityFragmentTitle);

        error = view.findViewById(R.id.firstErrorInfo);
        error.setVisibility(View.GONE);

        listView = view.findViewById(R.id.faculityListView);

        generateList(true);
        //((FirstSettings)getActivity()).setButtons(skip);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map.Entry<String, String> item = (Map.Entry<String, String>) adapterView.getItemAtPosition(i);
                ((FirstSettings)getActivity()).addArgs("action",action);
                ((FirstSettings)getActivity()).addArgs(data,item.getKey());
                ((FirstSettings)getActivity()).nextFragment(textView.getText().toString()+": "+item.getValue()+"\n", new ArrayList<String>());
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    protected LinkedHashMap<String, String> getMapFromElement(Document doc){
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        Elements els = doc.getElementsByClass("sidebar-search")
                .select("div.form-group:has(#faculity_1)")
                .select("option");
        for(Element el: els){
            result.put(el.attr("value"), el.text());
        }

        return result;
    }

    @Override
    public void generateList(boolean isNext) {
        options = new LinkedHashMap<>();
        FirstSettings.DownloadPageTask downloadTask = new FirstSettings.DownloadPageTask();
        try {
            Document document = downloadTask.execute(mainPage).get();
            options = getMapFromElement(document);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        options.remove("null");
        adapter = new MapAdapter(options);
        listView.setAdapter(adapter);
        if(options.size()==0){
            error.setVisibility(View.VISIBLE);
            error.setText(getResources().getString(R.string.no_data));
            if(!((FirstSettings)getActivity()).isOnline()){
                error.setText(error.getText().toString()+"\n"+getResources().getString(R.string.no_internet_short));
            }
        }
    }

    @Override
    public void setInfo(String msg, ArrayList<String> prevInfos) {

    }

    @Override
    public void removeLastInfo() {

    }

    @Override
    public ArrayList<String> getInfos() {
        return null;
    }

    @Override
    public void setTitle(String text) {
        textView.setText(text);
    }

    @Override
    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public void setData(String data) {
        this.data = data;
    }
    @Override
    public String getData() {
        return this.data;
    }

    @Override
    public boolean canSkip() {
        return this.skip;
    }

    @Override
    public String getInfo() {
        return "";
    }
}

