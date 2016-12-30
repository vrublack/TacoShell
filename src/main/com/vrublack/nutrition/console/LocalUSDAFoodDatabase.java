package com.vrublack.nutrition.console;

import com.vrublack.nutrition.core.DummySearchHistory;
import com.vrublack.nutrition.core.SearchHistory;
import com.vrublack.nutrition.core.usda.USDAFoodDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class LocalUSDAFoodDatabase extends USDAFoodDatabase
{
    private final static String FILENAME = "ABBREV_CUST.txt";

    private SearchHistory searchHistory;

    public LocalUSDAFoodDatabase()
    {
        searchHistory = new LocalSearchHistory();

        // parent constructor loads ascii file
    }

    public LocalUSDAFoodDatabase(boolean useSearchHistory)
    {
        if (useSearchHistory)
            searchHistory = new LocalSearchHistory();
        else
            searchHistory = new DummySearchHistory();

        // parent constructor loads ascii file
    }

    @Override
    protected SearchHistory getSearchHistory()
    {
        return searchHistory;
    }

    @Override
    public BufferedReader getBufferedReader() throws FileNotFoundException
    {
        return new BufferedReader(new FileReader(new File(FILENAME)));
    }
}
