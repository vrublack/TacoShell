package com.vrublack.nutrition.core.fatsecret;

import com.vrublack.nutrition.core.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Item from the FatSecret Database
 */
public class FatSecretFoodItem extends FoodItem implements Serializable
{
    private static final long serialVersionUID = 14235;

    private String id;

    private String name;

    private String brandName;

    private String type;

    private Serving[] servings;

    public FatSecretFoodItem(String id, String name, String brandName, String type, Serving[] servings)
    {
        this.id = id;
        this.name = name;
        this.brandName = brandName;
        this.type = type;
        this.servings = servings;
    }

    public FatSecretFoodItem()
    {
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
        if (brandName != null && !brandName.isEmpty())
            desc += "(" + brandName + ")";
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

            // metric unit
            if (UnitConverter.isConvertible(quantity.getSimpleUnit(), serving.metricQuantity.getSimpleUnit()))
            {
                float servingQuantifier = UnitConverter.convert(quantity.getQuantifier(), quantity.getSimpleUnit(),
                        serving.metricQuantity.getQuantifier(), serving.metricQuantity.getSimpleUnit());
                return serving.calories * servingQuantifier;
            }
        }

        throw new IllegalArgumentException("No supported unit");
    }

    @Override
    public float getRelativePopularity()
    {
        return 0;
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

            // metric unit
            if (UnitConverter.isConvertible(quantity.getSimpleUnit(), serving.metricQuantity.getSimpleUnit()))
            {
                float servingQuantifier = UnitConverter.convert(quantity.getQuantifier(), quantity.getSimpleUnit(),
                        serving.metricQuantity.getQuantifier(), serving.metricQuantity.getSimpleUnit());
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
            // was the metric unit already added?
            String metricUnit = serving.metricQuantity.getSimpleUnit();
            if (metricUnit != null)
            {
                boolean alreadyAdded = false;
                for (FoodQuantity acceptedUnit : acceptedUnits)
                    if (acceptedUnit.getSimpleUnit().equals(metricUnit))
                    {
                        alreadyAdded = true;
                        break;
                    }
                if (!alreadyAdded)
                    acceptedUnits.add(new FoodQuantity(1, metricUnit, serving.metricQuantity.getDetailedUnit()));
            }
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


    public static class Serving implements Serializable
    {
        private static final long serialVersionUID = 14235;

        private FoodQuantity quantity;
        private FoodQuantity metricQuantity;

        private float calories;

        private Map<Specification.NutrientType, NutrientQuantity> nutrients;

        public Serving(float quantifier, String unit, float metricQuantifier, String metricUnit, float calories, Map<Specification.NutrientType, NutrientQuantity> nutrients)
        {
            quantity = new FoodQuantity(quantifier, getSimpleUnit(unit), unit);
            metricQuantity = new FoodQuantity(metricQuantifier, getSimpleUnit(metricUnit), metricUnit);

            this.calories = calories;
            this.nutrients = nutrients;
        }

        public Serving()
        {

        }
    }
}
