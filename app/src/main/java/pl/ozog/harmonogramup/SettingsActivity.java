package pl.ozog.harmonogramup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.login.LoginException;

import pl.ozog.harmonogramup.adapters.LanguageAdapter;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    ActionBar actionBar;
    Spinner languageSpinner;
    Button resetButton, cancelButton, confirmButton;

    LinkedHashMap<String, String> languagesMap;
    String langFromSettings = "default";
    String oldLangCode = "";
    Map.Entry<String, String> selectedLanguage;
    boolean isLanguageSelected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        actionBar = getSupportActionBar();
        actionBar.hide();


        confirmButton = findViewById(R.id.settingConfirmButton);
        confirmButton.setOnClickListener(this);

        cancelButton = findViewById(R.id.settingsCancelButton);
        cancelButton.setOnClickListener(this);

        resetButton = findViewById(R.id.settingsResetButton);
        resetButton.setOnClickListener(this);

        Resources langRes = getResources();
        String[] arrLang = langRes.getStringArray(R.array.language_codes_list);

        languagesMap = new LinkedHashMap<>();
        for(String el: arrLang){
            String[] elFragments = el.split("\\|");
            if(elFragments.length>1){
                languagesMap.put(elFragments[1],elFragments[0]);
            }
        }

        LanguageAdapter la = new LanguageAdapter(languagesMap);
        languageSpinner = findViewById(R.id.settingsLanguageSpinner);
        languageSpinner.setAdapter(la);

        String selectedLangCode = "default";
        SharedPreferences sharedPreferences = getSharedPreferences("schedule", Context.MODE_PRIVATE);
        if(sharedPreferences.getAll().size()>0){
            langFromSettings = sharedPreferences.getString("appLanguageCode", "default");
            selectedLangCode = sharedPreferences.getString("selectedLanguageCode", langFromSettings);
        }
        oldLangCode = langFromSettings;
        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(languagesMap.keySet());
        selectedLanguage = new AbstractMap.SimpleEntry(selectedLangCode,languagesMap.get(selectedLangCode));

        int selPos = keys.indexOf(selectedLangCode);

        if (selPos>=0){
            languageSpinner.setSelection(selPos, false);
        }
        else{
            languageSpinner.setSelection(0, false);
            langFromSettings = "default";
        }
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                Map.Entry<String, String> item = (Map.Entry<String, String>) parent.getItemAtPosition(position);
                Log.e("Settings", "onItemSelected: sel: "+selectedLanguage.getKey()+" item: "+item.getKey());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("selectedLanguageCode", item.getKey());
                if(!selectedLanguage.getKey().equals(item.getKey())){
                    if(!item.getKey().equals("default")){
                        Locale lc = new Locale(item.getKey());
                        Locale.setDefault(lc);
                        Configuration conf = getBaseContext().getResources().getConfiguration();
                        conf.setLocale(lc);
                        getBaseContext().getResources().updateConfiguration(conf, getBaseContext().getResources().getDisplayMetrics());
                        selectedLanguage = item;
                        editor.apply();
                        recreate();
                    }else{

                        Locale lc = Resources.getSystem().getConfiguration().getLocales().get(0);
                        Locale.setDefault(lc);
                        Configuration conf = getBaseContext().getResources().getConfiguration();
                        conf.setLocale(lc);
                        getBaseContext().getResources().updateConfiguration(conf,getBaseContext().getResources().getDisplayMetrics());
                        selectedLanguage = item;
                        editor.apply();
                        recreate();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onBackPressed() {
//        Intent goBackIntent = new Intent();
//        setResult(Activity.RESULT_CANCELED, goBackIntent);
//        finish();
//        super.onBackPressed();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.settingConfirmButton:
                Intent confirmIntent = new Intent();
                SharedPreferences sharedPreferences = getSharedPreferences("schedule", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("appLanguageCode",selectedLanguage.getKey());
                editor.apply();
                setResult(Activity.RESULT_OK, confirmIntent);
                finish();
                break;

            case R.id.settingsCancelButton:
                Intent cancelIntent = new Intent();
                Locale lc = Resources.getSystem().getConfiguration().getLocales().get(0);
                Locale.setDefault(lc);
                Configuration conf = getBaseContext().getResources().getConfiguration();
                conf.setLocale(lc);
                getBaseContext().getResources().updateConfiguration(conf,getBaseContext().getResources().getDisplayMetrics());
                setResult(RESULT_CANCELED, cancelIntent);
                finish();
                break;

            case R.id.settingsResetButton:

                Locale lcReset = Resources.getSystem().getConfiguration().getLocales().get(0);
                Locale.setDefault(lcReset);
                Configuration confReset = getBaseContext().getResources().getConfiguration();
                confReset.setLocale(lcReset);
                getBaseContext().getResources().updateConfiguration(confReset,getBaseContext().getResources().getDisplayMetrics());
                ArrayList<String> keys = new ArrayList<>();
                keys.addAll(languagesMap.keySet());
                int selPos = keys.indexOf("default");

                if (selPos>=0){
                    languageSpinner.setSelection(selPos, false);
                }

                recreate();
                break;
        }
    }
}