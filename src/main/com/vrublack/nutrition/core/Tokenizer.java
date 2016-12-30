package com.vrublack.nutrition.core;

/**
 * Used in Parser. Tokens are separated by a whitespace or by a transition fromm letters to number, numbers to letters etc.
 */
public class Tokenizer
{
    public enum SegmentType
    {
        Letter,
        Number,
        Other
    }

    // start of current segment (inclusive)
    private int currentSegmentStart = 0;
    // end of current segment (exclusive)
    private int currentSegmentEnd = 0;

    private String str;


    public Tokenizer(String str)
    {
        this.str = str;
    }

    /**
     * Jumps to next token. The token contains whitespaces neither in the front nor in the back.
     *
     * @return Next token
     */
    public String next()
    {
        currentSegmentStart = currentSegmentEnd;

        // ignore whitespaces at the beginning
        while (true)
        {
            if (currentSegmentStart >= str.length())
                return null;
            else if (str.charAt(currentSegmentStart) == ' ')
                currentSegmentStart++;
            else
                break;
        }

        SegmentType segmentType = getSegmentType(str.charAt(currentSegmentStart));

        for (int i = currentSegmentStart; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (c == ' ' || !getSegmentType(c).equals(segmentType))
            {
                // end segment before this
                currentSegmentEnd = i;
                return str.substring(currentSegmentStart, currentSegmentEnd);
            }
        }

        // rest of the string is next segment
        currentSegmentEnd = str.length();
        return str.substring(currentSegmentStart);
    }

    /**
     * Like next(), but doesn't advance the internal position
     *
     * @return Next token
     */
    public String peek()
    {
        int tempCurrentSegmentStart = currentSegmentStart;
        int tempCurrentSegmentEnd = currentSegmentEnd;

        tempCurrentSegmentStart = tempCurrentSegmentEnd;

        // ignore whitespaces at the beginning
        while (true)
        {
            if (tempCurrentSegmentStart >= str.length())
                return null;
            else if (str.charAt(tempCurrentSegmentStart) == ' ')
                tempCurrentSegmentStart++;
            else
                break;
        }


        SegmentType segmentType = getSegmentType(str.charAt(tempCurrentSegmentStart));

        for (int i = tempCurrentSegmentStart; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (c == ' ' || !getSegmentType(c).equals(segmentType))
            {
                // end segment before this
                tempCurrentSegmentEnd = i;
                return str.substring(tempCurrentSegmentStart, tempCurrentSegmentEnd);
            }
        }

        // rest of the string is next segment
        tempCurrentSegmentEnd = str.length();
        return str.substring(tempCurrentSegmentStart);
    }

    public static SegmentType getSegmentType(char c)
    {
        if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')
            return SegmentType.Letter;
        else if (c >= '0' && c <= '9')
            return SegmentType.Number;
        else
            return SegmentType.Other;
    }

    public static SegmentType getSegmentType(String str)
    {
        if (str.isEmpty())
            return SegmentType.Other;
        else
            return getSegmentType(str.charAt(0));
    }
}
