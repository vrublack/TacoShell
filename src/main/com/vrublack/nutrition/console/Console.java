package com.vrublack.nutrition.console;


import com.Config;
import com.vrublack.nutrition.core.*;
import com.vrublack.nutrition.core.Formatter;
import com.vrublack.nutrition.core.fatsecret.FatsecretAPI;
import com.vrublack.nutrition.core.userdb.UserFoodDatabase;
import com.vrublack.nutrition.core.userdb.UserFoodItem;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Console
{
    private final static String INI_FILENAME = "preferences.ini";

    private RecordManager recordManager = new LocalRecordManager();
    private DailyRecord dailyRecord = recordManager.getRecordForToday();

    private SyncFoodDataSource dataSource;

    private LocalUserFoodDatabase userFoodDatabase = new LocalUserFoodDatabase();

    private Stack<DailyRecord.Memento> mementoStack = new Stack<>();

    private TextFormatter formatter;

    private SearchHistory history = new LocalSearchHistory();

    private boolean autoreport;


    public static void main(String[] args)
    {
        Console console = new Console();

        if (args.length == 1)
            console.switchDatasource(args[0]);
        else if (args.length != 0)
            System.out.println("Invalid number of arguments. You can specify the food source by \"usda\" or \"fatsecret\"");

        if (console.dataSource == null)
            console.dataSource = new LocalUSDAFoodDatabase();


        console.readIniFile();

        console.startInputLoop();
    }

    private void readIniFile()
    {
        List<Specification.NutrientType> nutrientTypes = null;
        List<NutrientQuantity.Unit> units = null;
        // load .ini preferences
        try
        {
            Ini ini = new Ini(new File(INI_FILENAME));
            Ini.Section display = ini.get("display");
            String[] showNutrients = display.get("show").split(",");
            String autoreport_str = display.get("autoreport");
            if (autoreport_str != null)
                autoreport = Boolean.valueOf(autoreport_str);
            else
                autoreport = false;
            nutrientTypes = new ArrayList<>();
            units = new ArrayList<>();
            for (String value : showNutrients)
            {
                try
                {
                    value = value.trim();
                    String nutrientStr = value.substring(0, value.indexOf(" ("));
                    String unitStr = value.substring(value.indexOf("(") + 1, value.indexOf(")"));
                    Specification.NutrientType nutrientType = Specification.getNutrientForUserInput(nutrientStr);
                    if (nutrientType == null)
                    {
                        System.err.println("Error: " + nutrientStr + " is an invalid nutrient");
                        continue;
                    }
                    nutrientTypes.add(nutrientType);
                    NutrientQuantity.Unit unit = NutrientQuantity.getUnitForUserInput(unitStr);
                    if (unit == null)
                    {
                        System.err.println("Error: " + unitStr + " is an invalid unit");
                        continue;
                    }
                    units.add(unit);
                } catch (Exception e)
                {
                    System.err.println("Invalid component: " + value);
                }
            }
        } catch (IOException e)
        {
            // apparently first launch, create ini file
            Ini newIni = new Ini();
            Profile.Section display = newIni.add("display");
            List<Specification.NutrientType> defaultNutrientTypes = TextFormatter.getDefaultShownNutrients();
            List<NutrientQuantity.Unit> defaultUnits = TextFormatter.getDefaultUnits();
            String show = "";
            for (int i = 0; i < defaultNutrientTypes.size(); i++)
            {
                String value = defaultNutrientTypes.get(i).name() + " (" + defaultUnits.get(i).name() + ")";
                show += value;
                if (i < defaultNutrientTypes.size() - 1)
                    show += ", ";
            }
            display.add("show", show);

            // comment showing all available nutrient types
            String availableNutrients = "Available nutrients: ";
            Specification.NutrientType[] values = Specification.NutrientType.values();
            for (int i = 0; i < values.length; i++)
            {
                Specification.NutrientType type = values[i];
                availableNutrients += type.name();
                if (i < values.length - 1)
                    availableNutrients += ", ";
            }
            // comment showing all available units
            String availableUnits = "Available units: ";
            NutrientQuantity.Unit[] unitValues = NutrientQuantity.Unit.values();
            for (int i = 0; i < unitValues.length; i++)
            {
                NutrientQuantity.Unit unit = unitValues[i];
                availableUnits += unit.name();
                if (i < unitValues.length - 1)
                    availableUnits += ", ";
            }

            display.putComment("show", availableNutrients + "\n#" + availableUnits);

            try
            {
                newIni.store(new File(INI_FILENAME));
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }

        formatter = new TextFormatter(nutrientTypes, units);
    }

    private void startInputLoop()
    {
        Scanner scanner = new Scanner(System.in);
        String input;

        report();

        while (scanner.hasNextLine() && !(input = scanner.nextLine()).equals("quit"))
        {
            input = input.trim();

            // ignore capitalization just for the command
            String command;
            String expr;
            int whitespaceIndex = input.indexOf(' ');
            if (whitespaceIndex == -1)
            {
                command = input;
                expr = "";
            } else
            {
                command = input.substring(0, whitespaceIndex);
                expr = input.substring(whitespaceIndex);
            }

            input = command.toLowerCase() + expr;

            try
            {
                if (input.equals("undo"))
                {
                    undo();
                    if (autoreport)
                        report();
                } else if (input.equals("report"))
                {
                    report();
                } else if (input.startsWith("last"))
                {
                    String expression = input.substring("last ".length());
                    last(Integer.parseInt(expression));
                } else if (input.startsWith("add "))
                {
                    String expression = input.substring("add ".length());
                    foodInput(expression, false, false);
                    if (autoreport)
                        report();
                } else if (input.startsWith("addm "))   // "add micronutrients"
                {
                    String expression = input.substring("add ".length());
                    foodInput(expression, false, true);
                    if (autoreport)
                        report();
                } else if (input.startsWith("a ")) // "quick add"
                {
                    String expression = input.substring("a ".length());
                    foodInput(expression, true, false);
                    if (autoreport)
                        report();
                } else if (input.startsWith("am ")) // "quick add micronutrients"
                {
                    String expression = input.substring("a ".length());
                    foodInput(expression, true, true);
                    if (autoreport)
                        report();
                } else if (input.equals("create"))
                {
                    create();
                } else if (input.startsWith("search "))
                {
                    String description = input.substring("search ".length());
                    search(description);
                } else if (input.startsWith("clear"))
                {
                    createMemento();
                    dailyRecord.clear();
                    recordManager.saveRecord(dailyRecord);
                    if (autoreport)
                        report();
                } else if (input.equals("delete") || input.equals("remove"))
                {
                    delete();
                } else if (input.startsWith("switch "))
                {
                    String expression = input.substring("switch ".length());
                    switchRecord(expression);
                    if (autoreport)
                        report();
                } else if (input.startsWith("source "))
                {
                    String expression = input.substring("source ".length());
                    switchDatasource(expression);
                } else if (input.startsWith("help"))
                {
                    // either "help" or "help [command]"
                    if (input.equals("help"))
                    {
                        help("");
                    } else
                    {
                        String expression = input.substring("help ".length());
                        help(expression);
                    }
                } else
                {
                    System.out.println("Unknown command or wrong format. Type \"help\" to display all available commands.\"");
                }

            } catch (NumberFormatException e)
            {
                System.out.println("A number was malformatted");
            } catch (Exception e)
            {
                System.out.println("Something went wrong :-(");
                e.printStackTrace();
            }
        }
    }

    private void delete()
    {
        if (dailyRecord.getEntryCount() == 0)
        {
            System.out.println("Error: current record is empty!");
            return;
        }

        // print entries with numbers
        System.out.println(formatter.formatNumbered(dailyRecord));
        System.out.println("# you want to delete: ");
        Scanner scanner = new Scanner(System.in);
        int number = -1;
        boolean bad = false;
        do
        {
            String str = scanner.nextLine();
            if (str.equals("esc"))
                return;
            else
            {
                try
                {
                    number = Integer.parseInt(str);
                    bad = !(number >= 1 && number <= dailyRecord.getEntryCount());
                } catch (NumberFormatException e)
                {
                    bad = true;
                }

                if (bad)
                    System.out.println("Invalid command. Type in a number from 1 to " + dailyRecord.getEntryCount() + " or \"esc\"");
            }
        } while (bad);

        createMemento();

        // remove!
        String f = formatter.format(dailyRecord.getEntry(number - 1));
        dailyRecord.remove(dailyRecord.getEntry(number - 1).getId());
        recordManager.saveRecord(dailyRecord);
        System.out.println("Removed " + f);

        if (autoreport)
            report();
    }

    private void last(int days)
    {
        if (days < 0)
        {
            System.out.println("Error: the amount of days must be nonnegative");
        }

        DailyRecord[] lastDays = recordManager.getLastDays(days);

        System.out.println(formatter.format(lastDays));
    }

    private void create()
    {
        Scanner scanner = new Scanner(System.in);
        String description = null;
        boolean correctInput = false;
        while (!correctInput)
        {
            System.out.println("Description?");
            description = scanner.nextLine();
            if (description.replaceAll(" ", "").isEmpty())
                System.out.println("Description can't be empty!");
            else if (description.contains("^"))
                System.out.println("^s are not allowed!");
            else
                correctInput = true;
        }


        Map<Specification.NutrientType, NutrientQuantity> nutrients = new HashMap<>();

        System.out.println("Please specify the following nutrients per 100g of the edible part of the food.");

        for (UserFoodDatabase.SavedNutrient savedNutrient : UserFoodDatabase.SAVED_NUTRIENTS)
        {
            correctInput = false;

            while (!correctInput)
            {
                System.out.println(savedNutrient.type + " in " + savedNutrient.defaultUnit + ": ");
                String amountStr = scanner.nextLine();
                try
                {
                    float amount = Float.parseFloat(amountStr);
                    correctInput = true;
                    nutrients.put(savedNutrient.type, new NutrientQuantity(amount, savedNutrient.defaultUnit));
                } catch (NumberFormatException e)
                {
                    System.out.println("Invalid number!");
                }
            }
        }

        String id = generateId();

        String kcalStr;
        float kcal = 0;
        correctInput = false;

        while (!correctInput)
        {
            System.out.println("Kcal (leave empty to calculate from entered nutrients): ");
            kcalStr = scanner.nextLine();
            if (kcalStr.isEmpty())
            {
                kcal = nutrients.get(Specification.NutrientType.Protein).getAmountInUnit() * 4
                        + nutrients.get(Specification.NutrientType.Fat).getAmountInUnit() * 9
                        + nutrients.get(Specification.NutrientType.Carbohydrates).getAmountInUnit() * 4;
                correctInput = true;
            } else
            {
                try
                {
                    kcal = Float.parseFloat(kcalStr);
                    correctInput = true;
                } catch (NumberFormatException e)
                {
                    System.out.println("Invalid number!");
                }
            }
        }

        UserFoodItem.DescriptionComp[] descriptionCompsArray = UserFoodDatabase.parseDescriptionComps(description);

        // Input of common measures
        System.out.println("You can now define how this entry should be converted (for example g -> ml or g -> piece)");

        List<UserFoodItem.CommonMeasure> commonMeasures = new ArrayList<>();
        boolean done = false;
        while (!done)
        {
            System.out.println("Enter unit or \"done\": ");
            String unit = scanner.nextLine();
            String canonicalUnit;
            if (unit.equals("done"))
            {
                done = true;
                continue;
            } else if (unit.replaceAll(" ", "").isEmpty())
            {
                System.out.println("Unit can't be empty!");
                continue;
            } else if (unit.contains("^"))
            {
                System.out.println("^s are not allowed!");
                continue;
            } else
            {
                canonicalUnit = UnitConverter.getUnitForUserInput(unit);
                if (canonicalUnit == null)  // can be the case for num types (piece, steak etc.)
                    canonicalUnit = unit;
            }

            correctInput = false;
            float amount = 0;
            while (!correctInput)
            {
                System.out.println("How much of this unit are in 1 g?");
                String amountStr = scanner.nextLine();
                try
                {
                    amount = Float.parseFloat(amountStr);
                    correctInput = true;
                } catch (NumberFormatException e)
                {
                    System.out.println("Invalid number!");
                }
            }

            commonMeasures.add(new UserFoodItem.CommonMeasure(canonicalUnit, amount, 1.0f));

            System.out.println("Got it.");
        }

        try
        {
            userFoodDatabase.createItem(new UserFoodItem(id, description, descriptionCompsArray, nutrients, kcal, 80,
                    commonMeasures.toArray(new UserFoodItem.CommonMeasure[commonMeasures.size()])));
            System.out.println("Item created.");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @return Seven-letter word from [a-z][A-Z].
     * Randomly generated. The chances of a collision are extremely small so the id can be treated as unique.
     */
    private static String generateId()
    {
        String newId = "";

        for (int i = 0; i < 7; i++)
        {
            int random = (int) (Math.random() * 52);

            if (random <= 25)
                newId += (char) ('a' + random);
            else
                newId += (char) ('A' + (random - 26));
        }

        return newId;
    }


    private void help(String s)
    {
        final String[][] commands = {
                {
                        "ADD", "Adds item to the record.", "[quantity] [item]\n\tquantity:\tHow much of the item should be added, e.g. \"2.5 tbsp\" or \"150g\" or \"large\". " +
                        "\n\t\t\tFor some items, an implicit quantity can be used if no quantity is specified, for example, \"ADD tomato\" would be interpreted as one tomato.\n\t\t\t" +
                        "The supported units depend on the data source and the specific item.\n" +
                        "\titem:\tEither a term that should be searched in the data source, like potatoes, or a specific nutrient (only cal, carbs, protein and fat are currently supported).\n\t\t\t" +
                        "When specifying the nutrients directly, multiple nutrients can be added in a single command, like \"ADD 10g carbs, 24g fat\". \n\t\t\tYou can also write * [factor] at the end" +
                        "of the line to multiply every specified nutrient by this number."
                },
                {
                        "ADDM", "Like ADD, but only adds micronutrients.", "(see ADD)"
                },
                {
                        "A", "Adds best matching item to the record without giving a choice.", "(see ADD)"
                },
                {
                        "AM", "Like A, but only adds micronutrients.", "(see ADD)"
                },
                {
                        "CREATE", "Add new item to the database locally.", "(no arguments)",
                },
                {
                        "SEARCH", "Displays matching items.", "[term]\n\tterm:\tA string that should be searched for in the data source."
                },
                {
                        "REPORT", "Shows added items for the current record.", "(no arguments)"
                },
                {
                        "SWITCH", "Switches current record.", "[date term]\n\tdate term:\tTerm specifying a date. Either \"today\", \"yesterday\", a weekday, \"prev\", \"next\"," +
                        "\n\t\t\t\tor a date in the following formats: \"dd.MM.yyyy\", \"dd.MM\", \"MM/dd/yyyy\", \"MM/dd\""
                },
                {
                        "CLEAR", "Deletes all entries in the current record.", "(no arguments)"
                },
                {
                        "UNDO", "Undoes last action.", "(no arguments)"
                },
                {
                        "REMOVE", "Lets you select an item to remove", "(no arguments)"
                },
                {
                        "SOURCE", "Switches the current data source.", "[source]\n\tsource:\t\"USDA\" (http://www.ars.usda.gov/Services/docs.htm?docid=24936) or \"FatSecret\" (https://www.fatsecret.com)"
                },
                {
                        "HELP", "Shows help.", "[command]\n\tcommand:\tCommand to show options for"
                }

        };

        if (s.isEmpty())
        {
            System.out.println("Here's a list of all available commands. Enter HELP [Command] to display options for a specific command.");

            TextMatrix matrix = new TextMatrix(2, commands.length);

            for (int i = 0; i < commands.length; i++)
            {
                matrix.setRow(i, new String[]{commands[i][0], commands[i][1]});
            }

            System.out.println(matrix.formatToString());
        } else
        {
            int index = -1;
            for (int i = 0; i < commands.length; i++)
            {
                if (commands[i][0].equalsIgnoreCase(s))
                {
                    index = i;
                    break;
                }
            }
            if (index == -1)
            {
                System.out.println("Unknown command!");
                return;
            }
            System.out.println("Syntax: " + commands[index][2]);
        }
    }

    private void switchDatasource(String expression)
    {
        expression = expression.toLowerCase();

        switch (expression)
        {
            case "fatsecret":
                dataSource = new FatsecretAPI();
                System.out.println("Data source switched to FatSecret API");
                break;
            case "usda":
                dataSource = new LocalUSDAFoodDatabase();
                System.out.println("Data source switched to USDA Database");
                break;
            default:
                System.out.println("Unknown datasource. Available datasources are fatsecret and usda.");
                break;
        }
    }

    private void switchRecord(String expression)
    {
        expression = expression.toLowerCase();

        DailyRecord requestedDailyRecord = null;

        switch (expression)
        {
            case "today":
                requestedDailyRecord = recordManager.getRecordForToday();
                break;
            case "yesterday":
                Calendar yesterday = new GregorianCalendar();
                yesterday.add(Calendar.DAY_OF_YEAR, -1);
                requestedDailyRecord = recordManager.getRecordForDay(yesterday);
                if (requestedDailyRecord == null)
                {
                    System.out.println("No record with the specified date is available!");
                    return;
                }
                break;
            case "monday":
            case "mo":
                requestedDailyRecord = recordManager.getRecordForDay(getLastDateWithWeekday(Calendar.MONDAY));
                if (requestedDailyRecord == null)
                {
                    System.out.println("No record with the specified date is available!");
                    return;
                }
                break;
            case "tuesday":
            case "tue":
                requestedDailyRecord = recordManager.getRecordForDay(getLastDateWithWeekday(Calendar.TUESDAY));
                if (requestedDailyRecord == null)
                {
                    System.out.println("No record with the specified date is available!");
                    return;
                }
                break;
            case "wednesday":
            case "wed":
                requestedDailyRecord = recordManager.getRecordForDay(getLastDateWithWeekday(Calendar.WEDNESDAY));
                if (requestedDailyRecord == null)
                {
                    System.out.println("No record with the specified date is available!");
                    return;
                }
                break;
            case "thursday":
            case "thu":
                requestedDailyRecord = recordManager.getRecordForDay(getLastDateWithWeekday(Calendar.THURSDAY));
                if (requestedDailyRecord == null)
                {
                    System.out.println("No record with the specified date is available!");
                    return;
                }
                break;
            case "friday":
            case "fr":
                requestedDailyRecord = recordManager.getRecordForDay(getLastDateWithWeekday(Calendar.FRIDAY));
                if (requestedDailyRecord == null)
                {
                    System.out.println("No record with the specified date is available!");
                    return;
                }
                break;
            case "saturday":
            case "sa":
                requestedDailyRecord = recordManager.getRecordForDay(getLastDateWithWeekday(Calendar.SATURDAY));
                if (requestedDailyRecord == null)
                {
                    System.out.println("No record with the specified date is available!");
                    return;
                }
                break;
            case "sunday":
            case "su":
                requestedDailyRecord = recordManager.getRecordForDay(getLastDateWithWeekday(Calendar.SUNDAY));
                if (requestedDailyRecord == null)
                {
                    System.out.println("No record with the specified date is available!");
                    return;
                }
                break;
            case "previous":
            case "prev":
                requestedDailyRecord = recordManager.getPrevious(dailyRecord.getDate());
                if (requestedDailyRecord == null)
                {
                    System.out.println("The current record is the earliest record available!");
                    return;
                }
                break;
            case "next":
                requestedDailyRecord = recordManager.getNext(dailyRecord.getDate());
                if (requestedDailyRecord == null)
                {
                    System.out.println("The current record is the latest record available!");
                    return;
                }
                break;
        }

        if (requestedDailyRecord == null)
        {
            // try to match date formats
            String[] formats = {"dd.MM.yyyy", "dd.MM", "MM/dd/yyyy", "MM/dd"};
            for (int i = 0; i < formats.length; i++)
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat(formats[i]);
                try
                {
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(dateFormat.parse(expression));
                    if (i == 1 || i == 3)
                    {
                        // if the formats don't contain the year, the current year has to be set
                        calendar.set(Calendar.YEAR, new GregorianCalendar().get(Calendar.YEAR));
                    }

                    requestedDailyRecord = recordManager.getRecordForDay(calendar);
                    if (requestedDailyRecord == null)
                    {
                        System.out.println("No record with the specified date is available!");
                        return;
                    }
                    break;
                } catch (java.text.ParseException ignored)
                {
                }
            }

            if (requestedDailyRecord == null)
            {
                System.out.println("No valid date option. Valid options are \"today\", \"yesterday\", a weekday, \"previous\", \"next\", " +
                        "or a date in the following formats: \"dd.MM.yyyy\", \"dd.MM\", \"MM/dd/yyyy\", \"MM/dd\"");
                return;
            } else
            {
                dailyRecord = requestedDailyRecord;
                mementoStack.clear();
            }
        } else
        {
            dailyRecord = requestedDailyRecord;
            mementoStack.clear();
        }

        SimpleDateFormat european = new SimpleDateFormat("dd.MM.yyyy");
        System.out.println("Switched to record from: " + european.format(RecordManager.getCalendar(dailyRecord.getDate()).getTime()));
    }

    private Calendar getLastDateWithWeekday(int weekday)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new GregorianCalendar().getTime());

        int dayOfWeek;
        do
        {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        } while (dayOfWeek != Calendar.WEDNESDAY);
        return cal;
    }

    private void createMemento()
    {
        mementoStack.push(new DailyRecord.Memento(dailyRecord));
    }

    /**
     * Display a list of entries from the dataSource that match the description
     */

    private void search(String description)
    {
        List<SearchResultItem> results = new CompositeFoodSource(dataSource, userFoodDatabase).search(description, history);
        printSearchResults(results);
    }

    private void printSearchResults(List<SearchResultItem> results)
    {
        // max amount of entries that will be shown to the user (upon request)
        final int entryLimit = Math.min(50, results.size());
        int currentPos = 0;

        float[] percentileReference = new float[Math.min(10, results.size())];
        for (int i = 0; i < percentileReference.length; i++)
            percentileReference[i] = results.get(i).getRelativePopularity();
        PercentileScale percentileScale = new PercentileScale(percentileReference);

        // make matrix with all the search results up to entryLimit
        final int matrixHeight = entryLimit + 1;
        final int matrixWidth = 3;
        TextMatrix matrix = new TextMatrix(matrixWidth, matrixHeight);
        matrix.setRow(0, new String[]{"", "DESCRIPTION", "POPULARITY"});
        for (int i = 0; i < entryLimit; i++)
        {
            float scaledPopularity = 100 * percentileScale.getPercentile(results.get(i).getRelativePopularity());
            matrix.setRow(i + 1, new String[]{"[" + (i + 1) + "]", results.get(i).toString(), formatter.formatPopularity(scaledPopularity)});
        }
        String[] formattedLines = matrix.formatToLines();

        System.out.println(formattedLines[0]);

        final boolean askForMore = false;

        final String prompt = "Type in \"more\" or \"esc\": ";
        if (askForMore)
            System.out.println(prompt);

        while (true)
        {
            int i = currentPos;
            if (currentPos < entryLimit)
            {
                for (; i < currentPos + 10 && i < entryLimit; i++)
                {
                    System.out.println(formattedLines[i + 1]);
                }
                currentPos = i;
            }

            if (!askForMore)
                return;

            Scanner scanner = new Scanner(System.in);
            int number;
            do
            {
                String str = scanner.nextLine();
                if (str.equals("esc"))
                    return;
                else if (str.equals("more"))
                    break;
                else
                    System.out.println("Invalid command. Type in \"more\" or \"esc\"");
            } while (true);
        }
    }

    private void foodInput(String input, boolean quickAdd, boolean microNutrientsOnly)
    {
        try
        {
            // first try to parse direct input
            DirectSpecification directSpecification;
            try
            {
                directSpecification = new DirectInputExpressionParser().parse(input);
            } catch (ParseException e)
            {
                directSpecification = null;
            }

            if (directSpecification != null)
            {
                directFoodInput(directSpecification);
            } else
            {
                // if it is no direct input, parse input string as foodItem input
                FoodInputExpression foodInputExpression = new FoodInputExpressionParser().parse(input);

                // search for foodItem in the database
                List<SearchResultItem> results = new CompositeFoodSource(dataSource, userFoodDatabase).search(foodInputExpression.getDescription(), history);
                if (results.isEmpty())
                {
                    System.out.println("No matches were found in the database. Consider adding the nutrients directly, like \"add 75g protein, 30g carbs\"");
                } else if (quickAdd)
                {
                    FoodItem item = new CompositeFoodSource(dataSource, userFoodDatabase).retrieve(results.get(0).getId(), history);
                    quickAddFood(foodInputExpression, item, microNutrientsOnly);
                } else
                {
                    pickFood(foodInputExpression, results, microNutrientsOnly);
                }
            }

        } catch (ParseException e)
        {
            System.out.println(e);
        }
    }


    private void directFoodInput(DirectSpecification expression)
    {
        createMemento();
        SimpleCalendar now = RecordManager.getSimpleCalendar(new GregorianCalendar());
        dailyRecord.add(expression, now);
        // save immediately
        recordManager.saveRecord(dailyRecord);
        System.out.println("\"" + formatter.format(expression) + "\" added");
    }

    private void quickAddFood(FoodInputExpression expression, FoodItem foodItem, boolean microNutrientsOnly)
    {
        try
        {
            createMemento();
            FoodSpecification specification = new FoodSpecification(foodItem, expression.getQuantity(), expression.getUnit(), microNutrientsOnly);
            SimpleCalendar now = RecordManager.getSimpleCalendar(new GregorianCalendar());
            dailyRecord.add(specification, now);
            // save immediately
            recordManager.saveRecord(dailyRecord);
            System.out.println("\"" + formatter.format(specification) + "\" added");
        } catch (IllegalArgumentException e)
        {
            System.out.println((expression.isImplicitUnit() ? "Implicit" : "Specified") + " unit not supported for this foodItem. You can use \""
                    + foodItem.getAcceptedUnits()[0] + "\", for example.");
        }
    }

    private void pickFood(FoodInputExpression expression, List<SearchResultItem> results, boolean microNutrientsOnly)
    {
        // max amount of entries that will be shown to the user (upon request)
        final int entryLimit = Math.min(50, results.size());
        int currentPos = 0;

        final String prompt = "Type in a number, \"esc\" or \"more\": ";
        System.out.println(prompt);

        float[] percentileReference = new float[Math.min(10, results.size())];
        for (int i = 0; i < percentileReference.length; i++)
            percentileReference[i] = results.get(i).getRelativePopularity();
        PercentileScale percentileScale = new PercentileScale(percentileReference);

        // make matrix with all the search results up to entryLimit
        final int matrixHeight = entryLimit + 1;
        final int matrixWidth = 3;
        TextMatrix matrix = new TextMatrix(matrixWidth, matrixHeight);
        matrix.setRow(0, new String[]{"", "DESCRIPTION", "POPULARITY"});
        for (int i = 0; i < entryLimit; i++)
        {
            float scaledPopularity = 100 * percentileScale.getPercentile(results.get(i).getRelativePopularity());
            matrix.setRow(i + 1, new String[]{"[" + (i + 1) + "]", results.get(i).toString(), formatter.formatPopularity(scaledPopularity)});
        }
        String[] formattedLines = matrix.formatToLines();

        System.out.println(formattedLines[0]);

        while (true)
        {
            int i = currentPos;
            if (currentPos < entryLimit)
            {
                for (; i < currentPos + 10 && i < entryLimit; i++)
                {
                    System.out.println(formattedLines[i + 1]);
                }
                currentPos = i;
            }
            Scanner scanner = new Scanner(System.in);
            int number;
            do
            {
                String str = scanner.nextLine();
                if (str.equals("esc"))
                    return;
                else if (str.equals("more"))
                    break;
                try
                {
                    number = Integer.parseInt(str);
                } catch (Exception e)
                {
                    System.out.println("Invalid input; " + prompt);
                    continue;
                }

                if (number < 1 || number > i)
                {
                    System.out.println("Invalid input; " + prompt);
                } else
                {
                    FoodItem foodItem = new CompositeFoodSource(dataSource, userFoodDatabase).retrieve(results.get(number - 1).getId(), history);
                    try
                    {
                        createMemento();
                        Specification specification = new FoodSpecification(foodItem, expression.getQuantity(), expression.getUnit(), microNutrientsOnly);
                        SimpleCalendar now = RecordManager.getSimpleCalendar(new GregorianCalendar());
                        dailyRecord.add(specification, now);
                        // save immediately
                        recordManager.saveRecord(dailyRecord);
                        System.out.println("\"" + formatter.format(specification) + "\" added");
                    } catch (IllegalArgumentException e)
                    {
                        System.out.println((expression.isImplicitUnit() ? "Implicit" : "Specified") + " unit not supported for this foodItem. You can use \""
                                + foodItem.getAcceptedUnits()[0] + "\", for example.");
                    }

                    return;
                }
            } while (true);
        }
    }

    private void report()
    {
        System.out.println(formatter.format(dailyRecord));
    }

    private void undo()
    {
        if (mementoStack.isEmpty())
        {
            System.out.println("There is no action that could be undone!");
            return;
        }

        DailyRecord.Memento lastMemento = mementoStack.pop();
        lastMemento.restore(dailyRecord);
        recordManager.saveRecord(dailyRecord);
    }

    private static int countMatches(String str, char c)
    {
        int count = 0;
        for (int i = 0; i < str.length(); i++)
        {
            if (str.charAt(i) == c)
                count++;
        }
        return count;
    }
}
