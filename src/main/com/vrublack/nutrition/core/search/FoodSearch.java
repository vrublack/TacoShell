package com.vrublack.nutrition.core.search;

import com.vrublack.nutrition.core.SearchHistory;
import com.vrublack.nutrition.core.SearchResultItem;

import java.util.List;

public interface FoodSearch
{
    List<SearchResultItem> searchFood(String searchString, SearchHistory history);
}
