package pl.ozog.harmonogramup.downloaders;

import android.os.AsyncTask;
import android.util.Log;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import pl.ozog.harmonogramup.ChooseSettings;

public class RangeTask extends AsyncTask<String, Void, Document> {

    private final ChooseSettings csRange;
    private static final String TAG = "RangeTask";
    public RangeTask(ChooseSettings csRange){
        this.csRange = csRange;
    }
    @Override
    protected Document doInBackground(String... strings) {

        Document doc = null;
        if(strings.length>0){
            try {
                Connection.Response response = Jsoup.connect(strings[0])
                        .method(Connection.Method.POST)
                        .data(csRange.getArgs())
                        .execute();

                doc = Jsoup.parse(response.body());
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
