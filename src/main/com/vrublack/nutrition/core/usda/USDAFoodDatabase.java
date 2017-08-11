package com.vrublack.nutrition.core.usda;

import com.vrublack.nutrition.core.*;
import com.vrublack.nutrition.core.search.DescriptionBase;
import com.vrublack.nutrition.core.search.FoodSearch;
import com.vrublack.nutrition.core.search.HashFoodSearch;
import com.vrublack.nutrition.core.search.LevenshteinFoodSearch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Represents database derived from "USDA National Nutrient Database for Standard Reference". This
 * class is abstract because it can't know where the file is and how exactly to retrieve it. However,
 * the format of the file is fixed.
 */
public abstract class USDAFoodDatabase implements SyncFoodDataSource
{
    private List<USDAFoodItem> entries;

    // search history that is being used in search
    private String lastSearchStr;

    private FoodSearch search;

    public USDAFoodDatabase()
    {
        parseAsciiFile(null, 0);

        try
        {
            search = new HashFoodSearch(getCanonicalSearchableFoodItems(), getDescriptionBase());
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            search = new LevenshteinFoodSearch(getSearchableFoodItems());
        }
    }

    public USDAFoodDatabase(Runnable onStatusUpdate, float percentagInterval)
    {
        parseAsciiFile(onStatusUpdate, percentagInterval);

        try
        {
            search = new HashFoodSearch(getCanonicalSearchableFoodItems(), getDescriptionBase());
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            search = new LevenshteinFoodSearch(getSearchableFoodItems());
        }
    }

    /**
     * @return BufferedReader that points to the USDA ascii file
     */
    // design pattern: template method
    public abstract BufferedReader getBufferedReader() throws FileNotFoundException;

    /**
     * @return Description base for hash search
     */
    // design pattern: template method
    public abstract DescriptionBase getDescriptionBase() throws FileNotFoundException;


