package com.vrublack.nutrition.core;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UnitConverter
{
    public enum UnitType implements IsSerializable, Serializable
    {
        Volume,
        Mass,
        Number
    }

    private static final String[] numberUnits = {"package", "serving", "piece", "bottle", "num", "slice", "jar",
            "container", "packet", "portion", "envelope", "mini", "small", "medium", "normal", "large", "long", "big", "extra large",
            "slice"};

    private static final String[] massUnits = {"oz", "lb", "g", "kg"};
    // How much of a base unit (grams in this case, but the base unit is arbitrary
    // as long as every value is specified in the same base unit) is in the corresponding massUnit
    private static final Float[] massUnitsBaseAmounts = {28.3495f, 453.592f, 1f, 1000f};

    private static final String[] volumeUnits = {"ml", "l", "liter", "fl oz", "jigger", "glass", "tbsp", "tsp", "cubic inch", "cup", "quart", "pint"};
    // How much of a base unit (ml in this case, but the base unit is arbitrary
    // as long as every value is specified in the same base unit) is in the corresponding volumeUnit
    private static final Float[] volumeUnitsBaseAmounts = {1f, 1000f, 1000f, 29.5735f, 44.3602f,
            236.588f, 14.7868f, 4.92892f, 16.3871f, 236.588f, 946.353f, 473.176f};

    // These don't depend on the nutrient type the refer to
    private static final Map<NutrientQuantity.Unit, Float> independetNutrientUnits = new HashMap<>();

    static
    {
        independetNutrientUnits.put(NutrientQuantity.Unit.g, 1000000.0f);
        independetNutrientUnits.put(NutrientQuantity.Unit.Mg, 1000.0f);
        independetNutrientUnits.put(NutrientQuantity.Unit.Microg, 1.0f);
    }

    // How much 1 IU is for some nutrients
    private static final Map<Specification.NutrientType, NutrientQuantity> internationalUnitConversions = new HashMap<>();

    static
    {
        internationalUnitConversions.put(Specification.NutrientType.VitaminC, new NutrientQuantity(50.0f, NutrientQuantity.Unit.Microg));
        internationalUnitConversions.put(Specification.NutrientType.VitaminD, new NutrientQuantity(0.025f, NutrientQuantity.Unit.Microg));
        internationalUnitConversions.put(Specification.NutrientType.VitaminE, new NutrientQuantity(0.667f, NutrientQuantity.Unit.Mg));
    }

    /**
     * @param input Unit that the user entered
     * @return Canonical form of the unit or null if the unit wasn't recognized
     */
    public static String getUnitForUserInput(String input)
    {
        if (input.isEmpty())
            return null;
        String uncorrected = getUnitForUserInputUncorrected(input);
        if (uncorrected != null)
            return uncorrected;
        // ignore singular/plural
        String plural = getUnitForUserInputUncorrected(input + "s");
        if (plural != null)
            return plural;
        String singular = getUnitForUserInputUncorrected(input.substring(0, input.length() - 1));
        if (singular != null)
            return singular;

        String unit;
        // look for unabbreviated unit and typos
        switch (input)
        {
            case "teaspoon":
            case "teaspoons":
                unit = "tsp";
                break;

            case "tablespoon":
            case "tablespoons":
                unit = "tbsp";
                break;

            case "gram":
            case "grams":
                unit = "g";
                break;

            case "kilogram":
            case "kilograms":
                unit = "kg";
                break;

            case "milliliters":
            case "milliliter":
            case "mililiter":  // accept wrong spelling
            case "mililiters":
                unit = "ml";
                break;

            case "liter":
            case "liters":
                unit = "l";
                break;

            case "pound":
            case "pounds":
            case "lb":
                unit = "lb";
                break;

            case "ounce":
            case "ounces":
                unit = "oz";
                break;

            case "fluid ounces":
            case "fluid ounce":
                unit = "fl oz";
                break;

            // unspecified unit means implicit "num"
            default:
                return null;
        }

        return unit;
    }

    private static String getUnitForUserInputUncorrected(String input)
    {
        if (Arrays.asList(numberUnits).contains(input) ||
                Arrays.asList(massUnits).contains(input) ||
                Arrays.asList(volumeUnits).contains(input))
        {
            return input;
        } else
            return null;
    }

    /**
     * Convenience-method for matchUnit(FoodQuantity source, FoodQuantity[] acceptedUnits, boolean strict),
     * where strict is false
     *
     * @param source        Source unit
     * @param acceptedUnits Units that are accepted
     * @return Quantity with unit from the acceptedUnits that best matches the sourceUnit or null if no unit matches the provided one.
     */
    public static FoodQuantity matchUnit(FoodQuantity source, FoodQuantity[] acceptedUnits)
    {
        return matchUnit(source, acceptedUnits, false);
    }

    /**
     * @param source        Source unit
     * @param acceptedUnits Units that are accepted
     * @param strict        If two units that are unknown, presumably numbers of something ("large", "medium", "steak" ...) can be converted into each other
     * @return Quantity with unit from the acceptedUnits that best matches the sourceUnit or null if no unit matches the provided one.
     */
    public static FoodQuantity matchUnit(FoodQuantity source, FoodQuantity[] acceptedUnits, boolean strict)
    {
        // try to find exact match for detailed unit
        for (FoodQuantity foodQuantity : acceptedUnits)
        {
            if (foodQuantity.getDetailedUnit().equals(source.getDetailedUnit()))
            {
                return new FoodQuantity(source.getQuantifier(), foodQuantity.getSimpleUnit(), foodQuantity.getDetailedUnit());
            }
        }

        // try to find exact match for simple unit
        for (FoodQuantity foodQuantity : acceptedUnits)
        {
            if (foodQuantity.getSimpleUnit().equals(source.getSimpleUnit()))
            {
                return new FoodQuantity(source.getQuantifier(), foodQuantity.getSimpleUnit(), foodQuantity.getDetailedUnit());
            }
        }

        // No exact matches have been found. Now find a common measure that the source unit can be converted into.

        UnitType sourceType = getUnitType(source.getSimpleUnit());

        if (strict && sourceType == UnitType.Number)
            return null;

        for (FoodQuantity foodQuantity : acceptedUnits)
        {
            if (getUnitType(foodQuantity.getSimpleUnit()).equals(sourceType))
            {
                // if number, use the def unit, otherwise keep the source unit
                if (sourceType == UnitType.Number)
                    return new FoodQuantity(source.getQuantifier(), foodQuantity.getSimpleUnit(), foodQuantity.getDetailedUnit());
                else
                    return new FoodQuantity(source.getQuantifier(), source.getSimpleUnit(), source.getSimpleUnit());
            }
        }

        return null;
    }


    /**
     * @param source                Original unit
     * @param desired               Unit that the source should be converted into
     * @param conversionDefinitions Conversion definitions that are used when the types of the source and target unit are not equal,
     *                              e.g. mass and volume
     * @return Quantifier in the desired unit, such that the unit is equal to the source unit
     * @throws IllegalArgumentException If the source and desired unit types can't be directly converted and no corresponding conversion definition is specified
     */
    public static float convert(FoodQuantity source, FoodQuantity desired, ConversionDefinition[] conversionDefinitions) throws IllegalArgumentException
    {
        String sourceSimple = source.getSimpleUnit();
        String desiredSimple = desired.getSimpleUnit();
        float sourceQuantifier = source.getQuantifier();
        float desiredQuantifier = desired.getQuantifier();

        // 1. are the units are directly convertible?
        if (isConvertible(sourceSimple, desiredSimple))
        {
            return convert(sourceQuantifier, sourceSimple, desiredQuantifier, desiredSimple);
        }

        // 2. if the units are not directly convertible, e.g. volume and mass, maybe there is  a
        // conversion definition for that type of conversion
        for (ConversionDefinition conversionDef : conversionDefinitions)
        {
            boolean match = isConvertible(sourceSimple, conversionDef.getFirstUnit())
                    && isConvertible(desiredSimple, conversionDef.getSecondUnit());
            boolean inverseMatch = isConvertible(desiredSimple, conversionDef.getFirstUnit())
                    && isConvertible(sourceSimple, conversionDef.getSecondUnit());

            if (match || inverseMatch)
            {
                if (!match)
                {
                    conversionDef = conversionDef.swapFirstAndSecondUnit();
                }

                // so this definition is used to convert the units
                float firstDefQuantifierInSourceUnit = convert(conversionDef.getFirstUnitQuantifier(), conversionDef.getFirstUnit(),
                        sourceQuantifier, sourceSimple);
                float secondDefQuantifierInDesiredUnit = convert(conversionDef.getSecondUnitQuantifier(), conversionDef.getSecondUnit(),
                        desiredQuantifier, desiredSimple);
                return secondDefQuantifierInDesiredUnit / firstDefQuantifierInSourceUnit;
            }
        }

        throw new IllegalArgumentException("The units can't be converted because the source and desired unit types don't match " +
                "and no corresponding conversion definition is specified");
    }

    public static boolean isConvertible(String firstUnit, String secondUnit)
    {
        UnitType firstUnitType = getUnitType(firstUnit);
        UnitType secondUnitType = getUnitType(secondUnit);

        // e.g. "small" is not directly convertible into "large" without conversion def
        return firstUnit.equals(secondUnit) || firstUnitType == secondUnitType && firstUnitType != UnitType.Number;
    }

    /**
     * @param sourceUnitQuantifier  Quantifier in original unit, e.g. <b>5</b> grams
     * @param simpleSourceUnit      Original unit without sourceUnitQuantifier
     * @param desiredUnitQuantifier Quantifier in desired unit, e.g. <b>5</b> grams
     * @param simpleDesiredUnit     Unit that the sourceUnitQuantifier should be converted into, without sourceUnitQuantifier
     * @return Amount in the desired unit
     * @throws IllegalArgumentException If the unit types are not equal
     */
    public static float convert(float sourceUnitQuantifier, String simpleSourceUnit, float desiredUnitQuantifier,
                                String simpleDesiredUnit) throws IllegalArgumentException
    {
        return sourceUnitQuantifier * convertSimpleUnit(simpleSourceUnit, simpleDesiredUnit) / desiredUnitQuantifier;
    }

    /**
     * @param sourceUnit Source unit without quantifier
     * @param targetUnit Target unit without quantifier
     * @return One of the source unit in the target unit
     * @throws IllegalArgumentException If the unit types are not equal
     */
    private static float convertSimpleUnit(String sourceUnit, String targetUnit) throws IllegalArgumentException
    {
        UnitType unitType = getUnitType(sourceUnit);
        String[] units = null;
        Float[] baseAmounts = null;
        if (unitType == UnitType.Number)
        {
            /*
            // convert numbers of of something 1:1, even though this might be incorrect, but if the user enters
            // "oatmeal", for example, he most likely means 1 package.
            return 1;
            */
            if (sourceUnit.equals(targetUnit))
                return 1.0f;
            else
                throw new IllegalArgumentException("Numbers of something cannot be converted");
        } else if (unitType == UnitType.Mass)
        {
            units = massUnits;
            baseAmounts = massUnitsBaseAmounts;
        } else if (unitType == UnitType.Volume)
        {
            units = volumeUnits;
            baseAmounts = volumeUnitsBaseAmounts;
        } else
        {
            throw new IllegalArgumentException("Illegal unit type");
        }

        int sourceIndex = Arrays.asList(units).indexOf(sourceUnit);
        int targetIndex = Arrays.asList(units).indexOf(targetUnit);
        if (targetIndex == -1)
            throw new IllegalArgumentException(sourceUnit + " cannot be converted into " + targetUnit);

        return baseAmounts[sourceIndex] / baseAmounts[targetIndex];
    }

    /**
     * @param simpleUnit Unit without amount
     * @return Type of unit
     */
    public static UnitType getUnitType(String simpleUnit)
    {
        if (simpleUnit == null)
            return null;

        UnitType[] availableTypes = new UnitType[]{UnitType.Volume, UnitType.Mass, UnitType.Number};

        for (UnitType unitType : availableTypes)
        {
            if (Arrays.asList(getAvailableUnits(unitType)).contains(simpleUnit))
                return unitType;
        }

        // if no known unit matches, this could be a number unit, like "melon" for a watermelon
        return UnitType.Number;
    }

    private static String[] getAvailableUnits(UnitType unitType)
    {
        if (unitType == UnitType.Number)
        {
            return numberUnits;
        } else if (unitType == UnitType.Mass)
        {
            return massUnits;
        } else if (unitType == UnitType.Volume)
        {
            return volumeUnits;
        } else
        {
            return null;
        }
    }

    /**
     * @param source       Original unit
     * @param desired      Unit into which the original unit should be converted
     * @param nutrientType Which unit the quantities refer to. This is important in some cases, like IU conversions being different for each vitamin.
     * @return Quantifier in the desired unit, such that the unit is equal to the source unit
     * @throws IllegalArgumentException If the units couldn't be converted.
     */
    public static float convert(NutrientQuantity source, NutrientQuantity desired, Specification.NutrientType nutrientType) throws IllegalArgumentException
    {
        // same unit?
        if (source.getUnit() == desired.getUnit())
            return source.getAmountInUnit() / desired.getAmountInUnit();


        // is a direct conversion available?
        if (independetNutrientUnits.containsKey(source.getUnit()) && independetNutrientUnits.containsKey(desired.getUnit()))
        {
            return source.getAmountInUnit() * (independetNutrientUnits.get(source.getUnit()) / independetNutrientUnits.get(desired.getUnit())) / desired.getAmountInUnit();
        }
        // first priority: Percentage
        if (NutritionReferences.getDailyReference(nutrientType) != null)
        {
            if (source.getUnit() == NutrientQuantity.Unit.Percent)
                // recursively convert
                return source.getAmountInUnit() / 100.0f * convert(NutritionReferences.getDailyReference(nutrientType), desired, nutrientType);
            else if (desired.getUnit() == NutrientQuantity.Unit.Percent)
                // recursively convert
                return convert(source, NutritionReferences.getDailyReference(nutrientType), nutrientType) / (desired.getAmountInUnit() / 100.0f);
        }
        // second priority: IU
        if (internationalUnitConversions.containsKey(nutrientType))
        {
            if (source.getUnit() == NutrientQuantity.Unit.IU)
                // recursively convert
                return source.getAmountInUnit() * convert(internationalUnitConversions.get(nutrientType), desired, nutrientType);
            else if (desired.getUnit() == NutrientQuantity.Unit.IU)
                // recursively convert
                return convert(source, internationalUnitConversions.get(nutrientType), nutrientType) / desired.getAmountInUnit();
        }

        throw new IllegalArgumentException("Conversion not possible");
    }

    /**
     * Tells the converter how to convert units that are not of the same type, for example "100ml is equivalent to 85g"
     */
    public static class ConversionDefinition implements IsSerializable, Serializable
    {
        private static final long serialVersionUID = 14235;

        private float firstUnitQuantifier;
        private String firstUnit;
        private String firstUnitDetailed;
        private UnitType firstUnitType;

        private float secondUnitQuantifier;
        private String secondUnit;
        private String secondUnitDetailed;
        private UnitType secondUnitType;

        /**
         * Initializes the ConversionDefinition with the two units, so that the first unit with quantifier is equivalent to
         * the second unit with quantifier, e.g. 100 ml is equivalent to 85 grams. The order of the two units is irrelevant for the conversion.
         *
         * @param firstUnitQuantifier  Quantifier in the first unit, e.g. <b>100</b> ml
         * @param firstUnit            First unit
         * @param secondUnitQuantifier Quantifier in the second unit, e.g. <b>85</b> grams
         * @param secondUnit           Second unit
         */
        public ConversionDefinition(float firstUnitQuantifier, String firstUnit, String firstUnitDetailed,
                                    float secondUnitQuantifier, String secondUnit, String secondUnitDetailed)
        {
            this.firstUnitQuantifier = firstUnitQuantifier;
            this.firstUnit = firstUnit;
            this.firstUnitDetailed = firstUnitDetailed;
            this.firstUnitType = getUnitType(firstUnit);
            this.secondUnitQuantifier = secondUnitQuantifier;
            this.secondUnit = secondUnit;
            this.secondUnitDetailed = secondUnitDetailed;
            this.secondUnitType = getUnitType(secondUnit);
        }

        public ConversionDefinition()
        {
        }

        public float getFirstUnitQuantifier()
        {
            return firstUnitQuantifier;
        }

        public String getFirstUnit()
        {
            return firstUnit;
        }

        public String getFirstUnitDetailed()
        {
            return firstUnitDetailed;
        }

        public UnitType getFirstUnitType()
        {
            return firstUnitType;
        }

        public float getSecondUnitQuantifier()
        {
            return secondUnitQuantifier;
        }

        public String getSecondUnit()
        {
            return secondUnit;
        }

        public String getSecondUnitDetailed()
        {
            return secondUnitDetailed;
        }

        public UnitType getSecondUnitType()
        {
            return secondUnitType;
        }

        /**
         * @return New instance with the first and second unit swapped (including quantifier).
         * Note that the order of the two units is irrelevant for the conversion.
         */
        public ConversionDefinition swapFirstAndSecondUnit()
        {
            return new ConversionDefinition(secondUnitQuantifier, secondUnit, secondUnitDetailed,
                    firstUnitQuantifier, firstUnit, firstUnitDetailed);
        }
    }
}