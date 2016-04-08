package com.vrublack.nutrition.core;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * Represents a specific amount of foodItem
 */
public abstract class Specification implements IsSerializable, Serializable
{
    private static final long serialVersionUID = 14235;


    public enum NutrientType implements IsSerializable, Serializable
    {
        Carbohydrates,
        Fat,
        FatSaturated,
        FatMonounsaturated,
        FatPolyunsaturated,
        FatTrans,
        Protein,
        Sugar,
        Calcium,
        Iron,
        Magnesium,
        Sodium,
        Zinc,
        VitaminC,
        VitaminB6,
        VitaminB12,
        VitaminA,
        VitaminE,
        VitaminD,
        Cholesterol,
        Potassium, Fiber, Water
    }

    /**
     * @return If the nutrientType is counted as a macro nutrient
     */
    public static boolean isMacroNutrient(NutrientType nutrientType)
    {
        return nutrientType == NutrientType.Carbohydrates || nutrientType == NutrientType.Fat || nutrientType == NutrientType.FatSaturated
                || nutrientType == NutrientType.FatMonounsaturated || nutrientType == NutrientType.FatPolyunsaturated
                || nutrientType == NutrientType.FatTrans || nutrientType == NutrientType.Protein || nutrientType == NutrientType.Sugar;
    }

    /**
     * @param input Nutrient that the user has entered
     * @return NutrientType or null if the input wasn't recognized. Capitalization and whitespaces are removed.
     */
    public static NutrientType getNutrientForUserInput(String input)
    {
        input = input.toLowerCase().replace(" ", "");
        for (NutrientType type : NutrientType.values())
        {
            if (type.name().toLowerCase().replace(" ", "").equals(input))
                return type;
        }

        return null;
    }

    public abstract String getId();

    public abstract float getCalories();

    /**
     * @return Amount of the specified nutrient or <code>null</code> if the nutrient isn't specified for this entry
     */
    public abstract NutrientQuantity getNutrient(NutrientType type);

    /**
     * @return Amount of the specified nutrient or an instance with 0 g if the nutrient isn't specified for this entry
     */
    public abstract NutrientQuantity getNutrientOrZero(NutrientType type);

    public abstract String getDescription();

    public abstract FoodQuantity getAmount();
}

