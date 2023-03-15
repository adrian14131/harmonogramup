package pl.ozog.harmonogramup;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import pl.ozog.harmonogramup.adapters.MapAdapter;


public class ChooseOptionalFragment extends FirstSettingsFragment {

    private String url = "https://harmonogram.up.krakow.pl/inc/functions/a_select.php";
    private TextView info, title;
    private ListView listView;
    private boolean skip = true;

    private ArrayList<String> infos;
    private String action, data;
    private MapAdapter adapter;

    private LinkedHashMap<String, String> options;

    public ChooseOptionalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_choose_optional, container, false);

        info = view.findViewById(R.id.optFragmentInfo);
        title = view.findViewById(R.id.optFragmentTitle);

        listView = view.findViewById(R.id.optListView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map.Entry<String, String> item = (Map.Entry)adapterView.getItemAtPosition(i);

                ((FirstSettings)getActivity()).addArgs("action",action);
                ((FirstSettings)getActivity()).addArgs(data, item.getKey());
                ((FirstSettings)getActivity()).nextFragment(title.getText().toString()+": "+item.getValue()+"\n", infos);
            }
        });


        return view;
    }


    @Override
    public void generateList(boolean isNext) {
        options = new LinkedHashMap<>();
        FirstSettings.ExecuteTask executeTask = new FirstSettings.ExecuteTask();
        try {
            Document document = executeTask.execute(url).get();
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
            if(isNext){
                ((FirstSettings)getActivity()).addArgs("action",action);
                ((FirstSettings)getActivity()).addArgs(data, "null");
                ((FirstSettings)getActivity()).nextFragment(title.getText().toString()+": "+getResources().getString(R.string.no_pl_brak)+"\n", infos);

            }
            else{
                ((FirstSettings)getActivity()).previousFragment();
            }

        }
    }

    @Override
    public void setInfo(String msg, ArrayList<String> prevInfos) {
        this.infos = prevInfos;
        if(!msg.isEmpty()){
            this.infos.add(msg);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(String info:infos){
            stringBuilder.append(info);
        }
        this.info.setText(stringBuilder.toString());
    }

    @Override
    public void removeLastInfo() {
        if(infos.size()>0){
            infos.remove(infos.size()-1);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(String info:infos){
            stringBuilder.append(info);
        }
        this.info.setText(stringBuilder.toString());
    }

    @Override
    public ArrayList<String> getInfos() {
        return this.infos;
    }

    @Override
    public void setTitle(String text) {
        title.setText(text);
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
        return info.getText().toString();
    }
}
