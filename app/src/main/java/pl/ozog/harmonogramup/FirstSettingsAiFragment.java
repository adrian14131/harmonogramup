package pl.ozog.harmonogramup;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.ozog.harmonogramup.downloaders.GptApiTask;

public class FirstSettingsAiFragment extends Fragment implements View.OnClickListener{

    TextView info, summary, progressBarLabel;
    ProgressBar progressBar;
    Button sendButton;

    String url, functionUrl;
    ArrayList<String> actions;
    ArrayList<String> datas;

    ArrayList<String> names;
    ChooseSettings choices;
    EditText inputText;
    LinearLayout summaryLinearLayout;
    final String exampleMessage = "Studiuję Edukację Techniczno Informatyczną na instytucie Nauk Technicznych. Studiuję na pierwszym roku, studia magisterskie, stacjonarne.";

    AiChoiceProccess aiChoiceProccess;
    public FirstSettingsAiFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        choices = new ChooseSettings();
    }

    public void initAiProccess(String url, String function, ArrayList<String> actions, ArrayList<String> datas, ArrayList<String> names){
        this.url = url;
        this.functionUrl = function;
        this.actions = actions;
        this.datas = datas;
        this.names = names;
        aiChoiceProccess = new AiChoiceProccess();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first_settings_ai, container, false);

        info = view.findViewById(R.id.aiFragmentInfo);
        summary = view.findViewById(R.id.aiSummaryTextView);
        progressBarLabel = view.findViewById(R.id.aiProgressBarLabel);
        progressBar = view.findViewById(R.id.aiProgressBar);
        sendButton = view.findViewById(R.id.aiSendButton);
        sendButton.setOnClickListener(this);
        inputText = view.findViewById(R.id.aiInputText);
        inputText.setText(exampleMessage);
        summaryLinearLayout = view.findViewById(R.id.aiSummaryLinearLayout);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.aiSendButton:
                aiChoiceProccess.startProccess(inputText.getText().toString());
                break;
        }
    }

    private void addSummaryTV(LinearLayout layout, String text){
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(()->{
            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            tv.setText(text);
            layout.addView(tv);
        });



    }
    private void updateResult(JSONObject jsonObject, ArrayList<String> fields, int size){

//        StringBuilder sb = new StringBuilder();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(()->{
            summaryLinearLayout.removeAllViews();
        });
        for (int i = 0; i < size; i++) {

            JSONObject jsonField = jsonObject.optJSONObject(fields.get(i));
            String value = jsonField!=null?jsonField.optString("value",""):"";
            addSummaryTV(summaryLinearLayout, names.get(i)+": "+value);
//            sb.append(names.get(i)+": "+value+"\n");


        }

