package com.vrublack.nutrition.core;

import java.util.List;

/**
 * Interface for a data source for foodItem that can be searched. The operations are synchronous.
 */
public interface SyncFoodDataSource
{

    /**
     * @param searchStr String to search for
     * @return List of results or <code>null</code> if an error occurred
     */
    List<SearchResultItem> search(String searchStr);

    /**
     * Returns item, but also submits this request to the search history. This should only be called if the user selected this entry.
     *
     * @param id ID
     * @return FoodItem with the specified id or <code>null</code> if no such items exists
     */
    FoodItem retrieve(String id);

    /**
     * Returns item without submitting it to the search history.
     *
     * @param id ID
     * @return FoodItem with the specified id or <code>null</code> if no such items exists
     */
    FoodItem get(String id);

}
