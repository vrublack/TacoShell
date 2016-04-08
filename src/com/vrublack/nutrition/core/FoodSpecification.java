package com.vrublack.nutrition.core;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * Represents specific amount of a foodItem
 */
public class FoodSpecification extends Specification implements IsSerializable, Serializable
{
    private static final long serialVersionUID = -5148953475058027497L;

    private FoodItem foodItem;

    private FoodQuantity quantity;

    private String id;

    private boolean microNutrientsOnly = false;


    /**
     * Default constructor for GWT RPC serialization. Don't use in own code!
     */
    public FoodSpecification()
    {

    }

    /**
     * @param foodItem Which foodItem
     * @param quantity Specifies the amount of the item. It will be converted into an appropriate unit (if possible).
     * @param strict   See UnitConverter.match
     * @throws IllegalArgumentException If the specified unit was inappropriate.
     */
    public FoodSpecification(FoodItem foodItem, FoodQuantity quantity, boolean strict)
    {
        init(foodItem, quantity.getQuantifier(), quantity.getSimpleUnit(), false, strict);
    }

    /**
     * @param foodItem   Which foodItem
     * @param quantifier Quantifier in the specified unit, e.g. <b>5</b> grams
     * @param unit       Unit. It will be converted into an appropriate unit (if possible)
     * @throws IllegalArgumentException If the specified unit was inappropriate.
     */
    public FoodSpecification(FoodItem foodItem, float quantifier, String unit) throws IllegalArgumentException
    {
        init(foodItem, quantifier, unit, false, false);
    }

    /**
     * @param foodItem           Which foodItem
     * @param quantifier         Quantifier in the specified unit, e.g. <b>5</b> grams
     * @param unit               Unit. It will be converted into an appropriate unit (if possible)
     * @param microNutrientsOnly If only micro nutrients should be included and 0 should be returned for all macros
     * @throws IllegalArgumentException If the specified unit was inappropriate.
     */
    public FoodSpecification(FoodItem foodItem, float quantifier, String unit, boolean microNutrientsOnly) throws IllegalArgumentException
    {
        init(foodItem, quantifier, unit, microNutrientsOnly, false);
    }

    private void init(FoodItem foodItem, float quantifier, String unit, boolean microNutrientsOnly, boolean strict)
    {
        this.foodItem = foodItem;
        this.quantity = UnitConverter.matchUnit(new FoodQuantity(quantifier, unit, unit), foodItem.getAcceptedUnits(), strict);
        if (this.quantity == null)
            throw new IllegalArgumentException("Invalid unit");
        this.id = foodItem.getId() + StringGenerator.generateString(5);
        this.microNutrientsOnly = microNutrientsOnly;
    }

    @Override
    public FoodQuantity getAmount()
    {
        return quantity;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public float getCalories()
    {
        if (microNutrientsOnly)
            return 0;
        else
            return foodItem.getCaloriesPerQuantity(quantity);
    }

    @Override
    public NutrientQuantity getNutrient(NutrientType type)
    {
        if (microNutrientsOnly && isMacroNutrient(type))
            return null;
        else
            return foodItem.getNutrientPerQuantity(type, quantity);
    }

    @Override
    public NutrientQuantity getNutrientOrZero(NutrientType type)
    {
        NutrientQuantity quantity = getNutrient(type);
        if (quantity == null)
            return new NutrientQuantity(0, NutrientQuantity.Unit.g);
        else
            return quantity;
    }

    @Override
    public String getDescription()
    {
        return foodItem.getDescription();
    }

    public FoodItem getFoodItem()
    {
        return foodItem;
    }

    public FoodQuantity getQuantity()
    {
        return quantity;
    }
}
