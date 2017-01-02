package com.vrublack.nutrition.core.usda;

import com.vrublack.nutrition.core.SearchHistory;
import com.vrublack.nutrition.core.search.DescriptionBase;

import java.io.*;
import java.util.*;


public class USDADatabaseGenerator
{
    private final static String FILENAME = "ABBREV_CUST.txt";

    private SearchHistory searchHistory;


    private void loadModifyAndSave() throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter("ABBREV_CUST_INVARIANT.txt"));

        try (BufferedReader br = new BufferedReader(new FileReader(new File(FILENAME))))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                line = modifyLine(line);

                writer.write(line + "\n");
            }

            writer.close();
        } catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private String modifyLine(String line)
    {
        List<String> c = split(line);

        String ndbNo = parseString(c.get(0));
        String description = parseString(c.get(1));
        String commonNames = parseString(c.get(2));
        String baseDesc = toBase(description);
        String baseCommonNames = toBase(commonNames);

        String newPrefix = "~" + ndbNo + "~^~" + description + "~^~" + baseDesc + "~^~" + commonNames + "~^~" + baseCommonNames + "~";

        List<String> newC = new ArrayList<>();
        newC.add(newPrefix);
        newC.addAll(c.subList(3, c.size()));
        return String.join("^", newC);
    }

    private String toBase(String desc)
    {
        List<String> baseComps = new ArrayList<>();

        for (String s : desc.split(","))
        {
            String[] baseComp = DescriptionBase.descriptionToBase(s);
            baseComps.add(String.join(" ", (CharSequence[]) baseComp));
        }

        return String.join(",", baseComps);
    }

    private String parseString(String str)
    {
        return str.substring(1, str.length() - 1);
    }

    private List<String> split(String line)
    {
        List<String> comps = new ArrayList<>();
        int lastSequenceStart = 0;
        int i;
        for (i = 0; i < line.length(); i++)
        {
            if (line.charAt(i) == '^')
            {
                comps.add(line.substring(lastSequenceStart, i));
                lastSequenceStart = i + 1;
            }
        }
        if (i - lastSequenceStart > 0)
            comps.add(line.substring(lastSequenceStart, i));
        return comps;
    }

    public static void main(String[] args) throws IOException
    {
        USDADatabaseGenerator mod = new USDADatabaseGenerator();
        mod.loadModifyAndSave();
    }
}
