package pl.ozog.harmonogramup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    ActionBar actionBar;
    LinkedHashMap<String, String> datas;
    LinkedHashMap<String, String> typeOfRange;
    ArrayList<String> groups;
    ArrayList<String> selectedGroups;
    ArrayList<RangeItem> rangeDatas;

    String url = "https://harmonogram.up.krakow.pl/inc/functions/a_search.php";
    //String url = "https://harmonogram.up.krakow.pl/inc/functions/a_search.php";

    Spinner torSpinner, rangeDataSpinner;
    MapSpinnerAdapter spinnerAdapter;
    Button prevButton, nextButton, changeButton, groupButton;
    EditText dateInput;
    ListView scheduleListView;
    ListView dialogGroupListView;
    Button dialogGroupOkButton;
    ArrayList<CourseItem> courses;
    static ChooseSettings cs, csRange;
    Calendar myCalendar;
    Date today;
    String range = "1";
    int spinnerSelectedPos = -1;
    public static final int GET_VALUE_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        actionBar.hide();
        datas = new LinkedHashMap<>();
        typeOfRange = new LinkedHashMap<>();
        typeOfRange.put("1", "Dzień");
        typeOfRange.put("2", "Tydzień");
        typeOfRange.put("3", "Semestr");
        myCalendar = Calendar.getInstance();
        today = Calendar.getInstance().getTime();
        cs = new ChooseSettings();
        csRange = new ChooseSettings();
