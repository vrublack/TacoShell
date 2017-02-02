package com.vrublack.nutrition.core.uga;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.vrublack.nutrition.core.*;
import com.vrublack.nutrition.core.util.Debug;
import com.vrublack.nutrition.core.util.HTTPRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class UGAScraper
{
    private final static String BASE_URL = "https://foodservice.uga.edu/locations/";
    private final static String[] DINING_HALLS = {"Bolton", "Oglethorpe", "Snelling", "Village Summit", "The Niche"};

    public static List<UGAFoodItem> scrapeAllLocations()
    {
        // fixes SSL bug in Java
        System.setProperty("jsse.enableSNIExtension", "false");
        // prevent website from showing mobile view
        System.setProperty("http.agent", "");


        List<UGAFoodItem> all = new ArrayList<>();

        Thread[] workers = new Thread[DINING_HALLS.length];

        final List<List<UGAFoodItem>> threadResults = new ArrayList<>();

        for (int i = 0; i < DINING_HALLS.length; i++)
        {
            final int _i = i;

            threadResults.add(null);
            workers[i] = new Thread()
            {
                @Override
                public void run()
                {
                    threadResults.set(_i, scrapeLocation(BASE_URL, DINING_HALLS[_i]));
                }
            };
        }

        for (Thread worker : workers)
            worker.start();

        for (Thread worker : workers)
            try
            {
                worker.join();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

        for (int i = 0; i < DINING_HALLS.length; i++)
            all.addAll(threadResults.get(i));

        // Many food items are available in multiple dining halls. Merge those.
        // Assume that food items are identical if and only if the names (=ids) are identical
        Map<String, UGAFoodItem> idToItems = new HashMap<>();
        for (UGAFoodItem item : all)
        {
            if (idToItems.containsKey(item.getId()))
                idToItems.get(item.getId()).addLocations(item);
            else
                idToItems.put(item.getId(), item);
        }

        List<UGAFoodItem> merged = new ArrayList<>();
        for (Map.Entry<String, UGAFoodItem> pair : idToItems.entrySet())
        {
            merged.add(pair.getValue());
        }

        return merged;
    }

    // fix for Java not supporting generic array creation
    static class NutrientKeyValue extends Pair<String, Specification.NutrientType>
    {
        public NutrientKeyValue(String key, Specification.NutrientType val)
        {
            super(key, val);
        }
    }

    private static List<UGAFoodItem> scrapeLocation(String baseUrl, String diningHall)
    {
        String content = null;

        try
        {
            content = HTTPRequest.executeGet(baseUrl, diningHall.replace(" ", "-"), new HTTPRequest.NameValuePair[]{});
            return UGAParser.parsePage(content, diningHall);

        } catch (URISyntaxException | IOException e)
        {
            e.printStackTrace();

            // Debug.writeToFile("uga_page.html", content);
        }

        return new ArrayList<>();
    }
}
