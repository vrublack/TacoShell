package com.vrublack.nutrition.core;

/**
 * Formats matrix for output in a monospace font
 */
public class TextMatrix extends DisplayMatrix
{
    public TextMatrix(int width, int height)
    {
        super(width, height);
    }

    /**
     * @return Formatted matrix rows with the columns aligned
     */
    public String[] formatToLines()
    {
        if (getHeight() == 0)
            return new String[]{""};

        // determine width for every column
        final int numCols = getWidth();
        final int numRows = getHeight();
        int[] colWidth = new int[numCols];
        for (int col = 0; col < numCols; col++)
        {
            for (int row = 0; row < numRows; row++)
            {
                colWidth[col] = Math.max(colWidth[col], getCell(row, col).length());
            }
        }

        // format the matrix
        final int padding = 4;  // padding between columns
        String[] result = new String[numRows];
        for (int row = 0; row < numRows; row++)
        {
            String rowStr = "";
            for (int col = 0; col < numCols; col++)
            {
                String entry = getCell(row, col);
                rowStr += entry;
                int remainingSpace = colWidth[col] + padding - entry.length();
                for (int k = 0; k < remainingSpace; k++)
                    rowStr += " ";
            }
            result[row] = rowStr;
        }

        return result;
    }

    /**
     * @return String of the matrix with the columns aligned
     */
    public String formatToString()
    {
        String[] lines = formatToLines();
        String result = "";
        for (String s : lines)
            result += s + "\n";
        return result;
    }
}
