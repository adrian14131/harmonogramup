package pl.ozog.harmonogramup.comparators;

import java.util.Comparator;

import pl.ozog.harmonogramup.items.CourseItem;

public class CourseDateComparator implements Comparator<CourseItem> {
    @Override
    public int compare(CourseItem c1, CourseItem c2) {
        return c1.getStartDate().compareTo(c2.getStartDate());
    }
}
