package pl.ozog.harmonogramup;

import android.content.SharedPreferences;

import java.util.LinkedHashMap;
import java.util.Set;

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
    public String getArg(String K){
        return args.get(K);
    }
    public void changeArg(String K, String V){
        if(args.containsKey(K))
            args.replace(K, V);
        else
            addArg(K, V);
    }

    public boolean isArg(String K){
        return args.containsKey(K);
    }

    public void removeLastArg(String K){
        args.remove(K);
    }

    public void setArgs(SharedPreferences sp){
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
            setArgs(datas);
        }
    }





}
