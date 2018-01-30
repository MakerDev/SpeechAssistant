package com.makerdev.speechassistant;

import android.Manifest;
import android.annotation.TargetApi;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.makerdev.speechassistant.stt_engine.TranslatorFactory;
import com.makerdev.speechassistant.stt_engine.utils.ConversionCallaback;

import com.makerdev.speechassistant.StringDistanceCalculator.NgramDistanceCalculator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssistantActivity extends AppCompatActivity implements ConversionCallaback {
    private static final int STT = 1;
    private static int CURRENT_MODE = -1;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    final private int STT_DELAY_MILLISECONDS = 400;

    private TextView sttOutput;
    private TextView errConsole;
    private TextView continueOutput;

    //텍스트뷰를 하드코딩할 시 사용할 배열
    private TextView[] lines = new TextView[5];

    private LinearLayout contentView;
    private Button speechToText;
    private boolean isContinueRecognition = false;

    private List<String> contents = new ArrayList<>();
    private int numOfContentLines = 0;
    private int currentLineIdx = 0;

    private String filePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        Intent intent = getIntent();
        filePath = intent.getStringExtra("filePath");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 21) {
            // Pain in A$$ Marshmallow + Permission APIs
            requestForPermission();
        } else {
            // Pre-Marshmallow
            setUpView();
        }


    }

    private void ReadFileFromStorage()
    {
        File file = new File(filePath);
        FileReader fr;
        BufferedReader bufferedReader;

        String line;

        try
        {
            fr = new FileReader(file);
            bufferedReader = new BufferedReader(fr);

            do {
                line = bufferedReader.readLine();
                contents.add(line);
            } while (line!=null);

            bufferedReader.close();
            fr.close();
        }
        catch(Exception e)
        {
            Snackbar.make(getWindow().getDecorView().getRootView(), "파일 읽기 실패", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
//            Toast.makeText(this, "파일 읽기 실패 ", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * Set up listeners on View
     */
    private void setUpView() {
        //Speech to text output
        sttOutput = (TextView) findViewById(R.id.stt_output);
        //Failure message
        errConsole = (TextView) findViewById(R.id.error_output);
        //Button to trigger STT
        speechToText = (Button) findViewById(R.id.start_listening);
        //Show Is continuing
        continueOutput = (TextView) findViewById(R.id.continue_Label);
        //Show Content
        contentView = (LinearLayout) findViewById(R.id.content);

        ReadFileFromStorage();
        numOfContentLines = contents.size();

        SetContentsByInflater(currentLineIdx);
        //Listen and convert convert speech to text
        speechToText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isContinueRecognition)
                {
                    //TODO : 현재 인식이 끝나지 않았으면 끝날때까지 기다렸다가 시작

                    StartSTT();
                    speechToText.setText("인식중단");
                }
                else
                {
                    speechToText.setText("인식시작");
                }

                isContinueRecognition = !isContinueRecognition;
                String output = "Continuing ? = " + String.valueOf(isContinueRecognition);
                continueOutput.setText(output);
            }
        });
    }

    private void StartSTT()
    {
        Snackbar.make(getWindow().getDecorView().getRootView(), "말하세요", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

//        Toast.makeText(this, "말하세요 ", Toast.LENGTH_SHORT).show();

        //Ask translator factory to start speech to text conversion
        //Hello There is optional
        TranslatorFactory.getInstance().
                getTranslator(TranslatorFactory.TRANSLATOR_TYPE.SPEECH_TO_TEXT, this).
                initialize("Hello There", this);

        CURRENT_MODE = STT;
    }

    //returns the best similar sentences's idx
    private int FindMostSimilarSentence(String said, int startIdx)
    {
        int bestIdx = -1;
        double bestSimilarity = 0.0;

        int count = startIdx + 10;

        if(OptionInformation.GetIsCheckPassedLines())
        {
            startIdx -= OptionInformation.GetNumOfLinesToCheckAgain();

            startIdx = (startIdx<0)?0:startIdx;
        }

        if(count > numOfContentLines)
        {
            count = numOfContentLines;
        }

        for(int i = startIdx ; i < count ; i++)
        {
            double similarity = NgramDistanceCalculator.CalcSimilarityByNgram(said, contents.get(i), 2);

            if(similarity > bestSimilarity)
            {
                bestIdx = i;
                bestSimilarity = similarity;
            }

        }

        TextView output = findViewById(R.id.similarity_output);
        output.setText(String.valueOf(bestSimilarity));

        //TODO:임계값 정확히 설정하기
        if(bestSimilarity <= 0.33)
            bestIdx = -1;

        return bestIdx;
    }

    //표시할 내용을 설정
    private void SetContents(int currentIdx)
    {
        int len = lines.length;

        for(int i=0 ; i<len ; i++)
        {
            int idx = currentIdx-2+i;

            if(idx<0)
            {
                idx = 0;
            }
            else if(idx>=numOfContentLines)
            {
                idx = numOfContentLines-1;
                lines[i].setText(contents.get(idx));
                break;
            }

            lines[i].setText(contents.get(idx));
        }
    }

    private void SetContentsByInflater(int currentIdx)
    {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        contentView.removeAllViewsInLayout();

        int startIdx = currentIdx-OptionInformation.GetNumOfLinesToShow()/2;
        int endIdx = startIdx + OptionInformation.GetNumOfLinesToShow();

        if(startIdx<=0)
        {
            startIdx = 0;
            endIdx = startIdx+OptionInformation.GetNumOfLinesToShow();
        }

        endIdx = (endIdx>numOfContentLines)?numOfContentLines:endIdx;



        for(int i=startIdx ; i<endIdx ; i++)
        {
            TextView text = (TextView) inflater.inflate(R.layout.textview_content, contentView, false);
            text.setText(contents.get(i));
            text.setPadding(0, 15, 0 ,10);
            contentView.addView(text);
        }
    }

    /**
     * On success is common of both translator so if you had triggered Speech to text show converted text on textview
     *
     * @param result
     */
    @Override
    public void onSuccess(String result) {
        //Toast.makeText(this, "Result " + result, Toast.LENGTH_SHORT).show();

        switch (CURRENT_MODE) {
            case STT:
                sttOutput.setText(result);

                int idx;
                int startIdx = currentLineIdx;

                if(OptionInformation.GetIsCheckPassedLines())
                {
                    //TODO : 최대 몇 줄까지 돌아볼지 정할 옵션 만들기
                    for(int i=1 ; i<OptionInformation.GetNumOfLinesToCheckAgain() ; i++)
                    {
                            if(startIdx - i <= 0)
                            {
                                startIdx = 0;
                                break;
                            }
                    }
                }

                idx = FindMostSimilarSentence(result, startIdx);

                if(idx>0)
                {
                    currentLineIdx = idx;

                    //TODO: 나중에 idx에 따른 에러처리하기
                    //TODO: 성능측정하여 TextView와 Inflater중 어떤 거 쓸 지 정하기
                    //SetContents(currentLineIdx);
                    SetContentsByInflater(currentLineIdx);
                }


                if(isContinueRecognition)
                {
                    Handler delayHandler = new Handler();
                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            StartSTT();
                        }
                    }, STT_DELAY_MILLISECONDS);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onCompletion() {
        //Toast.makeText(this, "Done ", Toast.LENGTH_SHORT).show();
    }


    /**
     * If translator failed error message will be come in this callback
     *
     * @param errorMessage
     */
    @Override
    public void onErrorOccured(String errorMessage) {
        errConsole.setText(errorMessage);

        //TODO:추후 중대한 에러 검증 하기
        if(isContinueRecognition)
        {
            if(isContinueRecognition)
            {
                Handler delayHandler = new Handler();
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        StartSTT();
                    }
                }, STT_DELAY_MILLISECONDS);
            }
        }
    }

    /**
     * Request Permission
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void requestForPermission() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();

        if (!isPermissionGranted(permissionsList, Manifest.permission.RECORD_AUDIO))
            permissionsNeeded.add("Require for Speech to text");

        if(!isPermissionGranted(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add(("Require for Txt Reading"));

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {

                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);

                for (int i = 1; i < permissionsNeeded.size(); i++) {
                    message = message + ", " + permissionsNeeded.get(i);
                }

                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

        //add listeners on view
        setUpView();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isPermissionGranted(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);

            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);

                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted

                    //add listeners on view
                    setUpView();

                } else {
                    // Permission Denied
                    Toast.makeText(this, "Some Permissions are Denied Exiting App", Toast.LENGTH_SHORT)
                            .show();

                    finish();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}