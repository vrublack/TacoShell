package com.vrublack.nutrition.console;

import com.vrublack.nutrition.core.*;
import com.vrublack.nutrition.core.Formatter;
import com.vrublack.nutrition.core.Pair;

import java.text.SimpleDateFormat;
import java.util.*;

public class TextFormatter extends Formatter
{
    private final static List<Specification.NutrientType> defaultShownNutrients =
            Arrays.asList(Specification.NutrientType.Carbohydrates,
                    Specification.NutrientType.Fat, Specification.NutrientType.Protein,
                    Specification.NutrientType.Sugar,
                    Specification.NutrientType.Fiber,
                    Specification.NutrientType.VitaminC,
                    Specification.NutrientType.Cholesterol, Specification.NutrientType.VitaminD,
                    Specification.NutrientType.VitaminA,
                    Specification.NutrientType.VitaminB12,
                    Specification.NutrientType.Iron);
    private final static List<NutrientQuantity.Unit> defaultDesiredUnits = Arrays.asList(NutrientQuantity.Unit.g, NutrientQuantity.Unit.g, NutrientQuantity.Unit.g,
            NutrientQuantity.Unit.g, NutrientQuantity.Unit.Percent, NutrientQuantity.Unit.Percent, NutrientQuantity.Unit.Percent,
            NutrientQuantity.Unit.Percent, NutrientQuantity.Unit.Percent, NutrientQuantity.Unit.Percent, NutrientQuantity.Unit.Percent);

    private List<Specification.NutrientType> shownNutrients;
    private List<NutrientQuantity.Unit> desiredUnits;


    /**
     * @param shownNutrients Nutrients that should be shown or null if default nutrients should be shown
     * @param desiredUnits   Units that should be shown for the corresponding nutrient or null if default units should be shown
     */
    public TextFormatter(List<Specification.NutrientType> shownNutrients, List<NutrientQuantity.Unit> desiredUnits)
    {
        setShownNutrients(shownNutrients, desiredUnits);
    }

    /**
     * @param shownNutrients Nutrients that should be shown or null if default nutrients should be shown
     * @param desiredUnits   Units that should be shown for the corresponding nutrient or null if default units should be shown
     */
    public void setShownNutrients(List<Specification.NutrientType> shownNutrients, List<NutrientQuantity.Unit> desiredUnits)
    {
        if (shownNutrients != null)
            this.shownNutrients = shownNutrients;
        else
            this.shownNutrients = defaultShownNutrients;
        if (desiredUnits != null)
            this.desiredUnits = desiredUnits;
        else
            this.desiredUnits = defaultDesiredUnits;
    }

    /**
     * @return Default nutrient type in it's corresponding unit from getDefaultUnits()
     */
    public static List<Specification.NutrientType> getDefaultShownNutrients()
    {
        return defaultShownNutrients;
    }

    /**
     * @return Default unit for it's corresponding nutrient type from getDefaultShownNutrients()
     */
    public static List<NutrientQuantity.Unit> getDefaultUnits()
    {
        return defaultDesiredUnits;
    }

    @Override
    public String format(float number)
    {
        String str = round(number);
        if (str.equals("0"))
            return "-";
        return str;
    }

    @Override
    public String round(float number)
    {
        int firstDecimalPlace = (int) ((number * 10) % 10);
        int beforeComma = (int) number;
        String result = Integer.toString(beforeComma);
        // don't round if the 1st decimal place is zero or the number is bigger than 10
        if (firstDecimalPlace != 0 && number < 10)
            result += "." + Integer.toString(firstDecimalPlace);
        return result;
    }

    /**
     * @param popularity Number in the range [0, 100]
     * @return String depicting the popularity
     */
    @Override
    public String formatPopularity(float popularity)
    {
        if (popularity == 0)
            return "-";
        final int maxRank = 5;
        final int maxPopularity = 100;
        int rank = (int) (maxRank * popularity / (float) maxPopularity);
        String result = "";
        for (int i = 0; i < rank; i++)
            result += "*";
        return result;
    }

