package pl.ozog.harmonogramup;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.ozog.harmonogramup.ai.AiScheduleApi;
import pl.ozog.harmonogramup.downloaders.GptApiTask;

public class FirstSettingsAiFragment extends Fragment implements View.OnClickListener, AiScheduleApi.AiScheduleApiCallback{

    TextView info, summary, progressBarLabel;
    ProgressBar progressBar;
    Button sendButton, voiceModeButton;

    String url, functionUrl;
    ArrayList<String> actions;
    ArrayList<String> datas;

    ArrayList<String> names;
    ArrayList<String> summaryList;
    ChooseSettings choices;
    EditText inputText;
    LinearLayout summaryLinearLayout;
    AiScheduleApi aiApi;
    private static final int SPEECH_REQUEST_CODE = 1994;
    final String exampleMessage = "Studiuję Edukację Techniczno Informatyczną na instytucie Nauk Technicznych. Studiuję na pierwszym roku, studia magisterskie, stacjonarne.";
    final String welcomeTTS = "Witaj. Opisz dokładnie co studiujesz (instytut, formę studiów, stopień, kierunek, rok studiów oraz opcjonalnie specjalizację i specjalność";


    String toSpeechStr = welcomeTTS;

    TextToSpeech tts;
    public FirstSettingsAiFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        choices = new ChooseSettings();
        summaryList = new ArrayList<>();

    }

    private void said(String text){
        tts.speak(text,TextToSpeech.QUEUE_FLUSH,null, null);
    }
    public void initAiProccess(String url, String function, ArrayList<String> actions, ArrayList<String> datas, ArrayList<String> names){
        this.url = url;
        this.functionUrl = function;
        this.actions = actions;
        this.datas = datas;
        this.names = names;

        aiApi = new AiScheduleApi(url, function, actions, datas, names, choices, tts);
        aiApi.setApiCallback(this);
        said(welcomeTTS);
    }

    private void displayVoiceRecognizer(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            String spokenText = results.get(0);
            if(spokenText!=null){
                if(!spokenText.isEmpty()){
                    inputText.setText(inputText.getText().toString()+"\n"+spokenText);
                }
            }
            // Do something with spokenText.
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        voiceModeButton = view.findViewById(R.id.voiceModeButton);
        voiceModeButton.setOnClickListener(this);
        inputText = view.findViewById(R.id.aiInputText);
        inputText.setText(exampleMessage);
        summaryLinearLayout = view.findViewById(R.id.aiSummaryLinearLayout);
        tts = new TextToSpeech(this.getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!= TextToSpeech.ERROR)
                    tts.setLanguage(new Locale("pl"));
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.aiSendButton:
                said("Rozpoczęto wyszukiwanie.");
                aiApi.startProccess(inputText.getText().toString());
                break;
            case R.id.voiceModeButton:
                displayVoiceRecognizer();
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
            summaryLinearLayout.removeAllViewsInLayout();
        });
        summaryList.clear();
        for (int i = 0; i < size; i++) {

            JSONObject jsonField = jsonObject.optJSONObject(fields.get(i));
            String value = jsonField!=null?jsonField.optString("value",""):"";
            addSummaryTV(summaryLinearLayout, names.get(i)+": "+value);
            summaryList.add(names.get(i)+": "+value);
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
    private void updateProgressBar(int value){

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(()->{
            progressBar.setProgress(value);
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

    @Override
    public void onAiProcessStart() {
        setButtonEnabled(sendButton, false);
    }

    @Override
    public void onAiProccessStop() {
        setButtonEnabled(sendButton, true);
    }

    @Override
    public void onAiApiUpdateResult(JSONObject jsonObject, ArrayList<String> fields, int size) {
        updateResult(jsonObject, fields, size);
    }

    @Override
    public void onAiApiError(String error) {
        updateInfo(error);
    }

    @Override
    public void onAiApiUpdateProgress(int value) {
        updateProgressBar(value);
    }

    @Override
    public void onAiApiUpdateProgressInfo(String text) {
        updateProgressBarLabel(text);
    }

    @Override
    public void onAiApiFinalResult() {
        said("Ukończono wyszukiwanie zostaniesz przeniesiony na ekran podsumowania.");
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(()->{
            ((FirstSettings) getActivity()).addArgs(choices.getArgs());
            ((FirstSettings) getActivity()).goToSummary(summaryList);
        });

    }
}