package com.vrublack.nutrition.core;

/**
 * Responsible for keeping track of which FoodItem a user is looking for when entering a particular search string
 */
public interface SearchHistory
{
    /**
     * @param searchString Search string that the user entered
     * @return NDB number of foodItem that a user most likely is looking for when entering the searchString
     */
    String getNDBNumberForSearchResult(String searchString);

    /**
     * Updates data with the provided feedback
     *
     * @param searchString      Search string that the user entered
     * @param selectedNDBNumber NDB number of the foodItem that the user selected after entering the search string
     */
    void putNDBNumberForSearchResult(String searchString, String selectedNDBNumber);
}
