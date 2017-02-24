package com.vrublack.nutrition.core.uga;

import com.vrublack.nutrition.core.SearchableFoodItem;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UGADictionaryGenerator
{
    public static void generateDict(List<UGAFoodItem> items, String outputFname)
    {
        try
        {
            Set<String> all = new HashSet<>();
            PrintWriter writer = new PrintWriter(outputFname, "UTF-8");

            for (UGAFoodItem item : items)
            {
                SearchableFoodItem.DescriptionComp[] comps = item.getDescriptionComps();
                for (SearchableFoodItem.DescriptionComp comp : comps)
                {
                    all.add(comp.comp);
                }
            }

            for (String s : all)
            {
                String[] subComps = s.split("[^\\w]");
                for (String subComp : subComps)
                    if (subComp.length() > 2 && Character.isAlphabetic(subComp.charAt(0)))
                        writer.write(subComp.toLowerCase() + '\n');
            }

            writer.close();
        } catch (IOException e)
        {
            // do something
        }

    }

    public static void main(String[] args)
    {
        List<UGAFoodItem> items = UGAFoodServices.readFromDownloadedPages("/Users/valentin/UGAFoodData");
        generateDict(items, "uga_dict.0");
    }
}
