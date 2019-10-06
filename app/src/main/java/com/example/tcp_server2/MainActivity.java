package com.example.tcp_server2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tcp_server2.dbManagement.ValuesDbContract;
import com.example.tcp_server2.dbManagement.ValuesDbProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    final static int SERVER_PORT = 6000;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    final static String MESSAGE_KEY_TEMP = "temp";
    final static String MESSAGE_KEY_LIGHT = "light";
    final static String MESSAGE_KEY_BUTTN1 = "buttn1";
    final static String MESSAGE_KEY_TITLE = "title";

    private Thread mMainThread = null;

    public TextView message;
    public TextView tvTemp;
    public TextView tvLight;
    public TextView tvButtn1;
    public Button btToggleOutput;
    public Button btSpeek;
    private ImageView iwStarSymbol;


    public Boolean buttonClicked = false;

    ServerSocket sSocket;
    Socket mClient;
    //View currentView;

    final Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            Bundle bundle;

            bundle = msg.getData();
            if (msg.what == 1){

                Calendar calendar = Calendar.getInstance();
                String mYear    = String.valueOf(calendar.get(Calendar.YEAR));
                String mMonth   = String.valueOf(calendar.get(Calendar.MONTH) + 1);
                String mDay     = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                String mHour    = String.valueOf(calendar.get(Calendar.HOUR));
                String mMinute  = String.valueOf(calendar.get(Calendar.MINUTE));
                String mSecond  = String.valueOf(calendar.get(Calendar.SECOND));

                String mCurrentTime = mYear+"/"+mMonth+"/"+mDay+" "+mHour+":"+mMinute+":"+mSecond;
                String mValue = String.valueOf(bundle.getString(MESSAGE_KEY_TEMP));
                InsertValue(mValue, mCurrentTime);

                message.setText(bundle.getString(MESSAGE_KEY_TITLE));
                tvTemp.setText(bundle.getString(MESSAGE_KEY_TEMP) + " C");
                tvLight.setText(bundle.getString(MESSAGE_KEY_LIGHT));
                if (bundle.getString(MESSAGE_KEY_BUTTN1).equals("0")) {
                    tvButtn1.setText("Closed");
                } else {
                    tvButtn1.setText("Opened");
                }
            }
            updateStartSymbol();
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //currentView = this.findViewById(android.R.id.content);
        message = (TextView) findViewById(R.id.textView_message_from_client);
        message.setText("No data");
        tvTemp = (TextView) findViewById(R.id.textView_temperature);
        tvLight = (TextView) findViewById(R.id.textView_light);
        tvButtn1 = (TextView) findViewById(R.id.textView_buttn1);
        btToggleOutput = (Button) findViewById(R.id.button_send_toggle);
        btSpeek = (Button) findViewById(R.id.button_speek);
        iwStarSymbol = (ImageView) findViewById(R.id.imageView_star);

        btToggleOutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClicked = !buttonClicked;
            }
        });

        btSpeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVoiceRecognitionActivity();
            }
        });

        updateStartSymbol();

        Thread communicationThread = new getData();
        communicationThread.start();

    }

    public void updateStartSymbol(){
        if (buttonClicked){
            iwStarSymbol.setVisibility(View.VISIBLE);
        } else {
            iwStarSymbol.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it
            // could have heard
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //mList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, matches));
            // matches is the result of voice input. It is a list of what the
            // user possibly said.
            // Using an if statement for the keyword you want to use allows the
            // use of any activity if keywords match
            // it is possible to set up multiple keywords to use the same
            // activity so more than one word will allow the user
            // to use the activity (makes it so the user doesn't have to
            // memorize words from a list)
            // to use an activity from the voice input information simply use
            // the following format;
            // if (matches.contains("keyword here") { startActivity(new
            // Intent("name.of.manifest.ACTIVITY")

            if (matches.contains("information")) {
                Log.i("server", "There was word - Information");
                /*try {
                    //PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(Socket.getOutputStream())), true);
                    //out.println("Message");
                    Log.i("server", "LED ON");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/

            }

            if (matches.contains("light on")) {
                Log.i("server", "LED ON");
                buttonClicked = true;
            }

            if (matches.contains("light off")) {
                Log.i("server", "LED OFF");
                buttonClicked = false;
            }
        }
    }

    public class getData extends Thread {
        @Override
        public void run() {
            Bundle b;

            b = new Bundle();
            Log.d("Server", "Start of function...");
            sSocket = null;
            Log.d("Server", "Try to create new server socket");

            try {
                sSocket = new ServerSocket(SERVER_PORT);

                while (!Thread.currentThread().isInterrupted()){
                    String incommingMessage = "";
                    String cleanedMessage = "";
                    String mTemp = "";
                    String mLight = "";
                    String mButtn1 = "";

                    BufferedReader mBufferReader;
                    BufferedWriter mBufferWriter;

                    Log.d("Server", "Try to find client");
                    mClient = sSocket.accept();
                    Log.d("Server", "Waiting for messages on Thread");

                    mBufferWriter = new BufferedWriter(new OutputStreamWriter(mClient.getOutputStream()));

                    if (buttonClicked) {
                        mBufferWriter.write("ON");
                        mBufferWriter.newLine();
                        mBufferWriter.flush();
                        Log.d("Server", "Writing to client ON");
                    } else {
                        mBufferWriter.write("OFF");
                        mBufferWriter.newLine();
                        mBufferWriter.flush();
                        Log.d("Server", "Writing to client OFF");
                    }

                    mBufferReader = new BufferedReader(new InputStreamReader(mClient.getInputStream()));
                    incommingMessage = mBufferReader.readLine() + System.getProperty("line.separator");

                    if (!(incommingMessage.equals(null))) {
                        Log.d("Server", "new message: " + incommingMessage);
                        cleanedMessage = correctMessage(incommingMessage);
                        mTemp = getPart(1, cleanedMessage);
                        mLight = getPart(2, cleanedMessage);
                        mButtn1 = getPart(3, cleanedMessage);
                        Log.d("Server", mTemp + mLight + mButtn1);
                        b.putString(MESSAGE_KEY_TITLE, cleanedMessage);
                        b.putString(MESSAGE_KEY_TEMP, mTemp);
                        b.putString(MESSAGE_KEY_LIGHT, mLight);
                        b.putString(MESSAGE_KEY_BUTTN1, mButtn1);

                        mMainThread = new SetBundle(mHandler,b);
                        mMainThread.start();
                    }

                    if (!mClient.equals(null)){
                        try{
                            Log.d("Server", "Closing client.");
                            mClient.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e){
                Log.d("Server", "Server socket creation failed");
                e.printStackTrace();
            }

            super.run();

        }
    }

    public String getPart(int partNumber, String s){
        String result = "";
        int msgLenght = s.length();
        int partCount = 0;
        boolean gotResult = false;
        int i;

        i = 0;
        if (msgLenght > 2) {
            while ((i < msgLenght-1) &&
                    (partCount != partNumber) &&
                    !gotResult) {

                if (((partNumber - 1) == partCount) && ((s.charAt(i) != ' ') || (i+1 == msgLenght))) {
                    result = result + s.charAt(i);
                }

                i++;

                if ((s.charAt(i) == ' ') || (i+1 == msgLenght)){
                    if ((partCount == partNumber-1)){
                        gotResult =true;
                        if (i+1 == msgLenght) {
                            result = result + s.charAt(i);
                        }
                    }
                    partCount++;
                }
            }
        }
        return result;
    }

    public String correctMessage(String s){
        String result = "";
        String button1Value = "";
        String temporLightValue = "";
        String temporTempValue = "";
        int msgLenght = s.length();
        int i;

        i = msgLenght - 1;
        if (msgLenght >2) {
            while ((s.charAt(i) != 'n') && (i>0)){
                i--;
            }
            i--;
            while ((s.charAt(i) != '/') && (i>0)){
                button1Value = button1Value + s.charAt(i);
                i--;
            }
            i--;
            while ((s.charAt(i) != '/') && (i>0)){
                temporLightValue = temporLightValue + s.charAt(i);
                i--;
            }
            i--;
            while ((s.charAt(i) != '/') && (i>0)){
                temporTempValue = temporTempValue + s.charAt(i);
                i--;
            }
            if (i == 0) {
                temporTempValue = temporTempValue + s.charAt(i);
            }
        }

        temporTempValue = reversString(temporTempValue);
        temporLightValue = reversString(temporLightValue);

        result =  temporTempValue + ' ' + temporLightValue + ' ' + button1Value;
        Log.d("Server", "Result: " +result);
        return result;
    };

    public String reversString(String s){
        String result = "";
        int sLenght = s.length();

        for (int i = sLenght - 1; i>=0; i-- ){
            result = result + s.charAt(i);
        }

        return result;
    }

    public class SetBundle extends Thread {

        private Handler handler;
        private Bundle bundle;

        public SetBundle(Handler h, Bundle b){
            this.handler = h;
            this.bundle = b;
        }

        @Override
        public void run() {
            Message msg = handler.obtainMessage();
            msg.what = 1;
            msg.setData(bundle);
            handler.sendMessage(msg);
            super.run();
        }
    }

    public void startVoiceRecognitionActivity() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Speech recognition demo");
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        }
        catch(ActivityNotFoundException e)
        {
            String appPackageName = "com.google.android.googlequicksearchbox";
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (mClient.isConnected()) {
                mClient.close();
            }
            if (!sSocket.isClosed()) {
                sSocket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void InsertValue(String val, String time) {
        try {
            ContentValues userValues = getValueData(val,time);
            ValuesDbProvider dbProvider = new ValuesDbProvider();
            dbProvider.insert(ValuesDbContract.ValuesEntry.CONTENT_URI, userValues);

        } catch (Exception ex) {
            Log.e("Insert", ex.toString());
        }
    }

    public ContentValues getValueData(String val, String time) {
        ContentValues mValues = new ContentValues();
        mValues.put(ValuesDbContract.ValuesEntry.COLUMN_Value, val);
        mValues.put(ValuesDbContract.ValuesEntry.COLUMN_Time, time);
        return mValues;
    }


}
