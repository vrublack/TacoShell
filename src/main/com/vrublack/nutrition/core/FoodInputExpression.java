package com.vrublack.nutrition.core;

/**
 * Represents something the user has entered, like "40g of sugar"
 */
public class FoodInputExpression
{
    private float quantity;

    private String unit;

    private String description;

    private boolean implicitUnit;

    public FoodInputExpression(String description, float quantity, String unit, boolean implicitUnit)
    {
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.implicitUnit = implicitUnit;
    }

    public String getDescription()
    {
        return description;
    }

    public String getUnit()
    {
        return unit;
    }

    public float getQuantity()
    {
        return quantity;
    }

    public boolean isImplicitUnit()
    {
        return implicitUnit;
    }
}
