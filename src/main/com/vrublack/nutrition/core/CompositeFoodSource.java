package com.vrublack.nutrition.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Combines to food sources into one
 */
public class CompositeFoodSource implements SyncFoodDataSource
{
    // Composite design pattern

    private SyncFoodDataSource left;
    private SyncFoodDataSource right;


    public CompositeFoodSource(SyncFoodDataSource left, SyncFoodDataSource right)
    {
        this.left = left;
        this.right = right;
    }

    @Override
    public List<SearchResultItem> search(String searchStr, SearchHistory history, boolean autocomplete)
    {
        List<SearchResultItem> leftResults = left.search(searchStr, history, autocomplete);
        List<SearchResultItem> rightResults = right.search(searchStr, history, autocomplete);

        List<SearchResultItem> all = new ArrayList<>();
        all.addAll(leftResults);
        all.addAll(rightResults);

        Collections.sort(all, new Comparator<SearchResultItem>()
        {
            @Override
            public int compare(SearchResultItem o1, SearchResultItem o2)
            {
                if (o1.getSearchScore() > o2.getSearchScore())
                    return -1;
                else if (o1.getSearchScore() == o2.getSearchScore())
                    return 0;
                else
                    return 1;
            }
        });

        return all;
    }

    @Override
    public FoodItem retrieve(String id, SearchHistory history)
    {
        FoodItem item = left.retrieve(id, history);
        if (item == null)
            item = right.retrieve(id, history);
        return item;
    }

    @Override
    public FoodItem get(String id)
    {
        FoodItem item = left.get(id);
        if (item == null)
            item = right.get(id);
        return item;
    }
}
