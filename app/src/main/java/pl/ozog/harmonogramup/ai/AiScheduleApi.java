package pl.ozog.harmonogramup.ai;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.theokanning.openai.service.OpenAiService;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.ozog.harmonogramup.ChooseFirstFragment;
import pl.ozog.harmonogramup.ChooseSettings;
import pl.ozog.harmonogramup.FirstSettings;
import pl.ozog.harmonogramup.FirstSettingsFragment;
import pl.ozog.harmonogramup.downloaders.GptApiTask;

public class AiScheduleApi{

    //data
    String url;
    String functionUrl;
    ArrayList<String> actions;
    ArrayList<String> datas;
    ArrayList<String> names;
    ChooseSettings choices;


    AiScheduleApiCallback callback;

    OpenAiService aiService;

    AiChoiceProccess aiChoiceProccess;
    public AiScheduleApi(String url, String functionUrl, ArrayList<String> actions, ArrayList<String> datas, ArrayList<String> names, ChooseSettings choices){

        this.aiService = new OpenAiService("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");

        //urls
        this.url = url;
        this.functionUrl = functionUrl;

        this.actions = actions;
        this.datas = datas;
        this.names = names;

        this.choices = choices;

        this.aiChoiceProccess = new AiChoiceProccess();

    }



    public void setApiCallback(AiScheduleApiCallback callback){
        this.callback = callback;
    }

    public void startProccess(String message){
        aiChoiceProccess.start(message);
    }


    private int calcPercentage(double value, double max){
        double percentage = value / max;
        percentage *= 100.0;
        return (int)percentage;
    }

    private class AiChoiceProccess{

        private ArrayList<String> fieldOrder;
        private Map<String, String> fieldName1;
        private Map<String, String> fieldName2;
        private Map<String, Boolean> needMoreInfos;
        private Map<String, Boolean> isRequaireds;
        private Map<String, String> moreInfos;

        private Map<String, String> defaultDatas;
        private ArrayList<String> result;

        private String adminMessage;

        private String userMessage;
        private int errorIndex = 0;

        public AiChoiceProccess() {
            init();
            prepareFields();
            prepareFieldOrder();
        }
        private void init(){
            result = new ArrayList<>();
            fieldOrder = new ArrayList<>();
            fieldName1 = new LinkedHashMap<>();
            fieldName2 = new LinkedHashMap<>();
            needMoreInfos = new LinkedHashMap<>();
            isRequaireds = new LinkedHashMap<>();
            moreInfos = new LinkedHashMap<>();
            defaultDatas = new LinkedHashMap<>();
            adminMessage = "Jest to przedstawienie się studenta, na podstawie tego wybierz element zawierający odpowiedni _NAME1 na liście z bazy JSON: _JSON. Wszystkie pola tego elementu muszą być zwrócone.\n" +
                    "Jeżeli nie podano jaki _NAME1 lub nie ma 100% pewności to wtedy zwróć pole \"error\"." +
                    "Odpowiedź zwróć w formacie JSON w polu o nazwie\"_KEY\"";
        }
        private void prepareFieldOrder(){
            fieldOrder.add("faculity");
            fieldOrder.add("form");
            fieldOrder.add("degree");
            fieldOrder.add("direction");
            fieldOrder.add("specialty");
            fieldOrder.add("specialization");
            fieldOrder.add("year");
        }

        private void prepareFields(){
            prepareFieldInfos("faculity", "instytut", "instytucie", false, "", true,"33");
            prepareFieldInfos("form", "formę studiów", "formie studiów", false, "", true,"1");
            prepareFieldInfos("degree", "stopień", "stopniu", false, "", true,"2");
            prepareFieldInfos("direction", "kierunek", "kierunku", false, "", true,"1213");
            prepareFieldInfos("specialty", "specjalność", "specjalności", false, "", false,"");
            prepareFieldInfos("specialization", "specjalizację", "specjalizacji", false, "", false,"");
            prepareFieldInfos("year", "rok studiów", "roku studiów", true, "przy wyborze zadecyduj czy teraz jest semestr letni czy zimowy", false,"518");
        }
        private void prepareFieldInfos(String key, String name1, String name2, Boolean needMoreInfo, String moreInfo, Boolean required, String defaultData){
            fieldName1.put(key, name1);
            fieldName2.put(key, name2);
            needMoreInfos.put(key, needMoreInfo);
            moreInfos.put(key, moreInfo);
            isRequaireds.put(key, required);
            defaultDatas.put(key, defaultData);
        }

        public void start(String userMessage){

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(()->{
                this.userMessage = userMessage;
                callback.onAiProcessStart();
                execute();
                callback.onAiProccessStop();
                Log.e("AI result","finished" );
                callback.onAiApiUpdateProgress(calcPercentage(result.size(), fieldOrder.size()));

                if(result.size()==fieldOrder.size()){
                    errorIndex = 0;
                    callback.onAiApiUpdateProgressInfo("Zakończono");
                }

                JSONObject jsonObject = makeJsonResult();
                Log.e("AI", "startProccess: "+jsonObject);
                callback.onAiApiUpdateResult(jsonObject, fieldOrder, result.size());




            });
        }
        public void execute(){
            for (int i = errorIndex; i <fieldOrder.size() ; i++) {
                callback.onAiApiUpdateProgressInfo("Ładowanie: "+names.get(i));
                if(i==0)
                    chooseInstitute();
                else
                    chooseOther(i);

                if(isError(i) && Boolean.TRUE.equals(isRequaireds.get(fieldOrder.get(i)))){
                    Log.e("AI", "execute: error on index"+i);
                    callback.onAiApiError("Błąd w polu "+fieldName1.get(fieldOrder.get(errorIndex))+". Podaj jeszcze raz informacje na temat tego pola.");
                    break;
                }

                callback.onAiApiUpdateProgress(calcPercentage(i, fieldOrder.size()));
                callback.onAiApiUpdateResult(makeJsonResult(), fieldOrder, result.size());
                setDataChoice(i);
            }
        }

