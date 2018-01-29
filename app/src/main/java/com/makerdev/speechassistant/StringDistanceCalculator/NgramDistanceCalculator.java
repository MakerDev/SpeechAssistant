package com.makerdev.speechassistant.StringDistanceCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raki2 on 2018-01-29.
 */

public class NgramDistanceCalculator {
    public static double CalcSimilarityByNgram(String sa, String sb, int num)
    {
        if(sa==null || sb==null || sa.length()<3 || sa.length()<3)
            return -1;

        List<String> a = Ngram(sa, num);
        List<String> b = Ngram(sb, num);

        if(a.size() <= 0)
            return -1;

        double count = 0.0;

        for(String i : a)
        {
            for(String j : b)
            {
                if(i.equals(j))
                {
                    count+=1;
                }
            }
        }

        return count/a.size();

    }

    private static List<String> Ngram(String s, int num)
    {
        List<String> res = new ArrayList<>();

        int slen = s.length() - num + 1;

        for(int i=0 ; i<slen ; i++)
        {
            String sub = s.substring(i, i+num);
            res.add(sub);
        }

        return res;
    }

}
