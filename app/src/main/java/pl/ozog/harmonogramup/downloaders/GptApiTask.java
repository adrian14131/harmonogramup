package pl.ozog.harmonogramup.downloaders;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GptApiTask extends AsyncTask<String, Void, String> {

    private final OpenAiService aiService;
    public GptApiTask(String apiKey) {
        this.aiService = new OpenAiService(apiKey);
    }
    public GptApiTask(OpenAiService aiService) {this.aiService = aiService;}
    @Override
    protected String doInBackground(String... strings) {
        List<ChatMessage> messages = new ArrayList<>();
        if(!strings[1].isEmpty() && !strings[0].isEmpty()){
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), strings[1]));
        }
        if(!strings[0].isEmpty()){
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), strings[0]));
        }
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0125")
                .messages(messages)
                .n(1)
//                .maxTokens(50)
                .logitBias(new HashMap<>())
                .build();
//        Log.e("AI", "doInBackground: "+aiService.createChatCompletion(chatCompletionRequest).getChoices() );
        String result = "";
        try{
            result = aiService.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage().getContent();
        }
        catch (RuntimeException r){
            Log.e("AI", "doInBackground: runtimeerror" +r);
            return "wait";
//            try {
//
//                Thread.sleep(20*1000);
//                result = aiService.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage().getContent();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }

        }
        return result;
    }
}
