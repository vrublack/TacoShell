package com.vrublack.nutrition.core;


import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * Food item that can be searched with <code>FoodSearch</code>
 */
public abstract class SearchableFoodItem extends FoodItem
{
    /**
     * @return Components that should be searched
     */
    public abstract DescriptionComp[] getDescriptionComps();

    /**
     * @return A summary description of key nutritional values for a nominated serving size,
     * e.g. "Per 342g - Calories: 835kcal | Fat: 32.28g | Carbs: 105.43g | Protein: 29.41g"
     */
    public abstract String getNutritionInformation();

    public static class DescriptionComp implements IsSerializable, Serializable
    {
        private static final long serialVersionUID = 1422335;

        public String comp;
        // priority with 1 being the highest and infinity being the lowest
        public int priority;
    }
}
