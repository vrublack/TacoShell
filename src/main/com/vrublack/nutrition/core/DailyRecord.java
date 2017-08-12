package com.vrublack.nutrition.core;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyRecord implements Serializable, IsSerializable
{
    private static final long serialVersionUID = 14235;

    private List<Specification> entries;
    // when the foodItem with the same index was added
    private List<SimpleCalendar> addedDates;

    private SimpleCalendar date;

    private Map<Integer, String> mealCheckpoints;

    public DailyRecord()
    {
        entries = new ArrayList<>();
        date = new SimpleCalendar();
        addedDates = new ArrayList<>();
        mealCheckpoints = new HashMap<>();
    }

    public DailyRecord(SimpleCalendar date)
    {
        this.date = date;
        // set hour and minute to 0 because only the day should be specified, the rest would
        // lead to unintended behaviour in date.compare
        this.date.setHour(0);
        this.date.setMinute(0);
        entries = new ArrayList<>();
        addedDates = new ArrayList<>();
        mealCheckpoints = new HashMap<>();
    }

    public Specification getEntry(int pos)
    {
        return entries.get(pos);
    }

    /**
     * Adds checkpoint, which could represent the end of a meal.
     *
     * @param index After which index the checkpoint should be
     * @param label Label of the meal
     */
    public void addMealCheckpoint(int index, String label)
    {
        if (mealCheckpoints == null)
            mealCheckpoints = new HashMap<>();
        mealCheckpoints.put(index, label);
    }

    /**
     * @param index Index after which the checkpoint would be
     * @return Label or null if no such checkpoint exists
     */
    public String getMealCheckpoint(int index)
    {
        if (mealCheckpoints == null)
            return null;
        else
            return mealCheckpoints.get(index);
    }

    public int getMealCheckpointNum()
    {
        if (mealCheckpoints == null)
            return 0;
        else
            return mealCheckpoints.size();
    }

    public SimpleCalendar getAddedDate(int pos)
    {
        return addedDates.get(pos);
    }

    public int getEntryCount()
    {
        return entries.size();
    }

    public List<Pair<Specification, SimpleCalendar>> asList()
    {
        List<Pair<Specification, SimpleCalendar>> list = new ArrayList<>();

        for (int i = 0; i < entries.size(); i++)
            list.add(new Pair<Specification, SimpleCalendar>(entries.get(i), addedDates.get(i)));

        return list;
    }

    public void add(Specification food, SimpleCalendar time)
    {
        entries.add(food);
        addedDates.add(time);
    }

    /**
     * Removes entry with id
     *
     * @return If an entry with this id existed
     */
    public boolean remove(String specificationId)
    {
        for (int i = 0; i < entries.size(); i++)
            if (entries.get(i).getId().equals(specificationId))
            {
                // need to decrease mappings after this
                for (int j = i; j < entries.size(); j++)
                {
                    if (mealCheckpoints.containsKey(j))
                    {
                        if (j - 1 >= 0 && !mealCheckpoints.containsKey(j - 1))
                            mealCheckpoints.put(j - 1, mealCheckpoints.get(j));
                        mealCheckpoints.remove(j);
                    }
                }
                entries.remove(i);
                addedDates.remove(i);

                return true;
            }
        return false;
    }

    public void clear()
    {
        entries = new ArrayList<>();
        addedDates = new ArrayList<>();
        mealCheckpoints = new HashMap<>();
    }

    public SimpleCalendar getDate()
    {
        return date;
    }

    /**
     * Calculates each macronutrient's share of total calories consumed.
     *
     * @return Number in the range [0, 100] of carbs, fat and protein
     */
    public float[] calculateMacroRatio()
    {
        float[] nutrientGrams = {0, 0, 0};
        Specification.NutrientType[] macroNutrients =
                {Specification.NutrientType.Carbohydrates,
                        Specification.NutrientType.Fat,
                        Specification.NutrientType.Protein};
        float calorieSum = 0;
        final NutrientQuantity targetQuantity = new NutrientQuantity(1, NutrientQuantity.Unit.g);
        for (Specification s : entries)
        {
            calorieSum += s.getCalories();
            for (int i = 0; i < macroNutrients.length; i++)
                nutrientGrams[i] += UnitConverter.convert(s.getNutrient(macroNutrients[i]), targetQuantity, macroNutrients[i]);
        }

        if (calorieSum == 0)
            return new float[]{0, 0, 0};

        return new float[]{
                100 * nutrientGrams[0] * 4 / calorieSum,
                100 * nutrientGrams[1] * 9 / calorieSum,
                100 * nutrientGrams[2] * 4 / calorieSum,
        };
    }

    /**
     * Remembers specific state of an instance of DailyRecord, so it can be restored using undo
     */
    public static class Memento
    {
        private List<Specification> entries;
        private List<SimpleCalendar> addedDates;
        private Map<Integer, String> mealCheckpoints;
        private SimpleCalendar date;

        public Memento(DailyRecord dailyRecord)
        {
            // create deep copy
            entries = new ArrayList<>();
            entries.addAll(dailyRecord.entries);
            addedDates = new ArrayList<>();
            addedDates.addAll(dailyRecord.addedDates);
            mealCheckpoints = new HashMap<>();
            if (dailyRecord.mealCheckpoints != null)
                mealCheckpoints.putAll(dailyRecord.mealCheckpoints);
            date = dailyRecord.date;
        }

        public void restore(DailyRecord dailyRecord)
        {
            dailyRecord.entries = entries;
            dailyRecord.addedDates = addedDates;
            dailyRecord.mealCheckpoints = mealCheckpoints;
        }

        public DailyRecord getDailyRecord()
        {
            DailyRecord record = new DailyRecord(date);
            record.entries = entries;
            record.addedDates = addedDates;
            record.mealCheckpoints = mealCheckpoints;
            return record;
        }
    }

}
