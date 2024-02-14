package pl.ozog.harmonogramup.items;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CourseItem {

    String name;
    String teacher;
    String date;
    String dayOfWeek;
    String fromTime;
    String toTime;
    String classRoom;
    String formOfCourse;
    String group;
    String semesterId;

    public CourseItem(String name, String teacher, String date, String dayOfWeek, String fromTime, String toTime, String classRoom, String formOfCourse, String group) {
        this.name = name;
        this.teacher = teacher;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.classRoom = classRoom;
        this.formOfCourse = formOfCourse;
        this.group = group;
    }
    public CourseItem(Elements cells, String courseForm){
        this(cells.get(0).text(),
                cells.get(1).text(),
                cells.get(2).text(),
                cells.get(3).text(),
                cells.get(4).text(),
                cells.get(5).text(),
                cells.get(6).text(),
                courseForm,
                cells.get(7).text());
    }
    public CourseItem(String name, String teacher, String date, String dayOfWeek, String fromTime, String toTime, String classRoom,String formOfCourse, String group, String semesterId){
        this.name = name;
        this.teacher = teacher;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.classRoom = classRoom;
        this.formOfCourse = formOfCourse;
        this.group = group;
        this.semesterId = semesterId;
    }

    public Date getStartDate(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
//            Date result = format.parse(this.date+" "+this.fromTime);
            return format.parse(this.date+" "+this.fromTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public String getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(String classRoom) {
        this.classRoom = classRoom;
    }

    public String getFormOfCourse() {
        return formOfCourse;
    }

    public void setFormOfCourse(String formOfCourse) {
        this.formOfCourse = formOfCourse;
    }

    public String getGroup() {
        return group;
    }

    public String getSemesterId()
    {
        return semesterId;
    }
    public void setSemesterId(String semesterId)
    {
        this.semesterId = semesterId;
    }

    @Override
    public String toString() {
        return "CourseItem{" +
                "name='" + name + '\'' +
                ", teacher='" + teacher + '\'' +
                ", date='" + date + '\'' +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", fromTime='" + fromTime + '\'' +
                ", toTime='" + toTime + '\'' +
                ", classRoom='" + classRoom + '\'' +
                ", formOfCourse='" + formOfCourse + '\'' +
                ", group='" + group + '\'' +
                ", semesterId='" + semesterId + '\'' +
                '}';
    }
}
