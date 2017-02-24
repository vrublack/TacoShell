package com.vrublack.nutrition.core.uga;

import com.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.vrublack.nutrition.core.*;
import com.vrublack.nutrition.core.search.DescriptionBase;
import org.unbescape.html.HtmlEscape;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses html page from UGA Food Services
 */
public class UGAParser
{
    public static List<UGAFoodItem> parsePage(String content, String diningHall)
    {
        // parse content

        List<UGAFoodItem> items = new ArrayList<>();
        int currentPos = 0;

        while ((currentPos = content.indexOf("data-nutrition", currentPos)) != -1)
        {
            try
            {
                int startInfo = content.indexOf("\"{", currentPos);
                int endInfo = content.indexOf("}\"", startInfo + 1);

                String data = content.substring(startInfo + 1, endInfo + 1);
                data = HtmlEscape.unescapeHtml(data);

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
                    unit = servingSize.substring(unitStart).toLowerCase();

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

                UGAScraper.NutrientKeyValue[] nutrientsKeyValue = {
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

                for (UGAScraper.NutrientKeyValue kv : nutrientsKeyValue)
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

                SearchableFoodItem.DescriptionComp[] descriptionComps = parseDescriptionComps(name);
                SearchableFoodItem.DescriptionComp[] canonicalDescriptionComps = toCanonical(descriptionComps);

                UGAFoodItem item = new UGAFoodItem(name, new FoodQuantity(quantifier, unit, unit), kcal, nutrients, descriptionComps,
                        canonicalDescriptionComps);

                item.addLocation(diningHall);
                items.add(item);

            } catch (NumberFormatException e)
            {
                if (Config.DEBUG)
                    System.err.println("Skipped food item");
            }

            currentPos++;
        }

        return items;
    }

    private static SearchableFoodItem.DescriptionComp[] toCanonical(SearchableFoodItem.DescriptionComp[] descriptionComps)
    {
        List<SearchableFoodItem.DescriptionComp> canonical = new ArrayList<>();

        try
        {
            DescriptionBase base = DescriptionBase.getDescriptionBase(new FileInputStream("uga_dict.0"));

            int compIndex = 1;

            for (SearchableFoodItem.DescriptionComp comp : descriptionComps)
            {
                String[] comps = base.descriptionToBase(comp.comp);
                for (String s : comps)
                {
                    SearchableFoodItem.DescriptionComp c = new SearchableFoodItem.DescriptionComp();
                    c.comp = s;
                    c.priority = compIndex++;
                    canonical.add(c);
                }
            }

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        return canonical.toArray(new SearchableFoodItem.DescriptionComp[canonical.size()]);
    }

    private static SearchableFoodItem.DescriptionComp[] parseDescriptionComps(String description)
    {
        // Simpler implementation than for USDA description comps for the following reasons:
        // 1. There are fewer food items from the UGA source than from the USDA source, so it's not as crucial
        // to distinguish the position of the comp.
        // 2. Descriptions used by UGA are in a less strict format, which makes a more intelligent processing more difficult.
        String[] strComps = description.split("[, ()]");
        List<SearchableFoodItem.DescriptionComp> descriptionComps = new ArrayList<>();
        for (String strComp : strComps)
        {
            strComp = strComp.trim();

            SearchableFoodItem.DescriptionComp descriptionComp = new SearchableFoodItem.DescriptionComp();
            descriptionComp.comp = strComp; // reverse order
            descriptionComp.priority = descriptionComps.size() + 1;
            descriptionComps.add(descriptionComp);
        }

        return descriptionComps.toArray(new SearchableFoodItem.DescriptionComp[descriptionComps.size()]);
    }

    // convenience method
    private static UGAScraper.NutrientKeyValue p(String key, Specification.NutrientType val)
    {
        return new UGAScraper.NutrientKeyValue(key, val);
    }

}
