package com.vrublack.nutrition.core;

import java.util.HashMap;
import java.util.Map;

public class NutritionReferences
{
    private static final Map<Specification.NutrientType, NutrientQuantity> dailyReferenceAmount = new HashMap<>();

    static
    {
        dailyReferenceAmount.put(Specification.NutrientType.Fat, new NutrientQuantity(65, NutrientQuantity.Unit.g));
        dailyReferenceAmount.put(Specification.NutrientType.FatSaturated, new NutrientQuantity(20, NutrientQuantity.Unit.g));
        dailyReferenceAmount.put(Specification.NutrientType.Cholesterol, new NutrientQuantity(300, NutrientQuantity.Unit.Mg));
        dailyReferenceAmount.put(Specification.NutrientType.Sodium, new NutrientQuantity(2400, NutrientQuantity.Unit.Mg));
        dailyReferenceAmount.put(Specification.NutrientType.Potassium, new NutrientQuantity(3500, NutrientQuantity.Unit.Mg));
        dailyReferenceAmount.put(Specification.NutrientType.Carbohydrates, new NutrientQuantity(300, NutrientQuantity.Unit.g));
        dailyReferenceAmount.put(Specification.NutrientType.Fiber, new NutrientQuantity(25, NutrientQuantity.Unit.g));
        dailyReferenceAmount.put(Specification.NutrientType.Protein, new NutrientQuantity(50, NutrientQuantity.Unit.g));
        dailyReferenceAmount.put(Specification.NutrientType.VitaminA, new NutrientQuantity(5000, NutrientQuantity.Unit.IU));
        dailyReferenceAmount.put(Specification.NutrientType.VitaminC, new NutrientQuantity(60, NutrientQuantity.Unit.Mg));
        dailyReferenceAmount.put(Specification.NutrientType.Calcium, new NutrientQuantity(1000, NutrientQuantity.Unit.Mg));
        dailyReferenceAmount.put(Specification.NutrientType.Iron, new NutrientQuantity(18, NutrientQuantity.Unit.Mg));
        dailyReferenceAmount.put(Specification.NutrientType.VitaminD, new NutrientQuantity(400, NutrientQuantity.Unit.IU));
        dailyReferenceAmount.put(Specification.NutrientType.VitaminE, new NutrientQuantity(30, NutrientQuantity.Unit.IU));
        dailyReferenceAmount.put(Specification.NutrientType.VitaminB6, new NutrientQuantity(2, NutrientQuantity.Unit.Mg));
        dailyReferenceAmount.put(Specification.NutrientType.VitaminB12, new NutrientQuantity(6, NutrientQuantity.Unit.Microg));
        dailyReferenceAmount.put(Specification.NutrientType.Magnesium, new NutrientQuantity(400, NutrientQuantity.Unit.Mg));
        dailyReferenceAmount.put(Specification.NutrientType.Zinc, new NutrientQuantity(15, NutrientQuantity.Unit.Mg));
        dailyReferenceAmount.put(Specification.NutrientType.VitaminA, new NutrientQuantity(5000, NutrientQuantity.Unit.IU));

    }


    /**
     * @param nutrientType Desired nutrient type
     * @return Recommended daily value according to <a href="http://www.dsld.nlm.nih.gov/dsld/dailyvalue.jsp">US National Institutes of Health</a>.
     * This is based on a 2000-Calorie diet. The recommended amounts are based on very old studies, so they may be far from
     * reality.
     */
    public static NutrientQuantity getDailyReference(Specification.NutrientType nutrientType)
    {
        return dailyReferenceAmount.get(nutrientType);
    }
}
