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

    private final DescriptionBase descriptionBase;


    public HashFoodSearch(List<CanonicalSearchableFoodItem> entries, DescriptionBase base)
    {
        descriptionBase = base;

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
    public List<SearchResultItem> searchFood(String searchString, SearchHistory history, boolean autocomplete)
    {
        String commonId = null;
        if (history != null)
        {
            // TODO do this after the query has been converted to base (to make it invariant under spelling mistakes and formatting)
            commonId = history.getNDBNumberForSearchResult(searchString);
        }

        // possibilities for autocompletions
        String[][] searchPossibilities;
        if (autocomplete)
            searchPossibilities = descriptionBase.descriptionToBaseAutocomplete(searchString);
        else
            searchPossibilities = new String[][]{descriptionBase.descriptionToBase(searchString)};

        // values are vectors for each possible completion.
        final Map<SearchableFoodItem, float[]> matchScores = new HashMap<>();
        for (int i = 0; i < searchPossibilities.length; i++)
        {
            // TODO only iterate over changed comps

            String[] queryComponents = searchPossibilities[i];
            // find entry that occurs most times in items to which the individual comps map
            for (String queryComp : queryComponents)
            {
                if (!entryComps.containsKey(queryComp))
                    continue;

                for (CanonicalSearchableFoodItem item : entryComps.get(queryComp))
                {
                    float matchScore = match(item, queryComp, commonId);
                    float[] prevScores;
                    if (!matchScores.containsKey(item))
                    {
                        matchScores.put(item, new float[searchPossibilities.length]);
                    }
                    prevScores = matchScores.get(item);
                    prevScores[i] += matchScore;
                }
            }
        }

        // write max of score vector to first index
        for (float[] v : matchScores.values())
        {
            for (int i = v.length - 2; i >= 0; i--)
                v[i] = Math.max(v[i + 1], v[i]);
        }

        List<SearchableFoodItem> l = new ArrayList<>(matchScores.keySet());

        Collections.sort(l, new Comparator<SearchableFoodItem>()
        {
            @Override
            public int compare(SearchableFoodItem o1, SearchableFoodItem o2)
            {
                float v1 = matchScores.get(o2)[0];
                float v2 = matchScores.get(o1)[0];
                if (v1 < v2)
                    return -1;
                else if (v1 > v2)
                    return 1;
                else
                    // do this to prevent undefined search order
                    return o1.getDescription().compareTo(o2.getDescription());
            }
        });

        List<SearchResultItem> results = new ArrayList<>();

        for (SearchableFoodItem item : l)
            results.add(new SearchResultItem(item.getId(), item.getDescription(), item.getNutritionInformation(),
                    item.getRelativePopularity(), matchScores.get(item)[0]));

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