    /**
     * Parses ascii file containing foodItem items and their nutrition values.
     * Source: http://www.ars.usda.gov/Services/docs.htm?docid=24936
     *
     * @param onStatusUpdate
     * @param percentagInterval
     */
    // template design pattern
    private void parseAsciiFile(Runnable onStatusUpdate, float percentagInterval)
    {
        entries = new LinkedList<>();
        try (BufferedReader br = getBufferedReader())
        {
            final int totalEntries = 8463;
            int count = 0;
            int intervalI = 1;

            String line;
            while ((line = br.readLine()) != null)
            {
                entries.add(parseFood(line));

                count++;
                if (onStatusUpdate != null && count / (float) totalEntries >= percentagInterval * intervalI)
                {
                    intervalI++;
                    onStatusUpdate.run();
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private USDAFoodItem parseFood(String line)
    {
        List<String> c = split(line);

        String ndbNo = parseString(c.get(0));
        String description = parseString(c.get(1));
        String canonicalDescription = parseString(c.get(2));
        String commonNames = parseString(c.get(3));
        String commonNamesBase = parseString(c.get(4));

        float waterG = parseValue(c.get(5));
        float kcal = parseValue(c.get(6));
        float protein = parseValue(c.get(7));
        float fat = parseValue(c.get(8));
        float carbs = parseValue(c.get(9));
        float fiber = parseValue(c.get(10));
        float sugars = parseValue(c.get(11));
        float calciumMg = parseValue(c.get(12));
        float ironMg = parseValue(c.get(13));
        float magnesiumMg = parseValue(c.get(14));
        float sodiumMg = parseValue(c.get(15));
        float zincMg = parseValue(c.get(16));
        float vitaminCMg = parseValue(c.get(17));
        float vitaminB6Mg = parseValue(c.get(18));
        float vitaminB12Microg = parseValue(c.get(19));
        float vitaminAIU = parseValue(c.get(20));
        float vitaminEMg = parseValue(c.get(21));
        float vitaminDMicrog = parseValue(c.get(22));
        float saturatedFatsG = parseValue(c.get(23));
        float monounsaturatedFatsG = parseValue(c.get(24));
        float polyunsaturatedFatsG = parseValue(c.get(25));
        float cholesterolMg = parseValue(c.get(26));
        int popularity = (int) parseValue(c.get(27));

        List<USDAFoodItem.CommonMeasure> commonMeasures = new ArrayList<>();
        // the rest are measures
        for (int i = 28; i < c.size(); i += 3)
        {
            float amnt = parseValue(c.get(i));
            String unit = parseString(c.get(i + 1));
            float grams = parseValue(c.get(i + 2));
            commonMeasures.add(new USDAFoodItem.CommonMeasure(unit, amnt, grams));
        }

        // TODO add common names to entry for search

        // parse components of the description

        USDAFoodItem.DescriptionComp[] canonicalDescriptionCompsArray = parseDescriptionComps(canonicalDescription, commonNamesBase);
        USDAFoodItem.DescriptionComp[] descriptionCompsArray = parseDescriptionComps(description, commonNamesBase);

        Map<Specification.NutrientType, NutrientQuantity> nutrients = new HashMap<>();
        nutrients.put(Specification.NutrientType.Water, new NutrientQuantity(waterG, NutrientQuantity.Unit.g));
        nutrients.put(Specification.NutrientType.Protein, new NutrientQuantity(protein, NutrientQuantity.Unit.g));
        nutrients.put(Specification.NutrientType.Fat, new NutrientQuantity(fat, NutrientQuantity.Unit.g));
        nutrients.put(Specification.NutrientType.Carbohydrates, new NutrientQuantity(carbs, NutrientQuantity.Unit.g));
        nutrients.put(Specification.NutrientType.Fiber, new NutrientQuantity(fiber, NutrientQuantity.Unit.g));
        nutrients.put(Specification.NutrientType.Sugar, new NutrientQuantity(sugars, NutrientQuantity.Unit.g));
        nutrients.put(Specification.NutrientType.Calcium, new NutrientQuantity(calciumMg, NutrientQuantity.Unit.Mg));
        nutrients.put(Specification.NutrientType.Iron, new NutrientQuantity(ironMg, NutrientQuantity.Unit.Mg));
        nutrients.put(Specification.NutrientType.Magnesium, new NutrientQuantity(magnesiumMg, NutrientQuantity.Unit.Mg));
        nutrients.put(Specification.NutrientType.Sodium, new NutrientQuantity(sodiumMg, NutrientQuantity.Unit.Mg));
        nutrients.put(Specification.NutrientType.Zinc, new NutrientQuantity(zincMg, NutrientQuantity.Unit.Mg));
        nutrients.put(Specification.NutrientType.VitaminC, new NutrientQuantity(vitaminCMg, NutrientQuantity.Unit.Mg));
        nutrients.put(Specification.NutrientType.VitaminB6, new NutrientQuantity(vitaminB6Mg, NutrientQuantity.Unit.Mg));
        nutrients.put(Specification.NutrientType.VitaminB12, new NutrientQuantity(vitaminB12Microg, NutrientQuantity.Unit.Microg));
        nutrients.put(Specification.NutrientType.VitaminA, new NutrientQuantity(vitaminAIU, NutrientQuantity.Unit.IU));
        nutrients.put(Specification.NutrientType.VitaminE, new NutrientQuantity(vitaminEMg, NutrientQuantity.Unit.Mg));
        nutrients.put(Specification.NutrientType.VitaminD, new NutrientQuantity(vitaminDMicrog, NutrientQuantity.Unit.Microg));
        nutrients.put(Specification.NutrientType.FatSaturated, new NutrientQuantity(saturatedFatsG, NutrientQuantity.Unit.g));
        nutrients.put(Specification.NutrientType.FatMonounsaturated, new NutrientQuantity(monounsaturatedFatsG, NutrientQuantity.Unit.g));
        nutrients.put(Specification.NutrientType.FatPolyunsaturated, new NutrientQuantity(polyunsaturatedFatsG, NutrientQuantity.Unit.g));
        nutrients.put(Specification.NutrientType.Cholesterol, new NutrientQuantity(cholesterolMg, NutrientQuantity.Unit.Mg));
        return new USDAFoodItem(ndbNo, description, descriptionCompsArray, canonicalDescriptionCompsArray, nutrients, kcal, popularity,
                commonMeasures.toArray(new USDAFoodItem.CommonMeasure[commonMeasures.size()]));
    }

    private USDAFoodItem.DescriptionComp[] parseDescriptionComps(String description, String commonNames)
    {
        String[] strComps = description.split(",");
        String[] commonNameStrComps = commonNames.split(",");
        List<USDAFoodItem.DescriptionComp> descriptionComps = new ArrayList<>();
        for (String strComp : strComps)
        {
            strComp = strComp.trim();

            // The component can be something like "wheat flour". In this case, the component should be split
            // further into two subcomponents (for search) and the order should be reversed, as the last part
            // is more important most of the time
            String[] subComps = strComp.split(" ");

            for (int i = subComps.length - 1; i >= 0; i--)
            {
                USDAFoodItem.DescriptionComp descriptionComp = new USDAFoodItem.DescriptionComp();
                descriptionComp.comp = subComps[i];
                descriptionComp.priority = descriptionComps.size() + 1;
                descriptionComps.add(descriptionComp);
            }
        }

        for (String strComp : commonNameStrComps)
        {
            strComp = strComp.trim();
            USDAFoodItem.DescriptionComp descriptionComp = new USDAFoodItem.DescriptionComp();
            descriptionComp.comp = strComp;
            descriptionComp.priority = 1;
            descriptionComps.add(descriptionComp);
        }

        return descriptionComps.toArray(new USDAFoodItem.DescriptionComp[descriptionComps.size()]);
    }

    private int countOccurrences(String s, char c)
    {
        int count = 0;
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) == c)
                count++;
        return count;
    }

    private List<String> split(String line)
    {
        List<String> comps = new ArrayList<>();
        int lastSequenceStart = 0;
        int i;
        for (i = 0; i < line.length(); i++)
        {
            if (line.charAt(i) == '^')
            {
                comps.add(line.substring(lastSequenceStart, i));
                lastSequenceStart = i + 1;
            }
        }
        if (i - lastSequenceStart > 0)
            comps.add(line.substring(lastSequenceStart, i));
        return comps;
    }

    private float parseValue(String str)
    {
        if (str.isEmpty())
            return 0;

        try
        {
            return Float.parseFloat(str);
        } catch (NumberFormatException e)
        {
            return 0;
        }
    }

    private String parseString(String str)
    {
        return str.substring(1, str.length() - 1);
    }


    @Override
    public List<SearchResultItem> search(String searchStr, SearchHistory history)
    {
        return search(searchStr, search, history);
    }

    public List<CanonicalSearchableFoodItem> getCanonicalSearchableFoodItems()
    {
        List<CanonicalSearchableFoodItem> searchableFoodItems = new ArrayList<>();
        searchableFoodItems.addAll(entries);
        return searchableFoodItems;
    }

    public List<SearchableFoodItem> getSearchableFoodItems()
    {
        List<SearchableFoodItem> searchableFoodItems = new ArrayList<>();
        searchableFoodItems.addAll(entries);
        return searchableFoodItems;
    }

    public List<SearchResultItem> search(String searchStr, FoodSearch search, SearchHistory history)
    {
        lastSearchStr = searchStr;
        return search.searchFood(searchStr, history);
    }


    @Override
    public FoodItem retrieve(String id, SearchHistory history)
    {
        for (FoodItem foodItem : entries)
            if (foodItem.getId().equals(id))
            {
                // update search feedback
                if (lastSearchStr != null)
                {
                    history.putNDBNumberForSearchResult(lastSearchStr, id);
                    lastSearchStr = null;
                }

                return foodItem;
            }
        return null;
    }

    @Override
    public FoodItem get(String id)
    {
        for (FoodItem foodItem : entries)
            if (foodItem.getId().equals(id))
            {
                return foodItem;
            }
        return null;
    }
}
