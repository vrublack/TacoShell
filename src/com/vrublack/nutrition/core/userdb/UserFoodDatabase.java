package com.vrublack.nutrition.core.userdb;


import com.vrublack.nutrition.core.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * Database of food items that the user created. Implementation mostly same as USDAFoodDatabase
 */
public abstract class UserFoodDatabase implements SyncFoodDataSource
{
    private List<UserFoodItem> entries;

    // search history that is being used in search
    private SearchHistory searchHistory = new DummySearchHistory();
    private String lastSearchStr;

    private static transient final int LINE_MIN_LENGTH = 26;

    // Positions in line
    private static transient final int ID_POS = 0;
    private static transient final int DESC_POS = 1;
    private static transient final int KCAL_POS = 4;
    private static transient final int POPULARITY_POS = 25;

    public static transient final SavedNutrient[] SAVED_NUTRIENTS =
            {
                    new SavedNutrient(Specification.NutrientType.Water, 3, NutrientQuantity.Unit.g),
                    new SavedNutrient(Specification.NutrientType.Protein, 5, NutrientQuantity.Unit.g),
                    new SavedNutrient(Specification.NutrientType.Fat, 6, NutrientQuantity.Unit.g),
                    new SavedNutrient(Specification.NutrientType.Carbohydrates, 7, NutrientQuantity.Unit.g),
                    new SavedNutrient(Specification.NutrientType.Fiber, 8, NutrientQuantity.Unit.g),
                    new SavedNutrient(Specification.NutrientType.Sugar, 9, NutrientQuantity.Unit.g),
                    new SavedNutrient(Specification.NutrientType.Calcium, 10, NutrientQuantity.Unit.Mg),
                    new SavedNutrient(Specification.NutrientType.Iron, 11, NutrientQuantity.Unit.Mg),
                    new SavedNutrient(Specification.NutrientType.Magnesium, 12, NutrientQuantity.Unit.Mg),
                    new SavedNutrient(Specification.NutrientType.Sodium, 13, NutrientQuantity.Unit.Mg),
                    new SavedNutrient(Specification.NutrientType.Zinc, 14, NutrientQuantity.Unit.Mg),
                    new SavedNutrient(Specification.NutrientType.VitaminC, 15, NutrientQuantity.Unit.Mg),
                    new SavedNutrient(Specification.NutrientType.VitaminB6, 16, NutrientQuantity.Unit.Mg),
                    new SavedNutrient(Specification.NutrientType.VitaminB12, 17, NutrientQuantity.Unit.Microg),
                    new SavedNutrient(Specification.NutrientType.VitaminA, 18, NutrientQuantity.Unit.IU),
                    new SavedNutrient(Specification.NutrientType.VitaminE, 19, NutrientQuantity.Unit.Mg),
                    new SavedNutrient(Specification.NutrientType.VitaminD, 20, NutrientQuantity.Unit.Microg),
                    new SavedNutrient(Specification.NutrientType.FatSaturated, 21, NutrientQuantity.Unit.g),
                    new SavedNutrient(Specification.NutrientType.FatMonounsaturated, 22, NutrientQuantity.Unit.g),
                    new SavedNutrient(Specification.NutrientType.FatPolyunsaturated, 23, NutrientQuantity.Unit.g),
                    new SavedNutrient(Specification.NutrientType.Cholesterol, 24, NutrientQuantity.Unit.Mg)
            };

    public static class SavedNutrient
    {
        public Specification.NutrientType type;

        /**
         * Position in line
         */
        public int position;

        /**
         * In which Unit this is specified
         */
        public NutrientQuantity.Unit defaultUnit;

        public SavedNutrient(Specification.NutrientType type, int position, NutrientQuantity.Unit defaultUnit)
        {
            this.type = type;
            this.position = position;
            this.defaultUnit = defaultUnit;
        }
    }


    public UserFoodDatabase()
    {
        parseAsciiFile();
    }

    /**
     * @return BufferedReader that points to the file
     */
    public abstract BufferedReader getBufferedReader() throws IOException;

    /**
     * @return Writer that is in append mode.
     */
    public abstract BufferedWriter getBufferedWriter() throws IOException;

