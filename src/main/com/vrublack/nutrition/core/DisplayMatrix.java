package com.vrublack.nutrition.core;

/**
 * Represents data with rows and columns
 */
public abstract class DisplayMatrix
{
    private String[][] matrix;

    private int width;
    private int height;

    private int i;

    public DisplayMatrix(int width, int height)
    {
        this.width = width;
        this.height = height;
        matrix = new String[height][];
        for (int i = 0; i < height; i++)
            matrix[i] = new String[width];
        i = 0;
    }

    public void setRow(int pos, String[] row)
    {
        i = pos;

        matrix[pos] = row;
    }

    public void setNextRow(String[] row)
    {
        if (i + 1 < matrix.length)
            matrix[++i] = row;
        else
            throw new ArrayIndexOutOfBoundsException();
    }

    public void setCell(int row, int col, String string)
    {
        matrix[row][col] = string;
    }

    public int getHeight()
    {
        return height;
    }

    public int getWidth()
    {
        return width;
    }

    public String getCell(int row, int col)
    {
        return matrix[row][col];
    }
}
