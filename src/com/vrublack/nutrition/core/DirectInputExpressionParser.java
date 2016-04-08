package com.vrublack.nutrition.core;

/**
 * Parses expressions like "60g of Protein"
 */
public class DirectInputExpressionParser
{
    private enum AcceptedType
    {
        Kcal,
        Carbohydrates,
        Fat,
        Protein
    }

    private Tokenizer tokenizer;

    /**
     * @param expression Expression to parse, like "60g of Protein" or "60g of Protein, 50g Fat" or "120 Calories"
     * @return Parsed specification or <code>null</code> if the expression wasn't interpreted as a direct input expression
     * @throws ParseException If the expression was interpreted as a direct input expression but is invalid
     */
    public DirectSpecification parse(String expression) throws ParseException
    {
        // ignore case
        tokenizer = new Tokenizer(expression.toLowerCase());

        return parseExpression();
    }

    private DirectSpecification parseExpression() throws ParseException
    {
        DirectSpecification directSpecification = new DirectSpecification();

        while (true)
        {
            float quantifier = 0;
            ParseException exception = null;

            try
            {
                quantifier = parseQuantity();
            } catch (ParseException e)
            {
                // don't immediately throw the exception because we are not sure if the expression should be interpreted
                // as a direct input expression
                exception = e;
            }

            String unit = null;
            try
            {
                unit = parseUnit();
            } catch (ParseException e)
            {
                // don't immediately throw the exception because we are not sure if the expression should be interpreted
                // as a direct input expression
                exception = e;
            }

            // skip "of" as part of the quantifier, e.gram. 5g of protein
            String peek = tokenizer.peek();
            if (peek != null && peek.equals("of"))
            {
                tokenizer.next();
            }

            AcceptedType type = parseFoodDescription();
            if (type != null && exception != null)
            {
                // that means that the expression can be interpreted as a direct input expression (because the description is
                // calories, fat or something like that) but is invalid
                throw exception;
            } else if (type == null)
            {
                // that means that the expression cannot be interpreted as a direct input expression
                return null;
            } else
            {
                if (type == AcceptedType.Kcal)
                {
                    if (!unit.isEmpty())
                        throw new ParseException("A unit for calories isn't allowed!");
                    else if (directSpecification.isNutrientSpecified())
                        throw new ParseException("You can't specify calories and nutrients at the same time.");
                    else
                        directSpecification.setCalories(quantifier);
                } else
                {
                    if (directSpecification.isCaloriesSpecified())
                        throw new ParseException("You can't specify calories and nutrients at the same time.");
                    if (unit.isEmpty())
                        unit = "g";     // grams is implicit unit
                    float convertedQuantifier = UnitConverter.convert(quantifier, unit, 1, "g");

                    if (type == AcceptedType.Carbohydrates)
                    {
                        directSpecification.putNutrient(Specification.NutrientType.Carbohydrates, convertedQuantifier);
                    } else if (type == AcceptedType.Protein)
                    {
                        directSpecification.putNutrient(Specification.NutrientType.Protein, convertedQuantifier);
                    } else if (type == AcceptedType.Fat)
                    {
                        directSpecification.putNutrient(Specification.NutrientType.Fat, convertedQuantifier);
                    }
                }
            }

            peek = tokenizer.peek();
            if (peek == null)
                return directSpecification;
            else if (peek.equals(","))   // a comma is not mandatory
                tokenizer.next();
            else if (peek.equals("*"))
            {
                // * [factor] at the end to multiply all values by the factor
                tokenizer.next();
                float factor = parseQuantity();
                if (tokenizer.peek() != null)
                    throw new ParseException("Multiplier only allowed at the end!");
                // multiply all nutrients by this
                directSpecification.multiply(factor);
                return directSpecification;
            }
        }
    }

    private float parseQuantity() throws ParseException
    {
        String num = tokenizer.peek();
        if (num == null)
            throw new ParseException("Unexpected end of string!");
        // no quantity specified at the beginning?
        if (Tokenizer.getSegmentType(num) != Tokenizer.SegmentType.Number)
        {
            throw new ParseException("You need to specify the amount for direct input!");
        }

        try
        {
            tokenizer.next();

            String next = tokenizer.peek();
            if (next != null && (next.equals(".") || next.equals(",")))
            {
                tokenizer.next();
                next = tokenizer.next();
                if (next == null)
                    throw new ParseException("Unexpected end of string!");
                else
                    num += "." + next;
            }
            return Float.parseFloat(num);
        } catch (NumberFormatException e)
        {
            throw new ParseException("Wrong quantity!");
        }
    }

    private String parseUnit() throws ParseException
    {
        String next = tokenizer.peek();
        if (next == null)
            throw new ParseException("Unexpected end of string!");
        // return empty string if the user didn't specify a segment
        if (Tokenizer.getSegmentType(next) != Tokenizer.SegmentType.Letter || getNutrient(next) != null)
        {
            return "";
        }

        String unit = UnitConverter.getUnitForUserInput(next);
        if (UnitConverter.getUnitType(unit) != UnitConverter.UnitType.Mass)
        {
            tokenizer.next();
            throw new ParseException("Only mass units (g, kg, ...) are allowed for direct input!");
        }

        tokenizer.next();

        return unit;
    }

    private AcceptedType getNutrient(String nutrientStr)
    {
        switch (nutrientStr.toLowerCase())
        {
            case "kcal":
            case "kcals":
            case "calories":
            case "calorie":
            case "cal":
                return AcceptedType.Kcal;
            case "carbohydrates":
            case "carbohydrate":
            case "carbs":
            case "carb":
            case "c":
                return AcceptedType.Carbohydrates;
            case "protein":
            case "proteins":
            case "p":
                return AcceptedType.Protein;
            case "fat":
            case "fats":
            case "f":
                return AcceptedType.Fat;
            default:
                return null;
        }
    }

    private AcceptedType parseFoodDescription()
    {
        String description = tokenizer.next();
        if (description == null)
            return null;
        return getNutrient(description);
    }

}
