package com.vrublack.nutrition.core.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class Debug
{
    public static void writeToFile(String filename, String content)
    {
        try
        {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            writer.println(content);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }
}
