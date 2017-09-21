package com.vrublack.nutrition.core.search;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellChecker;
import com.vrublack.nutrition.core.Pair;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Provides utilities to convert a description to a canonical, "base" form
 */
public class DescriptionBase
{
    private SpellDictionaryHashMap dictionary;
    private SpellChecker spellChecker;
    // these are already contained in dictionary, but have protected access there...
    private List<Pair<String, Float>> scoredDict;

    private englishStemmer stemmer;

    /**
     * @param simpleDict IS pointing to text file with all words in separate lines
     * @param scoredDict IS pointing to text file with "word,score" in each line
     * @return
     */
    public static DescriptionBase getDescriptionBase(InputStream simpleDict, InputStream scoredDict)
    {
        DescriptionBase b = new DescriptionBase();
        try
        {
            b.dictionary = new SpellDictionaryHashMap(new InputStreamReader(simpleDict));
        } catch (IOException e)
        {
            e.printStackTrace();

            return null;
        }
        b.spellChecker = new SpellChecker(b.dictionary);

        b.stemmer = new englishStemmer();

        b.scoredDict = new ArrayList<>();

        // re-read input stream
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(scoredDict));
            String line;
            while ((line = br.readLine()) != null)
            {
                String[] comps = line.split(",");
                b.scoredDict.add(new Pair<>(comps[0], Float.parseFloat(comps[1])));
            }

        } catch (IOException e)
        {
            // this means autocomplete won't be available
        }

        return b;
    }

    private DescriptionBase()
    {

    }

    /**
     * Tokenizes description and puts it in base form so that it it invariant under spelling errors, whitespace ("cornstarch" vs "corn starch")
     * and certain conjunctions of the word ("fried" vs "fries", "canned" vs "can")
     *
     * @param desc Description, like "brown sugar". Can also contain commas or other separators, like "Tomatoes, canned, no solids"
     * @return Decomposed query in base form
     */
    public String[] descriptionToBase(String desc)
    {
        String[] comps = desc.split("[^\\w]");

        List<String> decomposed = new ArrayList<>();
        for (String c : comps)
        {
            if (!c.isEmpty())
                // TODO correct spelling first?
                for (String d : decompose(c))
                    decomposed.add(componentToBase(d));
        }
        return decomposed.toArray(new String[decomposed.size()]);
    }

    /**
     * Like descriptionToBase(), but does autocomplete on the last component.
     *
     * @param desc Description string that the user has entered
     * @return Like descriptionToBase(), but for each possible completion for the last word,
     * an array of the entire decomposition plus the possible completion is returned.
     * NOTE: possible completion might be multiple components.
     */
    public String[][] descriptionToBaseAutocomplete(String desc)
    {
        String[] comps = desc.split("[^\\w]");

        List<String> decomposed = new ArrayList<>();
        for (int i = 0; i < comps.length; i++)
        {
            String c = comps[i];
            if (!c.isEmpty())
                decomposed.addAll(Arrays.asList(decompose(c)));
        }

        if (decomposed.isEmpty())
            return new String[][]{};
        String lastWord = decomposed.get(decomposed.size() - 1);
        // score in 1st comp because we want to sort by that later
        List<Pair<Float, String>> possibleCompletions = new ArrayList<>();
        // only do autocomplete on the last component
        for (Pair<String, Float> dictWord : scoredDict)
        {
            if (dictWord.first.startsWith(lastWord))
            {
                possibleCompletions.add(new Pair<>(dictWord.second, dictWord.first));
            }
        }
        Collections.sort(possibleCompletions);
        List<String> possibleCompletionsFiltered = new ArrayList<>();
        final int maxCompletions = 5;     // only take top N completions
        for (int i = 0; i < maxCompletions && i < possibleCompletions.size(); i++)
            possibleCompletionsFiltered.add(possibleCompletions.get(possibleCompletions.size() - i - 1).second);


        // If there are no (literal) completions, to autocorrect on the last word as well.
        // TODO Could do completion with error tolerance in the future, possibly.
        if (possibleCompletionsFiltered.isEmpty())
        {
            possibleCompletionsFiltered.add(lastWord);
        }

        // for each possible completion make list (decomposed[0], ... , decomposed[n - 1], possible completion)
        Set<String[]> allPoss = new HashSet<>();    // this avoids duplicates
        for (String possibleCompletion : possibleCompletionsFiltered)
        {
            List<String> poss = new ArrayList<>();
            int j;
            for (j = 0; j < decomposed.size() - 1; j++)
            {
                poss.add(componentToBase(decomposed.get(j)));
            }

            // only add first one, otherwise those with multiple components would alwats be ranked better
            // (example: tomato, tomato seed).
            poss.add(componentToBase(decompose(possibleCompletion)[0]));
            allPoss.add(poss.toArray(new String[poss.size()]));
        }

        return allPoss.toArray(new String[allPoss.size()][]);
    }

    /**
     * Breaks up words into sub-words, e.g. "cornstarch" would result in "corn" and "starch".
     *
     * @return Sub-words or word itself
     */
    private String[] decompose(String word)
    {
        // currently only decompose into at most two words because a) the algorithm is faster,
        // b) there are really only two-compound words and c) otherwise it would be more
        // likely that the word gets broken up into nonsense words

        // try every possible split position
        for (int splitPos = 1; splitPos < word.length(); splitPos++)
        {
            String prefix = word.substring(0, splitPos);
            String suffix = word.substring(splitPos);

            if (dictionary.isCorrect(prefix) && dictionary.isCorrect(suffix))
            {
                return new String[]{prefix, suffix};
            }
        }

        return new String[]{word};
    }


    /**
     * Brings the component into base form, that is correct spelling and form the stem (berry -> berries, for example)
     */

    private String componentToBase(String component)
    {
        component = component.toLowerCase();

        String corrected = component;

        // only spellcheck when length >= 3 since smaller words aren't in the dictionary
        // don't spellcheck numbers, obviously
        // don't spellcheck if already correct
        if (component.length() >= 3 && !Character.isDigit(component.charAt(0)) && !dictionary.isCorrect(component))
        {
            // Check spelling first. This dictionary was extracted from the food database,
            // so only food-realted words are contained
            List<Word> suggestions = spellChecker.getSuggestions(component, 10);
            if (!suggestions.isEmpty())
                corrected = suggestions.get(0).getWord();
        }

        stemmer.setCurrent(corrected);
        if (stemmer.stem())
            return stemmer.getCurrent();
        else
            return corrected;
    }
}
