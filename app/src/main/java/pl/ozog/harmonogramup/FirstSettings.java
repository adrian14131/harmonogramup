package pl.ozog.harmonogramup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class FirstSettings extends AppCompatActivity implements View.OnClickListener {

    ActionBar actionBar;
    protected static ChooseSettings cs;
    Button prevButton, skipButton;
    ArrayList<FirstSettingsFragment> fragments;
    ArrayList<String> fragmentTitles;
    ArrayList<String> actions;
    ArrayList<String> datas;
    FragmentTransaction ft;
    Boolean wasMain;
    String iRange, iDateInput;
    Integer iRangeDatePos;
    ChooseSummaryFragment csf;
    boolean isSummary = false;
    int actualFragment = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_settings);

        wasMain = getIntent().getBooleanExtra("isMain", false);
        iRange = getIntent().getStringExtra("range");
        iDateInput = getIntent().getStringExtra("dateInput");
        iRangeDatePos = getIntent().getIntExtra("dateRange",0);

        actionBar = getSupportActionBar();
        actionBar.hide();
        cs = new ChooseSettings();

        prevButton = findViewById(R.id.previousButton);
        prevButton.setOnClickListener(this);
        prevButton.setEnabled(false);
        prevButton.setTextColor(Color.argb(50,255,255,255));

        skipButton = findViewById(R.id.skipButton);
        skipButton.setOnClickListener(this);
        skipButton.setEnabled(false);
        skipButton.setTextColor(Color.argb(50, 255,255, 255));



        ft = getSupportFragmentManager().beginTransaction();

        fragmentTitles = new ArrayList<>();
        fragmentTitles.add("Jednostka");
        fragmentTitles.add("Forma");
        fragmentTitles.add("Stopień");
        fragmentTitles.add("Kierunek");
        fragmentTitles.add("Specjalność");
        fragmentTitles.add("Specjalizacja");
        fragmentTitles.add("Rok studiów");

        actions = new ArrayList<>();
        actions.add("get_form");
        actions.add("get_degree");
        actions.add("get_direction");
        actions.add("get_specialty");
        actions.add("get_specialization");
        actions.add("get_year_1");
        actions.add("");

        datas = new ArrayList<>();
        datas.add("faculity");
        datas.add("form");
        datas.add("degree");
        datas.add("direction");
        datas.add("specialty");
        datas.add("specialization");
        datas.add("year");

        fragments = new ArrayList<>();
        fragments.add((FirstSettingsFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentFirst));
        fragments.add((FirstSettingsFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentRequired));
        fragments.add((FirstSettingsFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentRequired));
        fragments.add((FirstSettingsFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentRequired));
        fragments.add((FirstSettingsFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentOptional));
        fragments.add((FirstSettingsFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentOptional));
        fragments.add((FirstSettingsFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentOptional));

        csf = (ChooseSummaryFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentSummary);

        for(int i=0; i<fragments.size(); i++){
            if(i!=actualFragment){
                ft.hide(fragments.get(i));
            }
        }
        ft.hide(csf);
        ft.commit();

