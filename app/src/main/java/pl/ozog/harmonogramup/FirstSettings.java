package pl.ozog.harmonogramup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.theokanning.openai.service.OpenAiService;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;


import pl.ozog.harmonogramup.ai.ChoiceNode;
import pl.ozog.harmonogramup.downloaders.GptApiTask;

public class FirstSettings extends AppCompatActivity implements View.OnClickListener {

    ActionBar actionBar;
    protected static ChooseSettings cs;
    Button prevButton, skipButton, aiModeButton, normalModeButton, testModeButton;
    ConstraintLayout mainLayout, menuLayout, aiLayout;
    ArrayList<FirstSettingsFragment> fragments;
    ArrayList<String> fragmentTitles;
    ArrayList<String> actions;
    ArrayList<String> datas;
    FragmentTransaction ft;
    Boolean wasMain;
    String iRange, iDateInput;
    Integer iRangeDatePos;
    ChooseSummaryFragment csf;

    FirstSettingsAiFragment aiFragment;

    boolean isSummary = false;
    int actualFragment = 0;

    OpenAiService aiService;

//    AiChoiceProccess aiChoiceProccess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_settings);


//        aiService = new OpenAiService("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");
        mainLayout = findViewById(R.id.settingsMainLayout);
        menuLayout = findViewById(R.id.firstSettingMenuLayout);
        aiLayout = findViewById(R.id.firstSettingAiLayout);

        aiModeButton = findViewById(R.id.aiModeButton);
        aiModeButton.setOnClickListener(this);
        normalModeButton = findViewById(R.id.normalModeButton);
        normalModeButton.setOnClickListener(this);
        testModeButton = findViewById(R.id.testModeButton);
        testModeButton.setOnClickListener(this);

        wasMain = getIntent().getBooleanExtra("isMain", false);
        iRange = getIntent().getStringExtra("range");
        iDateInput = getIntent().getStringExtra("dateInput");
        iRangeDatePos = getIntent().getIntExtra("dateRange",0);
//        mainLayout.setVisibility(View.VISIBLE);
        actionBar = getSupportActionBar();
        if(actionBar != null)
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
        aiFragment = (FirstSettingsAiFragment) getSupportFragmentManager().findFragmentById(R.id.aiFragment);
//        aiChoiceProccess = new AiChoiceProccess("https://harmonogram.up.krakow.pl", "https://harmonogram.up.krakow.pl/inc/functions/a_select.php", actions, datas);

        for(int i=0; i<fragments.size(); i++){
            if(i!=actualFragment){
                ft.hide(fragments.get(i));
            }
        }

        if(csf != null)
            ft.hide(csf);
        ft.commit();

//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.hide(fragments.get(0));
//        ft.commit();

    }


    protected void addArg(String K, String V){
        cs.changeArg(K,V);
//        if(cs.isArg(K)){
//            cs.changeArg(K,V);
//        }
//        else{
//            cs.addArg(K,V);
//        }
    }

    protected void addArgs(LinkedHashMap<String, String> choices){
        for(Map.Entry<String, String> entry: choices.entrySet()){
            cs.changeArg(entry.getKey(),entry.getValue());
//            Log.e("CS", "addArgs: "+entry.getKey()+": "+entry.getValue());
        }
    }
    protected void setArgs(ChooseSettings choices){
        cs = choices;
    }

    private void testSummary(){
        ArrayList<String> testList = new ArrayList<>();

        addArg("faculity", "33");
        addArg("action", "get_year_1");
        addArg("form", "1");
        addArg("degree", "2");
        addArg("direction", "1214");
        addArg("specialty", "2776");
        addArg("specialization", "null");
        addArg("year", "534");

        for(String title: fragmentTitles){
            testList.add(title+": test");

        }
        goToSummary(testList);
    }
    protected void goToSummary(ArrayList<String> msgs){
        actualFragment = 0;
        ArrayList<String> tmp = new ArrayList<>();
        for(String msg: msgs){
            nextFragment(msg+"\n",tmp);
        }
        mainLayout.setVisibility(View.VISIBLE);
        aiLayout.setVisibility(View.GONE);
        menuLayout.setVisibility(View.GONE);
//        actualFragment = fragments.size()-1;
//        ft = getSupportFragmentManager().beginTransaction();
//        ft.hide(fragments.get(0));
//        ft.show(csf);
//        ft.commit();
//        isSummary = true;
//        csf.setSummary(msgs);
//        setButtons(true);
//        skipButton.setText(getResources().getString(R.string.ok));
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
            ArrayList<String> tmp = new ArrayList<>(fragments.get(actualFragment).getInfos());
            tmp.add(msg);
            csf.setSummary(tmp);
            setButtons(true);
            skipButton.setText(getResources().getString(R.string.ok));
        }
    }
    protected void previousFragment(){
        if(isSummary){
            ft = getSupportFragmentManager().beginTransaction();
            ft.hide(csf);
            ft.show(fragments.get(actualFragment));
            ft.commit();
            isSummary = false;
            skipButton.setText(getResources().getString(R.string.skip_button));
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
//    protected void setInfo(String msg, Fragment fragment){
////        switch (fragment.getId()){
////
////        }
//    }
    private void saveSettings(){
        SharedPreferences sharedPreferences = getSharedPreferences("schedule",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        cs.addArg("action", "search");
        cs.addArg("common","0");
        LinkedHashMap<String, String> dataMaps = cs.getArgs();
        Set<String> keySet = dataMaps.keySet();
        editor.putStringSet("datas", keySet);
        for(String key:keySet){
            editor.putString(key, dataMaps.get(key));
        }


        editor.apply();




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
            case R.id.aiModeButton:
                aiLayout.setVisibility(View.VISIBLE);
//                Log.e("TAG", "onClick: "+aiInputText.getText().toString() );
                menuLayout.setVisibility(View.GONE);
                aiFragment.initAiProccess("https://harmonogram.up.krakow.pl", "https://harmonogram.up.krakow.pl/inc/functions/a_select.php", actions, datas, fragmentTitles);
                break;
            case R.id.normalModeButton:
                mainLayout.setVisibility(View.VISIBLE);
                menuLayout.setVisibility(View.GONE);
                break;
            case R.id.testModeButton:
                testSummary();
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

        private ChooseSettings tempcs = null;
        public ExecuteTask(ChooseSettings chooseSettings){
            this.tempcs = chooseSettings;

        }
        public ExecuteTask(){

        }
        @Override
        protected Document doInBackground(String... strings) {
            Document doc = null;
            if(strings.length > 0){
                try {
                    Connection.Response response;
                    if(tempcs == null){
                        response = Jsoup.connect(strings[0])
                                .method(Connection.Method.POST)
                                .data(cs.getArgs())
                                .execute();
                    }
                    else{
                        response = Jsoup.connect(strings[0])
                                .method(Connection.Method.POST)
                                .data(tempcs.getArgs())
                                .execute();
                    }

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
