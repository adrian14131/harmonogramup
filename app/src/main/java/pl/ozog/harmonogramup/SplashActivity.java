package pl.ozog.harmonogramup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Range;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import pl.ozog.harmonogramup.generators.ListGenerators;
import pl.ozog.harmonogramup.items.CourseItem;
import pl.ozog.harmonogramup.items.RangeItem;
import pl.ozog.harmonogramup.tools.FileTools;

public class SplashActivity extends AppCompatActivity{


    ActionBar actionBar;
    TextView info;
    File internalStorageDir;
    JSONObject jsonCourses, jsonGroups, jsonWeeks, jsonSemesters;
    Map<String, JSONObject> files = new HashMap<>();
    private static final String TAG = "SplashActivity";
    private final Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        loadLanguage();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        actionBar = getSupportActionBar();
        actionBar.hide();
        internalStorageDir = getDataDir();
        Log.e(TAG, "onCreate: getdatadir="+internalStorageDir);
        info = findViewById(R.id.splashInfoTextView);
        info.setVisibility(View.INVISIBLE);

        SharedPreferences sharedPreferences = getSharedPreferences("schedule",Context.MODE_PRIVATE);
        Log.e(TAG, "onCreate: sps: "+sharedPreferences.getAll());

        files.put("courses.json", jsonCourses);
        files.put("groups.json", jsonGroups);
        files.put("weeks.json", jsonWeeks);
        files.put("semesters.json", jsonSemesters);

        boolean offlineMode = sharedPreferences.getBoolean("offlineMode", false);
        boolean updateOption = sharedPreferences.getBoolean("updateOption", true);

