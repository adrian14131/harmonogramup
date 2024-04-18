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
import java.util.Set;
import java.util.concurrent.ExecutionException;


import pl.ozog.harmonogramup.ai.ChoiceNode;
import pl.ozog.harmonogramup.downloaders.GptApiTask;

public class FirstSettings extends AppCompatActivity implements View.OnClickListener {

    ActionBar actionBar;
    protected static ChooseSettings cs;
    Button prevButton, skipButton, aiModeButton, normalModeButton;
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

    ArrayList<ChoiceNode> aiChoicesList;
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
        aiChoicesList = new ArrayList<>();
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

        }
    }

//    public class AiChoiceProccess {
//
//        private ArrayList<String> fieldOrder;
//        private Map<String, String> fieldName1;
//        private Map<String, String> fieldName2;
//        private Map<String, Boolean> needMoreInfos;
//        private Map<String, Boolean> isRequaireds;
//        private Map<String, String> moreInfos;
//
//        private Map<String, String> defaultDatas;
//        private ArrayList<String> result = new ArrayList<>();
//
//        private String adminMessage;
//        private String url, functionUrl;
//        private ArrayList<String> actions;
//        private ArrayList<String> datas;
//        private String userMessage;
//        private OpenAiService aiService;
//        private int errorIndex = 0;
//        private ChooseSettings choices;
//        public AiChoiceProccess(String url, String function, ArrayList<String> actions, ArrayList<String> datas) {
//
//            this.aiService = new OpenAiService("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");
//            this.url= url;
//            this.functionUrl = function;
//            this.actions = actions;
//            this.datas = datas;
//            fieldOrder = new ArrayList<>();
//            fieldName1 = new LinkedHashMap<>();
//            fieldName2 = new LinkedHashMap<>();
//            needMoreInfos = new LinkedHashMap<>();
//            isRequaireds = new LinkedHashMap<>();
//            moreInfos = new LinkedHashMap<>();
//            defaultDatas = new LinkedHashMap<>();
//            prepareFields();
//            prepareFieldOrder();
//            choices = new ChooseSettings();
//            adminMessage = "Jest to przedstawienie się studenta, na podstawie tego wybierz element zawierający odpowiedni _NAME1 na liście z bazy JSON: _JSON. Wszystkie pola tego elementu muszą być zwrócone.\n" +
//                    "Jeżeli nie podano jaki _NAME1 lub nie ma 100% pewności to wtedy zwróć pole \"error\"." +
//                    "Odpowiedź zwróć w formacie JSON w polu o nazwie\"_KEY\"";
//
//        }
//        private void prepareFieldOrder(){
//            fieldOrder.add("faculity");
//            fieldOrder.add("form");
//            fieldOrder.add("degree");
//            fieldOrder.add("direction");
//            fieldOrder.add("specialty");
//            fieldOrder.add("specialization");
//            fieldOrder.add("year");
//        }
//
//        private void prepareFields(){
//            prepareFieldInfos("faculity", "instytut", "instytucie", false, "", true,"33");
//            prepareFieldInfos("form", "formę studiów", "formie studiów", false, "", true,"1");
//            prepareFieldInfos("degree", "stopień", "stopniu", false, "", true,"2");
//            prepareFieldInfos("direction", "kierunek", "kierunku", false, "", true,"1213");
//            prepareFieldInfos("specialty", "specjalność", "specjalności", false, "", false,"");
//            prepareFieldInfos("specialization", "specjalizację", "specjalizacji", false, "", false,"");
//            prepareFieldInfos("year", "rok studiów", "roku studiów", true, "przy wyborze zadecyduj czy teraz jest semestr letni czy zimowy", false,"518");
//        }
//        private void prepareFieldInfos(String key, String name1, String name2, Boolean needMoreInfo, String moreInfo, Boolean required, String defaultData){
//            fieldName1.put(key, name1);
//            fieldName2.put(key, name2);
//            needMoreInfos.put(key, needMoreInfo);
//            moreInfos.put(key, moreInfo);
//            isRequaireds.put(key, required);
//            defaultDatas.put(key, defaultData);
//        }
//
//        public void startProccess(String message){
//            userMessage = message;
//            execute();
//            for(String el: result){
//                Log.e("AI result list", el );
//            }
//            Log.e("AI", "startProccess: "+makeJsonResult());
//
//        }
//        public JSONArray makeJsonResult(){
//            JSONArray jsonArray = new JSONArray();
//            for(String jsonStr: result){
//                try {
//                    JSONObject jsonField = new JSONObject(jsonStr);
//                    jsonArray.put(jsonField);
//
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            return jsonArray;
//        }
//        private void execute(){
//            for(int i=errorIndex; i<fieldOrder.size()-1; i++){
//
//                if(i==0){
//                    getInstitute();
//                }
//                else {
//                    getOtherChoices(i);
//                }
//
//                if(checkIfError(i) && isRequaireds.get(fieldOrder.get(i))){
//                    Log.e("AI", "execute: error on index"+i);
//                    resultTextView.setText("Błąd w polu "+fieldName1.get(fieldOrder.get(errorIndex))+". Podaj jeszcze raz informacje na temat tego pola.");
//                    break;
//                }
//                setDataChoice(i);
//
//
//            }
//        }
//        private void setDataChoice(int index){
//            try {
//                JSONObject jsonObject = new JSONObject(result.get(index));
//                String key = fieldOrder.get(index);
//                jsonObject = jsonObject.optJSONObject(key);
//                String keyValue = jsonObject!=null?jsonObject.optString("key",null):null;
//                keyValue = keyValue==null?defaultDatas.get(key):keyValue;
//
//                choices.changeArg(datas.get(index), keyValue);
//
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        private boolean checkIfError(int index){
//            try {
//                JSONObject jsonObject = new JSONObject(result.get(index));
//                String message = jsonObject.optString(fieldOrder.get(index),"error");
//                Log.e("AI", "checkIfError: "+message );
//                if(message.equals("error") && isRequaireds.get(fieldOrder.get(index))){
//                    errorIndex = index;
//                    result.remove(index);
//                    return true;
//                }
//                else if(jsonObject.toString().contains("error") && isRequaireds.get(fieldOrder.get(index))){
//                    errorIndex = index;
//                    result.remove(index);
//                    return true;
//                }
//            } catch (JSONException e) {
//                if(isRequaireds.get(fieldOrder.get(index))){
//                    errorIndex = index;
//                    result.remove(index);
//                }
//
//                return true;
//            }
//            return false;
//        }
//        private String prepareAdminMessage(int index, String json){
//            String key = fieldOrder.get(index);
//            String result = adminMessage;
//
//            result = result.replace("_NAME1", fieldName1.get(key))
//                    .replace("_NAME2", fieldName2.get(key))
//                    .replace("_JSON", json)
//                    .replace("_KEY", key);
//
//            if(needMoreInfos.get(key)){
//                result+="\ndodatkowo w tym przypadku, "+moreInfos.get(key);
//            }
//            return result;
//        }
//        private void getInstitute(){
//            FirstSettings.DownloadPageTask downloadPageTask = new FirstSettings.DownloadPageTask();
//            try{
//                Document document = downloadPageTask.execute(url).get();
//                LinkedHashMap<String, String> options = ChooseFirstFragment.getMapFromElement(document);
//
//                String aiRes = askAI(mapToJson(options).toString(), 0);
//
//                Log.e("AI", "getInstitute: "+aiRes);
//
//                result.add(aiRes);
//
//            } catch (ExecutionException e) {
//                throw new RuntimeException(e);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        private JSONArray mapToJson(LinkedHashMap<String, String> map){
//            JSONArray result = new JSONArray();
//            for(Map.Entry<String, String> entry: map.entrySet()){
//                JSONObject jsonObject = new JSONObject();
//                try {
//                    jsonObject.put("key", entry.getKey());
//                    jsonObject.put("value", entry.getValue());
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//                result.put(jsonObject);
//
//            }
//            return result;
//        }
//        private void getOtherChoices(int index){
//            choices.changeArg("action",actions.get(index-1));
//            ExecuteTask executeTask = new ExecuteTask(choices);
//            try{
//                Document document = executeTask.execute(functionUrl).get();
//                LinkedHashMap<String, String> options = FirstSettingsFragment.getMapFromElement(document);
//                String aiRes = askAI(mapToJson(options).toString(), index);
//                Log.e("AI", "getOtherChoices: "+aiRes);
//                result.add(aiRes);
//
//            } catch (ExecutionException e) {
//                throw new RuntimeException(e);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//        }
//        private String askAI(String json, int index){
//            Log.e("AI", "askAI: "+json );
//            String strRes = "";
//            String rule = prepareAdminMessage(index, json);
//            try {
//                JSONArray array = new JSONArray(json);
//                if(array == null) return "{\""+fieldOrder.get(index)+"\": \"error\"}";
//                if(array.length()==0) return "{\""+fieldOrder.get(index)+"\": \"error\"}";
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//
//            GptApiTask apiTask = new GptApiTask(aiService);
//            try {
//                String message = prepareAdminMessage(index, json);
//                String aiRes = apiTask.execute(userMessage, rule).get();
//                strRes = aiRes;
//            } catch (ExecutionException e) {
//                throw new RuntimeException(e);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            return strRes;
//        }
//
//    }

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
