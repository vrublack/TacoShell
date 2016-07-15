package com.vrublack.nutrition.core.uga;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.vrublack.nutrition.core.NutrientQuantity;
import com.vrublack.nutrition.core.Pair;
import com.vrublack.nutrition.core.ParseException;
import com.vrublack.nutrition.core.Specification;
import com.vrublack.nutrition.core.util.HTTPRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class UGAScraper
{
    private final static String BASE_URL = "http://foodservice.uga.edu/locations/";
    private final static String[] DINING_HALLS = {"Bolton", "Oglethorpe", "Snelling", "Village Summit", "The Niche"};

    public static List<UGAFoodItem> scrapeAllLocations()
    {
        List<UGAFoodItem> all = new ArrayList<>();

        for (String site : DINING_HALLS)
            all.addAll(scrapeLocation(BASE_URL, site));

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

    // convenience method
    private static NutrientKeyValue p(String key, Specification.NutrientType val)
    {
        return new NutrientKeyValue(key, val);
    }

    private static List<UGAFoodItem> scrapeLocation(String baseUrl, String diningHall)
    {
        try
        {
            String content = HTTPRequest.executeGet(baseUrl, diningHall.replace(" ", "-"), new HTTPRequest.NameValuePair[]{});

            // parse content

            List<UGAFoodItem> items = new ArrayList<>();
            int currentPos = 0;

            while ((currentPos = content.indexOf("data-nutrition", currentPos)) != -1)
            {
                int startInfo = content.indexOf("\"{", currentPos);
                int endInfo = content.indexOf("}\"", startInfo + 1);

                String data = content.substring(startInfo + 1, endInfo + 1);
                data = data.replace("&quot;", "\"");

                JsonObject json;
                try
                {
                    // nutrition data is in JSON-format
                    json = new JsonParser().parse(data).getAsJsonObject();
                } catch (JsonSyntaxException e)
                {
                    e.printStackTrace();
                    continue;
                }

                float kcal = json.get("calories").getAsFloat();
                String name = json.get("serving-name").getAsString();
                String servingSize = json.get("serving-size").getAsString();

                // separate quantifier and unit in servingSize
                int unitStart = -1;
                for (int i = 0; i < servingSize.length(); i++)
                {
                    char c = servingSize.charAt(i);
                    if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')
                    {
                        unitStart = i;
                        break;
                    }
                }

                String unit;
                if (unitStart == -1)
                    unit = "pieces";
                else
                    unit = servingSize.substring(unitStart);

                // TODO fractions ("1/4") can also occur
                String quantifierStr = servingSize.substring(0, unitStart);
                float quantifier;
                try
                {
                    if (quantifierStr.contains("/")) // fraction

                    {
                        quantifier = 0;

                        if (quantifierStr.trim().contains(" "))     // something like "3 1/2" (three and a halve)
                        {
                            quantifier = Float.parseFloat(quantifierStr.substring(0, quantifierStr.indexOf(' ')));
                            quantifierStr = quantifierStr.substring(quantifierStr.indexOf(' ') + 1);
                        }

                        float numer = Float.parseFloat(quantifierStr.substring(0, quantifierStr.indexOf('/')));
                        float denom = Float.parseFloat(quantifierStr.substring(quantifierStr.indexOf('/') + 1));
                        quantifier += numer / denom;
                    } else
                    {
                        quantifier = Float.parseFloat(quantifierStr);
                    }
                } catch (NumberFormatException e)
                {
                    e.printStackTrace();
                    continue;
                }

                Map<Specification.NutrientType, NutrientQuantity> nutrients = new HashMap<>();

                NutrientKeyValue[] nutrientsKeyValue = {
                        p("total-fat", Specification.NutrientType.Fat),
                        p("sat-fat", Specification.NutrientType.FatSaturated),
                        p("trans-fat", Specification.NutrientType.FatTrans),
                        p("cholesterol", Specification.NutrientType.Cholesterol),
                        p("sodium", Specification.NutrientType.Sodium),
                        p("total-carb", Specification.NutrientType.Carbohydrates),
                        p("dietary-fiber", Specification.NutrientType.Fiber),
                        p("sugars", Specification.NutrientType.Sugar),
                        p("protein", Specification.NutrientType.Protein),
                };

                for (NutrientKeyValue kv : nutrientsKeyValue)
                {
                    if (json.has(kv.first))
                    {
                        try
                        {
                            String value = json.get(kv.first).getAsString();
                            if (!value.trim().isEmpty())
                                nutrients.put(kv.second, NutrientQuantity.parseFromString(value));
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                UGAFoodItem.Serving serving = new UGAFoodItem.Serving(quantifier, unit, kcal, nutrients);

                UGAFoodItem item = new UGAFoodItem(name, serving);
                item.addLocation(diningHall);
                items.add(item);

                currentPos++;
            }

            return items;

        } catch (URISyntaxException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
