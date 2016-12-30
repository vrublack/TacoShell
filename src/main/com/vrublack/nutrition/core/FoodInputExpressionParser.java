package com.vrublack.nutrition.core;

/**
 * Parses expression like "40g of sugar"
 */
public class FoodInputExpressionParser
{
    private Tokenizer tokenizer;

    /**
     * @param expression Expression, like "2 apples" or "4 medium bananas" or "150g of chicken breast"
     * @return Parsed expression
     * @throws ParseException If the expression is invalid
     */
    public FoodInputExpression parse(String expression) throws ParseException
    {
        // ignore case
        tokenizer = new Tokenizer(expression.toLowerCase());

        return parseExpression();
    }

    private FoodInputExpression parseExpression() throws ParseException
    {
        float quantity = parseQuantity();

        String unit = parseUnit();
        boolean implicitUnit = false;
        if (quantity == -1)
        {
            quantity = 1;
            implicitUnit = true;
        }

        String foodDescription = parseFoodDescription();

        return new FoodInputExpression(foodDescription, quantity, unit, implicitUnit);
    }

    private float parseQuantity() throws ParseException
    {
        String num = tokenizer.peek();
        if (num == null)
            throw new ParseException("Unexpected end of string!");
        // no quantity specified at the beginning?
        if (Tokenizer.getSegmentType(num) != Tokenizer.SegmentType.Number)
        {
            return -1;
        }

        try
        {
            tokenizer.next();

            String next = tokenizer.peek();
            if (next == null)
                throw new ParseException("Unexpected end of string!");
            if (next.equals(".") || next.equals(","))
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
        // don't call next() yet because the unit might be implicit
        String peek = tokenizer.peek();
        if (peek == null)
            throw new ParseException("Unexpected end of string!");
        // No unit specified? this could mean that the unit is implicitly "num"
        if (Tokenizer.getSegmentType(peek) != Tokenizer.SegmentType.Letter)
        {
            return "num";
        }

        String unit;

        boolean implicitUnit;
        // special case for "fl oz" and "fluid ounces" because they contain a whitespace
        if (peek.equals("fl") || peek.equals("fluid"))
        {
            tokenizer.next();
            String p = tokenizer.peek();
            if (p == null || (!p.equals("oz") && !p.equals("ounces")))
                throw new ParseException("Invalid unit!");
            unit = "fl oz";
            implicitUnit = false;
        } else
        {
            unit = UnitConverter.getUnitForUserInput(peek);
            if (unit == null)
            {
                implicitUnit = true;
                unit = "num";
            } else
            {
                implicitUnit = false;
            }
        }

        if (!implicitUnit)
            tokenizer.next();

        String peek2 = tokenizer.peek();
        if (!implicitUnit && peek2 != null && peek2.equals("of"))
        {
            // skip "of" as part of the quantity, e.gram. 5g of sugar
            tokenizer.next();
        }

        return unit;
    }

    private String parseFoodDescription() throws ParseException
    {
        String description = tokenizer.next();
        if (description == null)
            throw new ParseException("Unexpected end of string!");

        String s;
        // the rest of the input is the description
        while ((s = tokenizer.next()) != null)
            description += " " + s;

        return description;
    }
}
