package pl.ozog.harmonogramup.enums;

import java.io.File;

public enum FilesNames {

    COURSES("courses.json"),
    GROUPS("groups.json"),
    WEEKS("weeks.json"),
    SEMESTERS("semesters.json");
    String fileName;
    FilesNames(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public File getFile(File dir){
        return new File(dir, fileName);
    }
}
