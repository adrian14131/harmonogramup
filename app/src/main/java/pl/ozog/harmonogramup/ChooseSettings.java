package pl.ozog.harmonogramup;

import java.util.LinkedHashMap;

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
        args.replace(K, V);
    }

    public boolean isArg(String K){
        return args.containsKey(K);
    }

    public void removeLastArg(String K){
        args.remove(K);
    }





}
