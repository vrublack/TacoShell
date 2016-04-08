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
