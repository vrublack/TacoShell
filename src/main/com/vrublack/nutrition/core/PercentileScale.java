package com.vrublack.nutrition.core;

import java.util.Arrays;

public class PercentileScale
{
    private float[] referenceValues;

    /**
     * @param referenceValues Values that getPercentile should be based on
     */
    public PercentileScale(float[] referenceValues)
    {
        this.referenceValues = referenceValues;
        Arrays.sort(referenceValues);
    }

    /**
     * @return Value in the range [0, 1] indicating the percentile of the value based on the reference values
     */
    public float getPercentile(float value)
    {
        if (value == referenceValues[0])
            return 0;

        for (int i = 0; i < referenceValues.length; i++)
        {
            if (referenceValues[i] > value)
                return i * 1.0f / referenceValues.length;
        }

        return 1.0f;
    }
}
