package com.vrublack.nutrition.core.uga;


import com.vrublack.nutrition.core.*;
import com.vrublack.nutrition.core.search.FoodSearch;
import com.vrublack.nutrition.core.search.LevenshteinFoodSearch;
import com.vrublack.nutrition.core.SearchableFoodItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Scrapes the University of Georgia's food services website for meals and nutrition info.
 */
public class UGAFoodServices implements SyncFoodDataSource
{
    private List<UGAFoodItem> items;

    private FoodSearch foodSearch;


    public UGAFoodServices()
    {
        items = UGAScraper.scrapeAllLocations();
        foodSearch = new LevenshteinFoodSearch(getSearchableFoodItems(), null);
    }

    @Override
    public List<SearchResultItem> search(String searchStr)
    {
        return foodSearch.searchFood(searchStr);
    }

    @Override
    public FoodItem retrieve(String id)
    {
        // TODO use hashmap
        for (UGAFoodItem item : items)
            if (item.getId().equals(id))
                return item;
        return null;
    }

    @Override
    public FoodItem get(String id)
    {
        return retrieve(id);
    }

    public List<SearchableFoodItem> getSearchableFoodItems()
    {
        List<SearchableFoodItem> searchableFoodItems = new ArrayList<>();
        searchableFoodItems.addAll(items);
        return searchableFoodItems;
    }
}
