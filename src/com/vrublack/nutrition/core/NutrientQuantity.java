package com.vrublack.nutrition.core;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class NutrientQuantity implements IsSerializable, Serializable
{
    private static final long serialVersionUID = 14235;


    public enum Unit implements IsSerializable, Serializable
    {
        g,
        Mg,
        Microg,
        IU,
        Percent  // per cent of recommended daily value based on a 2000-calorie diet
    }

    private Unit unit;

    private float amountInUnit;

    /**
     * @param input Unit that the user has entered
     * @return Unit or null if the input wasn't recognized. Capitalization and whitespaces are removed.
     */
    public static Unit getUnitForUserInput(String input)
    {
        input = input.toLowerCase().replace(" ", "");
        for (Unit unit : Unit.values())
        {
            if (unit.name().toLowerCase().replace(" ", "").equals(input))
                return unit;
        }

        return null;
    }

    /**
     * @param amountInUnit Amount in unit per 1 of the referenceUnit
     * @param unit         Unit for amountInUnit
     */
    public NutrientQuantity(float amountInUnit, Unit unit)
    {
        this.amountInUnit = amountInUnit;
        this.unit = unit;
    }

    /**
     *
     * @param str A quantifier, followed by a unit (eg. "3.75 Mg")
     * @return Parsed Nutrient Quantity
     */
    public static NutrientQuantity parseFromString(String str) throws ParseException
    {
        str = str.toLowerCase();

        int unitStart = -1;
        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (c >= 'a' && c <= 'z')
            {
                unitStart = i;
                break;
            }
        }

        if (unitStart == -1)
            throw new ParseException("No unit");
        else if (unitStart == 0)
            throw new ParseException("No quantifier");

        float quantifier = Float.parseFloat(str.substring(0, unitStart));
        String unitStr = str.substring(unitStart);
        Unit unit;

        switch (unitStr)
        {
            case "g": unit = Unit.g;
                break;
            case "mg": unit = Unit.Mg;
                break;
            case "microg": unit = Unit.Microg;
                break;
            case "iu": unit = Unit.Microg;
                break;
            case "%":
            case "percent": unit = Unit.Percent;
                break;
            default: unit = null;
        }

        if (unit == null)
            throw new ParseException("Invalid unit");

        return new NutrientQuantity(quantifier, unit);
    }

    // default constructor so this class can be serialized
    public NutrientQuantity()
    {
    }

    public Unit getUnit()
    {
        return unit;
    }

    /**
     * @return Amount of nutrient in the unit per 100g of the edible part of the foodItem
     */
    public float getAmountInUnit()
    {
        return amountInUnit;
    }

    /**
     * @return New instance with the same unit but factor-times the amount
     */
    public NutrientQuantity scale(float factor)
    {
        return new NutrientQuantity(amountInUnit * factor, unit);
    }

    @Override
    public String toString()
    {
        return amountInUnit + unit.toString();
    }
}
