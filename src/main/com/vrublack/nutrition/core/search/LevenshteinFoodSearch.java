package com.vrublack.nutrition.core.search;

import com.vrublack.nutrition.core.SearchHistory;
import com.vrublack.nutrition.core.SearchResultItem;
import com.vrublack.nutrition.core.SearchableFoodItem;

import java.io.Serializable;
import java.util.*;

/**
 * Searches foodItem in a list of food items, using the design pattern "visitor"
 */
public class LevenshteinFoodSearch implements FoodSearch
{
    private final static float[] searchCompFactor = {1f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f};

    private List<SearchableFoodItem> entries;


    public LevenshteinFoodSearch(List<SearchableFoodItem> entries)
    {
        this.entries = entries;
    }

    @Override
    public List<SearchResultItem> searchFood(String searchString, SearchHistory history, boolean autocomplete)
    {
        // autocomplete is not yet implemented here, so ignore

        String ndbNumber = null;
        if (history != null)
        {
            ndbNumber = history.getNDBNumberForSearchResult(searchString);
        }

        // parse sorted components of the description
        String[] queryComponents = searchString.split("[ ,]");
        Arrays.sort(queryComponents);

        List<InternalResultItem> matches = new LinkedList<>();

        for (SearchableFoodItem entry : entries)
        {
            float score = match(queryComponents, entry, ndbNumber);
            if (score > 0)
            {
                matches.add(new InternalResultItem(entry, score));
            }
        }

        Collections.sort(matches, new Comparator<InternalResultItem>()
        {
            @Override
            public int compare(InternalResultItem o1, InternalResultItem o2)
            {
                if (o1.score == o2.score)
                    // do this to prevent undefined search order
                    return o1.foodItem.getDescription().compareTo(o2.foodItem.getDescription());
                else if (o1.score < o2.score)
                    return 1;
                else
                    return -1;
            }
        });

        return strip(matches);
    }

    private List<SearchResultItem> strip(List<InternalResultItem> matches)
    {
        List<SearchResultItem> searchResultItems = new LinkedList<>();
        for (InternalResultItem internalResultItem : matches)
        {
            String id = internalResultItem.foodItem.getId();
            String description = internalResultItem.foodItem.getDescription();
            String nutritionInformation = internalResultItem.foodItem.getNutritionInformation();

            searchResultItems.add(new SearchResultItem(id, description, nutritionInformation, internalResultItem.foodItem.getRelativePopularity(), internalResultItem.score));
        }
        return searchResultItems;
    }

    private float match(String[] searchComps, SearchableFoodItem entry, String commonNdbNumberForSearchString)
    {
        SearchableFoodItem.DescriptionComp[] descriptionComps = entry.getDescriptionComps();

        // if the specific search string was entered before and this foodItem item was what the user was looking for,
        // it is very likely that the user is looking for the same item again
        if (entry.getId().equals(commonNdbNumberForSearchString))
            return 200;

        float score = 0;
        // this prevents that search components are matched multiple times, such as "milk, buttermilk" when search for "milk", because
        // then "milk,buttermilk" would receive a higher score than simply "milk"
        float[] highestMatchesPerComponent = new float[searchComps.length];

        for (int i = 0; i < searchComps.length; i++)
        {
            for (int j = 0; j < descriptionComps.length; j++)
            {
                String searchComp = searchComps[i].toLowerCase();
                SearchableFoodItem.DescriptionComp descriptionComp = descriptionComps[j];
                String descriptionCompStr = descriptionComps[j].comp.toLowerCase();
                int comparison = compareComps(searchComp, descriptionCompStr);

                if (comparison == 0)    // they're equal
                {
                    // the further in the back of a description something stands, the more irrelevant it is,
                    // e.gram. POTATO is more relevant in "POTATO,RAW" than in "SOUP,POTATO"
                    float match = 80 * getPositionFactorForComponent(descriptionComp.priority);
                    if (match > highestMatchesPerComponent[i])
                        highestMatchesPerComponent[i] = match;
                } else      // not equal
                {
                    // maybe there's a partial match

                    // don't match partially if the comp is very small
                    if (descriptionCompStr.length() > 2)
                    {
                        // the first component is sort of the category, so if that is contained in the search string, it is more likely
                        // that this is what the user is looking for. Example: search for "buttermilk", correct entry is "Milk, buttermilk, ..."
                        if (descriptionComp.priority == 1 && searchComp.contains(descriptionCompStr))
                        {
                            score += 10;
                        } else if (searchComp.contains(descriptionCompStr) || descriptionCompStr.contains(searchComp))
                        {
                            float match = 10 * getPositionFactorForComponent(descriptionComp.priority);
                            if (match > highestMatchesPerComponent[i])
                                highestMatchesPerComponent[i] = match;
                        }
                    }
                }
            }
        }

        for (float aHighestMatchesPerComponent : highestMatchesPerComponent)
            score += aHighestMatchesPerComponent;

        if (score > 0)
            score += entry.getRelativePopularity() / 20;

        // add bonus for the occurrence of "raw" or "fresh" because those entries tend to be more commonly searched for,
        // but only if there were matches so far
        if (score > 0)
        {
            for (int j = 0; j < descriptionComps.length; j++)
                if (descriptionComps[j].comp.equalsIgnoreCase("raw") || descriptionComps[j].comp.equalsIgnoreCase("fresh"))
                {
                    score += 0.2;
                    break;
                }
        }

        return score;
    }

