package com.vrublack.nutrition.core;

/**
 * Dummy implementation of SearchFeedback
 */
public class DummySearchHistory implements SearchHistory
{
    @Override
    public String getNDBNumberForSearchResult(String searchString)
    {
        return null;
    }

    @Override
    public void putNDBNumberForSearchResult(String searchString, String selectedNDBNumber)
    {

    }
}
