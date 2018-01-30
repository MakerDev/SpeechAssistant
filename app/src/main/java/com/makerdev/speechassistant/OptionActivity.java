package com.makerdev.speechassistant;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

//TODO : 버튼이랑 스위치는 굳이 멤버로 둘 필요 없는 듯?
public class OptionActivity extends AppCompatActivity {
    TextView outputNumToShow;
    TextView outputNumToLookAgain;

    Button btn_upNumToShow;
    Button btn_downNumToShow;
    Button btn_upNumToLookAgain;
    Button btn_downNumToLookAgain;
    Button saveButton;

    android.support.v7.widget.SwitchCompat switch_enableLookAgain;

    LinearLayout passedSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        outputNumToLookAgain = findViewById(R.id.output_numToLookAgain);
        outputNumToShow = findViewById(R.id.output_numToShow);

        btn_upNumToLookAgain = findViewById(R.id.btn_numPassedUp);
        btn_upNumToShow = findViewById(R.id.btn_numToShowUp);
        btn_downNumToLookAgain = findViewById(R.id.btn_numPassedDown);
        btn_downNumToShow = findViewById(R.id.btn_numToShowDown);
        saveButton = findViewById(R.id.btn_saveOption);

        switch_enableLookAgain = findViewById(R.id.switchCheckPassed);

        passedSetting = findViewById(R.id.PassedSettings);

        SetupView();
    }

    public void OnClickButton(View v){
        int id = v.getId();

        switch(id){
            case R.id.btn_numPassedUp:
                OptionInformation.SetNumOfLinesToCheckAgain(OptionInformation.GetNumOfLinesToCheckAgain()+1);
                break;

            case R.id.btn_numPassedDown:
                OptionInformation.SetNumOfLinesToCheckAgain(OptionInformation.GetNumOfLinesToCheckAgain()-1);
                break;

            case R.id.btn_numToShowUp:
                OptionInformation.SetNumOfLinesToShow(OptionInformation.GetNumOfLinesToShow()+1);
                break;

            case R.id.btn_numToShowDown:
                OptionInformation.SetNumOfLinesToShow(OptionInformation.GetNumOfLinesToShow()-1);
                break;

            case R.id.btn_saveOption:
                SaveOptionInfo();
                break;

            case R.id.switchCheckPassed:
                OptionInformation.SetIsCheckPassedLines(switch_enableLookAgain.isChecked());
                break;

            default:
                break;
        }

        if(id!=R.id.btn_saveOption)
        {
            SetupView();
        }
    }

    private void SetupView()
    {
        outputNumToShow.setText("표시할 줄 수 : " + String.valueOf(OptionInformation.GetNumOfLinesToShow()));
        outputNumToLookAgain.setText("다시 볼 줄 수" + String.valueOf(OptionInformation.GetNumOfLinesToCheckAgain()));

        if(OptionInformation.GetIsCheckPassedLines())
        {
            passedSetting.setVisibility(View.VISIBLE);
        }
        else
        {
            passedSetting.setVisibility(View.INVISIBLE);
        }
    }


    private void SaveOptionInfo()
    {
        SharedPreferences sf = getSharedPreferences(OptionInformation.SAVE_FILE_NAME, 0);
        SharedPreferences.Editor editor = sf.edit();

        editor.putBoolean("IsCheckPassedLines", OptionInformation.GetIsCheckPassedLines());
        editor.putInt("NumOfLinesToShow", OptionInformation.GetNumOfLinesToShow());
        editor.putInt("NumOfLinesToLookAgain", OptionInformation.GetNumOfLinesToCheckAgain());

        editor.apply();
    }

}
