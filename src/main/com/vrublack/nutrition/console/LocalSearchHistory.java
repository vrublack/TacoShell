package com.vrublack.nutrition.console;

import com.vrublack.nutrition.core.Pair;
import com.vrublack.nutrition.core.SearchHistory;
import com.vrublack.nutrition.core.SearchStringStat;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores/retrieves search feedback from local file
 */
public class LocalSearchHistory implements SearchHistory
{
    // singleton pattern
    private static LocalSearchHistory instance;

    private final static String LOCAL_FILE_PATH = "search_stats";

    // a Map would be faster but for a small number of entries this is irrelevant
    private ArrayList<SearchStringStat> stats;

    private LocalSearchHistory()
    {
        ObjectInputStream ois = null;
        try
        {
            ois = new ObjectInputStream(new FileInputStream(new File(LOCAL_FILE_PATH)));
            stats = (ArrayList<SearchStringStat>) ois.readObject();
        } catch (IOException e)
        {
            stats = new ArrayList<>();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        } finally
        {
            if (ois != null)
                try
                {
                    ois.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
        }
    }

    public static LocalSearchHistory getInstance()
    {
        if (instance == null)
            instance = new LocalSearchHistory();
        return instance;
    }

    @Override
    public String getNDBNumberForSearchResult(String searchString)
    {
        for (SearchStringStat stat : stats)
        {
            if (stat.getSearchString().trim().equalsIgnoreCase(searchString.trim()))
            {
                return stat.getMostCommonPair().first;
            }
        }

        return null;
    }

    @Override
    public synchronized void putNDBNumberForSearchResult(final String searchString, final String selectedNDBNumber)
    {
        // this can be offloaded into a background thread
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                SearchStringStat existing = null;
                for (SearchStringStat stat : stats)
                {
                    if (stat.getSearchString().trim().equalsIgnoreCase(searchString.trim()))
                    {
                        existing = stat;
                        break;
                    }
                }

                if (existing == null)
                {
                    existing = new SearchStringStat(searchString);
                    stats.add(existing);
                }

                existing.putResult(selectedNDBNumber);


                // immediately save
                ObjectOutputStream oos = null;
                try
                {
                    oos = new ObjectOutputStream(new FileOutputStream(new File(LOCAL_FILE_PATH)));
                    oos.writeObject(stats);
                } catch (IOException e)
                {
                    e.printStackTrace();
                } finally
                {
                    if (oos != null)
                        try
                        {
                            oos.close();
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                }
            }
        };

        t.start();
    }

    /**
     * @return List of pairs (query, most commond id for that query)
     */
    public List<Pair<String, String>> getQueryIdPairs()
    {
        List<Pair<String, String>> pairs = new ArrayList<>();
        for (SearchStringStat stat : stats)
        {
            pairs.add(new Pair<>(stat.getSearchString(), stat.getMostCommonPair().first));
        }

        return pairs;
    }
}
