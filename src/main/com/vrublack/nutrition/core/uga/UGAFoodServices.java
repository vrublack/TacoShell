package com.vrublack.nutrition.core.uga;


import com.Config;
import com.vrublack.nutrition.core.*;
import com.vrublack.nutrition.core.search.FoodSearch;
import com.vrublack.nutrition.core.search.LevenshteinFoodSearch;
import com.vrublack.nutrition.core.SearchableFoodItem;
import com.vrublack.nutrition.core.util.Debug;
import com.vrublack.nutrition.core.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Scrapes the University of Georgia's food services website for meals and nutrition info.
 */
public class UGAFoodServices implements SyncFoodDataSource
{
    private List<UGAFoodItem> items;

    private FoodSearch foodSearch;


    /**
     * Parses items from UGA website
     */
    public UGAFoodServices()
    {
        items = UGAScraper.scrapeAllLocations();
        foodSearch = new LevenshteinFoodSearch(getSearchableFoodItems(), null);
    }

    /**
     * Reads items from saved web pages in directory
     *
     * @param directory Directory where web pages are in
     */
    public UGAFoodServices(String directory)
    {
        items = new ArrayList<>();

        Set<UGAFoodItem> itemsSet = new HashSet<>();

        File dir = new File(directory);
        String[] files = dir.list();
        int totalEntries = 0;
        for (String fname : files)
        {
            String content = null;
            try
            {
                content = Util.readFile(new File(directory, fname));
                List<UGAFoodItem> newItems = UGAParser.parsePage(content, "dontknow");
                totalEntries += newItems.size();
                itemsSet.addAll(newItems);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        items = new ArrayList<>(itemsSet);
        foodSearch = new LevenshteinFoodSearch(getSearchableFoodItems(), null);

        if (Config.DEBUG)
            System.out.println("Items loaded: " + totalEntries + " (" + items.size() + " unique)");
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
