package com.vrublack.nutrition.core.uga;


import com.vrublack.nutrition.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Scrapes the University of Georgia's food services website for meals and nutrition info.
 */
public class UGAFoodServices implements SyncFoodDataSource
{
    private List<UGAFoodItem> items = UGAScraper.scrapeAllLocations();

    @Override
    public List<SearchResultItem> search(String searchStr)
    {
        FoodSearch foodSearch = new FoodSearch();
        List<SearchableFoodItem> searchableFoodItems = new ArrayList<>();
        searchableFoodItems.addAll(items);
        return foodSearch.searchFood(searchStr, searchableFoodItems, null);
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
}
