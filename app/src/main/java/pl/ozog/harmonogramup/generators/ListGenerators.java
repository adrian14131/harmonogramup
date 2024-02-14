package pl.ozog.harmonogramup.generators;

import android.util.Range;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import pl.ozog.harmonogramup.ChooseSettings;
import pl.ozog.harmonogramup.comparators.CourseDateComparator;
import pl.ozog.harmonogramup.downloaders.RangeTask;
import pl.ozog.harmonogramup.downloaders.ScheduleTask;
import pl.ozog.harmonogramup.items.CourseItem;
import pl.ozog.harmonogramup.items.RangeItem;

public class ListGenerators {

    //from html document

    public static ArrayList<CourseItem> generateListOfCourses(ChooseSettings cs, String rangeMode, RangeItem rangeItem, String courseUrl){
        return generateListOfCourses(cs, rangeMode, rangeItem.getRange(), rangeItem.getSemestrId(), courseUrl);
    }
    public static ArrayList<CourseItem> generateListOfCourses(ChooseSettings cs, String rangeMode, String range, String semesterId, String coursesUrl){

        cs.addArg("range", rangeMode);
        cs.addArg("dataRange", range);
        cs.addArg("semestrRokId", semesterId);
        cs.addArg("group", "null");

        ScheduleTask st = new ScheduleTask(cs);
        try {
            Document document = st.execute(coursesUrl).get();
            return generateListOfCourses(document, rangeMode);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static ArrayList<CourseItem> generateListOfCourses(Document document, String rangeType){
        ArrayList<CourseItem> result;

        if(rangeType.equals("2"))
            result = generateListOfWeekCourses(document);
        else
            result = generateListOfCourses(document);

        return result;
    }
    private static ArrayList<CourseItem> generateListOfWeekCourses(Document document){
        ArrayList<CourseItem> result = new ArrayList<>();

        Elements table = document.select("table.week-table");
        if(table.size()==1){

            Elements courses = table.select("div.timetable-cell");
            for(Element course: courses){

                Map<String, String> infos = new TreeMap<>();
                for(Element info: course.select("[class^='timetable']")){

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

        Collections.sort(result, new CourseDateComparator());
        return result;
    }
    private static ArrayList<CourseItem> generateListOfCourses(Document document){
        ArrayList<CourseItem> result = new ArrayList<>();

        Elements rows = document.select("tbody").select("tr");
        for(Element row: rows){

            if(!row.hasClass("btn-tab")){
                continue;
            }
            else{

                Elements cells = row.select("td");
                String courseForm = "";
//                if(cells.size()<8){
//
//                }
                if(cells.size()>8){

                    if(cells.get(8).hasClass("table-more")){

                        Element tableMore = cells.get(8);
                        Elements tableMoreElements = tableMore.select("div.table-more-box-item");
                        courseForm = tableMoreElements.get(0).html().split("</span>")[1];
                    }
                }
                else{

                    int size = cells.size();
                    for(int i=0; i<9; i++){
                        if(i>=size){
                            cells.add(new Element("<td>Error</td>"));
                        }
                    }
                }
                CourseItem courseItem = new CourseItem(cells, courseForm);
                result.add(courseItem);
            }
        }
        return result;
    }
    public static ArrayList<CourseItem> filterCourseListByGroups(ArrayList<CourseItem> courses, ArrayList<String> groups){
        ArrayList<CourseItem> result = new ArrayList<>();
        //use strem filter
        if(groups.size() > 0){
            for(CourseItem ci: courses){
                if(groups.contains(ci.getGroup())){
                    result.add(ci);
                }
            }
            return result;
        }
        else
            return courses;
    }

    public static ArrayList<RangeItem> generateRangeList(ChooseSettings cs, String url){
        return generateRangeList(cs, cs.getArg("range"), url);
    }
    public static ArrayList<RangeItem> generateRangeList(ChooseSettings cs, String range, String url){

        ArrayList<RangeItem> result  = new ArrayList<>();

        switch(range){
            case "2":
                cs.addArg("action", "set_week");
                cs.addArg("range", range);
                break;

            case "3":
                cs.addArg("action", "set_semester");
                cs.addArg("range", range);
                break;
            default:
                return result;
        }

        RangeTask rts = new RangeTask(cs);
        try {
            Document document = rts.execute(url).get();
            Elements options = document.select("option");
            for(Element option : options){
                String semester = "null";
                if(option.attributes().hasKey("data-semestr-rok-id")){
                    semester = option.attr("data-semestr-rok-id");
                }
                result.add(new RangeItem(option.attr("value"), semester, option.text(), option.attributes().hasKey("selected")));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ArrayList<String> generateGroupList(ChooseSettings csOfRange, RangeItem rangeItem, ChooseSettings csOfSchedule, String coursesUrl){
        ArrayList<String> result = new ArrayList<>();
        csOfSchedule.addArg("range", "3");
        csOfSchedule.addArg("dataRange", rangeItem.getRange());
        csOfSchedule.addArg("semestrRokId", rangeItem.getSemestrId());

        ScheduleTask st = new ScheduleTask(csOfSchedule);

        try {
            Document document = st.execute(coursesUrl).get();
            ArrayList<CourseItem> courses = generateListOfCourses(document, "3");

            if(courses.size()>0){
                for(CourseItem ci: courses){
                    if(!result.contains(ci.getGroup())){
                        result.add(ci.getGroup());
                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
    public static ArrayList<String> generateGroupList(ChooseSettings csOfRange, String rangeUrl, ChooseSettings csOfSchedule, String coursesUrl){

        ArrayList<RangeItem> rangeList = generateRangeList(csOfRange, "3", rangeUrl);
        LinkedHashSet<String> tempset = new LinkedHashSet<>();
        ArrayList<String> result = new ArrayList<>();

        for(RangeItem ri: rangeList){
            tempset.addAll(generateGroupList(csOfRange, ri, csOfSchedule, coursesUrl));
        }
        result.addAll(tempset);
        Collections.sort(result);
        return result;

    }
}