    @Override
    public String format(DirectSpecification specification)
    {
        List<String> comps = new ArrayList<>();

        for (Specification.NutrientType type : Specification.NutrientType.values())
        {
            NutrientQuantity quantity;
            if ((quantity = specification.getNutrient(type)) != null)
            {
                comps.add(format(quantity.getAmountInUnit()) + format(quantity.getUnit()) + " " + type);
            }
        }

        String result = "";
        for (int i = 0; i < comps.size(); i++)
        {
            result += comps.get(i);
            if (i < comps.size() - 1)
                result += ", ";
        }

        return result;
    }

    @Override
    public String format(FoodSpecification specification)
    {
        return specification.getFoodItem() + " (" + format(specification.getQuantity()) + ")";
    }

    @Override
    public String format(FoodQuantity foodQuantity)
    {
        if (foodQuantity == null)
            return "";
        return format(foodQuantity.getQuantifier()) + " " + foodQuantity.getDetailedUnit();
    }

    @Override
    public String format(DailyRecord dailyRecord)
    {
        // + 4 for header, 2 blank lines and total stats
        final int matrixHeight = dailyRecord.getEntryCount() + 5;
        final int matrixWidth = shownNutrients.size() + 4;

        List<String> emptyLineArray = new ArrayList<>();
        for (int i = 0; i < matrixWidth; i++)
            emptyLineArray.add("");
        final String[] emptyLine = emptyLineArray.toArray(new String[emptyLineArray.size()]);

        TextMatrix matrix = new TextMatrix(matrixWidth, matrixHeight);

        List<String> headerRow = new ArrayList<>(Arrays.asList("", "AMOUNT", "NAME", "CAL"));
        for (Specification.NutrientType type : shownNutrients)
        {
            headerRow.add(format(type).toUpperCase());
        }

        matrix.setRow(0, headerRow.toArray(new String[headerRow.size()]));
        matrix.setRow(1, emptyLine);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH");

        List<Specification> specifications = new ArrayList<>();
        for (Pair<Specification, SimpleCalendar> p : dailyRecord.asList())
            specifications.add(p.first);
        SpecificationList specificationList = new SpecificationList(specifications, shownNutrients, desiredUnits);
        List<Map<String, NutrientQuantity>> columns = specificationList.getNutrientColumns();

        for (int i = 0; i < dailyRecord.getEntryCount(); i++)
        {
            Specification entry = dailyRecord.getEntry(i);

            String time;
            if (entry.getId().startsWith("21309"))
                time = "ALL DAY, EVERY DAY, EVERY HOUR";
            else
                time = dateFormat.format(RecordManager.getCalendar(dailyRecord.getAddedDate(i)).getTime()) + "h";


            String description = entry.toString();
            String amount = format(entry.getAmount());

            final int wrapDescriptionAfter = 45;
            final int wrapAmountAfter = 20;

            // ellipsize instead of wrapping for now
            if (description.length() > wrapDescriptionAfter)
                description = description.substring(0, wrapDescriptionAfter + 1) + "...";
            if (amount.length() > wrapAmountAfter)
                amount = amount.substring(0, wrapAmountAfter + 1) + "...";

            List<String> newRow = new ArrayList<>();
            newRow.add(time);
            newRow.add(amount);
            newRow.add(description);
            newRow.add(format(entry.getCalories()));
            for (int i1 = 0; i1 < shownNutrients.size(); i1++)
            {
                Specification.NutrientType type = shownNutrients.get(i1);
                NutrientQuantity quantity = columns.get(i1).get(entry.getId());
                String formattedAmount = format(quantity.getAmountInUnit());
                if (!formattedAmount.equals("-") &&
                        quantity.getUnit() != specificationList.getDefaultUnits().get(i1))
                {
                    formattedAmount += " " + format(quantity.getUnit());
                }
                newRow.add(formattedAmount);
            }

            matrix.setRow(i + 2, newRow.toArray(new String[newRow.size()]));
        }

        matrix.setRow(dailyRecord.getEntryCount() + 2, emptyLine);

        // TOTAL row
        List<String> totalStatsRow = new ArrayList<>();
        totalStatsRow.add("TOTAL");
        totalStatsRow.add("");
        totalStatsRow.add("");
        totalStatsRow.add(format(specificationList.getTotalKcal()));
        List<Float> totals = specificationList.getTotals();
        for (int i = 0; i < shownNutrients.size(); i++)
        {
            Specification.NutrientType type = shownNutrients.get(i);
            totalStatsRow.add(format(totals.get(i)) + " " + format(desiredUnits.get(i)));
        }
        matrix.setRow(dailyRecord.getEntryCount() + 3, totalStatsRow.toArray(new String[totalStatsRow.size()]));

        // RATIO row
        List<String> ratioRow = new ArrayList<>();
        ratioRow.add("RATIO");
        ratioRow.add("");
        ratioRow.add("");
        ratioRow.add("100%");
        float totalKcal = specificationList.getTotalKcal();
        for (int i = 0; i < shownNutrients.size(); i++)
        {
            Specification.NutrientType type = shownNutrients.get(i);
            float kcal;
            switch (type)
            {
                case Fat:
                case FatMonounsaturated:
                case FatPolyunsaturated:
                case FatSaturated:
                case FatTrans:
                    kcal = totals.get(i) * 9;
                    break;
                case Carbohydrates:
                case Sugar:
                case Protein:
                    kcal = totals.get(i) * 4;
                    break;
                default:
                    kcal = -1;
            }

            String percentStr;
            if (kcal == -1)
                percentStr = "";
            else
            {
                float percent;
                if (totalKcal == 0)
                    percent = 0;
                else
                    percent = kcal / totalKcal * 100;
                percentStr = format(percent) + " %";
            }
            ratioRow.add(percentStr);
        }
        matrix.setRow(dailyRecord.getEntryCount() + 4, ratioRow.toArray(new String[ratioRow.size()]));

        String dateStr = format(RecordManager.getCalendar(dailyRecord.getDate()), false);
        return "Report for " + dateStr + ":\n\n" + matrix.formatToString();
    }

