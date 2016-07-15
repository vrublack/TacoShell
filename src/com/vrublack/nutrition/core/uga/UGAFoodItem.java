package com.vrublack.nutrition.core.uga;

import com.sun.istack.internal.Nullable;
import com.vrublack.nutrition.core.*;

import java.io.Serializable;
import java.util.*;

public class UGAFoodItem extends SearchableFoodItem implements Serializable
{
    private static final long serialVersionUID = 14235;

    private String id;

    private String name;

    private Serving[] servings;

    private Set<String> locations = new HashSet<>();

    // for search
    private DescriptionComp[] descriptionComps;

    public UGAFoodItem(String name, Serving serving)
    {
        this.id = Integer.toString(Math.abs(name.hashCode()));
        // limit id to 7 characters
        if (this.id.length() > 7)
            this.id = this.id.substring(0, 8);

        this.name = name;
        // currently only one serving
        this.servings = new Serving[]{serving};

        this.descriptionComps = parseDescriptionComps(name);
    }

    public UGAFoodItem()
    {
    }

    private int countOccurrences(String s, char c)
    {
        int count = 0;
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) == c)
                count++;
        return count;
    }

    private DescriptionComp[] parseDescriptionComps(String description)
    {
        String[] strComps = description.split(",");
        List<DescriptionComp> descriptionComps = new ArrayList<>();
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
                    DescriptionComp descriptionComp = new DescriptionComp();
                    descriptionComp.comp = subComps[1]; // reverse order
                    descriptionComp.priority = descriptionComps.size() + 1;
                    descriptionComps.add(descriptionComp);
                }

                DescriptionComp descriptionComp = new DescriptionComp();
                descriptionComp.comp = subComps[0];
                descriptionComp.priority = descriptionComps.size() + 1;
                descriptionComps.add(descriptionComp);
            } else
            {
                DescriptionComp descriptionComp = new DescriptionComp();
                descriptionComp.comp = strComp;
                descriptionComp.priority = descriptionComps.size() + 1;
                descriptionComps.add(descriptionComp);
            }
        }

        return descriptionComps.toArray(new DescriptionComp[descriptionComps.size()]);
    }


    /**
     * @param location Location (dining hall, etc.) where this item is available
     */
    public void addLocation(String location)
    {
        locations.add(location);
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getDescription()
    {
        String desc = "";
        desc += name;
        desc += " " + locations.toString();
        return desc;
    }

    @Override
    public float getCaloriesPerQuantity(FoodQuantity quantity)
    {
        for (Serving serving : servings)
        {
            if (UnitConverter.isConvertible(quantity.getSimpleUnit(), serving.quantity.getSimpleUnit()))
            {
                float servingQuantifier = UnitConverter.convert(quantity.getQuantifier(), quantity.getSimpleUnit(),
                        serving.quantity.getQuantifier(), serving.quantity.getSimpleUnit());
                return serving.calories * servingQuantifier;
            }
        }

        throw new IllegalArgumentException("No supported unit");
    }

    @Override
    public float getRelativePopularity()
    {
        return 0.7f;
    }

    @Override
    public NutrientQuantity getNutrientPerQuantity(Specification.NutrientType type, FoodQuantity quantity)
    {
        for (Serving serving : servings)
        {
            if (UnitConverter.isConvertible(quantity.getSimpleUnit(), serving.quantity.getSimpleUnit()))
            {
                float servingQuantifier = UnitConverter.convert(quantity.getQuantifier(), quantity.getSimpleUnit(),
                        serving.quantity.getQuantifier(), serving.quantity.getSimpleUnit());
                NutrientQuantity nutrientQuantity = serving.nutrients.get(type);
                if (nutrientQuantity != null)
                    return nutrientQuantity.scale(servingQuantifier);
                else
                    return null;
            }
        }

        throw new IllegalArgumentException("No supported unit");
    }

    @Override
    public NutrientQuantity getNutrientOrZeroPerQuantity(Specification.NutrientType type, FoodQuantity quantity)
    {
        NutrientQuantity nutrientQuantity = getNutrientPerQuantity(type, quantity);
        if (nutrientQuantity != null)
            return nutrientQuantity;
        else
            return new NutrientQuantity(0, NutrientQuantity.Unit.g);
    }

    @Override
    public String getAbbreviatedDescription()
    {
        return getDescription();
    }

    @Override
    public FoodQuantity[] getAcceptedUnits()
    {
        List<FoodQuantity> acceptedUnits = new ArrayList<>();

        for (Serving serving : servings)
        {
            acceptedUnits.add(new FoodQuantity(1, serving.quantity.getSimpleUnit(), serving.quantity.getDetailedUnit()));
        }

        return acceptedUnits.toArray(new FoodQuantity[acceptedUnits.size()]);
    }

    /**
     * @return First component of measure, for example "cup" instead of "cup, crumbled, not packed"
     */
    private static String getSimpleUnit(String unit)
    {
        // fl oz is the only known unit that has a whitespace in between
        if (unit.startsWith("fl oz"))
            return "fl oz";

        // get first component
        for (int i = 0; i < unit.length(); i++)
        {
            char c = unit.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z'))
            {
                // no letter
                return unit.substring(0, i);
            }
        }

        return unit;
    }

    public void addLocations(UGAFoodItem item)
    {
        this.locations.addAll(item.locations);
    }

    @Override
    public DescriptionComp[] getDescriptionComps()
    {
        return descriptionComps;
    }

    @Override
    public String getNutritionInformation()
    {
        NutrientQuantity carbs = servings[0].nutrients.get(Specification.NutrientType.Carbohydrates);
        NutrientQuantity protein = servings[0].nutrients.get(Specification.NutrientType.Protein);
        NutrientQuantity fat = servings[0].nutrients.get(Specification.NutrientType.Fat);
        float kcal = servings[0].calories;

        return "Per 100g - Calories: " + kcal
                + "kcal | Fat: " + (fat == null ? "-" : fat)
                + " | Carbs: " + (carbs == null ? "-" : carbs)
                + " | Protein: " + (protein == null ? "-" : protein);
    }

    public static class Serving implements Serializable
    {
        private static final long serialVersionUID = 14235;

        // eg. 10 pieces
        private FoodQuantity quantity;

        private float calories;

        private Map<Specification.NutrientType, NutrientQuantity> nutrients;

        public Serving(float quantifier, String unit, float calories, Map<Specification.NutrientType, NutrientQuantity> nutrients)
        {
            quantity = new FoodQuantity(quantifier, getSimpleUnit(unit), unit);

            this.calories = calories;
            this.nutrients = nutrients;
        }

        public Serving()
        {

        }
    }
}
