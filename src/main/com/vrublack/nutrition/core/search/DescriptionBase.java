package com.vrublack.nutrition.core.search;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellChecker;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides utilities to convert a description to a canonical, "base" form
 */
public class DescriptionBase
{
    private SpellDictionaryHashMap dictionary;
    private SpellChecker spellChecker;

    private englishStemmer stemmer;

    public static DescriptionBase getDescriptionBase(InputStream is)
    {
        DescriptionBase b = new DescriptionBase();
        try
        {
            b.dictionary = new SpellDictionaryHashMap(new InputStreamReader(is));
        } catch (IOException e)
        {
            e.printStackTrace();

            return null;
        }
        b.spellChecker = new SpellChecker(b.dictionary);

        b.stemmer = new englishStemmer();

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
