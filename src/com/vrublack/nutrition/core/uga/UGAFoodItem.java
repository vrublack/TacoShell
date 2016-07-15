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

    // Quantity per which the nutrients are specified
    private FoodQuantity quantity;

    private float calories;

    // How many nutrients are in [quantity] of the food
    private Map<Specification.NutrientType, NutrientQuantity> nutrients;

    private UnitConverter.ConversionDefinition[] conversionDefs;

    private Set<String> locations = new HashSet<>();

    // for search
    private DescriptionComp[] descriptionComps;

    public UGAFoodItem(String name, FoodQuantity quantity, float kcal, Map<Specification.NutrientType, NutrientQuantity> nutrients)
    {
        this.id = Integer.toString(Math.abs(name.hashCode()));
        // limit id to 7 characters
        if (this.id.length() > 7)
            this.id = this.id.substring(0, 8);

        this.name = name;
        // currently only one serving
        this.quantity = quantity;

        this.calories = kcal;
        this.nutrients = nutrients;

        this.descriptionComps = parseDescriptionComps(name);

        // the quantity is always one serving
        conversionDefs = new UnitConverter.ConversionDefinition[1];
        conversionDefs[0] = new UnitConverter.ConversionDefinition(quantity.getQuantifier(), quantity.getSimpleUnit(), quantity.getDetailedUnit(),
                1, "serving", "serving");
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
        // convert specified quantity into the quantity that the nutrients of this item are specified in
        float quantifier = UnitConverter.convert(quantity, this.quantity, conversionDefs);
        return calories * quantifier;
    }

    @Override
    public float getRelativePopularity()
    {
        return 0.7f;
    }

    @Override
    public NutrientQuantity getNutrientPerQuantity(Specification.NutrientType type, FoodQuantity quantity)
    {
        // convert specified quantity into the quantity that the nutrients of this item are specified in
        float quantifier = UnitConverter.convert(quantity, this.quantity, conversionDefs);
        NutrientQuantity nutrientQuantity = nutrients.get(type);
        if (nutrientQuantity != null)
            return nutrientQuantity.scale(quantifier);
        else
            return null;
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

        acceptedUnits.add(quantity);
        acceptedUnits.add(new FoodQuantity(1, "serving", "serving"));

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
        NutrientQuantity carbs = nutrients.get(Specification.NutrientType.Carbohydrates);
        NutrientQuantity protein = nutrients.get(Specification.NutrientType.Protein);
        NutrientQuantity fat = nutrients.get(Specification.NutrientType.Fat);
        float kcal = calories;

        return "Per " + quantity.toString() + " - Calories: " + kcal
                + "kcal | Fat: " + (fat == null ? "-" : fat)
                + " | Carbs: " + (carbs == null ? "-" : carbs)
                + " | Protein: " + (protein == null ? "-" : protein);
    }
}
