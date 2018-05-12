package com.lopez.richard.nova_v6_en2;

/**
 * Created by Richard on 31-March-2018
 */

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.conversation.v1.model.InputData;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.language_translator.v2.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v2.model.Language;
import com.ibm.watson.developer_cloud.language_translator.v2.model.TranslationResult;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Transcript;

//import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.Conversation;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneCategory;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneScore;
import com.lopez.richard.nova_v6_en2.snowboy.AppResCopy;
import com.lopez.richard.nova_v6_en2.snowboy.AudioDataSaver;
import com.lopez.richard.nova_v6_en2.snowboy.MsgEnum;
import com.lopez.richard.nova_v6_en2.snowboy.RecordingThread;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    Intent newIntent;
    TextView tv_consola;
    File filePath;
    LightService service;
    RecordingThread recordingSnowBoyThread;
    private static long activeTimes = 0;
    private int preVolume = -1;

    private static final String APP_TAG_VS_STT = "NOVA-STT";
    private static final String APP_TAG_NOVABOT = "NOVA-BOT: ";
    private static final String APP_TAG_NOVA_RECORD = "NOVA-RECORD: ";
    private static final String APP_TAG_NOVA_TONEANALYSER = "NOVA-TONE_ANALYZER: ";
    private static final String APP_TAG_NOVA_TRANSLATOR = "NOVA-TRANSLATOR: ";

    //GRABACION
    public Button btn_start, btn_stop;

    private static final String AUDIO_RECORDER_FOLDER = "NOVARecords";
    private static final String AUDIO_RECORDER_FILE_NAME = "grabacionNova1";
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String ARCHIVO_MENSAJE_TXT = "mensaje.txt";
    private static final String ARCHIVO_RESPUESTA_TXT = "respuesta.txt";

    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    // FIN BLOQUE GRABACION

    SpeechToText stt_service;
    SpeechResults speechResults;

    //-- credenciales 2018
    private static final String CONVERSATION_SERVICE_WORKSPACE = "58b930e3-40bc-4eb6-805a-985fcf85d8dd";
    private static final String CONVERSATION_SERVICE_USERNAME = "84ef0a74-264c-48ff-9edd-a81eb0ea77a9";
    private static final String CONVERSATION_SERVICE_PASSWORD = "cNu4CJJh1Lig";

    private static final String TTS_SERVICE_USERNAME = "30bfa608-44e2-43f4-9c4e-6ced2dba3cc8";
    private static final String TTS_SERVICE_PASSWORD = "0o407lHde3P3";

    private static final String STT_SERVICE_USERNAME = "a9087b30-276e-4e07-8f81-d238668a7735";
    private static final String STT_SERVICE_PASSWORD = "5pYY2QlSdaRG";

    //--- fin credenciales

    String pathDirectorio;
    String pathArchivoTexto;
    String textoLeidoDelTxt;

    String texto;

    // -----   BOT
    private Map<String, Object> context = new HashMap<>();

    // --- TTS
    StreamPlayer streamPlayer;

    // WEATHER
    final static String APP_NOVA_WEATHER = "NOVA_WEATHER";

    // Tone Analyzer
    private ToneAnalyzer ta_service;
    //final String username_TA = "xxxxxxxxxxx";
    //final String password_TA = "yyyyyyyyyyy";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // INICIO BLOQUE GRABACION
        enableButtons(false);

        bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        btn_start = (Button) findViewById(R.id.btnStart);
        btn_stop = (Button) findViewById(R.id.btnStop);

        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(APP_TAG_NOVA_RECORD, "START Grabación");
                enableButtons(true);
                startRecording();
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(APP_TAG_NOVA_RECORD, "STOP Grabación");
                enableButtons(false);
                stopRecording();

                // LLAMOS AL METODO QUE ACCEDE A LA GRABACIOM Y QUE LUEGO ENVIA A WATSON
                conversorSTT();
            }
        });
        // FIN BLOQUE GRABACION

        tv_consola = findViewById(R.id.consolatxt);
        service = RetrofitInstance.getRetrofitInstance().create(LightService.class);
        AppResCopy.copyResFromAssetsToSD(this);
        setProperVolume();
        recordingSnowBoyThread = new RecordingThread(handle, new AudioDataSaver());
        recordingSnowBoyThread.startRecording();
    }

    public void conversorSTT() {
        filePath = new File(Environment.getExternalStorageDirectory()
                + File.separator
                + AUDIO_RECORDER_FOLDER
                + File.separator
                + AUDIO_RECORDER_FILE_NAME
                + AUDIO_RECORDER_FILE_EXT_WAV);
        Log.d(APP_TAG_VS_STT, "getPath: " + filePath.getPath());
        Log.d(APP_TAG_VS_STT, "getAboslutePath: " + filePath.getAbsolutePath());

        setPathDirectorio(Environment.getExternalStorageDirectory() + File.separator + AUDIO_RECORDER_FOLDER);
        Log.d(APP_TAG_VS_STT, "DIRECTORIO: " + getPathDirectorio());


        newIntent = new Intent();
        newIntent.setDataAndType(Uri.parse("file://" + filePath), "audio/*");
        //String archivo = newIntent.getData().toString();

        try {
            speechToText2(newIntent.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void actualizarConsola() {
        String texto = leemosArchitoDeTexto(ARCHIVO_MENSAJE_TXT);
        Pattern encender = Pattern.compile("encender");
        Pattern apagar = Pattern.compile("apagar");
        Call<String> response = null;
        if(encender.matcher(texto).find()){
            response = service.setLightOn();
        } else if(apagar.matcher(texto).find()){
            response = service.setLightOff();
        }

        if(response != null) {
            Log.i("REST_CLIENT", "URL requested: " + response.request().url());

            response.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.i("REST_CLIENT", "REST server response: " + response.body());
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.i("REST_CLIENT", "REST client/server error: " + t.getMessage());
                }
            });
        }


        tv_consola.setText("Received: " + texto);
    }

    public synchronized void speechToText2(final Uri uri) throws IOException {
        final File audio = downloadWavFile(uri);
        Log.d(APP_TAG_VS_STT, "ENTRAMOS A speechToText2()");
        Log.d(APP_TAG_VS_STT, "Verificamos archivo de audio: " + filePath);

        stt_service = new SpeechToText();
        stt_service.setUsernameAndPassword(STT_SERVICE_USERNAME, STT_SERVICE_PASSWORD);

        final RecognizeOptions options2 = new RecognizeOptions.Builder()
                .continuous(true)
                .interimResults(true)
                .contentType("audio/wav")
                .model("es-ES_BroadbandModel")
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                speechResults = stt_service.recognize(audio, options2).execute();

                StringBuilder sb = new StringBuilder();
                for (Transcript transcript : speechResults.getResults()) {
                    String word = transcript.getAlternatives().get(0).getTranscript();
                    sb.append(word);
                }
                Log.d(APP_TAG_VS_STT, " ANTES DE QUE MUERA creamos archivo de texto");
                setTexto(sb.toString());
                Log.d(APP_TAG_VS_STT, " set set set texto @@@@@@@@@@@@@@@@@@@@@@@ " + getTexto() + " @@@@@@@@@@@@@@@@");

                crearArchivoTxt222(ARCHIVO_MENSAJE_TXT, sb.toString());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        actualizarConsola();
                    }
                });

                Log.d(APP_TAG_VS_STT, " $$$$$$$$$$$$$ " + sb.toString() + " $$$$$$$$$$$$$");


                Log.d(APP_TAG_VS_STT, "<<<<<<<<<<<<<<<<< A PUNTO DE ENVIAR PREGUNTA A WATSON >>>>>>>>>>>>>>>");
                Log.d(APP_TAG_VS_STT, "<<<<<<<<<<<<<<<<< A PUNTO DE ENVIAR PREGUNTA A WATSON >>>>>>>>>>>>>>>");
                sendMessageToWatsonBot(sb.toString());
                Log.d(APP_TAG_VS_STT, "<<<<<<<<<<<<<<<<< PREGUNTA A WATSON ENVIADA >>>>>>>>>>>>>>>");
                Log.d(APP_TAG_VS_STT, "<<<<<<<<<<<<<<<<< PREGUNTA A WATSON ENVIADA >>>>>>>>>>>>>>>");


                Log.d(APP_TAG_NOVABOT, "---------------------------------------------");
                Log.d(APP_TAG_NOVABOT, "--------- RESPUESTA DE WATSON ---------------");
                //Log.d(APP_TAG_NOVABOT, "--------- " + getRespuestaWatson() + " ---------------");
                Log.d(APP_TAG_NOVABOT, "---------------------------------------------");

            }
        }).start();

        //Fuera del HILO el texto almacenado MUERE y no es posible recuperarlo
        Log.d(APP_TAG_VS_STT, getTexto() + "-----------------getTexto()------------------------------"); // muestra null

    }

    private File downloadWavFile(Uri uri) throws IOException {
        File f = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "sample.wav");
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }

        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = getContentResolver().openInputStream(uri);
            fos = new FileOutputStream(f);
            byte[] buf = new byte[256];
            int len;
            while ((len = is.read(buf, 0, buf.length)) != -1) {
                fos.write(buf, 0, len);
            }
        } finally {
            if (is != null)
                is.close();
            if (fos != null)
                fos.close();
        }
        return f;
    }

    public synchronized void crearArchivoTxt222(String nombreArchivo, String contenido) {
        String archivotxt = nombreArchivo; //"mensaje.txt"; //ARCHIVO_MENSAJE_TXT
        String textoDelArchivo = contenido;

        Log.d("CREADOR: ", "-- ANTES DE CREAR ARCHIVOOOO");

        //Si existe archivo txt lo borramos
        File f = new File(getPathDirectorio() + "/" + archivotxt);
        if (f.exists()) {
            f.delete();
        }

        File txt = new File(getPathDirectorio(), archivotxt);
        Log.d("CREADOR: ", "-- OK ARCHIVO creadooo");
        Log.d("CREADOR: ", txt.getPath());

        setPathArchivoTexto(txt.getPath());
        Log.d(APP_TAG_VS_STT, "ARCHIVO TXT: " + getPathArchivoTexto());

        //int file_size = Integer.parseInt(String.valueOf(txt.getPath().length()/1024));
        int file_size = Integer.parseInt(String.valueOf(txt.getPath().length()));
        Log.d("CREADOR: ", "TAMAñO - RECIEN CREADO SIN CONTENIDO: " + Integer.toString(file_size) + " Kb");
        file_size = 0;

        FileOutputStream escritor = null;
        try {
            escritor = new FileOutputStream(new File(txt.getPath()), true); // true es igual q Context.MODE_APPEND
            escritor.write(textoDelArchivo.getBytes());
            file_size = Integer.parseInt(String.valueOf(txt.getPath().length()));
            Log.d("YOOOOOO: ", "TAMAñO - DESPUES DE SER CREADO + CON CONTENIDO: " + Integer.toString(file_size) + " Kb");
        } catch (Exception ex) {
            Log.e("YOOOO", "Error al escribir fichero a memoria interna");
            ex.printStackTrace();

        }
    }

    public void setPathDirectorio(String pathDirectorio) {
        this.pathDirectorio = pathDirectorio;
    }

    public String getPathDirectorio() {
        return pathDirectorio;
    }

    public void setPathArchivoTexto(String pathArchivoTexto) {
        this.pathArchivoTexto = pathArchivoTexto;
    }

    public String getPathArchivoTexto() {
        return pathArchivoTexto;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public synchronized String leemosArchitoDeTexto(String queArchivoLeemos) {
        File f = new File(getPathDirectorio() + "/" + queArchivoLeemos);//ARCHIVO_MENSAJE_TXT);
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(f.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader inputRead = new InputStreamReader(fileIn);

        char[] inputBuffer = new char[100];
        String s = "";
        int charRead;

        try {
            while ((charRead = inputRead.read(inputBuffer)) > 0) {
                // char to string conversion
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                s += readstring;
            }

            textoLeidoDelTxt = s;

            inputRead.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("LEYENDO: ", "ssss--- " + s + " ---");
        Log.d("LEYENDO: ", "textoLeidoDelTxt:--- " + textoLeidoDelTxt + " ---");

        return textoLeidoDelTxt;

    }

    private synchronized void sendMessageToWatsonBot(String msg_to_watson) {
        Log.d(APP_TAG_NOVABOT, " -- INICIO del metodo sendMessageToWatsonBot -- var recibida: " + msg_to_watson);
        final String pregunta = msg_to_watson;
        final String userText_analizar = msg_to_watson;
        //final String userText_a_traducir = msg_to_watson;
        Log.d(APP_TAG_NOVABOT, "REVISANDO - PARAMETRO RECIBIDO: " + pregunta);
        Log.d(APP_TAG_NOVABOT, "REVISANDO - MSG ENVIADO: " + pregunta);


        Thread thread = new Thread(new Runnable() {
            public void run() {
            try {
                Log.d(APP_TAG_NOVABOT, " INICIO CREDENCIALES PARA EL BOT");
                //antiguo
                /*
                final ConversationService service = new ConversationService(ConversationService.VERSION_DATE_2016_09_20);
                service.setUsernameAndPassword("bdfc8ba7-5d73-49cb-a410-1ac0c233f5e7", "jVEDCP3Ey4U2");
                */
                Log.d(APP_TAG_NOVABOT, " FIN CREDENCIALES PARA EL BOT");

                /////------------TONE ANALYZER
                /*
                Thread thread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        ta_service = new ToneAnalyzer(ToneAnalyzer.VERSION_DATE_2016_05_19);
                        ta_service.setUsernameAndPassword(username_TA, password_TA);

                        // Call the service and get the tone
                        ToneAnalysis tone = ta_service.getTone(userText_analizar, null).execute();//
                        Log.d("SIMPLETONEA", tone.toString());

                        for (ToneCategory tc : tone.getDocumentTone().getTones()) {
                            Log.d("SIMPLEToneA", "=============== NEW CATEGORY: *" + tc.getName() + "*==================");
                            for (ToneScore ts : tc.getTones()) {

                                switch (ts.getName()) {
                                    case "Anger":
                                        //
                                        if (ts.getScore() >= 0.75) {
                                            Log.d("Control IF", ">=0.75: " + ts.getName() + ", Score: " + String.valueOf(ts.getScore()));
                                        }
                                        break;

                                    case "Disgust":
                                        //
                                        if (ts.getScore() >= 0.75) {
                                            Log.d("Control IF", ">=0.75: " + ts.getName() + ", Score: " + String.valueOf(ts.getScore()));
                                        }
                                        break;

                                    case "Fear":
                                        if (ts.getScore() >= 0.75) {
                                            Log.d(APP_TAG_NOVA_TONEANALYSER, "Control IF >=0.75: " + ts.getName() + ", Score: " + String.valueOf(ts.getScore()));
                                            Log.d(APP_TAG_NOVA_TONEANALYSER, userText_analizar);

                                            String claveFear75 = "Miedo75";
                                            MessageRequest newMessage = new MessageRequest.Builder().inputText(claveFear75).context(context).build();
                                            MessageResponse response = service.message("75bffb0a-149e-4ce0-8206-6d68921e3b5a", newMessage).execute(); //workspaceID, message

                                            if (response.getOutput() != null && response.getOutput().containsKey("text")) {
                                                final String mensajeDeAlivio = response.getOutput().get("text").toString().replace("[", "").replace("]", "");

                                                Log.d(APP_TAG_NOVA_TONEANALYSER, mensajeDeAlivio);

                                                executeWatsonTTS("Fear " + String.valueOf((int) (ts.getScore() * 100)) + "%. " + mensajeDeAlivio.toString());
                                            } else {
                                                  Log.d(APP_TAG_NOVA_TONEANALYSER, "Texto vacio: -" + userText_analizar + "-");
                                              }
                                          }
                                          break;

                                    case "Joy":
                                        //
                                        if (ts.getScore() >= 0.75) {
                                            Log.d(APP_TAG_NOVA_TONEANALYSER, "Control IF >=0.75: " + ts.getName() + ", Score: " + String.valueOf(ts.getScore()));
                                            Log.d(APP_TAG_NOVA_TONEANALYSER, userText_analizar);

                                            String claveJoy75 = "Alegria75";
                                            MessageRequest newMessage = new MessageRequest.Builder().inputText(claveJoy75).context(context).build();
                                            MessageResponse response = service.message("75bffb0a-149e-4ce0-8206-6d68921e3b5a", newMessage).execute();

                                            if (response.getOutput() != null && response.getOutput().containsKey("text")) {
                                                final String mensajeDeAlivio = response.getOutput().get("text").toString().replace("[", "").replace("]", "");

                                                Log.d(APP_TAG_NOVA_TONEANALYSER, mensajeDeAlivio);

                                                executeWatsonTTS("Joy " + String.valueOf((int) (ts.getScore() * 100)) + "%. " + mensajeDeAlivio.toString());
                                            } else {
                                                  Log.d(APP_TAG_NOVA_TONEANALYSER, "Texto vacio: -" + userText_analizar + "-");
                                             }
                                        }
                                        break;

                                    case "Sadness":
                                        //
                                        if (ts.getScore() >= 0.75) {
                                            Log.d(APP_TAG_NOVA_TONEANALYSER, "Control IF >=0.75: " + ts.getName() + ", Score: " + String.valueOf(ts.getScore()));
                                            Log.d(APP_TAG_NOVA_TONEANALYSER, userText_analizar);

                                            String claveSadness75 = "Tristeza75";
                                            MessageRequest newMessage = new MessageRequest.Builder().inputText(claveSadness75).context(context).build();
                                            MessageResponse response = service.message("75bffb0a-149e-4ce0-8206-6d68921e3b5a", newMessage).execute();

                                            if (response.getOutput() != null && response.getOutput().containsKey("text")) {
                                               final String mensajeDeAlivio = response.getOutput().get("text").toString().replace("[", "").replace("]", "");

                                                Log.d(APP_TAG_NOVA_TONEANALYSER, mensajeDeAlivio);

                                                executeWatsonTTS("Sadness " + String.valueOf((int) (ts.getScore() * 100)) + "%. " + mensajeDeAlivio.toString());
                                            } else {
                                                    Log.d(APP_TAG_NOVA_TONEANALYSER, "Texto vacio: -" + userText_analizar + "-");
                                         }

                                        }
                                        break;

                                    default:
                                        //
                                      break;
                                    }

                                }
                            }
                        }
                    });
                    */

                    /////-----FIN -------TONE ANALYZER

            Thread thread2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    ////////------------------ Conversation 2018
                    com.ibm.watson.developer_cloud.conversation.v1.model.Context context = null;

                    Conversation service = new Conversation(Conversation.VERSION_DATE_2017_05_26);
                    service.setUsernameAndPassword(CONVERSATION_SERVICE_USERNAME, CONVERSATION_SERVICE_PASSWORD);
                    InputData input = new InputData.Builder(pregunta).build();
                    MessageOptions options = new MessageOptions.Builder(CONVERSATION_SERVICE_WORKSPACE).input(input).context(context).build();
                    MessageResponse response = service.message(options).execute();
                    Log.d(APP_TAG_NOVABOT, "=============================: "+response.getOutput().get("text").toString().replace("[","").replace("]",""));
                    ////////------------------  FIN - Conversation 2018

                    /*if (response.getContext() != null) {
                           context.clear();
                           context = response.getContext();
                    }*/
                    if (response != null) {
                        if (response.getOutput() != null && response.getOutput().containsKey("text")) {
                            final String outputmessage = response.getOutput().get("text").toString().replace("[", "").replace("]", "");

                            //Menssaje RESPUESTA recibido desde WATSON
                            Log.d(APP_TAG_NOVABOT, "MSG RECIBIDO DE WATSON: -" + outputmessage + "-");
                            Log.d(APP_TAG_NOVABOT, "VERIFICANDO - MSG RECIBIDO DE WATSON: " + outputmessage);//getRespuestaDeWatson());
                            // tv_consola.setText(outputmessage.toString()); //ESTO DETENIA QUE SE EJECUTE EL RESTO

                            /*
                            //control clima
                            Log.d(APP_TAG_NOVABOT, "Antes de executeWatson ");
                            final String respuestaClaveWeather = "Here is the weather forecast";

                            if (outputmessage.equalsIgnoreCase(respuestaClaveWeather)) {
                                Log.d(APP_TAG_NOVABOT, "ENTRA AL IF --- respuestaClaveWeather:-" + respuestaClaveWeather + "-");

                                Function.placeIdTask asyncTask = new Function.placeIdTask(new Function.AsyncResponse() {
                                   public void processFinish(String weather_city, String weather_description, String weather_temperature, String weather_humidity, String weather_pressure, String weather_updatedOn, String weather_iconText, String sun_rise) {

                                      Log.d(APP_TAG_NOVABOT, "ENTRO A FUNCTION");

                                      Log.d(APP_NOVA_WEATHER, "weather_city: " + weather_city);
                                      Log.d(APP_NOVA_WEATHER, "weather_updatedOn: " + weather_updatedOn);
                                      Log.d(APP_NOVA_WEATHER, "weather_description --ANTES: " + weather_description);
                                      Log.d(APP_NOVA_WEATHER, "weather_temperature: " + weather_temperature);
                                      Log.d(APP_NOVA_WEATHER, "weather_humidity: " + weather_humidity);
                                      Log.d(APP_NOVA_WEATHER, "weather_pressure: " + weather_pressure);

                                      Log.d("APP_NOVA_WEATHER", "=============== REPORTE DEL CLIMA ==================");
                                      final String watsonWeatherTexto = respuestaClaveWeather + " " +
                                          weather_city + ". " +
                                          weather_temperature + ". " +
                                          weather_description + ". " +
                                          "Humidity: " + weather_humidity + ". " +
                                          "Pressure: " + weather_pressure;
                                      Log.d("APP_NOVA_WEATHER", "=============== " + watsonWeatherTexto + " ==================");

                                      executeWatsonTTS(watsonWeatherTexto);
                                      //executeWatsonTTS(weather_city);

                                 }
                            });

                            asyncTask.execute("-17.389500", "-66.156799"); //Cochabamba
                            //asyncTask.execute("47.066669", "15.450000"); //Graz

                            ////////----------------

                            } else {
                            */
                            //executeWatsonTTS(outputmessage);
                            hablaWatsonTTS(outputmessage);
                            Log.d(APP_TAG_NOVABOT, "Despues de executeWatson ");
                            //}
                        }
                    }

                }
                    //}).start();
            });

                /*
                //tone analizer
                thread1.start();
                thread1.join();
                */
                thread2.start();
                thread2.join();

            } catch (Exception e) {
                    e.printStackTrace();
              }
            }
        });

        thread.start();
    }

    /**
     * TTS
     */
    private void hablaWatsonTTS(String textoParaHablar){
        TextToSpeech service = new TextToSpeech();
        service.setUsernameAndPassword(TTS_SERVICE_USERNAME, TTS_SERVICE_PASSWORD);

        try {
            Log.d(APP_TAG_NOVABOT, "======== 1");
            streamPlayer = new StreamPlayer();

            Log.d(APP_TAG_NOVABOT, "======== 2");
            streamPlayer.playStream(service.synthesize(textoParaHablar, Voice.ES_SOFIA).execute());

            Log.d(APP_TAG_NOVABOT, "======== 3");

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * MODULO: GRABACION
     */
    private void enableButton(int id, boolean isEnable) {
        ((Button) findViewById(id)).setEnabled(isEnable);
    }

    /**
     * MODULO: GRABACION
     */
    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart, !isRecording);
        enableButton(R.id.btnStop, isRecording);
    }

    /**
     * MODULO: GRABACION
     */
    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }
        Log.d("RUTA DE GRABACION: ", "--------- " + file.getAbsolutePath());
        setPathDirectorio(file.getAbsolutePath());
        String archivoGrabado = file.getAbsolutePath() + "/" + AUDIO_RECORDER_FILE_NAME + AUDIO_RECORDER_FILE_EXT_WAV; //MODIFICADO 2

        Log.d("RUTA DE GRABACION: ", archivoGrabado);
        return archivoGrabado;
    }

    /**
     * MODULO: GRABACION
     */
    private String getTempFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }
        File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

        if (tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    /**
     * MODULO: GRABACION
     */
    private void startRecording() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);

        int i = recorder.getState();
        if (i == 1)
            recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");

        recordingThread.start();
    }

    /**
     * MODULO: GRABACION
     */
    private void writeAudioDataToFile() {
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read = 0;

        if (null != os) {
            while (isRecording) {
                read = recorder.read(data, 0, bufferSize);

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * MODULO: GRABACION
     */
    private void stopRecording() {
        if (null != recorder) {
            isRecording = false;

            int i = recorder.getState();
            if (i == 1)
                recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        copyWaveFile(getTempFilename(), getFilename());
        deleteTempFile();
    }

    /**
     * MODULO: GRABACION
     */
    private void deleteTempFile() {
        File file = new File(getTempFilename());

        file.delete();
    }

    /**
     * MODULO: GRABACION
     */
    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            Log.d(APP_TAG_NOVA_RECORD, "File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * MODULO: GRABACION
     */
    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
    public Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MsgEnum message = MsgEnum.getMsgEnum(msg.what);
            switch(message) {
                case MSG_ACTIVE:
                    activeTimes++;
                    Log.d("SNOWBOY", " ----> Detected " + activeTimes + " times");
                    // Toast.makeText(Demo.this, "Active "+activeTimes, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_INFO:
                    Log.d("SNOWBOY"," ----> "+message);
                    break;
                case MSG_VAD_SPEECH:
                    Log.d("SNOWBOY"," ----> normal voice");
                    break;
                case MSG_VAD_NOSPEECH:
                    Log.d("SNOWBOY"," ----> no speech");
                    break;
                case MSG_ERROR:
                    Log.d("SNOWBOY"," ----> " + msg.toString());
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };
    @Override
    public void onDestroy() {
        restoreVolume();
        recordingSnowBoyThread.stopRecording();
        super.onDestroy();
    }
    private void setProperVolume() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        preVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d("SNOWBOY", " ----> preVolume = "+preVolume);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.d("SNOWBOY", " ----> maxVolume = "+maxVolume);
        int properVolume = (int) ((float) maxVolume * 0.2);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, properVolume, 0);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d("SNOWBOY", " ----> currentVolume = "+currentVolume);
    }

    private void restoreVolume() {
        if(preVolume>=0) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, preVolume, 0);
            Log.d("SNOWBOY", " ----> set preVolume = "+preVolume);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            Log.d("SNOWBOY", " ----> currentVolume = "+currentVolume);
        }
    }

}
