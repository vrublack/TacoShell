package com.vrublack.nutrition.core;


import java.text.SimpleDateFormat;
import java.util.*;

public abstract class RecordManager
{
    private static final String internalDateFormat = "yyyy-MM-dd";

    private List<String> availableRecords;

    private Map<String, DailyRecord> loadedRecords;

    public RecordManager()
    {
        loadedRecords = new HashMap<>();
    }

    public abstract List<String> loadFileNames();

    public abstract DailyRecord loadRecord(String dateString);

    public abstract void saveRecord(DailyRecord record);


    /**
     * @return Record for today. Creates new one if it doesn't exist.
     */
    public DailyRecord getRecordForToday()
    {
        if (availableRecords == null)
            availableRecords = loadFileNames();

        Calendar today = new GregorianCalendar();
        String fileName = getDateString(today);

        DailyRecord record = getRecordForDay(today);

        if (record == null)
        {
            // create new record for today
            record = new DailyRecord(getSimpleCalendar(today));
            availableRecords.add(fileName);
            loadedRecords.put(fileName, record);
            saveRecord(record);
            return record;
        }

        return record;
    }

    /**
     * @param calendar Requested date
     * @return Record for that day or <code>null</code> if it doesn't exist
     */
    public DailyRecord getRecordForDay(Calendar calendar)
    {
        return getRecordForDay(getDateString(calendar));
    }

    /**
     * @param calendar Requested date
     * @return Record for that day or <code>null</code> if it doesn't exist
     */
    public DailyRecord getRecordForDay(SimpleCalendar calendar)
    {
        return getRecordForDay(getDateString(getCalendar(calendar)));
    }

    /**
     * @param dateString String version of requested date according to <code>internalDateFormat</code>
     * @return Record for that day or <code>null</code> if it doesn't exist
     */
    private DailyRecord getRecordForDay(String dateString)
    {
        if (availableRecords == null)
            availableRecords = loadFileNames();

        if (!availableRecords.contains(dateString))
        {
            return null;
        } else
        {
            if (loadedRecords.get(dateString) != null)
            {
                return loadedRecords.get(dateString);
            } else
            {
                DailyRecord record = loadRecord(dateString);
                loadedRecords.put(dateString, record);
                return record;
            }
        }
    }

    /**
     * @param calendar Calendar specifying date
     * @return Record from the latest date before the specified date or <code>null</code> if the specified
     * date is earlier than the earliest available record
     */
    public DailyRecord getPrevious(Calendar calendar)
    {
        if (availableRecords == null)
            availableRecords = loadFileNames();

        Collections.sort(availableRecords);

        String fileName = getDateString(calendar);
        for (int i = availableRecords.size() - 1; i >= 0; i--)
        {
            if (availableRecords.get(i).compareTo(fileName) < 0)
            {
                return getRecordForDay(availableRecords.get(i));
            }
        }

        return null;
    }

    /**
     * @param calendar Calendar specifying date
     * @return Record from the latest date before the specified date or <code>null</code> if the specified
     * date is earlier than the earliest available record
     */
    public DailyRecord getPrevious(SimpleCalendar calendar)
    {
        return getPrevious(getCalendar(calendar));
    }

    /**
     * @param calendar Calendar specifying date
     * @return Record from the earliest date after the specified date or <code>null</code> if the specified
     * date is later than the latest available record
     */
    public DailyRecord getNext(Calendar calendar)
    {
        if (availableRecords == null)
            availableRecords = loadFileNames();

        Collections.sort(availableRecords);

        String fileName = getDateString(calendar);
        for (int i = 0; i < availableRecords.size(); i++)
        {
            if (availableRecords.get(i).compareTo(fileName) > 0)
            {
                return getRecordForDay(availableRecords.get(i));
            }
        }

        return null;
    }

    /**
     * @param n Number of days
     * @return Records of last n days (days that have no daily record are also counted), excluding the current day
     */
    public DailyRecord[] getLastDays(int n)
    {
        Calendar nDaysAgo = new GregorianCalendar();
        nDaysAgo.add(Calendar.DAY_OF_MONTH, -n);
        String nDaysAgoFormat = getDateString(nDaysAgo);

        Collections.sort(availableRecords);

        String todayStr = getDateString(new GregorianCalendar());

        List<DailyRecord> lastRecords = new ArrayList<>();
        for (int i = availableRecords.size() - 1;
             i >= 0 && availableRecords.get(i).compareTo(nDaysAgoFormat) >= 0; i--)
        {
            if (!availableRecords.get(i).equals(todayStr))
            {
                DailyRecord r = getRecordForDay(availableRecords.get(i));
                if (r != null)
                    lastRecords.add(r);
            }
        }

        return lastRecords.toArray(new DailyRecord[lastRecords.size()]);
    }

    /**
     * @param calendar Calendar specifying date
     * @return Record from the earliest date after the specified date or <code>null</code> if the specified
     * date is later than the latest available record
     */
    public DailyRecord getNext(SimpleCalendar calendar)
    {
        return getNext(getCalendar(calendar));
    }


    public String getDateString(Calendar calendar)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(internalDateFormat);
        return simpleDateFormat.format(calendar.getTime());
    }

    public static SimpleCalendar getSimpleCalendar(Calendar calendar)
    {
        return new SimpleCalendar(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, // month is zero-based
                calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    public static Calendar getCalendar(SimpleCalendar simpleCalendar)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.YEAR, simpleCalendar.getYear());
        calendar.set(Calendar.MONTH, simpleCalendar.getMonth() - 1);    // month is zero-based
        calendar.set(Calendar.DAY_OF_MONTH, simpleCalendar.getDay());
        calendar.set(Calendar.HOUR_OF_DAY, simpleCalendar.getHour());
        calendar.set(Calendar.MINUTE, simpleCalendar.getMinute());
        return calendar;
    }

    public static boolean isToday(SimpleCalendar simpleCalendar)
    {
        return getSimpleCalendar(new GregorianCalendar()).equals(simpleCalendar);
    }

    /**
     * @return How many records are available
     */
    public int size()
    {
        if (availableRecords == null)
            availableRecords = loadFileNames();

        return availableRecords.size();
    }
}
