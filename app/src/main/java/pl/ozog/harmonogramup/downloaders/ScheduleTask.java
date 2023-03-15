package pl.ozog.harmonogramup.downloaders;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import pl.ozog.harmonogramup.ChooseSettings;

public class ScheduleTask extends AsyncTask<String, Void, Document> {

    private final ChooseSettings cs;
    private static final String TAG = "ScheduleTask";
    public ScheduleTask(ChooseSettings cs){
        this.cs = cs;
    }
    @Override
    protected Document doInBackground(String...strings) {

        Document doc = null;
        if(strings.length>0){
            try {
                Connection.Response response = Jsoup.connect(strings[0])
                        .method(Connection.Method.POST)
                        .data(cs.getArgs())
                        .maxBodySize(1024*1024*10)
                        .execute();
                String table = "<table>"+response.body()+"</table>";
                double size = response.bodyAsBytes().length/(1024.0*1024.0);
                Log.i("DOC SIZE", "Size: "+size);
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