        public void chooseInstitute(){
            FirstSettings.DownloadPageTask downloadPageTask = new FirstSettings.DownloadPageTask();
            try{
                Document document = downloadPageTask.execute(url).get();
                LinkedHashMap<String, String> options = ChooseFirstFragment.getMapFromElement(document);

                String aiRes = askAI(mapToJson(options).toString(), 0, true);
                Log.e("AI", "getInstitute: "+aiRes);
                if(waitRepeat(20,aiRes)){
                    callback.onAiApiUpdateProgressInfo("Ładowanie: "+names.get(0));
                    chooseInstitute();
                    return;
                }
                result.add(aiRes);

            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        public void chooseOther(int index){
            choices.changeArg("action",actions.get(index-1));
            FirstSettings.ExecuteTask executeTask = new FirstSettings.ExecuteTask(choices);
            try{
                Document document = executeTask.execute(functionUrl).get();
                LinkedHashMap<String, String> options = FirstSettingsFragment.getMapFromElement(document);
                String aiRes = askAI(mapToJson(options).toString(), index, true);

                Log.e("AI", "getOtherChoices: "+aiRes);
                if(waitRepeat(20,aiRes)){
                    callback.onAiApiUpdateProgressInfo("Ładowanie: "+names.get(index));
                    chooseOther(index);

                    return;
                }
                result.add(aiRes);

            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean isError(int index){

            try{
                JSONObject jsonObject = new JSONObject(result.get(index));
                String message = jsonObject.optString(fieldOrder.get(index), "error");
                Log.e("AI", "checkIfError: "+message );
                if((message.equals("error") || jsonObject.toString().contains("error")) && Boolean.TRUE.equals(isRequaireds.get(fieldOrder.get(index)))){
                    errorIndex = index;
                    result.remove(index);
                    return true;
                }

            } catch (JSONException e) {
                if(isRequaireds.get(fieldOrder.get(index))){
                    errorIndex = index;
                    result.remove(index);
                }
                return true;
            }
            return false;
        }

        private void setDataChoice(int index){
            try{
                JSONObject jsonObject = new JSONObject(result.get(index));
                String key = fieldOrder.get(index);
                jsonObject = jsonObject.optJSONObject(key);
                String keyValue = jsonObject!=null? jsonObject.optString("key", null) : null;
                keyValue = keyValue==null?defaultDatas.get(key):keyValue;

                choices.changeArg(datas.get(index), keyValue);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        public JSONObject makeJsonResult(){
            JSONObject jsonObject = new JSONObject();
            for (int i = 0; i < result.size(); i++) {

                try{
                    JSONObject jsonField = new JSONObject(result.get(i));
                    jsonField = jsonField.optJSONObject(fieldOrder.get(i));
                    jsonObject.put(fieldOrder.get(i),jsonField);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            return jsonObject;
        }
        private JSONArray mapToJson(LinkedHashMap<String, String> map){
            JSONArray result = new JSONArray();
            for(Map.Entry<String, String> entry: map.entrySet()){
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("key", entry.getKey());
                    jsonObject.put("value", entry.getValue());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                result.put(jsonObject);

            }
            return result;
        }
        private String prepareAdminMessage(int index, String json){
            String key = fieldOrder.get(index);
            String result = adminMessage;

            result = result.replace("_NAME1", fieldName1.get(key))
                    .replace("_NAME2", fieldName2.get(key))
                    .replace("_JSON", json)
                    .replace("_KEY", key);

            if(needMoreInfos.get(key)){
                result+="\ndodatkowo w tym przypadku, "+moreInfos.get(key);
            }
            return result;
        }

        private boolean waitRepeat(int seconds, String aiRes){
            long endTime = System.currentTimeMillis()+(seconds*1000);
            if(aiRes.equals("wait")){
                Handler handler = new Handler(Looper.getMainLooper());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        callback.onAiApiUpdateProgressInfo("Oczekiwanie "+((int)((endTime-System.currentTimeMillis())/1000))+"s");
                        if(System.currentTimeMillis()<endTime-1000)
                            handler.postDelayed(this, 1000);
                    }
                };
                handler.post(runnable);
                try{
                    Thread.sleep(seconds*1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
            return false;
        }

        private String askAI(String json, int index, boolean inAsyncTask){
            Log.e("AI", "askAI: "+json );
            String strRes = "";
            String rule = prepareAdminMessage(index, json);
            try{
                JSONArray array = new JSONArray(json);
                if(array == null) return "{\""+fieldOrder.get(index)+"\": \"error\"}";
                if(array.length()==0) return "{\""+fieldOrder.get(index)+"\": \"error\"}";

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            if(inAsyncTask){
                GptApiTask apiTask = new GptApiTask(aiService);
                try{
                    String aiRes = apiTask.execute(userMessage, rule).get();
                    strRes = aiRes;

                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            else {

            }

            return strRes;
        }



    }

    public interface AiScheduleApiCallback{

//        void onAiApiResult();

        void onAiProcessStart();
        void onAiProccessStop();
        void onAiApiUpdateResult(JSONObject jsonObject, ArrayList<String> fields, int size);
        void onAiApiError(String error);

//        void onAiApiInfo(String text);
        void onAiApiUpdateProgress(int value);
        void onAiApiUpdateProgressInfo(String text);



    }
}
