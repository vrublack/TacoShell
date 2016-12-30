package com.vrublack.nutrition.core;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class SimpleCalendar implements Serializable, IsSerializable, Comparable<SimpleCalendar>
{
    private static final long serialVersionUID = 14235;

    private int day;
    private int month;
    private int year;
    private int hour;
    private int minute;


    public SimpleCalendar(int day, int month, int year, int hour, int minute)
    {
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
    }

    public SimpleCalendar()
    {

    }

    public int getDay()
    {
        return day;
    }

    public int getMonth()
    {
        return month;
    }

    public int getYear()
    {
        return year;
    }

    public int getHour()
    {
        return hour;
    }

    public int getMinute()
    {
        return minute;
    }

    public void setDay(int day)
    {
        this.day = day;
    }

    public void setMonth(int month)
    {
        this.month = month;
    }

    public void setYear(int year)
    {
        this.year = year;
    }

    public void setHour(int hour)
    {
        this.hour = hour;
    }

    public void setMinute(int minute)
    {
        this.minute = minute;
    }

    /**
     * @return Date in format yyyy-MM-dd
     */
    public String format()
    {
        return zeroPrefix(year, 4) + "-" + zeroPrefix(month, 2) + "-" + zeroPrefix(day, 2);
    }

    @Override
    public String toString()
    {
        return format();
    }

    private String zeroPrefix(int number, int fixedDigitsNum)
    {
        String formattedNumber = Integer.toString(number);

        while (formattedNumber.length() < fixedDigitsNum)
        {
            formattedNumber = "0" + formattedNumber;
        }

        return formattedNumber;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleCalendar that = (SimpleCalendar) o;

        if (day != that.day) return false;
        if (month != that.month) return false;
        if (year != that.year) return false;
        if (hour != that.hour) return false;
        return minute == that.minute;

    }

    @Override
    public int hashCode()
    {
        int result = day;
        result = 31 * result + month;
        result = 31 * result + year;
        result = 31 * result + hour;
        result = 31 * result + minute;
        return result;
    }


    @Override
    public int compareTo(SimpleCalendar o)
    {
        if (year != o.year)
            return Integer.compare(year, o.year);
        if (month != o.month)
            return Integer.compare(month, o.month);
        if (day != o.day)
            return Integer.compare(day, o.day);
        if (hour != o.hour)
            return Integer.compare(month, o.month);
        if (minute != o.minute)
            return Integer.compare(minute, o.minute);
        return 0;
    }
}
