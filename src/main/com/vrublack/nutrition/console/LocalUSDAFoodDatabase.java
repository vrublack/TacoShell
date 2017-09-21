package com.vrublack.nutrition.console;

import com.vrublack.nutrition.core.DummySearchHistory;
import com.vrublack.nutrition.core.SearchHistory;
import com.vrublack.nutrition.core.search.DescriptionBase;
import com.vrublack.nutrition.core.usda.USDAFoodDatabase;

import java.io.*;

public class LocalUSDAFoodDatabase extends USDAFoodDatabase
{
    private final static String FILENAME = "ABBREV_CUST.txt";


    public LocalUSDAFoodDatabase()
    {
        // parent constructor loads ascii file
    }

    @Override
    public BufferedReader getBufferedReader() throws FileNotFoundException
    {
        return new BufferedReader(new FileReader(new File(FILENAME)));
    }

    @Override
    public DescriptionBase getDescriptionBase() throws FileNotFoundException
    {
        return DescriptionBase.getDescriptionBase(new FileInputStream("food_english.0"), new FileInputStream("food_scored.txt"));
    }
}
