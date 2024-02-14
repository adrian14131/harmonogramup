package pl.ozog.harmonogramup.generators;

import android.util.Range;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;

import pl.ozog.harmonogramup.ChooseSettings;
import pl.ozog.harmonogramup.enums.FilesNames;
import pl.ozog.harmonogramup.items.CourseItem;
import pl.ozog.harmonogramup.items.RangeItem;
import pl.ozog.harmonogramup.tools.FileTools;

public class ListJsonGenerators {



    public static ArrayList<String> loadGroupList(File dir, String fileName){
        return loadGroupList(new File(dir, fileName));
    }
    public static ArrayList<String> loadGroupList(File file){
        ArrayList<String> result = new ArrayList<>();
        LinkedHashSet<String> setSemesters = new LinkedHashSet<>();
        JSONObject groupsJson = FileTools.loadJsonFromFile(file);
        JSONArray semesters = groupsJson.optJSONArray("semesters");
        if(semesters != null){
            for(int i=0; i<semesters.length(); i++){
                JSONObject semester = semesters.optJSONObject(i);
                if(semester != null){
                    JSONArray groups = semester.optJSONArray("groups");
                    if(groups != null){
                        for(int j=0; j<groups.length(); j++){
                            String groupName = groups.optString(j);
                            if(groupName != null){
                                setSemesters.add(groupName);
                            }
                        }
                    }
                }
            }
        }
        result.addAll(setSemesters);
        Collections.sort(result);
        return result;
    }
    public static ArrayList<RangeItem> loadRangeList(File dir, ChooseSettings chooseSettings){
        switch (chooseSettings.getArg("range")){
            case "2":
                return loadRangeList(new File(dir, FilesNames.WEEKS.getFileName()), "weeks");
            case "3":
                return loadRangeList(new File(dir, FilesNames.SEMESTERS.getFileName()), "semesters");
            default:
                return new ArrayList<>();
        }
    }
    public static ArrayList<RangeItem> loadRangeList(File dir, String fileName, String arrayName){
        return loadRangeList(new File(dir, fileName), arrayName);
    }
    public static ArrayList<RangeItem> loadRangeList(File file, String arrayName){
        ArrayList<RangeItem> result = new ArrayList<>();

        JSONObject rangesJson = FileTools.loadJsonFromFile(file);
        JSONArray rangeArray = rangesJson.optJSONArray(arrayName);
        if(rangeArray != null){
            result = new Gson().fromJson(rangeArray.toString(), new TypeToken<ArrayList<RangeItem>>() {}.getType());
        }

        return result;
    }

    public static ArrayList<CourseItem> loadCourseList(File file){

        JSONObject coursesJson = FileTools.loadJsonFromFile(file);
        JSONArray semesterArray = coursesJson.optJSONArray("semesters");
        if(semesterArray != null){
            return loadCourseFromSemester(semesterArray, null);
        }
        return new ArrayList<>();
    }
    private static ArrayList<CourseItem> loadCourseFromSemester(JSONArray semesterArray, String semesterId){
        ArrayList<CourseItem> result = new ArrayList<>();
        for(int i=0; i<semesterArray.length(); i++){
            JSONObject semester = semesterArray.optJSONObject(i);
            if(semester != null){
                String id = semester.optString("range");
                if(semesterId == null){
                    JSONArray coursesArray = semester.optJSONArray("courses");
                    if(coursesArray != null){
                        result.addAll(new Gson().fromJson(coursesArray.toString(), new TypeToken<ArrayList<CourseItem>>(){}.getType()));
                    }
                    continue;
                }
                if(semesterId.equals(id)){
                    JSONArray coursesArray = semester.optJSONArray("courses");
                    if(coursesArray != null){
                        result.addAll(new Gson().fromJson(coursesArray.toString(), new TypeToken<ArrayList<CourseItem>>(){}.getType()));
                    }
                }
            }
        }
        return result;
    }
    public static ArrayList<CourseItem> loadCourseFromSemester(File file, String semesterId){
        JSONObject coursesJson = FileTools.loadJsonFromFile(file);
        JSONArray semesterArray = coursesJson.optJSONArray("semesters");
        if(semesterArray != null){
            return loadCourseFromSemester(semesterArray, semesterId);
        }
        return new ArrayList<>();
    }
    public static ArrayList<CourseItem> loadCourseList(File file, ChooseSettings chooseSettings, ArrayList<RangeItem> rangeItems, SimpleDateFormat sdf){

//        ArrayList<CourseItem> loadedCourses = new ArrayList<>();

//        JSONArray rangeArray

        switch (chooseSettings.getArg("range")){

            case "1":
                return filterCourseByDay(loadCourseList(file), chooseSettings.getArg("dateRange"));
            case "2":
                return filterCourseByWeek(loadCourseFromSemester(file, chooseSettings.getArg("semestrRokId")),obtainWeek(rangeItems, chooseSettings), sdf);
            case "3":
                return loadCourseFromSemester(file, chooseSettings.getArg("dataRange"));
            default:
                return new ArrayList<>();

        }
    }
    public static ArrayList<CourseItem> filterCourseByDay(ArrayList<CourseItem> courseItems, String day){
        ArrayList<CourseItem> result = new ArrayList<>();
        for(CourseItem ci: courseItems){
            if(ci.getDate().equals(day)){
                result.add(ci);
            }
        }
        return result;
    }
    private static RangeItem obtainWeek(ArrayList<RangeItem> rangeItems, ChooseSettings chooseSettings){
        for(RangeItem rangeItem: rangeItems){
            if(rangeItem.getRange().equals(chooseSettings.getArg("dataRange")) && rangeItem.getSemestrId().equals(chooseSettings.getArg("semestrRokId"))){
                return rangeItem;
            }
        }
        return null;
    }
    public static ArrayList<CourseItem> filterCourseByWeek(ArrayList<CourseItem> courseItems, RangeItem week, SimpleDateFormat sdf){

        ArrayList<CourseItem> result = new ArrayList<>();
        String[] weekDates = week.getText().replace('.','-').split(" - ");
        if(weekDates.length >= 2){
            try {
                Date begin = sdf.parse(weekDates[0]+" 00:00:00");
                Date end = sdf.parse(weekDates[1]+" 23:59:59");
                for(CourseItem courseItem: courseItems){
                    Date courseDate = sdf.parse(courseItem.getDate()+" 12:00:00");
                    if(courseDate.getTime()>begin.getTime() && courseDate.getTime()< end.getTime()){
                        result.add(courseItem);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        return result;
    }




}
