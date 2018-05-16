package com.lopez.richard.nova_v6_en2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

public class SpeechToTextG implements RecognitionListener {
    Context context;
    RecognizerIntent recognizerIntent;
    SpeechRecognizer speechRecognizer;
    private ResultListener resultListener;

    public SpeechToTextG(Context context){
        this.context = context;
        this.resultListener = null;
        create();
    }


    private void create() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

        speechRecognizer.setRecognitionListener(this);

        /*
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getActivity().getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        */
    }

    public void recognize(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getClass().getPackage().getName());
        //Given an hint to the recognizer about what the user is going to say
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //specify the max number of results
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-BO");

        //User of SpeechRecognizer to "send" the intent.
        speechRecognizer.startListening(intent);
        Log.i("RECOGNIZE","Intent sent");
    }


    @Override

    public void onBeginningOfSpeech() {

    }

    @Override

    public void onBufferReceived(byte[] buffer) {

    }

    @Override

    public void onEndOfSpeech() {

    }
    @Override

    public void onError(int errorCode) {
        Log.e("ERROR", "Error code " + String.valueOf(errorCode));
        resultListener.onError(errorCode);
    }

    @Override

    public void onEvent(int arg0, Bundle arg1) {
    }

    @Override

    public void onPartialResults(Bundle arg0) {
    }

    @Override

    public void onReadyForSpeech(Bundle arg0) {
    }

    @Override

    public void onResults(Bundle results) {

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(resultListener != null){
                resultListener.onResult(matches);
        }
        for(String item:matches) {
            Log.d("RESULTS", ">>>>> " + item);
        }

    }

    @Override
    public void onRmsChanged(float f){}

    public void setResultListener(ResultListener resultListener){
        this.resultListener = resultListener;
    }

    public interface ResultListener {
        public void onResult(ArrayList<String> result);
        public void onError(int error);
    }

}
