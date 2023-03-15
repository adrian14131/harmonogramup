package pl.ozog.harmonogramup;

import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;


public class ChooseSettings {
    LinkedHashMap<String, String> args;

    public ChooseSettings(){
        this.args = new LinkedHashMap<>();
    }

    public LinkedHashMap<String, String> getArgs() {
        return args;
    }

    public void setArgs(LinkedHashMap<String, String> args) {
        this.args = args;
    }

    public void addArg(String K, String V){
        args.put(K,V);

    }
    public void changeArg(String K, String V){
        args.replace(K, V);
    }

    public boolean isArg(String K){
        return args.containsKey(K);
    }

    public void removeLastArg(String K){
        args.remove(K);
    }





}
