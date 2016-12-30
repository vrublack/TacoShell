package com.vrublack.nutrition.core;

import com.vrublack.nutrition.core.util.StringGenerator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents set of nutrient values that were entered directly
 */
public class DirectSpecification extends Specification implements Serializable
{
    private static final long serialVersionUID = 14235;

    // Nutrients in grams
    private Map<NutrientType, Float> nutrients;
    // Unit for each nutrient type. This is used instead of NutrientQuantitiy to ensure backwards
    // compatibility with old serialized DailyRecords.
    private Map<NutrientType, NutrientQuantity.Unit> nutrientUnits;

    private boolean nutrientsSpecified;

    private float calories;

    private String description;

    private String id = StringGenerator.generateString(5);

    public DirectSpecification()
    {
        nutrients = new HashMap<>();
        nutrientsSpecified = false;
        calories = -1;
    }

    @Override
    public String getId()
    {
        return id;
    }

    /**
     * The old serialized records don't have nutrientUnits yet, so if nutrientUnits is null,
     * it has to be initialized with Unit.g for all nutrient values, since gram was the standard
     * unit.
     */
    private void compatibilityFix()
    {
        if (nutrientUnits == null)
        {
            nutrientUnits = new HashMap<>();
            for (Map.Entry<NutrientType, Float> e : nutrients.entrySet())
                nutrientUnits.put(e.getKey(), NutrientQuantity.Unit.g);
        }
    }

    /**
     * @param nutrientType Type of nutrient to add
     * @param unit         Unit for amount
     * @param amount       Amount in unit
     */
    public void putNutrient(Specification.NutrientType nutrientType, NutrientQuantity.Unit unit, Float amount)
    {
        compatibilityFix();

        nutrients.put(nutrientType, amount);
        nutrientUnits.put(nutrientType, unit);
        nutrientsSpecified = true;
    }

    public void putNutrient(Specification.NutrientType nutrientType, NutrientQuantity quantity)
    {
        compatibilityFix();

        nutrients.put(nutrientType, quantity.getAmountInUnit());
        nutrientUnits.put(nutrientType, quantity.getUnit());
        nutrientsSpecified = true;
    }

    public void setCalories(float calories)
    {
        this.calories = calories;
    }

    /**
     * @return If putNutrient was called on this object
     */
    public boolean isNutrientSpecified()
    {
        return nutrientsSpecified;
    }

    /**
     * @return If setCalories was called on this object
     */
    public boolean isCaloriesSpecified()
    {
        return calories != -1;
    }

    @Override
    public NutrientQuantity getNutrient(NutrientType type)
    {
        compatibilityFix();

        Float grams = nutrients.get(type);
        if (grams == null)
            return null;
        else
            return new NutrientQuantity(grams, nutrientUnits.get(type));
    }

    public NutrientQuantity getNutrientOrZero(NutrientType type)
    {
        NutrientQuantity quantity = getNutrient(type);
        if (quantity == null)
            return new NutrientQuantity(0, NutrientQuantity.Unit.g);
        else
            return quantity;
    }

    /**
     * @return The calories of this entry. If the calories weren't set, the value is calculated by the nutrients.
     */
    public float getCalories()
    {
        if (calories == -1)
            // the unit is always g
            return getNutrientOrZero(NutrientType.Carbohydrates).getAmountInUnit() * 4 + getNutrientOrZero(NutrientType.Fat).getAmountInUnit() * 9
                    + getNutrientOrZero(NutrientType.Protein).getAmountInUnit() * 4;
        else
            return calories;
    }

    /**
     * Multiplies all specified values by the factor
     */
    public void multiply(float factor)
    {
        for (Map.Entry<NutrientType, Float> e : nutrients.entrySet())
        {
            nutrients.put(e.getKey(), e.getValue() * factor);
        }

        if (calories != -1)
            calories *= factor;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public String getDescription()
    {
        return description == null ? "(Direct input)" : description;
    }

    @Override
    public FoodQuantity getAmount()
    {
        return null;
    }
}
