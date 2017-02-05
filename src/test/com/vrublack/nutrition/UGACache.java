package com.vrublack.nutrition;

import com.vrublack.nutrition.core.uga.UGAFoodItem;
import com.vrublack.nutrition.core.uga.UGAFoodServices;

import java.util.List;

public class UGACache
{
    public static void main(String[] args)
    {
        List<UGAFoodItem> items = UGAFoodServices.readFromDownloadedPages("/Users/valentin/UGAFoodData");
        UGAFoodServices.itemsToFile(items, "UGA.cache");
    }
}