//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.hide(fragments.get(0));
//        ft.commit();

    }

    protected void addArgs(String K, String V){
        if(cs.isArg(K)){
            cs.changeArg(K,V);
        }
        else{
            cs.addArg(K,V);
        }
    }

    protected boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if(netInfo!=null){
            return netInfo.isConnected() && netInfo.isAvailable();
        }
        return false;
    }

    protected void nextFragment(String msg, ArrayList<String> prevInfos){

        if(actualFragment+1<fragments.size()){
            ft = getSupportFragmentManager().beginTransaction();
            ft.hide(fragments.get(actualFragment));
            actualFragment++;
            ft.show(fragments.get(actualFragment));
            ft.commit();
            fragments.get(actualFragment).setInfo(msg, prevInfos);
            fragments.get(actualFragment).setTitle(fragmentTitles.get(actualFragment));
            fragments.get(actualFragment).setAction(actions.get(actualFragment));
            fragments.get(actualFragment).setData(datas.get(actualFragment));
            fragments.get(actualFragment).generateList(true);
            setButtons(fragments.get(actualFragment).canSkip());


        }
        else if(actualFragment+1==fragments.size()){

            ft = getSupportFragmentManager().beginTransaction();
            ft.hide(fragments.get(actualFragment));
            ft.show(csf);
            ft.commit();
            isSummary = true;
            ArrayList<String> tmp = new ArrayList<>();
            tmp.addAll(fragments.get(actualFragment).getInfos());
            tmp.add(msg);
            csf.setSummary(tmp);
            setButtons(true);
            skipButton.setText("OK");
        }
    }
    protected void previousFragment(){
        if(isSummary){
            ft = getSupportFragmentManager().beginTransaction();
            ft.hide(csf);
            ft.show(fragments.get(actualFragment));
            ft.commit();
            isSummary = false;
            skipButton.setText("Pomiń");
            setButtons(fragments.get(actualFragment).canSkip());
        }
        else
        {
            if(actualFragment>0){
                cs.removeLastArg(datas.get(actualFragment));
                ft = getSupportFragmentManager().beginTransaction();
                ft.hide(fragments.get(actualFragment));
                actualFragment=actualFragment-1;

                ft.show(fragments.get(actualFragment));
                ft.commit();
                if(actualFragment>0){
                    cs.removeLastArg(datas.get(actualFragment));
                    cs.addArg("action", actions.get(actualFragment-1));

                }
                fragments.get(actualFragment).setTitle(fragmentTitles.get(actualFragment));
                fragments.get(actualFragment).setAction(actions.get(actualFragment));
                fragments.get(actualFragment).setData(datas.get(actualFragment));
                fragments.get(actualFragment).generateList(false);
                fragments.get(actualFragment).removeLastInfo();



                setButtons(fragments.get(actualFragment).canSkip());
            }
        }

    }
    protected void setInfo(String msg, Fragment fragment){
//        switch (fragment.getId()){
//
//        }
    }
    private void saveSettings(){
        SharedPreferences sharedPreferences = getSharedPreferences("schedule",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        cs.addArg("action", "search");
        LinkedHashMap<String, String> dataMaps = cs.getArgs();
        Set<String> keySet = dataMaps.keySet();
        editor.putStringSet("datas", keySet);
        for(String key:keySet){
            editor.putString(key, dataMaps.get(key));
        }

        editor.commit();




    }
    protected void setButtons(boolean skip){
        if(actualFragment>0){
            prevButton.setEnabled(true);
            prevButton.setTextColor(Color.argb(255,255,255,255));
        }
        else{
            prevButton.setEnabled(false);
            prevButton.setTextColor(Color.argb(50,255,255,255));
        }
        skipButton.setEnabled(skip);
        if(skip){

            skipButton.setTextColor(Color.argb(255,255,255,255));
        }
        else{

            skipButton.setTextColor(Color.argb(50,255,255,255));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.previousButton:
                previousFragment();
                break;
            case R.id.skipButton:
                if(isSummary){
                    saveSettings();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);

                    finish();
                }
                else
                {
                    cs.addArg("action", actions.get(actualFragment));
                    cs.addArg(datas.get(actualFragment), "null");
                    nextFragment(fragmentTitles.get(actualFragment)+": brak\n", fragments.get(actualFragment).getInfos());
                }

                break;
        }
    }

    public static class DownloadPageTask extends AsyncTask<String, Void, Document> {
        @Override
        protected  Document doInBackground(String... strings) {
            Document doc = null;
            if(strings.length>0){
                try {
                    doc = Jsoup.connect(strings[0]).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(doc==null){
                doc = Jsoup.parse("<h1></h1>");
            }
            return doc;
        }
    }



    @Override
    public void onBackPressed() {
        if(wasMain){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra("goBackFromFirstSettings", true);
            intent.putExtra("range", iRange);
            intent.putExtra("dateInput", iDateInput);
            intent.putExtra("dateRange",iRangeDatePos);
            startActivity(intent);
        }
        super.onBackPressed();
    }


    public static class ExecuteTask extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String... strings) {
            Document doc = null;
            if(strings.length > 0){
                try {
                    Connection.Response response = Jsoup.connect(strings[0])
                            .method(Connection.Method.POST)
                            .data(cs.getArgs())
                            .execute();
                    doc = Jsoup.parse(response.body());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(doc==null){
                doc = Jsoup.parse("<h1></h1>");
            }

            return doc;
        }
    }
}