//        ScheduleTaskTest stt = new ScheduleTaskTest();
//        try {
//            Document docTest = stt.execute(url).get();
//            docTest.body();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        SharedPreferences sharedPreferences = getSharedPreferences("schedule", Context.MODE_PRIVATE);
        if(sharedPreferences.getAll().size()>0){
            Set<String> keys = sharedPreferences.getStringSet("datas", null);
            if(keys != null){
                for(String key:keys){
                    String value = sharedPreferences.getString(key, null);
                    if(value != null){
                        datas.put(key, value);
                    }
                }
            }
        }
        cs.setArgs(datas);
        Log.e("SUM", "onCreate: "+datas.toString());
        groups = generateGroup();
        Set<String> selGr = sharedPreferences.getStringSet("selectedGroups", null);
        if(selGr == null){
            Set<String> sets = new HashSet<String>();
            sets.addAll(groups);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet("selectedGroups", sets);
            Log.e("TAG", "filtredCursesList: not null");
            selectedGroups = groups;
        }
        else{
            Log.e("TAG", "filtredCursesList: null"+selGr.size());
            selectedGroups = new ArrayList<>();
            selectedGroups.addAll(selGr);
        }



        prevButton = findViewById(R.id.prevRangeButton);
        prevButton.setOnClickListener(this);

        nextButton = findViewById(R.id.nextRangeButton);
        nextButton.setOnClickListener(this);

        changeButton = findViewById(R.id.changeDirectionButton);
        changeButton.setOnClickListener(this);

        groupButton = findViewById(R.id.changeGroupButton);
        groupButton.setOnClickListener(this);
        dateInput = findViewById(R.id.dateInput);
        setUpLabel();

        rangeDataSpinner = findViewById(R.id.dataRangeSpinner);

        torSpinner = findViewById(R.id.typeOfRange);
        spinnerAdapter = new MapSpinnerAdapter(typeOfRange);
        torSpinner.setAdapter(spinnerAdapter);
        torSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Map.Entry<String, String> item = (Map.Entry) adapterView.getItemAtPosition(i);
                range = item.getKey();
                switch (item.getKey()){
                    case "1":
                        dateInput.setVisibility(View.VISIBLE);
                        rangeDataSpinner.setVisibility(View.GONE);

                        setUpLabel();
                        String format = "yyyy-MM-dd";
                        SimpleDateFormat sdf = new SimpleDateFormat(format, new Locale("pl", "PL"));
                        generateSchedule("1", sdf.format(today), "null", false);
                        myCalendar.setTime(today);
                        break;
                    case "2":
                        dateInput.setVisibility(View.GONE);
                        rangeDataSpinner.setVisibility(View.VISIBLE);
                        csRange.addArg("action", "set_week");
                        csRange.addArg("range", item.getKey());
                        RangeTask rt = new RangeTask();
                        try {
                            Document doc = rt.execute("https://harmonogram.up.krakow.pl/inc/functions/a_select.php").get();
                            spinnerSelectedPos = -1;
                            ArrayList<RangeItem> items = generateListForSpinner(doc);
                            RangeSpinnerAdapter rangeAdapter = new RangeSpinnerAdapter(items);
//                            Log.e("spin", "onItemSelected: "+doc.body() );
                            rangeDataSpinner.setAdapter(rangeAdapter);
                            if(spinnerSelectedPos<items.size()){
                                rangeDataSpinner.setSelection(spinnerSelectedPos);
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "3":
                        dateInput.setVisibility(View.GONE);
                        rangeDataSpinner.setVisibility(View.VISIBLE);
                        csRange.addArg("action", "set_semester");
                        csRange.addArg("range", item.getKey());
                        RangeTask rts = new RangeTask();
                        try {
                            Document doc = rts.execute("https://harmonogram.up.krakow.pl/inc/functions/a_select.php").get();
                            spinnerSelectedPos = -1;
                            ArrayList<RangeItem> items = generateListForSpinner(doc);
                            RangeSpinnerAdapter rangeAdapter = new RangeSpinnerAdapter(items);
                            Log.e("spin", "onItemSelected: "+doc.body() );
                            rangeDataSpinner.setAdapter(rangeAdapter);
                            if(spinnerSelectedPos<items.size()){
                                rangeDataSpinner.setSelection(spinnerSelectedPos);
                            }


                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
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
        scheduleListView = findViewById(R.id.scheduleListView);

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
//                Map.Entry<String, String> item = (Map.Entry)torSpinner.getSelectedItem();
//
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
        SimpleDateFormat sdf = new SimpleDateFormat(format, new Locale("pl", "PL"));
        generateSchedule("1", sdf.format(today), "null", false);
    }

    private void setUpLabel(){
        String format = "yyyy-MM-dd EEEE";
        SimpleDateFormat sdf = new SimpleDateFormat(format, new Locale("pl", "PL"));
        dateInput.setText(sdf.format(today));
    }
    private void updateLabel(){
        String format = "yyyy-MM-dd EEEE";
        SimpleDateFormat sdf = new SimpleDateFormat(format, new Locale("pl", "PL"));
        dateInput.setText(sdf.format(myCalendar.getTime()));
        format = "yyyy-MM-dd";
        sdf = new SimpleDateFormat(format, new Locale("pl", "PL"));

        generateSchedule(((Map.Entry<String, String>)torSpinner.getSelectedItem()).getKey(), sdf.format(myCalendar.getTime()), "null", false);
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
    private void generateSchedule(String rangeMode, String dateRange, String semestr, boolean showData){
        cs.addArg("range", rangeMode);
        cs.addArg("dataRange", dateRange);
        cs.addArg("semestrRokId", semestr);
        cs.addArg("group", "null");
        ScheduleTask et = new ScheduleTask();

        try {
            Document doc = et.execute(url).get();
            if(selectedGroups.size()>0){
                courses = filtredCursesList(generateListOfCourses(doc));
            }
            else{
                courses = generateListOfCourses(doc);
            }


            ScheduleAdapterDay sad = new ScheduleAdapterDay(courses, showData);

            scheduleListView.setAdapter(sad);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private ArrayList<CourseItem> generateListOfCourses(Document document){
        ArrayList<CourseItem> temp = new ArrayList<>();
        ArrayList<CourseItem> fTemp = new ArrayList<>();


//        Iterator<Element> elementIterator = rows.iterator();
        if(range.equals("2")){
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
                    temp.add(courseItem);
                }
            }

            Comparator<CourseItem> compareByDate = new Comparator<CourseItem>() {
                @Override
                public int compare(CourseItem c1, CourseItem c2) {
                    return c1.getStartDate().compareTo(c2.getStartDate());
                }
            };
            Collections.sort(temp, compareByDate);
        }
        else{
            Elements rows = document
                    .select("tbody").select("tr");
//        Log.e("RANGE", "range: "+range);
            //version 1.2
            for(Element row: rows){
                if(!row.hasClass("btn-tab")){
                    continue;
                }
                else{
                    Elements rowItems = row.select("td");
                    String courseForm = "";
                    if(rowItems.size()<8){
                        Log.e("SIZE", rowItems.get(0).text()+" "+rowItems.get(1).text()+" "+rowItems.get(2).text());
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
                    temp.add(courseItem);
                }
            }
        }

//        while(elementIterator.hasNext()){
//            Element row = elementIterator.next();
//            if(!row.hasClass("btn-tab")){
//                continue;
//            }
//            else{
//                Elements rowItems = row.select("td");
//                String courseForm = "";
//                if(rowItems.size()>8){
//                    if(rowItems.get(8).hasClass("table-more")){
//                        Element tableMore = rowItems.get(8);
//                        Elements tableMoreElement = tableMore.select("div.table-more-box-item");
//                        courseForm = tableMoreElement.get(0).html().split("</span> ")[1];
//                    }
//
//                }
//                if(rowItems.size()==3){
//                    Log.e("SIZE", rowItems.get(0).text()+" "+rowItems.get(1).text()+" "+rowItems.get(2).text());
//                }
//                CourseItem courseItem = new CourseItem(
//                        rowItems.get(0).text(),
//                        rowItems.get(1).text(),
//                        rowItems.get(2).text(),
//                        rowItems.get(3).text(),
//                        rowItems.get(4).text(),
//                        rowItems.get(5).text(),
//                        rowItems.get(6).text(),
//                        courseForm,
//                        rowItems.get(7).text()
//                );
//                temp.add(courseItem);
//            }
//        }
        //version 1.1
//        while(elementIterator.hasNext()){
//            Element firstRow = elementIterator.next();
//            if(!firstRow.hasClass("btn-tab")){
//                continue;
//            }
//            else{
//                Elements rowItemsFirst = firstRow.select("td");
//
//                Element secondRow = null;
//                if(elementIterator.hasNext()){
//                    secondRow = elementIterator.next();
//                    if(!secondRow.hasClass("hidden-row")){
//
//                        continue;
//                    }
//                    else{
//
//                        Elements rowItemsSecond = secondRow.select("div.hidden-row-box-item");\
//
//
//                        CourseItem courseItem = new CourseItem(
//                                rowItemsFirst.get(0).text(),
//                                rowItemsFirst.get(1).text(),
//                                rowItemsFirst.get(2).text(),
//                                rowItemsFirst.get(3).text(),
//                                rowItemsFirst.get(4).text(),
//                                rowItemsFirst.get(5).text(),
//                                rowItemsFirst.get(6).text(),
//                                rowItemsSecond.get(0).html().split("</span> ")[1],
//                                rowItemsFirst.get(7).text()
//                        );
//                        temp.add(courseItem);
//                    }
//                }
//            }
//
//        }
        //version 1.0
//        for(Element row:rows){
//
//            Elements rowItems = row.select("td");
//            if(rowItems.hasClass("btn-tab")){
//
//            }
//            CourseItem courseItem = new CourseItem(rowItems.get(0).text(),
//                    rowItems.get(1).text(),
//                    rowItems.get(2).text(),
//                    rowItems.get(3).text(),
//                    rowItems.get(4).text(),
//                    rowItems.get(5).text(),
//                    rowItems.get(6).text(),
//                    rowItems.get(7).text(),
//                    rowItems.get(15).text());
//
//            temp.add(courseItem);
//        }

        return temp;

    }

    private ArrayList<String> generateGroup(){

        ArrayList<RangeItem> list = new ArrayList<>();
        csRange.addArg("action", "set_semester");
        csRange.addArg("range", "3");
        RangeTask rts = new RangeTask();
        try {
            Document doc = rts.execute("https://harmonogram.up.krakow.pl/inc/functions/a_select.php").get();
            Elements els = doc.select("option");
            for(Element el: els){
                String semestr = "null";
                if(el.attributes().hasKey("data-semestr-rok-id")){
                    semestr = el.attr("data-semestr-rok-id");

                }
                list.add(new RangeItem(el.attr("value"), semestr, el.text(), false));

            }


        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<String> groupsName = new ArrayList<>();

        for(RangeItem ri: list){
            cs.addArg("range", "3");
            cs.addArg("dataRange", ri.getRange());
            cs.addArg("semestrRokId", ri.getSemestrId());

            ScheduleTask et = new ScheduleTask();
            try {
                Document doc = et.execute(url).get();
                ArrayList<CourseItem> tempCourses = new ArrayList<>();
                tempCourses = generateListOfCourses(doc);
                if(tempCourses.size()>0){
                    for(CourseItem ci: tempCourses){
                        if(!groupsName.contains(ci.getGroup())){
                            groupsName.add(ci.getGroup());

                        }
                    }
                }
                else{
                    continue;
                }


            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(groupsName);





        return groupsName;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.prevRangeButton:
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
            case R.id.nextRangeButton:
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
            case R.id.changeDirectionButton:
                Intent intent = new Intent(view.getContext(), FirstSettings.class);
                intent.putExtra("isMain", true);
                startActivity(intent);
                break;
            case R.id.changeGroupButton:
                showGroupDialog(groups);
//                DialogFragment df = new ChooseGroup();
//                df.show(getSupportFragmentManager(),"test");
        }
    }
    private ArrayList<CourseItem> filtredCursesList(ArrayList<CourseItem> nonFiltredCourses){
        ArrayList<CourseItem> filtredCourses = new ArrayList<>();
//        Log.e("TAG", "filtredCursesList: filter not null");
//        Log.e("TAG", "filtredCursesList: "+selectedGroups.size());
        if(selectedGroups.size()>0){
            for(CourseItem ci: nonFiltredCourses){
                if(selectedGroups.contains(ci.getGroup())){
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
        alertDialogBuilder.setTitle("Wybierz grupy");
        alertDialogBuilder.setPositiveButton("Zatwierdź", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SparseBooleanArray checked = dialogGroupListView.getCheckedItemPositions();
                Log.e("TAG", "onClickt: "+checked.size() );
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
                Log.e("TAG", "onClick: "+range);
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
    public static class ScheduleTask extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String...strings) {
            Document doc = null;
            if(strings.length>0){
                try {
                    Connection.Response response = Jsoup.connect(strings[0])
                            .method(Connection.Method.POST)
                            .data(cs.getArgs())
                            .maxBodySize(1024*1024*10)
                            .execute();

                    String table = "<table>"+response.body()+"</table>";
                    double size = response.bodyAsBytes().length/(1024.0*1024.0);
                    Log.i("DOC SIZE", "Size: "+size);
                    doc = Jsoup.parse(table);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("entry", "doNotInBackground: "+e);
                }
            }
            if(doc==null){
                doc = Jsoup.parse("<h1></h1>");
            }
            return doc;
        }
    }
    public static class ScheduleTaskTest extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String...strings) {
            Document doc = null;
            if(strings.length>0){
                Map<String, String> mapa = new TreeMap<>();
                mapa.put("specialty", "null");
                mapa.put("form", "1");
                mapa.put("year", "37");
                mapa.put("faculity", "33");
                mapa.put("deegre", "1");
                mapa.put("action", "search");
                mapa.put("specialization", "null");
                mapa.put("direction", "1210");
                mapa.put("range", "3");
                mapa.put("dataRange", "8");
                try {
                    Connection.Response response = Jsoup.connect(strings[0])
                            .method(Connection.Method.POST)
                            .data(mapa)
                            .execute();

                    String table = "<table>"+response.body()+"</table>";
                    doc = Jsoup.parse(table);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("entry", "doNotInBackground: "+e);
                }
            }
            if(doc==null){
                doc = Jsoup.parse("<h1></h1>");
            }
            return doc;
        }
    }
    public static class RangeTask extends AsyncTask<String, Void, Document>{

        @Override
        protected Document doInBackground(String... strings) {
            Document doc = null;
            if(strings.length>0){

                try {
                    Connection.Response response = Jsoup.connect(strings[0])
                            .method(Connection.Method.POST)
                            .data(csRange.getArgs())
                            .execute();

                    doc = Jsoup.parse(response.body());
                    Log.e("spin", "onItemSelected: is");
                } catch (IOException e) {
                    Log.e("spin", "onItemSelected: is"+e);
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
