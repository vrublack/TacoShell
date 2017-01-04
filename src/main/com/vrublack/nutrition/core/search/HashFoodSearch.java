package com.vrublack.nutrition.core.search;

import com.vrublack.nutrition.core.CanonicalSearchableFoodItem;
import com.vrublack.nutrition.core.SearchHistory;
import com.vrublack.nutrition.core.SearchResultItem;
import com.vrublack.nutrition.core.SearchableFoodItem;

import java.util.*;

/**
 * Searches foodItem in a list of food items, using the design pattern "visitor". Faster than LevenshteinFoodSearch by
 * an order of magnitude but requires the items to have description comps in canonical formm (see DescriptionBase).
 */
public class HashFoodSearch implements FoodSearch
{
    // these are all > 0.5f to make two matches worth more than one match, regardless of the position
    private final static float[] searchCompFactor = {1f, 0.9f, 0.8f, 0.7f, 0.6f};

    // use hashset as inner structure to make sure components of query don't get matched twice (milk -> "milk, buttermilk")
    private Map<String, Set<CanonicalSearchableFoodItem>> entryComps;

    private SearchHistory searchHistory;

    public HashFoodSearch(List<CanonicalSearchableFoodItem> entries, SearchHistory history)
    {
        searchHistory = history;

        entryComps = new HashMap<>();
        for (CanonicalSearchableFoodItem entry : entries)
        {
            for (SearchableFoodItem.DescriptionComp comp : entry.getCanonicalDescriptionComps())
            {
                if (!entryComps.containsKey(comp.comp))
                    entryComps.put(comp.comp, new HashSet<CanonicalSearchableFoodItem>());
                entryComps.get(comp.comp).add(entry);
            }
        }
    }

    @Override
    public List<SearchResultItem> searchFood(String searchString)
    {
        String commonId = null;
        if (searchHistory != null)
        {
            commonId = searchHistory.getNDBNumberForSearchResult(searchString);
        }

        String[] queryComponents = DescriptionBase.descriptionToBase(searchString);

        final Map<SearchableFoodItem, Float> matchScores = new HashMap<>();

        // find entry that occurrs most times in items to which the inidividual comps map
        for (String queryComp : queryComponents)
        {
            if (!entryComps.containsKey(queryComp))
                continue;

            for (CanonicalSearchableFoodItem item : entryComps.get(queryComp))
            {
                float matchScore = match(item, queryComp, commonId);

                if (!matchScores.containsKey(item))
                    matchScores.put(item, 0f);
                matchScores.put(item, matchScores.get(item) + matchScore);
            }
        }

        List<SearchableFoodItem> l = new ArrayList<>(matchScores.keySet());

        Collections.sort(l, new Comparator<SearchableFoodItem>()
        {
            @Override
            public int compare(SearchableFoodItem o1, SearchableFoodItem o2)
            {
                return matchScores.get(o2).compareTo(matchScores.get(o1));
            }
        });

        List<SearchResultItem> results = new ArrayList<>();

        for (SearchableFoodItem item : l)
            results.add(new SearchResultItem(item.getId(), item.getDescription(), item.getNutritionInformation(),
                    item.getRelativePopularity(), matchScores.get(item)));

        return results;
    }

    /**
     * @param item      Potential matching item
     * @param queryComp Comp of the query that the user entered
     * @param commonId  Id of food item that users have previously chosen for the query
     * @return Score indicating how well the item matches the queryComp
     */
    private float match(CanonicalSearchableFoodItem item, String queryComp, String commonId)
    {
        // if the specific search string was entered before and this foodItem item was what the user was looking for,
        // it is very likely that the user is looking for the same item again
        if (item.getId().equals(commonId))
            return 200;

        float matchScore = 40 * getPositionFactorForComponent(item.getPriorityForCanonicalComp(queryComp));

        if (matchScore > 0)
            matchScore += item.getRelativePopularity() / 20;

        return matchScore;
    }

    private float getPositionFactorForComponent(int position)
    {
        if (position - 1 < searchCompFactor.length)
            return searchCompFactor[position - 1];
        else
            return 0.55f;
    }
}
