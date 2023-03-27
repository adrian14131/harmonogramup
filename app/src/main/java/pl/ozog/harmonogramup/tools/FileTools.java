package pl.ozog.harmonogramup.tools;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class FileTools {

    private static final String TAG = "FileTools";

    public static boolean compareFileToJson(File dir, String fileName, JSONObject toCompare){

        File file = new File(dir, fileName);

        boolean isEqual = compareFileToJson(file, toCompare);
        Log.e(TAG, "compareFileToJson: for file: "+fileName+" is equal: "+isEqual);
        return isEqual;
    }
    public static boolean compareFileToJson(File file, JSONObject toCompare){
        if(!file.exists()){
            return false;
        }
        JSONObject fromFile = loadJsonFromFile(file);
        if(fromFile == null){
            return false;
        }
        return fromFile.toString().equals(toCompare.toString());
    }
    public static JSONObject loadJsonFromFile(File dir, String fileName){

        File file = new File(dir, fileName);
        return loadJsonFromFile(file);
    }
    public static JSONObject loadJsonFromFile(File file){
        if(!file.exists())
            return null;
        JSONObject result = null;
        try {
            result = new JSONObject(streamToString(new FileInputStream(file)));
        } catch (JSONException | UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
    public static File saveJsonToFile(File dir, String fileName, JSONObject jsonObject){

        File file = new File(dir, fileName);
        return saveJsonToFile(file, jsonObject);
    }
    public static File saveJsonToFile(File file, JSONObject jsonObject){
        try {
            if(!file.exists()){

                file.createNewFile();

            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(jsonObject.toString(2));
            fileWriter.flush();
        } catch (IOException | JSONException e) {

            e.printStackTrace();
            return null;
        }
        return file;
    }
    private static String streamToString(InputStream is) throws UnsupportedEncodingException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        try{
            while ((line = reader.readLine()) != null){
                sb.append(line + "\n");

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                is.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
