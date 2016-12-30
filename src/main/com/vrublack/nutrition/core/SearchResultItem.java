package com.vrublack.nutrition.core;


import com.Config;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.vrublack.nutrition.core.util.Debug;

import java.io.Serializable;

/**
 * Abbreviated version of a FoodItem returned from search
 */
public class SearchResultItem implements IsSerializable, Serializable
{
    private static final long serialVersionUID = 14235;

    private String id;

    private String description;

    private String nutritionInformation;

    private float relativePopularity;

    private float searchScore;


    public SearchResultItem(String id, String description, String nutritionInformation, float relativePopularity, float searchScore)
    {
        this.id = id;
        this.description = description;
        this.nutritionInformation = nutritionInformation;
        this.relativePopularity = relativePopularity;
        this.searchScore = searchScore;
    }

    public SearchResultItem()
    {

    }

    /**
     * @return How often the foodItem is consumed, with 100 being the most often and 0 being the least often.
     */
    public float getRelativePopularity()
    {
        return relativePopularity;
    }

    /**
     * @return Unique Id that can be used to retrieve the full data for this item from the data source
     */
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return Description of the item, e.g. "Red tomatoes"
     */
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return A summary description of key nutritional values for a nominated serving size, e.g. "Per 100g: 13g carbs, 2.5g fat"
     */
    public String getNutritionInformation()
    {
        return nutritionInformation;
    }

    public void setNutritionInformation(String nutritionInformation)
    {
        this.nutritionInformation = nutritionInformation;
    }

    /**
     * @return Number indicating how well this matched the search term.
     */
    public float getSearchScore()
    {
        return searchScore;
    }

    @Override
    public String toString()
    {
        if (Config.DEBUG)
            return getId() + " | " + getDescription();
        else
            return getDescription();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchResultItem that = (SearchResultItem) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }
}
