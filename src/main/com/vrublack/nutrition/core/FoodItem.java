package com.vrublack.nutrition.core;


import com.Config;

import java.io.Serializable;

public abstract class FoodItem implements Serializable
{
    private static final long serialVersionUID = 4956085901145474830L;

    /**
     * @return String that uniquely identifies this items within the food data source where the item is from.
     */
    public abstract String getId();

    public abstract String getDescription();

    /**
     * @return Kcal per quantity
     */
    public abstract float getCaloriesPerQuantity(FoodQuantity quantity);

    /**
     * @return How often the foodItem is consumed, with 100 being the most often and 0 being the least often.
     */
    public abstract float getRelativePopularity();

    /**
     * @param type     Which nutrient
     * @param quantity Per how much the returned amount should be
     * @return Nutrient per the specified unit
     */
    public abstract NutrientQuantity getNutrientPerQuantity(Specification.NutrientType type, FoodQuantity quantity);

    /**
     * @param type     Which nutrient
     * @param quantity Per how much the returned amount should be
     * @return Nutrient per quantity or "0g" if the nutrient isn't specified
     */
    public abstract NutrientQuantity getNutrientOrZeroPerQuantity(Specification.NutrientType type, FoodQuantity quantity);

    public abstract String getAbbreviatedDescription();

    /**
     * @return List of units that are accepted in getNutrientPerQuantity(...). It must return at least one unit.
     */
    public abstract FoodQuantity[] getAcceptedUnits();

    @Override
    public String toString()
    {
        if (Config.DEBUG)
            return getId() + " | " + getDescription();
        else
            return getDescription();
    }
}
