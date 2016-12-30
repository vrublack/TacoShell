package com.vrublack.nutrition.core;

public abstract class Formatter
{
    public abstract String format(float number);

    public abstract String round(float number);

    /**
     * @param popularity Number in the range [0, 100]
     * @return String depicting the popularity
     */
    public abstract String formatPopularity(float popularity);

    public abstract String format(DirectSpecification specification);

    public abstract String format(FoodSpecification specification);

    public abstract String format(FoodQuantity foodQuantity);

    public abstract String format(DailyRecord dailyRecord);

    /**
     * Displays totals of days and an average
     */
    public abstract String format(DailyRecord[] lastDays);

    public String format(Specification specification)
    {
        if (specification instanceof DirectSpecification)
            return format((DirectSpecification) specification);
        else
            return format((FoodSpecification) specification);
    }

    public abstract String format(Specification.NutrientType nutrientType);

    public abstract String format(NutrientQuantity.Unit unit);
}
