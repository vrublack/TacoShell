package com.vrublack.nutrition;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.vrublack.nutrition.core.uga.UGAFoodItem;
import com.vrublack.nutrition.core.uga.UGAFoodServices;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class UGACache
{
    public static void main(String[] args) throws FileNotFoundException
    {
        List<UGAFoodItem> items = UGAFoodServices.readFromDownloadedPages("/Users/valentin/UGAFoodData");
        UGAFoodServices.itemsToFile(items, "UGA.cache");

        Kryo kryo = new Kryo();
        Input input = new Input(new FileInputStream("UGA.cache"));
        ArrayList<UGAFoodItem> itemsLoaded = kryo.readObject(input, ArrayList.class);
        input.close();
    }
}