        if(isOnline() || offlineMode){
            if(hasConfig(sharedPreferences)){
                if(!offlineMode){
                    loadDatasToJson(MainActivity.searchUrl, MainActivity.selectUrl);
                    if(isNewData(internalStorageDir)){
                        if(isOnline() && updateOption){
                            showInfo(getResources().getString(R.string.updating_data)+"\n"+getResources().getString(R.string.wait_moment));
                            update(internalStorageDir);
                        }
                    }
                }
                if(hasOfflineData(internalStorageDir)){

                    if(isOnline() && !offlineMode)
//                        showInfo(false);
                        showInfo(getResources().getString(R.string.loading));
                    else if(offlineMode)
                        showInfo(getResources().getString(R.string.loading_offline_data));
                    else
                        showInfo(getResources().getString(R.string.no_internet_short)+"\n"+getResources().getString(R.string.loading_offline_data));
                    goToMain();
                }
                else{
                    showInfo(getResources().getString(R.string.no_data));
                }
            }
            else{
                if(isOnline()){
                    showInfo(getResources().getString(R.string.field_not_choosed)+"\n"+getResources().getString(R.string.wait_moment));
                    loadDatasToJson(MainActivity.searchUrl, MainActivity.selectUrl);
                    update(internalStorageDir);
//                    if(isNewData(internalStorageDir)){
//                        update(internalStorageDir);
//                    }
                    goToFirstConfig();
                }
                else{
                    showInfo(getResources().getString(R.string.field_not_choosed)+"\n"+getResources().getString(R.string.no_internet_connection));
                }
            }
        }
        else{
            if(hasConfig(sharedPreferences)){
                if(hasOfflineData(internalStorageDir)){
                    showInfo(getResources().getString(R.string.no_internet_short)+"\n"+getResources().getString(R.string.loading_offline_data));
                    goToMain();
//                    showInfo(getResources().getString(R.string.no_data)+"\n"+getResources().getString(R.string.no_internet_connection));
                }
                else{
                    showInfo(getResources().getString(R.string.no_data)+"\n"+getResources().getString(R.string.no_internet_connection));
                }
            }
            else{
                showInfo(getResources().getString(R.string.field_not_choosed)+"\n"+getResources().getString(R.string.no_internet_connection));
            }
        }
    }

    private boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if(netInfo!=null){
            return netInfo.isConnected() && netInfo.isAvailable();
        }
        return false;
    }
    private void goToMain(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);

                    finish();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        },1000);
    }
    private void goToFirstConfig(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    Intent intent = new Intent(getApplicationContext(), FirstSettings.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);

                    finish();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        },1000);
    }
    private boolean hasOfflineData(File dir){
        for(String fileName: files.keySet()){
            File file = new File(dir, fileName);
            if(!file.exists()){
                return false;
            }
        }
        return true;
    }
    private void loadLanguage(){
        SharedPreferences sharedPreferences = getSharedPreferences("schedule",Context.MODE_PRIVATE);
        String lang = sharedPreferences.getString("appLanguageCode","default");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(sharedPreferences.contains("selectedLanguageCode"))
            editor.remove("selectedLanguageCode");
        if(!lang.equals("default")){
            Locale lc = new Locale(lang);
            Locale.setDefault(lc);
            Configuration conf = getBaseContext().getResources().getConfiguration();
            conf.setLocale(lc);
            getBaseContext().getResources().updateConfiguration(conf,getBaseContext().getResources().getDisplayMetrics());
        }
        else{
            Locale lc = Resources.getSystem().getConfiguration().getLocales().get(0);
            Locale.setDefault(lc);
            Configuration conf = getBaseContext().getResources().getConfiguration();
            conf.setLocale(lc);
            getBaseContext().getResources().updateConfiguration(conf,getBaseContext().getResources().getDisplayMetrics());
            editor.putString("appLanguageCode", "default");
        }
        editor.apply();
    }
    private boolean hasConfig(SharedPreferences sp){

        if(sp.contains("datas")){
            Set<String> keys = sp.getStringSet("datas", null);
            if(keys==null)
                return false;
            return !keys.isEmpty();
        }
        return false;
    }

    private boolean isNewData(File internalStorageDir){
        for(Map.Entry<String, JSONObject> entry: files.entrySet()){
            Log.e(TAG, "isNewData: "+entry.getKey());
            if(!FileTools.compareFileToJson(internalStorageDir, entry.getKey(), entry.getValue())){
                Log.e(TAG, "is new datas for "+entry.getKey() );
                return true;
            }
            else{
                Log.e(TAG, "is not new datas for "+entry.getKey());
            }
        }
//        if(!FileTools.compareFileToJson(internalStorageDir, "courses.json", jsonCourses) || !FileTools.compareFileToJson(internalStorageDir, "groups.json", jsonGroups)){
//            Log.e(TAG, "is new datas" );
//        }
//        else{
//            Log.e(TAG, "is not new datas ");
//        }
        return false;
    }
    private boolean update(File internalStorageDir){
        for(Map.Entry<String, JSONObject> entry: files.entrySet()){
            FileTools.saveJsonToFile(internalStorageDir, entry.getKey(), entry.getValue());
            Log.e(TAG, "update: "+entry.getKey());
        }
        return false;
    }

    private boolean loadDatasToJson(String coursesUrl, String rangesUrl){
        SharedPreferences sp = getSharedPreferences("schedule", Context.MODE_PRIVATE);
        //ArrayList<RangeItem> rangeSemesters = MainActivity.generateRangeList(rangesUrl, new ChooseSettings(), "3");
        ArrayList<RangeItem> rangeSemesters = ListGenerators.generateRangeList(new ChooseSettings(), "3", rangesUrl);
        jsonCourses = loadCoursesToJson(coursesUrl, rangeSemesters, sp);
        files.put("courses.json", jsonCourses);
        //if(FileTools.saveJsonToFile(internalStorageDir, "courses.json", jsonCourses) == null) return false;
        jsonGroups = loadGroupsToJson(coursesUrl, rangeSemesters, sp);
        files.put("groups.json", jsonGroups);
        //if(FileTools.saveJsonToFile(internalStorageDir,"groups.json", jsonGroups) == null) return false;
        jsonWeeks = loadWeeksToJson(rangesUrl);
        files.put("weeks.json", jsonWeeks);
        //if(FileTools.saveJsonToFile(internalStorageDir, "weeks.json", jsonWeeks) == null) return false;
        jsonSemesters = loadSemestersToJson(rangesUrl);
        files.put("semesters.json", jsonSemesters);
        //if(FileTools.saveJsonToFile(internalStorageDir, "semesters.json", jsonSemesters) == null) return false;
        for(JSONObject entry: files.values()){
            if(entry == null){
                return false;
            }
        }
        return true;
    }
    private JSONObject loadCoursesToJson(String coursesUrl, ArrayList<RangeItem> ris, SharedPreferences sp){

        JSONObject result = new JSONObject();
        JSONArray semesters = new JSONArray();
        try {
            for(RangeItem ri: ris){

                    JSONObject semester = new JSONObject(new Gson().toJson(ri));

                    //ArrayList<CourseItem> cis = MainActivity.generateListOfCourses(MainActivity.getScheduleSettings(sp), ri, "3", coursesUrl);
                    ChooseSettings chooseSettings = new ChooseSettings();
                    chooseSettings.setArgs(sp);
                    ArrayList<CourseItem> cis = ListGenerators.generateListOfCourses(chooseSettings, "3", ri, coursesUrl);
                    JSONArray courses = new JSONArray(new Gson().toJson(cis));
                    semester.put("courses", courses);
                    semesters.put(semester);


            }
            result.put("semesters", semesters);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }
    private JSONObject loadGroupsToJson(String coursesUrl, ArrayList<RangeItem> ris, SharedPreferences sp) {
        JSONObject result = new JSONObject();
        JSONArray semesters = new JSONArray();
        try{
            for(RangeItem ri: ris){
                JSONObject semester = new JSONObject(new Gson().toJson(ri));
                ChooseSettings chooseSettings = new ChooseSettings();
                chooseSettings.setArgs(sp);
                ArrayList<String> groups = ListGenerators.generateGroupList(new ChooseSettings(), ri, chooseSettings, coursesUrl);
                        //MainActivity.generateGroup(new ChooseSettings(), MainActivity.getScheduleSettings(sp), coursesUrl, ri);
                JSONArray jsonGroups = new JSONArray(new Gson().toJson(groups));
                semester.put("groups", jsonGroups);
                semesters.put(semester);

            }
            result.put("semesters", semesters);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }
    private JSONObject loadRangesToJson(String url, String range, String listName){
        JSONObject result = new JSONObject();
        ChooseSettings csWeeks = new ChooseSettings();
        csWeeks.addArg("range", range);
        try {
            ArrayList<RangeItem> weeks = ListGenerators.generateRangeList(csWeeks, url);
            JSONArray jsonWeeks = new JSONArray(new Gson().toJson(weeks));
            result.put(listName, jsonWeeks);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
    private JSONObject loadWeeksToJson(String url){
        return loadRangesToJson(url,"2", "weeks");
    }
    private JSONObject loadSemestersToJson(String url){
        return loadRangesToJson(url,"3", "semesters");
    }



    private void showInfo(String message){
        info.setText(message);
        info.setVisibility(View.VISIBLE);
    }
    private void showInfo(boolean show){
        if(show)
            info.setVisibility(View.VISIBLE);
        else
            info.setVisibility(View.INVISIBLE);
    }
}
