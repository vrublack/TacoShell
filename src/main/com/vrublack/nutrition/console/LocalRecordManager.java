package com.vrublack.nutrition.console;


import com.vrublack.nutrition.core.DailyRecord;
import com.vrublack.nutrition.core.RecordManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LocalRecordManager extends RecordManager
{
    private static final String FOLDER = "records";

    @Override
    public List<String> loadFileNames()
    {
        File folder = new File(FOLDER);
        List<String> fileNames = new ArrayList<>();
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null)
            return fileNames;

        for (File file : listOfFiles)
        {
            if (file.isFile())
            {
                fileNames.add(file.getName());
            }
        }

        return fileNames;
    }

    @Override
    public DailyRecord loadRecord(String dateString)
    {
        File recordFile = new File(FOLDER + "/" + dateString);
        if (!recordFile.exists())
            return null;

        try
        {
            FileInputStream fis = new FileInputStream(recordFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            return (DailyRecord) ois.readObject();

        } catch (FileNotFoundException e)
        {
            return null;
        } catch (IOException e)
        {
            return null;
        } catch (ClassNotFoundException e)
        {
            return null;
        }
    }

    @Override
    public void saveRecord(DailyRecord record)
    {
        File folder = new File(FOLDER);
        if (!folder.exists())
            folder.mkdir();
        try
        {
            String fileName = FOLDER + "/" + getDateString(RecordManager.getCalendar(record.getDate()));
            FileOutputStream fos = new FileOutputStream(new File(fileName));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(record);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
