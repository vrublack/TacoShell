package com.vrublack.nutrition.core.usda;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.vrublack.nutrition.core.*;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents specific foodItem in a USDAFoodDatabase
 */
public class USDAFoodItem extends CanonicalSearchableFoodItem implements IsSerializable, Serializable
{
    private static final long serialVersionUID = 14235;

    private String id;
    private String description;
    private DescriptionComp[] descriptionComps;
    private DescriptionComp[] canonicalDescriptionComps;
    private float kcal;
    private int popularity;

    private static int highestPopularity;

    // Nutrients in grams per 100 grams
    private Map<Specification.NutrientType, NutrientQuantity> nutrients;

    private final static FoodQuantity referenceQuantity = new FoodQuantity(100, "g", "g");

    private CommonMeasure[] commonMeasures;

    private UnitConverter.ConversionDefinition[] conversionDefs;

    /**
     * @param id                        NDB number
     * @param description               String that describes foodItem item
     * @param descriptionComps          Components in the description, e.g. "UNCOOKED OIL,OLIVE" -> {"oil", "olive", "uncooked"}
     * @param canonicalDescriptionComps Description comps in canonical form
     * @param nutrients                 Nutrients
     * @param kcal                      Kcal per 100g
     * @param popularity                How often the foodItem is consumed. The higher the number the more often the foodItem is consumed.
     * @param commonMeasures            Common measures for this foodItem item
     */
    public USDAFoodItem(String id, String description, DescriptionComp[] descriptionComps, DescriptionComp[] canonicalDescriptionComps,
                        Map<Specification.NutrientType, NutrientQuantity> nutrients, float kcal, int popularity,
                        CommonMeasure[] commonMeasures)
    {
        this.nutrients = nutrients;

        this.id = id;
        this.description = description;
        this.descriptionComps = descriptionComps;
        this.canonicalDescriptionComps = canonicalDescriptionComps;
        this.kcal = kcal;
        this.popularity = popularity;

        if (popularity > highestPopularity)
            highestPopularity = popularity;

        this.commonMeasures = commonMeasures;

        // derive conversionDefs from commonMeasures
        conversionDefs = new UnitConverter.ConversionDefinition[commonMeasures.length];
        for (int i = 0; i < commonMeasures.length; i++)
        {
            // a common measure always defines a conversion between a unit and grams
            conversionDefs[i] = new UnitConverter.ConversionDefinition(commonMeasures[i].quantifier, commonMeasures[i].getSimpleUnit(), commonMeasures[i].getCompleteUnit(),
                    commonMeasures[i].getAmountInGrams(), "g", "g");
        }
    }

    // default constructor so this class can be serialized in GWT RPC
    public USDAFoodItem()
    {
    }

    @Override
    public String getId()
    {
        return id;
    }

    public CommonMeasure[] getCommonMeasures()
    {
        return commonMeasures;
    }

    @Override
    public DescriptionComp[] getDescriptionComps()
    {
        return descriptionComps;
    }

    @Override
    public DescriptionComp[] getCanonicalDescriptionComps()
    {
        return canonicalDescriptionComps;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public float getCaloriesPerQuantity(FoodQuantity quantity)
    {
        // the unit has to be converted to 100 grams because that's what the kcal are specified in
        float hundredGramQuantifier = UnitConverter.convert(quantity, referenceQuantity, conversionDefs);
        return kcal * hundredGramQuantifier;
    }

    @Override
    public float getRelativePopularity()
    {
        return 100 * popularity / (float) highestPopularity;
    }

    @Override
    public NutrientQuantity getNutrientPerQuantity(Specification.NutrientType type, FoodQuantity quantity)
    {
        // the unit has to be converted to 100 grams because that's what the nutrients are specified in
        float hundredGramQuantifier = UnitConverter.convert(quantity, referenceQuantity, conversionDefs);
        NutrientQuantity nutrientQuantity = nutrients.get(type);
        if (nutrientQuantity != null)
            return nutrientQuantity.scale(hundredGramQuantifier);
        else
            return null;
    }

    @Override
    public NutrientQuantity getNutrientOrZeroPerQuantity(Specification.NutrientType type, FoodQuantity quantity)
    {
        NutrientQuantity resultQuantity = getNutrientPerQuantity(type, quantity);
        if (resultQuantity == null)
            return new NutrientQuantity(0, NutrientQuantity.Unit.g);
        else
            return resultQuantity;
    }

    @Override
    public String getAbbreviatedDescription()
    {
        final int limit = 60;
        boolean needsAbbreviation = description.length() > limit;
        if (needsAbbreviation)
            return description.substring(0, limit) + "...";
        else
            return description;
    }

    @Override
    public FoodQuantity[] getAcceptedUnits()
    {
        FoodQuantity[] list = new FoodQuantity[commonMeasures.length + 1];
        for (int i = 0; i < commonMeasures.length; i++)
        {
            list[i] = new FoodQuantity(1, commonMeasures[i].getSimpleUnit(), commonMeasures[i].getCompleteUnit());
        }

        list[commonMeasures.length] = new FoodQuantity(1, "g", "g");

        return list;
    }

    @Override
    public String getNutritionInformation()
    {
        NutrientQuantity carbs = getNutrientPerQuantity(Specification.NutrientType.Carbohydrates, referenceQuantity);
        NutrientQuantity protein = getNutrientPerQuantity(Specification.NutrientType.Protein, referenceQuantity);
        NutrientQuantity fat = getNutrientPerQuantity(Specification.NutrientType.Fat, referenceQuantity);

        return "Per 100g - Calories: " + getCaloriesPerQuantity(referenceQuantity)
                + "kcal | Fat: " + (fat == null ? "-" : fat)
                + " | Carbs: " + (carbs == null ? "-" : carbs)
                + " | Protein: " + (protein == null ? "-" : protein);
    }

    public static class CommonMeasure implements IsSerializable, Serializable
    {
        private static final long serialVersionUID = 14235;

        /**
         * Name of the measure, like "cup" or "gram" or "cup, crumbled, not packed"
         */
        private String unit;

        /**
         * Quantifier in the unit
         */
        private float quantifier;

        /**
         * Amount of grams in the specified quantifier of the specified unit
         */
        private float amountInGrams;

        public CommonMeasure(String unit, float quantifier, float amountInGrams)
        {
            this.unit = unit;
            this.quantifier = quantifier;
            this.amountInGrams = amountInGrams;
        }

        // default constructor so this class can be serialized in GWT RPC
        public CommonMeasure()
        {
        }

        /**
         * @return Amount in the measure, for example "0.5 cup" -> 0.5, or "cup" -> 1
         */
        public float getMeasureAmount()
        {
            return quantifier;
        }

        /**
         * @return First component of measure, for example "cup" instead of "cup, crumbled, not packed"
         */
        public String getSimpleUnit()
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

        /**
         * @return Full-length measure. Can include details, like "cup, crumbled, not packed"
         */
        public String getCompleteUnit()
        {
            return unit;
        }

        /**
         * @return How many grams are in the measure
         */
        public float getAmountInGrams()
        {
            return amountInGrams;
        }
    }
}
