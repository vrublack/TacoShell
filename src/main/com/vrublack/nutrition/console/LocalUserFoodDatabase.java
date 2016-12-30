package com.vrublack.nutrition.console;

import com.vrublack.nutrition.core.userdb.UserFoodDatabase;

import java.io.*;

public class LocalUserFoodDatabase extends UserFoodDatabase
{
    private final static String FILENAME = "USER_DB.txt";

    public LocalUserFoodDatabase()
    {
        // parent constructor loads ascii file
    }

    @Override
    public BufferedReader getBufferedReader() throws IOException
    {
        File file = new File(FILENAME);
        if (!file.exists())
            file.createNewFile();
        return new BufferedReader(new FileReader(file));
    }

    @Override
    public BufferedWriter getBufferedWriter() throws IOException
    {
        return new BufferedWriter(new FileWriter(FILENAME, true));
    }
}
