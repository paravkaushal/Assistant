package com.example.android.assistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;

public class MainActivity extends AppCompatActivity {

    ImageView mic;
    TextView userTextTV, agentTextTV;
    TextToSpeech tts;
    String searchEngine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this , new String[] {Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_CONTACTS}, 121);

        mic = findViewById(R.id.imageView);
        userTextTV = findViewById(R.id.textView);
        agentTextTV = findViewById(R.id.textView2);
        tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.UK);
                }
            }
        });
        final AIConfiguration config = new AIConfiguration("7eb94054e7e54e88a6897e315c749449",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        final AIService aiService = AIService.getService(getApplicationContext(), config);
        aiService.setListener(new AIListener() {
            @Override
            public void onResult(AIResponse result) {
                tts.speak(result.getResult().getFulfillment().getSpeech(),TextToSpeech.QUEUE_ADD, null, null);
                agentTextTV.setText(result.getResult().getFulfillment().getSpeech());
                userTextTV.setText(result.getResult().getResolvedQuery());

                if(result.getResult().getAction().equals("websearch")) {
                    String query = result.getResult().getStringParameter("any", "none");
                    if(!(query.equals("none"))) {
                        searchEngine = result.getResult().getStringParameter("search-engine", "google");

                        String url = "";
                        if (searchEngine.equals("google")) {
                            url = "https://www.google.com/search?q=" + query;
                        } else if (searchEngine.equals("yahoo")) {
                            url = "https://search.yahoo.com/search?q=" + query;
                        } else if (searchEngine.equals("bing")) {
                            url = "https://www.bing.com/search?q=" + query;
                        }
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                } else if(result.getResult().getAction().equals("websearchfollowup")) {
                    String query = result.getResult().getStringParameter("any");
                    if(!(query.equals("none"))) {
                        String url = "";
                        if (searchEngine.equals("google")) {
                            url = "https://www.google.com/search?q=" + query;
                        } else if (searchEngine.equals("yahoo")) {
                            url = "https://search.yahoo.com/search?q=" + query;
                        } else if (searchEngine.equals("bing")) {
                            url = "https://www.bing.com/search?q=" + query;
                        }
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                } else if(result.getResult().getAction().equals("app_launch")){
                        LaunchingSystem ls = new LaunchingSystem(getApplicationContext(),tts);
                        ls.initialteLaunchingProcess(result.getResult());

                } else if(result.getResult().getAction().equals("maka_call")){
                    Call call = new Call(getApplicationContext(), tts);
                    call.initiateCallProcess(result.getResult());

                } else if (result.getResult().getAction().equals("send_sms")) {
                    Sms sms = new Sms(getApplicationContext(), tts);
                    sms.initiateSmsProcess(result.getResult());
                }
            }

            @Override
            public void onError(AIError error) {
                mic.setImageResource(R.drawable.mic23);
            }

            @Override
            public void onAudioLevel(float level) {

            }

            @Override
            public void onListeningStarted() {
                mic.setImageResource(R.drawable.mic1);

            }

            @Override
            public void onListeningCanceled() {
                mic.setImageResource(R.drawable.mic23);
            }

            @Override
            public void onListeningFinished() {
                mic.setImageResource(R.drawable.mic23);
            }
        });
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aiService.startListening();
            }
        });
    }
}
