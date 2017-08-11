package com.vrublack.nutrition;


import com.vrublack.nutrition.console.LocalSearchHistory;
import com.vrublack.nutrition.console.LocalUSDAFoodDatabase;
import com.vrublack.nutrition.core.search.DescriptionBase;
import com.vrublack.nutrition.core.search.LevenshteinFoodSearch;
import com.vrublack.nutrition.core.Pair;
import com.vrublack.nutrition.core.SearchResultItem;
import com.vrublack.nutrition.core.search.HashFoodSearch;
import com.vrublack.nutrition.core.search.FoodSearch;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SearchEvaluation
{
    private LocalSearchHistory hist = new LocalSearchHistory();

    private LocalUSDAFoodDatabase db = new LocalUSDAFoodDatabase();    // don't use search history

    public void evaluateWithHistory() throws FileNotFoundException
    {
        System.out.println("--EVALUATING WITH HISTORY--");
        evaluatePairs(hist.getQueryIdPairs());
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

    public void evaluateWithCustomPairs() throws FileNotFoundException
    {
        List<Pair<String, String>> pairs = loadPairs("src/test/resources/search_pairs.csv");
        System.out.println("--EVALUATING WITH CUSTOM PAIRS--");
        evaluatePairs(pairs);
    }

    private void evaluatePairs(List<Pair<String, String>> queryIdPairs) throws FileNotFoundException
    {
        float levenshteinScore = 0;
        float stemScore = 0;
        long levenshteinTime = 0;
        long stemTime = 0;
        int count = 0;

        FoodSearch levenshteinSearch = new LevenshteinFoodSearch(db.getSearchableFoodItems());
        FoodSearch stemSearch = new HashFoodSearch(db.getCanonicalSearchableFoodItems(), DescriptionBase.getDescriptionBase(new FileInputStream("food_english.0")));

        for (int i = 0; i < queryIdPairs.size(); i++)
        {
            Pair<String, String> pair = queryIdPairs.get(i);
            if (Character.isDigit(pair.second.charAt(0)))   // disregard user food items
            {
                count++;

                long start = System.nanoTime();
                float levenshteinResult = evaluateSearch(pair.first, pair.second, levenshteinSearch);
                levenshteinTime += System.nanoTime() - start;
                start = System.nanoTime();
                float stemResult = evaluateSearch(pair.first, pair.second, stemSearch);
                stemTime += System.nanoTime() - start;
                levenshteinResult = Math.min(levenshteinResult, 20);
                stemResult = Math.min(stemResult, 20);

                if (levenshteinResult < stemResult)
                    System.out.printf("Worse result (%.1f != %.1f)\n", levenshteinResult, stemResult);

                levenshteinScore += levenshteinResult;
                stemScore += stemResult;
                // System.out.println("Evaluated " + i + " out " + queryIdPairs.size() + " with score " + result);
            }
        }

        System.out.printf("levenshtein: %.1f, stem: %.1f\n\n", levenshteinScore, stemScore);
        System.out.printf("levenshtein avg time: %.1f ms, stem avg time: %.1f ms\n",
                levenshteinTime / (float) count / 1000000, stemTime / (float) count / 1000000);
    }

    private float evaluateSearch(String searchStr, String expectedId, FoodSearch search)
    {
        List<SearchResultItem> results = db.search(searchStr, new LocalSearchHistory());
        int index = results.indexOf(new SearchResultItem(expectedId, null, null, -1, -1));
        if (index == -1)
            return Float.POSITIVE_INFINITY;
        else
            return index * index;
    }

    public static void main(String[] args) throws FileNotFoundException
    {
        SearchEvaluation eval = new SearchEvaluation();
        // eval.evaluateWithCustomPairs();
        eval.evaluateWithHistory();
    }
}
