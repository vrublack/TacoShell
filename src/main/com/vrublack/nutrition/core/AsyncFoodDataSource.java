package com.vrublack.nutrition.core;

import java.util.List;

/**
 * Interface for a data source for foodItem that can be searched. The operations are asynchronous.
 */
public interface AsyncFoodDataSource
{

    /**
     * @param searchStr String to search for
     * @param callback  Called when the search results are available. This is asynchronous because the
     *                  implementing class may not be a local database but fetch the results via a remote
     *                  connection.
     */
    void search(String searchStr, SearchCallback callback);

    interface SearchCallback
    {
        void onSuccess(List<SearchResultItem> results);

        void onFailure(int errorCode);
    }

    /**
     * Returns item, but also submits this request to the search history. This should only be called if the user selected this entry.
     *
     * @param id       ID
     * @param callback Called when the item is available. This is asynchronous because the
     *                 implementing class may not be a local database but fetch the results via a remote
     *                 connection.
     */
    FoodItem retrieve(String id, RetrieveCallback callback);

    /**
     * Returns item without submitting it to the search history.
     *
     * @param id       ID
     * @param callback Called when the item is available. This is asynchronous because the
     *                 implementing class may not be a local database but fetch the results via a remote
     *                 connection.
     */
    FoodItem get(String id, RetrieveCallback callback);

    interface RetrieveCallback
    {
        void onSuccess(FoodItem foodItem);

        void onFailure(int errorCode);
    }

}