    public String format(Calendar calendar, boolean abbreviated)
    {
        if (abbreviated)
        {
            String month = new SimpleDateFormat("MMMM", Locale.US).format(calendar.getTime()).substring(0, 3);
            return new SimpleDateFormat("dd").format(calendar.getTime()) + " " + month;
        } else
            return new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime());
    }

    /**
     * @param record
     * @return Entries from record, numbered from 1 to n
     */
    public String formatNumbered(DailyRecord record)
    {
        TextMatrix entriesNumbered = new TextMatrix(3, record.getEntryCount() + 1);
        entriesNumbered.setRow(0, new String[]{"#", "AMOUNT", "NAME"});

        for (int i = 1; i < record.getEntryCount() + 1; i++)
        {
            entriesNumbered.setRow(i, new String[]{Integer.toString(i), format(record.getEntry(i - 1).getAmount()),
                    record.getEntry(i - 1).getDescription()});
        }

        return entriesNumbered.formatToString();
    }

    @Override
    public String format(DailyRecord[] lastDays)
    {
        // + 4 for header, 2 blank lines and total stats
        final int matrixHeight = lastDays.length + 4;
        final int matrixWidth = shownNutrients.size() + 2;

        List<String> emptyLineArray = new ArrayList<>();
        for (int i = 0; i < matrixWidth; i++)
            emptyLineArray.add("");
        final String[] emptyLine = emptyLineArray.toArray(new String[emptyLineArray.size()]);

        TextMatrix matrix = new TextMatrix(matrixWidth, matrixHeight);

        List<String> headerRow = new ArrayList<>(Arrays.asList("DAY", "CAL"));
        for (Specification.NutrientType type : shownNutrients)
        {
            headerRow.add(format(type).toUpperCase());
        }

        matrix.setRow(0, headerRow.toArray(new String[headerRow.size()]));
        matrix.setRow(1, emptyLine);

        List<Specification> daySpecifications = new ArrayList<>();
        for (DailyRecord dailyRecord : lastDays)
        {
            // compute the total amounts

            List<Specification> specifications = new ArrayList<>();
            for (Pair<Specification, SimpleCalendar> p : dailyRecord.asList())
                specifications.add(p.first);
            SpecificationList specificationList = new SpecificationList(specifications, shownNutrients, desiredUnits);

            DirectSpecification daySpecification = new DirectSpecification();
            List<Float> totals = specificationList.getTotals();
            List<NutrientQuantity.Unit> defaultUnits = specificationList.getDefaultUnits();

            for (int i = 0; i < totals.size(); i++)
            {
                Float total = totals.get(i);
                Specification.NutrientType nutrient = shownNutrients.get(i);
                daySpecification.putNutrient(nutrient, new NutrientQuantity(total, defaultUnits.get(i)));
            }
            daySpecification.setCalories(specificationList.getTotalKcal());
            String dateStr = format(RecordManager.getCalendar(dailyRecord.getDate()), true);
            daySpecification.setDescription(dateStr);
            daySpecifications.add(daySpecification);
        }
        SpecificationList specificationList = new SpecificationList(daySpecifications, shownNutrients, desiredUnits);
        List<Map<String, NutrientQuantity>> columns = specificationList.getNutrientColumns();

        for (int i = 0; i < daySpecifications.size(); i++)
        {
            // reversed because the older entries should be shown first like in a daily record
            Specification entry = daySpecifications.get(daySpecifications.size() - i - 1);

            List<String> newRow = new ArrayList<>();
            newRow.add(entry.toString());
            newRow.add(format(entry.getCalories()));
            for (int i1 = 0; i1 < shownNutrients.size(); i1++)
            {
                Specification.NutrientType type = shownNutrients.get(i1);
                NutrientQuantity quantity = columns.get(i1).get(entry.getId());
                String formattedAmount = format(quantity.getAmountInUnit());
                if (!formattedAmount.equals("-") &&
                        quantity.getUnit() != specificationList.getDefaultUnits().get(i1))
                {
                    formattedAmount += " " + format(quantity.getUnit());
                }
                newRow.add(formattedAmount);
            }

            matrix.setRow(i + 2, newRow.toArray(new String[newRow.size()]));
        }

        matrix.setRow(lastDays.length + 2, emptyLine);

        List<String> totalStatsRow = new ArrayList<>();
        totalStatsRow.add("AVERAGE");
        final int n = lastDays.length;
        totalStatsRow.add(format(specificationList.getTotalKcal() / n));
        List<Float> totals = specificationList.getTotals();
        for (int i = 0; i < shownNutrients.size(); i++)
        {
            Specification.NutrientType type = shownNutrients.get(i);
            totalStatsRow.add(format(totals.get(i) / n) + " " + format(desiredUnits.get(i)));
        }

        matrix.setRow(lastDays.length + 3, totalStatsRow.toArray(new String[totalStatsRow.size()]));

        return matrix.formatToString();
    }

    @Override
    public String format(Specification.NutrientType nutrientType)
    {
        switch (nutrientType)
        {
            case Carbohydrates:
                return "Carbs";
            case Cholesterol:
                return "Cholest";
            case FatMonounsaturated:
                return "Monounsat fat";
            case FatPolyunsaturated:
                return "Polyunsat fat";
            case FatSaturated:
                return "Sat fat";
            case FatTrans:
                return "Trans fat";
            case VitaminC:
                return "Vit C";
            case VitaminA:
                return "Vit A";
            case VitaminB12:
                return "Vit B12";
            case VitaminB6:
                return "Vit B6";
            case VitaminD:
                return "Vit D";
            case VitaminE:
                return "Vit E";
            default:
                return nutrientType.toString();
        }
    }

    @Override
    public String format(NutrientQuantity.Unit unit)
    {
        switch (unit)
        {
            case g:
                return "g";
            case Microg:
                return "\u00B5g";  // micro-symbol
            case Mg:
                return "mg";
            case IU:
                return "IU";
            case Percent:
                return "%";
            default:
                return "";
        }
    }
}
