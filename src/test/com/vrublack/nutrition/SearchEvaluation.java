package com.vrublack.nutrition;


import com.vrublack.nutrition.console.LocalSearchHistory;
import com.vrublack.nutrition.console.LocalUSDAFoodDatabase;
import com.vrublack.nutrition.core.Pair;
import com.vrublack.nutrition.core.SearchResultItem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SearchEvaluation
{
    private LocalSearchHistory hist = new LocalSearchHistory();

    private LocalUSDAFoodDatabase db = new LocalUSDAFoodDatabase(false);    // don't use search history

    public float evaluateWithHistory()
    {
        System.out.println("--EVALUATING WITH HISTORY--");
        return evaluatePairs(hist.getQueryIdPairs());
    }

    private List<Pair<String, String>> loadPairs(String filename)
    {
        List<Pair<String, String>> pairs = new ArrayList<>();

        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] comps = line.split(",");
                pairs.add(new Pair<>(comps[0].trim(), comps[1].trim()));
            }

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return pairs;
    }

    public float evaluateWithCustomPairs()
    {
        List<Pair<String, String>> pairs = loadPairs("src/test/resources/search_pairs.csv");
        System.out.println("--EVALUATING WITH CUSTOM PAIRS--");
        return evaluatePairs(pairs);
    }

    private float evaluatePairs(List<Pair<String, String>> queryIdPairs)
    {
        float score = 0;
        for (int i = 0; i < queryIdPairs.size(); i++)
        {
            Pair<String, String> pair = queryIdPairs.get(i);
            if (Character.isDigit(pair.second.charAt(0)))   // disregard user food items
            {
                float result = evaluateSearch(pair.first, pair.second);
                result = Math.min(result, 20);
                score += result;
                System.out.println("Evaluated " + i + " out " + queryIdPairs.size() + " with score " + result);
            }
        }

        return score;
    }

    private float evaluateSearch(String searchStr, String expectedId)
    {
        List<SearchResultItem> results = db.search(searchStr);
        int index = results.indexOf(new SearchResultItem(expectedId, null, null, -1, -1));
        float error;
        if (index == -1)
            return Float.POSITIVE_INFINITY;
        else
            return index * index;
    }

    public static void main(String[] args)
    {
        SearchEvaluation eval = new SearchEvaluation();
        float customScore = eval.evaluateWithCustomPairs();
        float historyScore = eval.evaluateWithHistory();

        System.out.printf("Custom score: %.1f, history score: %.1f", customScore, historyScore);
    }
}
