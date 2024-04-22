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

import pl.ozog.harmonogramup.ai.AiScheduleApi;
import pl.ozog.harmonogramup.downloaders.GptApiTask;

public class FirstSettingsAiFragment extends Fragment implements View.OnClickListener, AiScheduleApi.AiScheduleApiCallback{

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
    AiScheduleApi aiApi;
    final String exampleMessage = "Studiuję Edukację Techniczno Informatyczną na instytucie Nauk Technicznych. Studiuję na pierwszym roku, studia magisterskie, stacjonarne.";

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
        aiApi = new AiScheduleApi(url, function, actions, datas, names, choices);
        aiApi.setApiCallback(this);
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
                aiApi.startProccess(inputText.getText().toString());
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
}