package pl.ozog.harmonogramup;

import android.util.Log;

import androidx.fragment.app.Fragment;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public abstract class FirstSettingsFragment extends Fragment {


    protected static LinkedHashMap<String, String> getMapFromElement(Document doc){
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        Elements els = doc.select("option");
        for(Element el: els){
            if(!el.hasAttr("disabled")){
                result.put(el.attr("value"), el.text());
            }
        }

        return result;
    }

    public abstract void generateList(boolean isNext);
    public abstract void setInfo(String msg, ArrayList<String> prevInfos);
    public abstract void removeLastInfo();
    public abstract ArrayList<String> getInfos();
    public abstract void setTitle(String text);
    public abstract void setAction(String action);
    public abstract void setData(String data);
    public abstract String getData();
    public abstract boolean canSkip();
    public abstract String getInfo();

}
