package com.vrublack.nutrition.core.util;

public class StringGenerator
{
    public static String generateString(int length)
    {
        String token = "";

        for (int i = 0; i < length; i++)
        {
            int random = (int) (Math.random() * 52);

            if (random <= 25)
                token += (char) ('a' + random);
            else
                token += (char) ('A' + (random - 26));
        }

        return token;
    }

}
