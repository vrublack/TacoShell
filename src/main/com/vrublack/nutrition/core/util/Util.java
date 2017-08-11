package com.vrublack.nutrition.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Provides utility and convenience methods
 */
public class Util
{
    public static String readFile(File path) throws IOException
    {
        try (BufferedReader br = new BufferedReader(new FileReader(path)))
        {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null)
            {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            return sb.toString();
        }
    }
}
