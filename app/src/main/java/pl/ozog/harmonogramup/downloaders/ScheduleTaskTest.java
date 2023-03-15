package pl.ozog.harmonogramup.downloaders;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class ScheduleTaskTest extends AsyncTask<String, Void, Document> {
    private static final String TAG = "ScheduleTaskTest";
    @Override
    protected Document doInBackground(String...strings) {
        Document doc = null;
        if(strings.length>0){
            Map<String, String> mapa = new TreeMap<>();
            mapa.put("specialty", "null");
            mapa.put("form", "1");
            mapa.put("year", "37");
            mapa.put("faculity", "33");
            mapa.put("deegre", "1");
            mapa.put("action", "search");
            mapa.put("specialization", "null");
            mapa.put("direction", "1210");
            mapa.put("range", "3");
            mapa.put("dataRange", "8");
            try {
                Connection.Response response = Jsoup.connect(strings[0])
                        .method(Connection.Method.POST)
                        .data(mapa)
                        .execute();

                String table = "<table>"+response.body()+"</table>";
                doc = Jsoup.parse(table);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        if(doc==null){
            doc = Jsoup.parse("<h1></h1>");
        }
        return doc;
    }
}