    private float getPositionFactorForComponent(int position)
    {
        if (position - 1 < searchCompFactor.length)
            return searchCompFactor[position - 1];
        else
            return 0.2f;
    }

    private int compareComps(String strOne, String strTwo)
    {
        // ignore singular/plural
        if ((strOne + "s").equals(strTwo) || (strTwo + "s").equals(strOne)
                // potatoES, tomatoES
                || (strOne + "es").equals(strTwo) || (strTwo + "es").equals(strOne))
            return 0;
            // already in plural? put in singular
        else if (strOne.endsWith("s") && strOne.substring(0, strOne.length() - 1).equals(strTwo)
                || strOne.endsWith("es") && strOne.substring(0, strOne.length() - 2).equals(strTwo)
                || strTwo.endsWith("s") && strTwo.substring(0, strTwo.length() - 1).equals(strOne)
                || strTwo.endsWith("es") && strTwo.substring(0, strTwo.length() - 2).equals(strOne))
            return 0;

        int levenshteinDist = computeLevenshteinDistance(strOne, strTwo);
        if (levenshteinDist == 0)
            return 0;

        // allow long words to contain typos
        if (Math.min(strOne.length(), strTwo.length()) >= 5 && levenshteinDist == 1 ||
                Math.min(strOne.length(), strTwo.length()) >= 9 && levenshteinDist == 2)
            return 0;

        // the rest is identical to the implementation in String.compare
        int len1 = strOne.length();
        int len2 = strTwo.length();
        int lim = Math.min(len1, len2);

        int k = 0;
        while (k < lim)
        {
            char c1 = strOne.charAt(k);
            char c2 = strTwo.charAt(k);
            if (c1 != c2)
            {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }

    /**
     * Calculate normalized levenshtein distance
     *
     * @return Normalized levenshtein distance (in the range of [0, 1] with 1 being the furthest
     */
    private static double computeNormalizedDistance(String lhs, String rhs)
    {
        int max = Math.max(lhs.length(), rhs.length());
        if (max != 0)
            return computeLevenshteinDistance(lhs, rhs) / (double) max;
        else
            return 1;
    }

    /**
     * @return Levenshtein distance
     */
    private static int computeLevenshteinDistance(String a, String b)
    {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++)
        {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++)
            {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    private static int minimum(int a, int b, int c)
    {
        return Math.min(Math.min(a, b), c);
    }

    private static class InternalResultItem implements Serializable
    {
        private static final long serialVersionUID = 14235;

        public SearchableFoodItem foodItem;
        public float score;

        public InternalResultItem(SearchableFoodItem foodItem, float score)
        {
            this.foodItem = foodItem;
            this.score = score;
        }

        public InternalResultItem()
        {

        }
    }
}