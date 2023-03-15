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

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import pl.ozog.harmonogramup.adapters.MapSpinnerAdapter;
import pl.ozog.harmonogramup.adapters.RangeSpinnerAdapter;
import pl.ozog.harmonogramup.adapters.ScheduleAdapterDay;
import pl.ozog.harmonogramup.downloaders.RangeTask;
import pl.ozog.harmonogramup.downloaders.ScheduleTask;
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

        SharedPreferences sharedPreferences = getSharedPreferences("schedule", Context.MODE_PRIVATE);
        csMain = getScheduleSettings(sharedPreferences);
        datas = csMain.getArgs();

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
                        RangeTask rt = new RangeTask(csRange);
                        try {
                            Document doc = rt.execute(selectUrl).get();
                            spinnerSelectedPos = -1;
                            ArrayList<RangeItem> items = generateListForSpinner(doc);
                            RangeSpinnerAdapter rangeAdapter = new RangeSpinnerAdapter(items);
                            rangeDataSpinner.setAdapter(rangeAdapter);
                            if(spinnerSelectedPos<items.size()){
                                rangeDataSpinner.setSelection(spinnerSelectedPos);
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
//                    case "3":
//                        dateInput.setVisibility(View.GONE);
//                        rangeDataSpinner.setVisibility(View.VISIBLE);
//                        csRange.addArg("action", "set_semester");
//                        csRange.addArg("range", item.getKey());
//                        RangeTask rts = new RangeTask(csRange);
//                        try {
//                            Document doc = rts.execute(selectUrl).get();
//                            spinnerSelectedPos = -1;
//                            ArrayList<RangeItem> items = generateListForSpinner(doc);
//                            RangeSpinnerAdapter rangeAdapter = new RangeSpinnerAdapter(items);
//                            rangeDataSpinner.setAdapter(rangeAdapter);
//                            if(spinnerSelectedPos<items.size()){
//                                rangeDataSpinner.setSelection(spinnerSelectedPos);
//                            }
//
//
//                        } catch (ExecutionException | InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        break;

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

    public static ChooseSettings getScheduleSettings(SharedPreferences sp){
        ChooseSettings cs = new ChooseSettings();
        LinkedHashMap<String, String> datas = new LinkedHashMap<>();
        Set<String> keys = sp.getStringSet("datas", null);
        if(keys != null){
            for(String key : keys){
                String value = sp.getString(key, null);
                if(value != null){
                    datas.put(key, value);
                }
            }
        }
        if(!datas.isEmpty()){
            cs.setArgs(datas);
        }
        return cs;
    }
    private ArrayList<RangeItem> generateListForSpinner(Document doc){
        ArrayList<RangeItem> list = new ArrayList<>();
        Elements els = doc.select("option");
        int tmpInt = 0;
        for(Element el: els){

            if(el.attributes().hasKey("selected")){
                spinnerSelectedPos = tmpInt;
            }
            String semestr = "null";
            if(el.attributes().hasKey("data-semestr-rok-id")){
                semestr = el.attr("data-semestr-rok-id");

            }
            list.add(new RangeItem(el.attr("value"), semestr, el.text(), false));
            tmpInt++;
        }
        if(spinnerSelectedPos<list.size() && spinnerSelectedPos!=-1){
            list.get(spinnerSelectedPos).setSelected(true);

        }
        else{
            spinnerSelectedPos = 0;
        }
        return list;
    }
    private void generateSchedule(String rangeMode, String dateRange, String semester, boolean showDate){

        csMain.addArg("range", rangeMode);
        csMain.addArg("dataRange", dateRange);
        csMain.addArg("semestrRokId", semester);
        csMain.addArg("group", "null");
        ScheduleTask scheduleTask = new ScheduleTask(csMain);

        try {
            Document doc = scheduleTask.execute(searchUrl).get();
            if(selectedGroups.size()>0){
                courses = filtredCursesList(generateListOfCourses(doc));
            }
            else{
                courses = generateListOfCourses(doc);
            }


            ScheduleAdapterDay sad = new ScheduleAdapterDay(courses, showDate, this);

            scheduleListView.setAdapter(sad);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<CourseItem> generateListOfCourses(ChooseSettings cs, RangeItem range, String rangeType, String coursesUrl){
        cs.addArg("range", rangeType);
        cs.addArg("dataRange", range.getRange());
        cs.addArg("semestrRokId", range.getSemestrId());
        cs.addArg("group", "null");
        ScheduleTask st = new ScheduleTask(cs);
        try{
            Document doc = st.execute(coursesUrl).get();
            return generateListOfCourses(doc, rangeType);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static ArrayList<CourseItem> generateListOfCourses(Document document){
        return generateListOfCourses(document, range);
    }
    private static ArrayList<CourseItem> generateListOfCourses(Document document, String rangeType){

        ArrayList<CourseItem> result = new ArrayList<>();
        ArrayList<CourseItem> fTemp = new ArrayList<>();

        if(rangeType.equals("2")){
            Elements table = document.select("table.week-table");
            if(table.size()==1){
                Elements subjects = table.select("div.timetable-cell");
                for(Element subject: subjects){
                    Map<String, String> infos = new TreeMap<>();
                    for(Element info: subject.select("[class^='timetable']")){
                        Elements spans = info.select("span");
                        for(Element span: spans){
                            if(span.root() == document){
                                span.remove();
                            }
                        }
                        infos.put(info.className(), info.text());
                    }
                    Map<String, String> newInfos = new TreeMap<>();
                    for(Map.Entry<String, String> entry: infos.entrySet()){
                            switch(entry.getKey()){
                                case "timetable-hours-1st":
                                    String[] times = entry.getValue().split(" - ");
                                    if(times.length>1){
                                        newInfos.put("from",times[0]);
                                        newInfos.put("to", times[1]);
                                    }
                                    else{
                                        newInfos.put("from", "");
                                        newInfos.put("to", "");
                                    }
                                    break;
                                case "timetable-hours-2nd":
                                    String[] datesElements = entry.getValue().split(" ");
                                    if(datesElements.length>1){
                                        newInfos.put("dayOfWeek",datesElements[0]);
                                        newInfos.put("date", datesElements[1]);
                                    }
                                    else{
                                        newInfos.put("dayOfWeek", "");
                                        newInfos.put("date", "");
                                    }
                                    break;
                                default:
                            }
                    }
                    infos.putAll(newInfos);
                    CourseItem courseItem = new CourseItem(
                            infos.get("timetable-subject"),
                            infos.get("timetable-leader"),
                            infos.get("date"),
                            infos.get("dayOfWeek"),
                            infos.get("from"),
                            infos.get("to"),
                            infos.get("timetable-room"),
                            infos.get("timetable-form-class"),
                            infos.get("timetable-group")
                    );
                    result.add(courseItem);
                }
            }
            Comparator<CourseItem> compareByDate = new Comparator<CourseItem>() {
                @Override
                public int compare(CourseItem c1, CourseItem c2) {
                    return c1.getStartDate().compareTo(c2.getStartDate());
                }
            };
            Collections.sort(result, compareByDate);
        }
        else{
            Elements rows = document
                    .select("tbody").select("tr");
            //version 1.2
            for(Element row: rows){
                if(!row.hasClass("btn-tab")){
                    continue;
                }
                else{
                    Elements rowItems = row.select("td");
                    String courseForm = "";
                    if(rowItems.size()<8){
                        Log.e(TAG, "SIZE:"+rowItems.get(0).text()+" "+rowItems.get(1).text()+" "+rowItems.get(2).text());
                    }
                    if(rowItems.size()>8){
                        if(rowItems.get(8).hasClass("table-more")){
                            Element tableMore = rowItems.get(8);
                            Elements tableMoreElement = tableMore.select("div.table-more-box-item");
                            courseForm = tableMoreElement.get(0).html().split("</span> ")[1];
                        }

                    }
                    else{
                        int size = rowItems.size();
                        for(int i=0; i<9; i++){
                            if(i>=size){
                                rowItems.add(new Element("<td>Error</td>"));
                            }
                        }
                    }
                    CourseItem courseItem = new CourseItem(
                            rowItems.get(0).text(),
                            rowItems.get(1).text(),
                            rowItems.get(2).text(),
                            rowItems.get(3).text(),
                            rowItems.get(4).text(),
                            rowItems.get(5).text(),
                            rowItems.get(6).text(),
                            courseForm,
                            rowItems.get(7).text()
                    );
                    result.add(courseItem);
                }
            }
        }
        return result;

    }

    private void showPopup(View v){

        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.other_menu, popup.getMenu());
        popup.show();
    }
    public static ArrayList<RangeItem> generateRangeList(String url, ChooseSettings cs){
        return generateRangeList(url, cs, cs.getArg("range"));
    }
    public static ArrayList<RangeItem> generateRangeList(String url, ChooseSettings cs, String range){
        ArrayList<RangeItem> list = new ArrayList<>();
        switch (range){
            case "2":
                cs.addArg("action", "set_week");
                cs.addArg("range", range);
                break;

            case "3":
                cs.addArg("action", "set_semester");
                cs.addArg("range", range);
                break;
            default:
                return list;
        }


        RangeTask rts = new RangeTask(cs);
        try{
            Document doc = rts.execute(url).get();
            Elements els = doc.select("option");
            for(Element el: els){
                String semestr = "null";
                if(el.attributes().hasKey("data-semestr-rok-id")){
                    semestr = el.attr("data-semestr-rok-id");
                }
                list.add(new RangeItem(el.attr("value"), semestr, el.text(), el.attributes().hasKey("selected")));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return list;
    }


    private ArrayList<String> generateGroup(){


        return generateGroup(csRange, csMain, selectUrl, searchUrl);
    }
    public static ArrayList<String> generateGroup(ChooseSettings csOfRange, ChooseSettings csForSchedule, String coursesUrl, RangeItem range){
        ArrayList<String> groupsName = new ArrayList<>();
        csForSchedule.addArg("range", "3");
        csForSchedule.addArg("dataRange", range.getRange());
        csForSchedule.addArg("semestrRokId", range.getSemestrId());
        ScheduleTask et = new ScheduleTask(csForSchedule);
        try {
            Document doc = et.execute(coursesUrl).get();
            ArrayList<CourseItem> tempCourses = new ArrayList<>();
            tempCourses = generateListOfCourses(doc);

            if(tempCourses.size()>0){
                for(CourseItem ci: tempCourses){
                    if(!groupsName.contains(ci.getGroup())){
                        groupsName.add(ci.getGroup());

                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return groupsName;
    }
    public static ArrayList<String> generateGroup(ChooseSettings csOfRange, ChooseSettings csForSchedule, String rangeUrl, String coursesUrl){

        ArrayList<RangeItem> list = generateRangeList(rangeUrl, csOfRange, "3");

        ArrayList<String> groupsName = new ArrayList<>();

        for(RangeItem ri: list){

            groupsName.addAll(generateGroup(csOfRange, csForSchedule, coursesUrl, ri));
        }
        LinkedHashSet<String> tempSet = new LinkedHashSet<>();
        tempSet.addAll(groupsName);
        groupsName.clear();
        groupsName.addAll(tempSet);
        Collections.sort(groupsName);
        return groupsName;
    }



    private ArrayList<CourseItem> filtredCursesList(ArrayList<CourseItem> nonFiltredCourses){

        return filtredCoursesList(nonFiltredCourses, selectedGroups);
    }
    public static ArrayList<CourseItem> filtredCoursesList(ArrayList<CourseItem> nonFiltredCourses, ArrayList<String> groups){
        ArrayList<CourseItem> filtredCourses = new ArrayList<>();
        if(groups.size()>0){
            for(CourseItem ci: nonFiltredCourses){
                if(groups.contains(ci.getGroup())){
                    filtredCourses.add(ci);
                }
            }
            return filtredCourses;
        }
        else{
            return nonFiltredCourses;
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


}
