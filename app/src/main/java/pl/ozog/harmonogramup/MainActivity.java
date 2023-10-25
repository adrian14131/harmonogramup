package pl.ozog.harmonogramup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import pl.ozog.harmonogramup.adapters.MapSpinnerAdapter;
import pl.ozog.harmonogramup.adapters.RangeSpinnerAdapter;
import pl.ozog.harmonogramup.adapters.ScheduleAdapterDay;
import pl.ozog.harmonogramup.enums.FilesNames;
import pl.ozog.harmonogramup.generators.ListGenerators;
import pl.ozog.harmonogramup.generators.ListJsonGenerators;
import pl.ozog.harmonogramup.items.CourseItem;
import pl.ozog.harmonogramup.items.RangeItem;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    static final int SETTINGS_CODE = 2001;
    private static final String TAG = "MainActivity";

    ActionBar actionBar;
    LinkedHashMap<String, String> datas;
    LinkedHashMap<String, String> typeOfRange;
    ArrayList<String> groups;
    ArrayList<String> selectedGroups;
    ArrayList<RangeItem> rangeDatas;

    public static String searchUrl = "https://harmonogram.up.krakow.pl/inc/functions/a_search.php";
    public static String selectUrl = "https://harmonogram.up.krakow.pl/inc/functions/a_select.php";
    Spinner torSpinner, rangeDataSpinner;
    MapSpinnerAdapter spinnerAdapter;
    Button prevButton, nextButton, changeButton, groupButton;
    ImageButton otherButton;
    EditText dateInput;
    ListView scheduleListView;
    ListView dialogGroupListView;
    Button dialogGroupOkButton;
    ArrayList<CourseItem> courses;
    static ChooseSettings csMain, csRange;
    Calendar myCalendar;
    Date today;
    static String range = "1";
    int spinnerSelectedPos = -1;
    TextView textView;

    File internalStorageDir;
    boolean offlineMode;

    Menu menu;
    public static final int GET_VALUE_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        actionBar.hide();
        datas = new LinkedHashMap<>();
        typeOfRange = new LinkedHashMap<>();
        typeOfRange.put("1", getResources().getString(R.string.day));
        typeOfRange.put("2", getResources().getString(R.string.week));
        typeOfRange.put("3", getResources().getString(R.string.semester));
        myCalendar = Calendar.getInstance();
        today = Calendar.getInstance().getTime();
        csRange = new ChooseSettings();

        internalStorageDir = getDataDir();
        SharedPreferences sharedPreferences = getSharedPreferences("schedule", Context.MODE_PRIVATE);
        csMain = new ChooseSettings();
        csMain.setArgs(sharedPreferences);

        updatePatch();

        datas = csMain.getArgs();
        offlineMode = sharedPreferences.getBoolean("offlineMode", false);
        boolean goBackFFS = getIntent().getBooleanExtra("goBackFromFirstSettings",false);

        groups = generateGroup();
        Set<String> selGr = sharedPreferences.getStringSet("selectedGroups", null);
        if(selGr == null){
            Set<String> sets = new HashSet<String>();
            sets.addAll(groups);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet("selectedGroups", sets);

            selectedGroups = groups;
        }
        else{

            selectedGroups = new ArrayList<>();
            selectedGroups.addAll(selGr);
        }
        prevButton = findViewById(R.id.mainPrevRangeButton);
        prevButton.setOnClickListener(this);

        nextButton = findViewById(R.id.mainNextRangeButton);
        nextButton.setOnClickListener(this);

        changeButton = findViewById(R.id.mainChangeDirectionButton);
        changeButton.setOnClickListener(this);

        groupButton = findViewById(R.id.mainChangeGroupButton);
        groupButton.setOnClickListener(this);

        otherButton = findViewById(R.id.mainMoreButton);
        otherButton.setOnClickListener(this);

        dateInput = findViewById(R.id.mainDateInput);
        setUpLabel();

        rangeDataSpinner = findViewById(R.id.mainDataRangeSpinner);

        torSpinner = findViewById(R.id.mainTypeOfRange);
        spinnerAdapter = new MapSpinnerAdapter(typeOfRange);
        torSpinner.setAdapter(spinnerAdapter);
        torSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Map.Entry<String, String> item = (Map.Entry) adapterView.getItemAtPosition(i);
                range = item.getKey();

                switch (range){
                    case "1":
                        dateInput.setVisibility(View.VISIBLE);
                        rangeDataSpinner.setVisibility(View.GONE);

                        setUpLabel();
                        String format = "yyyy-MM-dd";
                        SimpleDateFormat sdf = new SimpleDateFormat(format, getResources().getConfiguration().getLocales().get(0));
                        generateSchedule("1", sdf.format(today), "null", false);
                        myCalendar.setTime(today);
                        break;
                    case "2":
                    case "3":
                        dateInput.setVisibility(View.GONE);
                        rangeDataSpinner.setVisibility(View.VISIBLE);
                        if(range.equals("2"))
                            csRange.addArg("action", "set_week");
                        else
                            csRange.addArg("action", "set_semester");
                        csRange.addArg("range", range);
                        spinnerSelectedPos = -1;
                        ArrayList<RangeItem> rangeItems;
                        if(canDownload()){
                             rangeItems = ListGenerators.generateRangeList(csRange, selectUrl);
                        }
                        else{
                            rangeItems = ListJsonGenerators.loadRangeList(internalStorageDir, csRange);
                        }

                        int tmpInt = 0;
                        for(RangeItem ri: rangeItems){
                            if(ri.isSelected()){
                                spinnerSelectedPos = tmpInt;
                            }
                            tmpInt++;
                        }
                        if(spinnerSelectedPos<rangeItems.size() && spinnerSelectedPos!=-1){
                            rangeItems.get(spinnerSelectedPos).setSelected(true);
                        }
                        else{
                            spinnerSelectedPos = 0;
                        }
                        RangeSpinnerAdapter rangeAdapter = new RangeSpinnerAdapter(rangeItems);
                        rangeDataSpinner.setAdapter(rangeAdapter);
                        if(spinnerSelectedPos<rangeItems.size()){
                            rangeDataSpinner.setSelection(spinnerSelectedPos);
                        }
                        break;

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        rangeDataSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                RangeItem ri = (RangeItem)adapterView.getItemAtPosition(i);
                generateSchedule(range, ri.getRange(), ri.getSemestrId(), true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        scheduleListView = findViewById(R.id.mainScheduleListView);
        //swipe function

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {


                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                updateLabel();
            }
        };
        dateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DatePickerDialog(MainActivity.this, dateSetListener, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format, getResources().getConfiguration().getLocales().get(0));
        generateSchedule("1", sdf.format(today), "null", false);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.mainPrevRangeButton:
                if(range.equals("1")){
                    myCalendar.add(Calendar.DATE, -1);
                    updateLabel();
                }
                else{
                    if(rangeDataSpinner.getSelectedItemPosition()>0){
                        rangeDataSpinner.setSelection(rangeDataSpinner.getSelectedItemPosition()-1);
                    }
                }
                break;
            case R.id.mainNextRangeButton:
                if(range.equals("1")){
                    myCalendar.add(Calendar.DATE, 1);
                    updateLabel();
                }
                else{
                    if(rangeDataSpinner.getSelectedItemPosition()<rangeDataSpinner.getCount()-1){
                        rangeDataSpinner.setSelection(rangeDataSpinner.getSelectedItemPosition()+1);
                    }
                }

                break;
            case R.id.mainChangeDirectionButton:
                Intent intent = new Intent(view.getContext(), FirstSettings.class);
                intent.putExtra("isMain", true);
                intent.putExtra("range", range);
                intent.putExtra("dateInput", dateInput.getText().toString());

                if(rangeDataSpinner.getSelectedItemPosition()<0){
                    intent.putExtra("dateRange",0);
                }
                else{
                    intent.putExtra("dateRange",rangeDataSpinner.getSelectedItemPosition());
                }

                startActivity(intent);
                break;
            case R.id.mainChangeGroupButton:
                showGroupDialog(groups);
                break;
            case R.id.mainMoreButton:
                showPopup(view);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case SETTINGS_CODE:

                if(resultCode == Activity.RESULT_OK){
                    recreate();
                }
                break;
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        Log.e(TAG, "onMenuOpened: "+featureId );
        return super.onMenuOpened(featureId, menu);
    }

    public void onClick(MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);

                startActivityForResult(settingsIntent,SETTINGS_CODE);
                break;
            case R.id.action_groups:
                showGroupDialog(groups);
                break;
            case R.id.action_change_field:
                Intent intent = new Intent(MainActivity.this, FirstSettings.class);
                intent.putExtra("isMain", true);
                intent.putExtra("range", range);
                intent.putExtra("dateInput", dateInput.getText());
                if(rangeDataSpinner.getSelectedItemPosition()<0){
                    intent.putExtra("dateRange",0);
                }
                else{
                    intent.putExtra("dateRange",rangeDataSpinner.getSelectedItemPosition());
                }

                startActivity(intent);
                break;
            case R.id.action_common_activities:

                if(csMain.getArg("common") == null){
                    csMain.addArg("common", "1");
                }
                else{
                    if(csMain.getArg("common").equals("0")) csMain.addArg("common", "1");
                    else if(csMain.getArg("common").equals("1")) csMain.addArg("common", "0");
                }

                SharedPreferences sharedPreferences = getSharedPreferences("schedule", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("common", csMain.getArg("common"));
                editor.apply();
                if(range.equals("1")) updateLabel();
                else rangeDataSpinner.setSelection(rangeDataSpinner.getSelectedItemPosition());
                break;
        }
    }

    private void setUpLabel(){

        String format = "yyyy-MM-dd EEEE";
        SimpleDateFormat sdf = new SimpleDateFormat(format, getResources().getConfiguration().getLocales().get(0));
        dateInput.setText(sdf.format(today));
    }
    private void updateLabel(){

        String format = "yyyy-MM-dd EEEE";
        SimpleDateFormat sdf = new SimpleDateFormat(format, getResources().getConfiguration().getLocales().get(0));
        dateInput.setText(sdf.format(myCalendar.getTime()));
        format = "yyyy-MM-dd";
        sdf = new SimpleDateFormat(format, getResources().getConfiguration().getLocales().get(0));

        generateSchedule(((Map.Entry<String, String>)torSpinner.getSelectedItem()).getKey(), sdf.format(myCalendar.getTime()), "null", false);
    }

    private void generateSchedule(String rangeMode, String dateRange, String semester, boolean showDate){

        csMain.addArg("range", rangeMode);
        csMain.addArg("dataRange", dateRange);
        csMain.addArg("semestrRokId", semester);
        csMain.addArg("group", "null");

        if(canDownload()){
            courses = ListGenerators.generateListOfCourses(csMain, rangeMode, dateRange, semester, searchUrl);
        }
        else{
            String format2 = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sdf2 = new SimpleDateFormat(format2, getResources().getConfiguration().getLocales().get(0));

            courses = ListJsonGenerators.loadCourseList(FilesNames.COURSES.getFile(internalStorageDir), csMain, ListJsonGenerators.loadRangeList(internalStorageDir, csMain), sdf2);
        }

        courses = ListGenerators.filterCourseListByGroups(courses, selectedGroups);

        ScheduleAdapterDay sad = new ScheduleAdapterDay(courses, showDate, this);

        scheduleListView.setAdapter(sad);

    }

    private void showPopup(View v){

        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.other_menu, popup.getMenu());
        MenuItem mi = popup.getMenu().findItem(R.id.action_common_activities);
        if(csMain.getArg("common").equals("0")) mi.setTitle(R.string.show_common_activities);
        else if(csMain.getArg("common").equals("1")) mi.setTitle(R.string.hide_common_activities);
        popup.show();
    }

    private ArrayList<String> generateGroup(){

        if(canDownload()){
            return ListGenerators.generateGroupList(csRange, selectUrl, csMain, searchUrl);
        }
        else
        {
            return ListJsonGenerators.loadGroupList(FilesNames.GROUPS.getFile(internalStorageDir));
        }

    }

    private void showGroupDialog(final ArrayList<String> grs){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.choose_groups));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SparseBooleanArray checked = dialogGroupListView.getCheckedItemPositions();
                ArrayList<String> grsSelected = new ArrayList<>();
                for(int i=0; i<dialogGroupListView.getCount(); i++){
                    if(checked.get(i)){
                        grsSelected.add(grs.get(i));
                    }
                }
                if(grsSelected.isEmpty()) {
                    selectedGroups = groups;
                }
                else{
                    selectedGroups = grsSelected;
                }
                Set<String> sets = new HashSet<String>();
                sets.addAll(selectedGroups);
                SharedPreferences sharedPreferences = getSharedPreferences("schedule", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putStringSet("selectedGroups", sets);
                editor.apply();
                if(range.equals("1")){

                    updateLabel();
                }
                else{
                    rangeDataSpinner.setSelection(rangeDataSpinner.getSelectedItemPosition());
                    RangeItem ri = (RangeItem)rangeDataSpinner.getItemAtPosition(rangeDataSpinner.getSelectedItemPosition());
                    generateSchedule(range, ri.getRange(), ri.getSemestrId(), true);
                }
            }
        });

        LayoutInflater li = this.getLayoutInflater();
        View groupDialogView = li.inflate(R.layout.dialog_groups, null);
        dialogGroupListView = groupDialogView.findViewById(R.id.groupListView);

        ArrayAdapter<String> adTest = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, groups);

        dialogGroupListView.setAdapter(adTest);
        dialogGroupListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        for(int i=0; i<dialogGroupListView.getCount();i++){
            if(selectedGroups.contains(groups.get(i))){
                dialogGroupListView.setItemChecked(i, true);
            }
            else {
                dialogGroupListView.setItemChecked(i, false);
            }
        }
        alertDialogBuilder.setView(groupDialogView);

        alertDialogBuilder.create();
        AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
    }
    private boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if(netInfo!=null){
            return netInfo.isConnected() && netInfo.isAvailable();
        }
        return false;
    }
    private boolean canDownload(){
        return isOnline() && !offlineMode;
    }

    private void updatePatch(){

        if(csMain.getArg("common")==null){
            csMain.addArg("common", "0");
            SharedPreferences sharedPreferences = getSharedPreferences("schedule", Context.MODE_PRIVATE);
            Set<String> keys = new HashSet<String>(sharedPreferences.getStringSet("datas",Set.of("")));
            Log.e(TAG, "updatePatch: "+keys );
            if(!keys.contains("common")){
                keys.add("common");
                Log.e(TAG, "updatePatch: add common"+keys );
                SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.remove("datas");
                editor.putStringSet("datas",keys);
                editor.apply();
            }
        }
    }
}
