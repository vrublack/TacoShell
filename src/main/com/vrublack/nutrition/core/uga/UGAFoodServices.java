package com.vrublack.nutrition.core.uga;


import com.Config;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.vrublack.nutrition.core.*;
import com.vrublack.nutrition.core.search.DescriptionBase;
import com.vrublack.nutrition.core.search.FoodSearch;
import com.vrublack.nutrition.core.search.HashFoodSearch;
import com.vrublack.nutrition.core.search.LevenshteinFoodSearch;
import com.vrublack.nutrition.core.SearchableFoodItem;
import com.vrublack.nutrition.core.util.Debug;
import com.vrublack.nutrition.core.util.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
        try
        {
            foodSearch = new HashFoodSearch(getCanonicalSearchableFoodItems(), null,
                    DescriptionBase.getDescriptionBase(new FileInputStream("uga_dict.0")));
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Initializes with cached items
     *
     * @param is        Stream to read cached items
     * @param baseDicIs InputStream to base dictionary
     */
    public UGAFoodServices(InputStream is, InputStream baseDicIs)
    {
        readCached(is);
        foodSearch = new HashFoodSearch(getCanonicalSearchableFoodItems(), null,
                DescriptionBase.getDescriptionBase(baseDicIs));
    }

    /**
     * Reads items from saved web pages in directory
     *
     * @param directory Directory where web pages are in
     */
    public static List<UGAFoodItem> readFromDownloadedPages(String directory)
    {
        List<UGAFoodItem> items = new ArrayList<>();

        Map<String, UGAFoodItem> itemsSet = new HashMap<>();

        File dir = new File(directory);
        String[] files = dir.list();
        int totalEntries = 0;
        for (String fname : files)
        {
            String content = null;
            try
            {
                content = Util.readFile(new File(directory, fname));
                int diningHallEnd = fname.indexOf('_');
                if (diningHallEnd == -1)
                    continue;
                String diningHall = fname.substring(0, diningHallEnd);
                switch (diningHall)
                {
                    case "bolton":
                        diningHall = "Bolton";
                        break;
                    case "village-summit":
                        diningHall = "Village Summit";
                        break;
                    case "snelling":
                        diningHall = "Snelling";
                        break;
                    case "oglethorpe":
                        diningHall = "Oglethorpe";
                        break;
                    case "niche":
                        diningHall = "Niche";
                        break;
                }
                List<UGAFoodItem> newItems = UGAParser.parsePage(content, diningHall);
                totalEntries += newItems.size();

                long setSizeBefore = itemsSet.size();

                // add these items to the set if they don't exist yet, and if they exist, add the name of the dining hall,
                // if not already added
                for (UGAFoodItem it : newItems)
                {
                    if (itemsSet.containsKey(it.getId()))
                    {
                        itemsSet.get(it.getId()).addLocation((String) it.getLocations().toArray()[0]);
                    } else
                    {
                        itemsSet.put(it.getId(), it);
                    }
                }

                System.out.println("Added " + (itemsSet.size() - setSizeBefore) + " items");

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        items = new ArrayList<>(itemsSet.values());

        if (Config.DEBUG)
            System.out.println("Items loaded: " + totalEntries + " (" + items.size() + " unique)");

        return items;
    }

    private void readCached(InputStream is)
    {
        Kryo kryo = new Kryo();
        Input input = new Input(is);
        items = kryo.readObject(input, ArrayList.class);
        input.close();
    }

    public static void itemsToFile(List<UGAFoodItem> items, String fname)
    {
        try
        {
            Kryo kryo = new Kryo();
            Output output = new Output(new FileOutputStream(fname));
            kryo.writeObject(output, items);
            output.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
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

    public List<CanonicalSearchableFoodItem> getCanonicalSearchableFoodItems()
    {
        List<CanonicalSearchableFoodItem> searchableFoodItems = new ArrayList<>();
        searchableFoodItems.addAll(items);
        return searchableFoodItems;
    }

}
