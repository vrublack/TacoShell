package com.vrublack.nutrition.core;


import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class FoodQuantity implements Serializable, IsSerializable
{
    private static final long serialVersionUID = 14235;

    private String simpleUnit;

    private String detailedUnit;

    private float quantifier;


    public FoodQuantity(float quantifier, String simpleUnit, String detailedUnit)
    {
        this.quantifier = quantifier;
        this.simpleUnit = simpleUnit;
        this.detailedUnit = detailedUnit;
    }

    public FoodQuantity()
    {

    }

    public void setDetailedUnit(float amountInComplexUnit, String detailedUnit)
    {
        this.quantifier = amountInComplexUnit;
        this.detailedUnit = detailedUnit;
    }

    public void setSimpleUnit(String simpleUnit)
    {
        this.simpleUnit = simpleUnit;
    }

    /**
     * @return Unit without details, so just "cup" instead of "cup, sliced"
     */
    public String getSimpleUnit()
    {
        return simpleUnit;
    }

    /**
     * @return Detailed unit that should be shown to the user, like cup, liter, gram, ...
     * Can also include details, like "cup, sliced"
     */
    public String getDetailedUnit()
    {
        return detailedUnit;
    }

    /**
     * Quantifier in detailedUnit, e.g. <b>230</b> ml
     */
    public float getQuantifier()
    {
        return quantifier;
    }

    @Override
    public String toString()
    {
        return quantifier + " " + detailedUnit;
    }
}
