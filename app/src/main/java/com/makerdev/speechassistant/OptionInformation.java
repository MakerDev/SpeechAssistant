package com.makerdev.speechassistant;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by raki2 on 2018-01-30.
 */

//옵션정보 저장할 클래스
public class OptionInformation extends AppCompatActivity{
    public static final String SAVE_FILE_NAME = "Option_Saved";
    public static final int MAX_LINES_TO_SHOW = 10;
    public static final int MAX_LINES_TO_LOOK_AGAIN = 10;

    private static boolean isCheckPassedLines = false;
    private static int numOfLinesToCheckAgain = 7;
    private static int numOfLinesToShow = 5;

    //TODO : 현재 / 다음 줄에 색칠하는 옵션 구현
    private static boolean isDrawUnderScore = false;

    public static void SetIsCheckPassedLines(boolean b)
    {
        isCheckPassedLines = b;
    }

    public static boolean GetIsCheckPassedLines()
    {
        return isCheckPassedLines;
    }

    public static void SetNumOfLinesToCheckAgain(int num)
    {
        numOfLinesToCheckAgain = num;

        if(numOfLinesToCheckAgain < 0)
        {
            numOfLinesToCheckAgain = 0;
        }
        else if(numOfLinesToCheckAgain > MAX_LINES_TO_LOOK_AGAIN)
        {
            numOfLinesToCheckAgain = MAX_LINES_TO_LOOK_AGAIN;
        }
    }

    public static int GetNumOfLinesToCheckAgain()
    {
        return numOfLinesToCheckAgain;
    }

    public static void SetNumOfLinesToShow(int num)
    {
        numOfLinesToShow = num;

        if(numOfLinesToShow < 0)
        {
            numOfLinesToShow = 0;
        }
        else if(numOfLinesToShow > MAX_LINES_TO_SHOW)
        {
            numOfLinesToShow = MAX_LINES_TO_SHOW;
        }
    }

    public static int GetNumOfLinesToShow()
    {
        return numOfLinesToShow;
    }

}
