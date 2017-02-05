package com.vrublack.nutrition.core.uga;

import com.vrublack.nutrition.core.*;
import com.vrublack.nutrition.core.SearchableFoodItem;

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

    private FoodQuantity servingQuantity;

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

        this.servingQuantity = new FoodQuantity(1, "serving", "serving (" + quantity + ")");

        // the quantity is always one serving
        conversionDefs = new UnitConverter.ConversionDefinition[1];
        conversionDefs[0] = new UnitConverter.ConversionDefinition(quantity.getQuantifier(), quantity.getSimpleUnit(), quantity.getDetailedUnit(),
                servingQuantity.getQuantifier(), servingQuantity.getSimpleUnit(), servingQuantity.getDetailedUnit());
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
        // Simpler implementation than for USDA description comps for the following reasons:
        // 1. There are fewer food items from the UGA source than from the USDA source, so it's not as crucial
        // to distinguish the position of the comp.
        // 2. Descriptions used by UGA are in a less strict format, which makes a more intelligent processing more difficult.
        String[] strComps = description.split("[, ()]");
        List<DescriptionComp> descriptionComps = new ArrayList<>();
        for (String strComp : strComps)
        {
            strComp = strComp.trim();

            DescriptionComp descriptionComp = new DescriptionComp();
            descriptionComp.comp = strComp; // reverse order
            descriptionComp.priority = descriptionComps.size() + 1;
            descriptionComps.add(descriptionComp);
        }

        return descriptionComps.toArray(new DescriptionComp[descriptionComps.size()]);
    }


    /**
     * @param location Location (dining hall, etc.) where this item is available
     */
    public void addLocation(String location)
    {
        locations.add(location);

        // add to description comps
        DescriptionComp[] newComps = new DescriptionComp[descriptionComps.length + 1];
        System.arraycopy(descriptionComps, 0, newComps, 0, descriptionComps.length);
        newComps[descriptionComps.length] = new DescriptionComp();
        newComps[descriptionComps.length].comp = location;
        newComps[descriptionComps.length].priority = 5;
        descriptionComps = newComps;
    }

    public Set<String> getLocations()
    {
        return locations;
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
        acceptedUnits.add(servingQuantity);

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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UGAFoodItem that = (UGAFoodItem) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
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
