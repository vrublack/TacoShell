package com.vrublack.nutrition.core;

import java.util.*;

/**
 * Provides functionalities that are useful when displaying a list of specifications in a table.
 */
public class SpecificationList
{
    private List<Specification.NutrientType> desiredNutrients;

    private List<Specification> specifications;

    private List<Map<String, NutrientQuantity>> nutrientColumns = new ArrayList<>();

    private List<NutrientQuantity.Unit> defaultUnits = new ArrayList<>();

    private List<Float> totals = new ArrayList<>();

    private float totalKcal;


    /**
     * @param specifications   Specifications ("rows")
     * @param desiredNutrients Nutrients that should be shown ("columns")
     * @param desiredUnits     Unit all values of a column should be converted into (not guaranteed though). If a unit is null, then the most common
     *                         unit will be used for the column
     */
    public SpecificationList(List<Specification> specifications, List<Specification.NutrientType> desiredNutrients,
                             List<NutrientQuantity.Unit> desiredUnits) throws IllegalArgumentException
    {
        init(specifications, desiredNutrients, desiredUnits);
    }

    /**
     * @param specifications   Specifications ("rows")
     * @param desiredNutrients Nutrients that should be shown ("columns")
     */
    public SpecificationList(List<Specification> specifications, List<Specification.NutrientType> desiredNutrients) throws IllegalArgumentException
    {
        List<NutrientQuantity.Unit> desiredUnits = new ArrayList<>();
        for (Specification.NutrientType t : desiredNutrients)
            desiredUnits.add(null);
        init(specifications, desiredNutrients, desiredUnits);
    }

    private void init(List<Specification> specifications, List<Specification.NutrientType> desiredNutrients,
                      List<NutrientQuantity.Unit> desiredUnits) throws IllegalArgumentException
    {
        this.desiredNutrients = desiredNutrients;
        this.specifications = specifications;

        if (desiredNutrients.size() != desiredUnits.size())
            throw new IllegalArgumentException("desiredNutrients.size() != desiredUnits.size()");

        for (int i = 0; i < desiredNutrients.size(); i++)
        {
            Specification.NutrientType nutrientType = desiredNutrients.get(i);
            Map<String, NutrientQuantity> columnList = new HashMap<>();
            NutrientQuantity.Unit desiredUnit = desiredUnits.get(i);
            for (Specification specification : specifications)
            {
                NutrientQuantity quantity = specification.getNutrientOrZero(nutrientType);
                // try to convert
                if (desiredUnit != null)
                {
                    NutrientQuantity desiredQuantity = new NutrientQuantity(1.0f, desiredUnit);
                    try
                    {
                        float convertedFloat = UnitConverter.convert(quantity, desiredQuantity, nutrientType);
                        NutrientQuantity newQuantity = new NutrientQuantity(convertedFloat, desiredUnit);
                        columnList.put(specification.getId(), newQuantity);
                    } catch (IllegalArgumentException e)
                    {
                        columnList.put(specification.getId(), quantity);
                    }
                } else
                {
                    columnList.put(specification.getId(), quantity);
                }
            }
            nutrientColumns.add(columnList);
        }

        for (int i = 0; i < desiredNutrients.size(); i++)
        {
            Specification.NutrientType nutrientType = desiredNutrients.get(i);

            if (desiredUnits.get(i) != null)
            {
                defaultUnits.add(desiredUnits.get(i));
            } else
            {
                // if it wasn't specified, pick most common unit
                NutrientQuantity.Unit mostCommonUnit = getMostCommonUnit(nutrientColumns.get(i).values());
                defaultUnits.add(mostCommonUnit);
            }
        }

        for (int i = 0; i < desiredNutrients.size(); i++)
        {
            totals.add(getTotal(nutrientColumns.get(i).values(), defaultUnits.get(i), specifications.size() - 1));
        }

        for (Specification specification : specifications)
            totalKcal += specification.getCalories();
    }

    /**
     * @return Unit of most quantities
     */
    private NutrientQuantity.Unit getMostCommonUnit(Collection<NutrientQuantity> nutrientQuantities)
    {
        int[] count = new int[5];

        for (NutrientQuantity quantity : nutrientQuantities)
            count[quantity.getUnit().ordinal()]++;

        int max = -1;
        NutrientQuantity.Unit maxUnit = null;

        for (int i = 0; i < count.length; i++)
        {
            if (count[i] > max)
            {
                max = count[i];
                maxUnit = NutrientQuantity.Unit.values()[i];
            }
        }

        return maxUnit;
    }

    private Float getTotal(Collection<NutrientQuantity> nutrientQuantities, NutrientQuantity.Unit unit, int index)
    {
        Float total = 0f;

        int count = 0;
        for (NutrientQuantity nutrientQuantity : nutrientQuantities)
        {
            if (count++ > index)
                break;

            // Ignore units that are not the same as the default unit for now.
            // In the future, they could also be converted, although that's more
            // tricky with % Daily value and IU
            if (nutrientQuantity.getUnit() == unit)
                total += nutrientQuantity.getAmountInUnit();
        }

        return total;
    }

    /**
     * @return Columns which contains the NutrientQuantities for all items with the same unit
     * as the desired unit of that column, if possible. However, this is not guaranteed.
     * Each column refers to the NutrientType with the same index
     */
    public List<Map<String, NutrientQuantity>> getNutrientColumns()
    {
        return nutrientColumns;
    }

    /**
     * @return Units that should be used for the caption
     */
    public List<NutrientQuantity.Unit> getDefaultUnits()
    {
        return defaultUnits;
    }

    /**
     * @return Sums of each nutrient column in the default unit for that column
     */
    public List<Float> getTotals()
    {
        return totals;
    }

    /**
     * @param index Index (inclusive) to which items should be summed up
     * @return Sums of each nutrient column in the default unit for that column
     */
    public List<Float> getTotals(int index)
    {
        List<Float> totals = new ArrayList<>();

        for (int i = 0; i < desiredNutrients.size(); i++)
        {
            totals.add(getTotal(nutrientColumns.get(i).values(), defaultUnits.get(i), index));
        }

        return totals;
    }

    /**
     * @return Sum of all kcal
     */
    public float getTotalKcal()
    {
        return totalKcal;
    }

    /**
     * @param index Index (inclusive) to which items should be summed up
     * @return Sum of all kcal
     */
    public float getTotalKcal(int index)
    {
        float totalKcal = 0;
        for (int i = 0; i < Math.min(index + 1, specifications.size()); i++)
        {
            Specification specification = specifications.get(i);
            totalKcal += specification.getCalories();
        }

        return totalKcal;
    }

}