//        summary.setText(sb.toString());
    }
    private void updateInfo(String text){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(()->{
            info.setText(text);
        });
    }
    private int calcPercentage(double value, double max){
        double percentage = value / max;
        percentage *= 100.0;
        return (int)percentage;
    }
    private void updateProgressBar(double value, double max){

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(()->{
            progressBar.setProgress(calcPercentage(value, max));
        });
    }

    private void updateProgressBarLabel(String message){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(()->{
            progressBarLabel.setText(message);
        });
    }
    private void setButtonEnabled(Button button, boolean enabled){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(()->{
            button.setEnabled(enabled);
        });
    }
    public class AiChoiceProccess {

        private ArrayList<String> fieldOrder;
        private Map<String, String> fieldName1;
        private Map<String, String> fieldName2;
        private Map<String, Boolean> needMoreInfos;
        private Map<String, Boolean> isRequaireds;
        private Map<String, String> moreInfos;

        private Map<String, String> defaultDatas;
        private ArrayList<String> result = new ArrayList<>();

        private String adminMessage;

        private String userMessage;
        private OpenAiService aiService;
        private int errorIndex = 0;
//        private ChooseSettings choices;
        public AiChoiceProccess() {

            this.aiService = new OpenAiService("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");

            fieldOrder = new ArrayList<>();
            fieldName1 = new LinkedHashMap<>();
            fieldName2 = new LinkedHashMap<>();
            needMoreInfos = new LinkedHashMap<>();
            isRequaireds = new LinkedHashMap<>();
            moreInfos = new LinkedHashMap<>();
            defaultDatas = new LinkedHashMap<>();
            prepareFields();
            prepareFieldOrder();
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

        public void startProccess(String message){
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(()->{
                userMessage = message;
                setButtonEnabled(sendButton, false);

                execute();
                setButtonEnabled(sendButton, true);

                Log.e("AI result","finished" );
                updateProgressBar(result.size(), fieldOrder.size());
                if(result.size()==fieldOrder.size()){
                    errorIndex = 0;
                    updateProgressBarLabel("Zakończono");
                }


                JSONObject jsonObject = makeJsonResult();
                Log.e("AI", "startProccess: "+jsonObject);
                updateResult(jsonObject, fieldOrder, result.size());
            });
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
//        public JSONObject makeJsonResult(){
//            JSONArray jsonArray = new JSONArray();
//            for(String jsonStr: result){
//                try {
//                    JSONObject jsonField = new JSONObject(jsonStr);
//                    jsonArray.put(jsonField);
//
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            return jsonArray;
//        }
        private void execute(){
            for(int i=errorIndex; i<fieldOrder.size(); i++){

                updateProgressBarLabel("Ładowanie: "+names.get(i));
                if(i==0){
                    getInstitute();
                }
                else {
                    getOtherChoices(i);
                }

                if(checkIfError(i) && isRequaireds.get(fieldOrder.get(i))){
                    Log.e("AI", "execute: error on index"+i);
                    updateInfo("Błąd w polu "+fieldName1.get(fieldOrder.get(errorIndex))+". Podaj jeszcze raz informacje na temat tego pola.");
                    break;
                }

                double percentage = (double) i / (double) fieldOrder.size();
                percentage *= 100.0;

                updateProgressBar(i, fieldOrder.size());

                updateResult(makeJsonResult(), fieldOrder, result.size());
                setDataChoice(i);


            }
        }
        private void setDataChoice(int index){
            try {
                JSONObject jsonObject = new JSONObject(result.get(index));
                String key = fieldOrder.get(index);
                jsonObject = jsonObject.optJSONObject(key);
                String keyValue = jsonObject!=null?jsonObject.optString("key",null):null;
                keyValue = keyValue==null?defaultDatas.get(key):keyValue;

                choices.changeArg(datas.get(index), keyValue);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        private boolean checkIfError(int index){
            try {
                JSONObject jsonObject = new JSONObject(result.get(index));
                String message = jsonObject.optString(fieldOrder.get(index),"error");
                Log.e("AI", "checkIfError: "+message );
                if(message.equals("error") && isRequaireds.get(fieldOrder.get(index))){
                    errorIndex = index;
                    result.remove(index);
                    return true;
                }
                else if(jsonObject.toString().contains("error") && isRequaireds.get(fieldOrder.get(index))){
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
        private void getInstitute(){
            FirstSettings.DownloadPageTask downloadPageTask = new FirstSettings.DownloadPageTask();
            try{
                Document document = downloadPageTask.execute(url).get();
                LinkedHashMap<String, String> options = ChooseFirstFragment.getMapFromElement(document);

                String aiRes = askAI(mapToJson(options).toString(), 0, true);
                Log.e("AI", "getInstitute: "+aiRes);
                if(waitRepeat(20,aiRes)){
                    updateProgressBarLabel("Ładowanie: "+names.get(0));
                    getInstitute();

                    return;
                }
                result.add(aiRes);

            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
        private boolean waitRepeat(int seconds, String aiRes){
            long endTime = System.currentTimeMillis()+(seconds*1000);
            if(aiRes.equals("wait")){
                Handler handler = new Handler(Looper.getMainLooper());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        updateProgressBarLabel("Oczekiwanie "+((int)((endTime-System.currentTimeMillis())/1000))+"s");
                        if(System.currentTimeMillis()<endTime-1000)
                            handler.postDelayed(this, 1000);
                    }
                };
                handler.post(runnable);
//                updateProgressBarLabel("Oczekiwanie "+((int)(millisUntilFinished/1000))+"s");
                try {
                    Thread.sleep(seconds*1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
            return false;
        }
        private void getOtherChoices(int index){
            choices.changeArg("action",actions.get(index-1));
            FirstSettings.ExecuteTask executeTask = new FirstSettings.ExecuteTask(choices);
            try{
                Document document = executeTask.execute(functionUrl).get();
                LinkedHashMap<String, String> options = FirstSettingsFragment.getMapFromElement(document);
                String aiRes = askAI(mapToJson(options).toString(), index, true);

                Log.e("AI", "getOtherChoices: "+aiRes);
                if(waitRepeat(20,aiRes)){
                    updateProgressBarLabel("Ładowanie: "+names.get(index));
                    getOtherChoices(index);

                    return;
                }
                result.add(aiRes);

            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        private String askAI(String json, int index, boolean inAsyncTask){
            Log.e("AI", "askAI: "+json );
            String strRes = "";
            String rule = prepareAdminMessage(index, json);
            try {
                JSONArray array = new JSONArray(json);
                if(array == null) return "{\""+fieldOrder.get(index)+"\": \"error\"}";
                if(array.length()==0) return "{\""+fieldOrder.get(index)+"\": \"error\"}";
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            if(inAsyncTask){
                GptApiTask apiTask = new GptApiTask(aiService);
                try {

                    String aiRes = apiTask.execute(userMessage, rule).get();
                    strRes = aiRes;
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            else{
//                return getAIResponse(userMessage, rule);
            }

            return strRes;
        }
//        private String getAIResponse(String... strings){
//            List<ChatMessage> messages = new ArrayList<>();
//            if(!strings[1].isEmpty() && !strings[0].isEmpty()){
//                messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), strings[1]));
//            }
//            if(!strings[0].isEmpty()){
//                messages.add(new ChatMessage(ChatMessageRole.USER.value(), strings[0]));
//            }
//            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
//                    .builder()
//                    .model("gpt-3.5-turbo-0125")
//                    .messages(messages)
//                    .n(1)
////                .maxTokens(50)
//                    .logitBias(new HashMap<>())
//                    .build();
//            String result = "";
//            try{
//                result = aiService.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage().getContent();
//            }
//            catch (RuntimeException r){
//                Log.e("AI", "doInBackground: runtimeerror" );
//                return "wait";
//            }
//            return result;
//        }


    }
}