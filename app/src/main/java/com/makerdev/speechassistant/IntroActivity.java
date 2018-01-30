package com.makerdev.speechassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class IntroActivity extends AppCompatActivity {
    Button btn_setting;
    Button btn_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        LoadOptionInfo();

        btn_setting = findViewById(R.id.btn_setting);
        btn_start = findViewById(R.id.btn_start);
    }

    public void OnIntroButtonClicked(View v)
    {
        int id = v.getId();
        Intent intent;

        switch(id){
            case R.id.btn_setting:
                intent = new Intent(getApplicationContext(), OptionActivity.class);
                startActivity(intent);

                break;

            case R.id.btn_start:
                intent = new Intent(getApplicationContext(), FileSelectActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }

    //TODO:퍼미션 코드 전부 여기로 옮기기
    //TODO:하드코딩된 문자열 string.xml로 옮기기
    private void LoadOptionInfo()
    {
        SharedPreferences sf = getSharedPreferences(OptionInformation.SAVE_FILE_NAME, 0);
        int numLinesToShow = sf.getInt("NumOfLinesToShow", -1);
        int numLinesToLookAgain = sf.getInt("NumOfLinesToLookAgain", -1);
        boolean isCheckPassed = sf.getBoolean("IsCheckPassedLines", false);

        if(numLinesToShow==-1 || numLinesToLookAgain==-1)
        {
            return;
        }

        OptionInformation.SetNumOfLinesToShow(numLinesToShow);
        OptionInformation.SetNumOfLinesToCheckAgain(numLinesToLookAgain);
        OptionInformation.SetIsCheckPassedLines(isCheckPassed);
    }
}