    /**
     * Parses ascii file containing foodItem items and their nutrition values.
     */
    // template design pattern
    private void parseAsciiFile()
    {
        entries = new LinkedList<>();
        try (BufferedReader br = getBufferedReader())
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                entries.add(parseFood(line));
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private UserFoodItem parseFood(String line)
    {
        List<String> c = split(line);

        String id = parseString(c.get(ID_POS));
        String description = parseString(c.get(DESC_POS));
        float kcal = parseValue(c.get(KCAL_POS));
        int popularity = (int) parseValue(c.get(POPULARITY_POS));

        Map<Specification.NutrientType, NutrientQuantity> nutrients = new HashMap<>();

        for (SavedNutrient savedNutrient : SAVED_NUTRIENTS)
        {
            float nutrientInUnit = parseValue(c.get(savedNutrient.position));
            nutrients.put(savedNutrient.type, new NutrientQuantity(nutrientInUnit, savedNutrient.defaultUnit));
        }

        List<UserFoodItem.CommonMeasure> commonMeasures = new ArrayList<>();
        // the rest are measures
        for (int i = LINE_MIN_LENGTH; i < c.size(); i += 3)
        {
            float amnt = parseValue(c.get(i));
            String unit = parseString(c.get(i + 1));
            float grams = parseValue(c.get(i + 2));
            commonMeasures.add(new UserFoodItem.CommonMeasure(unit, amnt, grams));
        }

        UserFoodItem.DescriptionComp[] descriptionCompsArray = parseDescriptionComps(description);

        return new UserFoodItem(id, description, descriptionCompsArray, nutrients, kcal, popularity,
                commonMeasures.toArray(new UserFoodItem.CommonMeasure[commonMeasures.size()]));
    }

    public static UserFoodItem.DescriptionComp[] parseDescriptionComps(String description)
    {
        String[] strComps = description.split(",");
        List<UserFoodItem.DescriptionComp> descriptionComps = new ArrayList<>();
        for (String strComp : strComps)
        {
            strComp = strComp.trim();

            // The component can be something like "wheat flour". In this case, the component should be split
            // further into two subcomponents (for search) and the order should be reversed, as the last part
            // is more important most of the time
            if (countOccurrences(strComp, ' ') == 1)
            {
                String[] subComps = strComp.split(" ");
                if (subComps.length == 2)
                {
                    UserFoodItem.DescriptionComp descriptionComp = new UserFoodItem.DescriptionComp();
                    descriptionComp.comp = subComps[1]; // reverse order
                    descriptionComp.priority = descriptionComps.size() + 1;
                    descriptionComps.add(descriptionComp);
                }

                UserFoodItem.DescriptionComp descriptionComp = new UserFoodItem.DescriptionComp();
                descriptionComp.comp = subComps[0];
                descriptionComp.priority = descriptionComps.size() + 1;
                descriptionComps.add(descriptionComp);
            } else
            {
                UserFoodItem.DescriptionComp descriptionComp = new UserFoodItem.DescriptionComp();
                descriptionComp.comp = strComp;
                descriptionComp.priority = descriptionComps.size() + 1;
                descriptionComps.add(descriptionComp);
            }
        }

        return descriptionComps.toArray(new UserFoodItem.DescriptionComp[descriptionComps.size()]);
    }

    private static int countOccurrences(String s, char c)
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

    /**
     * Adds item to the database (persistent).
     */
    public void createItem(UserFoodItem item) throws IOException
    {
        BufferedWriter writer = getBufferedWriter();

        FoodQuantity defaultQuantity = new FoodQuantity(100.0f, "g", "g");

        String[] comps = new String[LINE_MIN_LENGTH + 3 * item.getCommonMeasures().length];

        comps[ID_POS] = "~" + item.getId() + "~";
        comps[DESC_POS] = "~" + item.getDescription() + "~";
        comps[KCAL_POS] = Float.toString(item.getCaloriesPerQuantity(defaultQuantity));
        comps[POPULARITY_POS] = Integer.toString(item.getPopularity());

        // nutrients
        for (SavedNutrient savedNutrient : SAVED_NUTRIENTS)
        {
            NutrientQuantity quantity = item.getNutrientPerQuantity(savedNutrient.type, defaultQuantity);
            if (quantity == null)
                comps[savedNutrient.position] = "";
            else
                comps[savedNutrient.position] =
                        Float.toString(quantity.getAmountInUnit());
        }

        // common measures
        UserFoodItem.CommonMeasure[] commonMeasures = item.getCommonMeasures();
        for (int i = 0; i < commonMeasures.length; i++)
        {
            UserFoodItem.CommonMeasure commonMeasure = commonMeasures[i];
            comps[LINE_MIN_LENGTH + i * 3 + 0] = Float.toString(commonMeasure.getMeasureAmount());
            comps[LINE_MIN_LENGTH + i * 3 + 1] = "~" + commonMeasure.getCompleteUnit() + "~";
            comps[LINE_MIN_LENGTH + i * 3 + 2] = Float.toString(commonMeasure.getAmountInGrams());
        }

        String line = "";

        for (int i = 0; i < comps.length; i++)
        {
            line += comps[i];
            if (i < comps.length - 1)
                line += "^";
        }

        writer.write(line + "\n");

        writer.close();

        entries.add(item);
    }


    @Override
    public List<SearchResultItem> search(String searchStr)
    {
        lastSearchStr = searchStr;
        FoodSearch foodSearch = new FoodSearch();
        List<SearchableFoodItem> searchableFoodItems = new ArrayList<>();
        searchableFoodItems.addAll(entries);
        return foodSearch.searchFood(searchStr, searchableFoodItems, searchHistory);
    }

    @Override
    public FoodItem retrieve(String id)
    {
        for (FoodItem foodItem : entries)
            if (foodItem.getId().equals(id))
            {
                // update search feedback
                if (lastSearchStr != null)
                {
                    searchHistory.putNDBNumberForSearchResult(lastSearchStr, id);
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
