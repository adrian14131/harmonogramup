package pl.ozog.harmonogramup;

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

//    String faculty;
//    String direction;
//    String semester;
//    String formOfStudy;
//    String degree;
//    String specialty;
//    String specialization;
    String group;

    public CourseItem() {
    }

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

    public Date getStartDate(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date result = format.parse(this.date+" "+this.fromTime);
            return result;
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

    public void setGroup(String group) {
        this.group = group;
    }

}
