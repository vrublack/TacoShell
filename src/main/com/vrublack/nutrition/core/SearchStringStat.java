package com.vrublack.nutrition.core;

import java.io.Serializable;
import java.util.ArrayList;


public class SearchStringStat implements Serializable
{
    private static final long serialVersionUID = 10;

    private String searchString;

    // 1st comp: NDB number, 2nd comp: how often that number was chosen for the search string
    private ArrayList<Pair<String, Integer>> ndbNoFrequencyMapping;

    public SearchStringStat(String searchString)
    {
        this.searchString = searchString;
        ndbNoFrequencyMapping = new ArrayList<>();
    }


    public SearchStringStat()
    {
    }

    public String getSearchString()
    {
        return searchString;
    }

    /**
     * @param ndbNumber NDB number of the foodItem that the user selected after searching for this search string
     */
    public void putResult(String ndbNumber)
    {
        for (int i = 0; i < ndbNoFrequencyMapping.size(); i++)
            if (ndbNoFrequencyMapping.get(i).first.equals(ndbNumber))
            {
                int prevFrequency = ndbNoFrequencyMapping.get(i).second;
                ndbNoFrequencyMapping.set(i, new Pair<>(ndbNumber, prevFrequency + 1));
                return;
            }

        // this ndb number hasn't been recorded yet
        ndbNoFrequencyMapping.add(new Pair<>(ndbNumber, 1));
    }

    /**
     * @return Pair consisting of NDB number (1st comp) and frequency (2nd comp) with the highest frequency of all pairs
     */
    public Pair<String, Integer> getMostCommonPair()
    {
        if (ndbNoFrequencyMapping.isEmpty())
            return null;

        Pair<String, Integer> highestPair = ndbNoFrequencyMapping.get(0);

        for (Pair<String, Integer> pair : ndbNoFrequencyMapping)
            if (pair.second > highestPair.second)
                highestPair = pair;

        return highestPair;
    }


    /**
     * @return Pair consisting of NDB number (1st comp) and frequency (2nd comp) with the second highest frequency of all pairs
     */
    public Pair<String, Integer> getSecondMostCommonPair()
    {
        Pair<String, Integer> mostCommonPair = getMostCommonPair();
        if (mostCommonPair == null)
            return null;

        // remove most common pair and then do the same as in getMostCommonPair
        ArrayList<Pair<String, Integer>> listCopy = new ArrayList<>();
        listCopy.addAll(ndbNoFrequencyMapping);
        listCopy.remove(mostCommonPair);

        if (listCopy.isEmpty())
            return null;

        Pair<String, Integer> highestPair = listCopy.get(0);

        for (Pair<String, Integer> pair : listCopy)
            if (pair.second > highestPair.second)
                highestPair = pair;

        return highestPair;
    }

    @Override
    public String toString()
    {
        return searchString;
    }
}
