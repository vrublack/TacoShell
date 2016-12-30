package com.vrublack.nutrition.core;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        Potassium, Fiber, Water,
        Kcal    // often not used
    }

    /**
     * Aliases for Nutrient Types
     */
    private static final Map<NutrientType, String[]> aliases = new HashMap<>();

    static
    {
        // this allows for error tolerance of user input

        aliases.put(NutrientType.Carbohydrates, new String[]{"carbs", "carb", "carbohydrate"});

        aliases.put(NutrientType.Fat, new String[]{"fats"});

        aliases.put(NutrientType.FatSaturated, new String[]{"fat saturated", "fats saturated", "saturated fat", "saturated fats"});

        aliases.put(NutrientType.FatMonounsaturated, new String[]{"fat monounsaturated", "monounsaturated fat", "fats monounsaturated",
                "monounsaturated fats", "fat monounsat", "monounsat fat", "fats monounsat", "monounsat fats"});

        aliases.put(NutrientType.FatPolyunsaturated, new String[]{"fat polyunsaturated", "polyunsaturated fat", "fats polyunsaturated",
                "polyunsaturated fats", "fat polyunsat", "polyunsat fat", "fats polyunsat", "polyunsat fats"});

        aliases.put(NutrientType.FatTrans, new String[]{"fat trans", "fats trans", "trans fat", "trans fats"});

        aliases.put(NutrientType.Kcal, new String[]{"calories", "calorie", "cal", "cals", "kcals"});
    }

    /**
     * Supercategories for Nutrient Types (eg., Fat is a supercategory of FatSaturated)
     */
    private static final Map<NutrientType, NutrientType> supercategories = new HashMap<>();

    static
    {
        supercategories.put(NutrientType.FatTrans, NutrientType.Fat);
        supercategories.put(NutrientType.FatPolyunsaturated, NutrientType.Fat);
        supercategories.put(NutrientType.FatMonounsaturated, NutrientType.Fat);
        supercategories.put(NutrientType.FatSaturated, NutrientType.Fat);

        supercategories.put(NutrientType.Sugar, NutrientType.Carbohydrates);

        // technically, kcal is a supercategory of everything, but they don't translate 1:1 so we'll omit them (1 g carbs isn't 1 kcal)
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
        input = input.toLowerCase().replace("-", "");
        for (NutrientType type : NutrientType.values())
        {
            if (type.name().toLowerCase().replace(" ", "").equals(input))
                return type;

            // search aliases
            if (aliases.containsKey(type))
            {
                for (String alias : aliases.get(type))
                    if (alias.toLowerCase().replace(" ", "").equals(input))
                        return type;
            }
        }

        return null;
    }

    /**
     *
     * @param type Sub-category
     * @return Supercategory of type, eg. Carbohydrates would be a super-category of Sugar. <code>null</code> if there
     * is no such super-category
     */
    public static NutrientType getSuperCategory(NutrientType type)
    {
        if (supercategories.containsKey(type))
            return supercategories.get(type);
        else
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

    @Override
    public String toString()
    {
        return getDescription();
    }
}

