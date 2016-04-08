package com.vrublack.nutrition.core.fatsecret;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vrublack.nutrition.core.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FatsecretAPI implements SyncFoodDataSource
{
    // credentials which are loaded from a separate file since they must remain secret
    private String apiKey;
    private String apiSecret;
    private final static String credentialFile = "fatsecret_credentials.txt";

    public FatsecretAPI()
    {
        loadCredentials();
    }

    /**
     * <p>Load credentials from project-relative file. The credentials must remain secret to the person who registered for them,
     * and if they were included in a source file and the source is disclosed, people could see them. Instead, the project-relative file
     * fatsecret_credentials.txt is used, and not committed in git.</p>
     * <p/>
     * <p>If you want to use the Fatsecret API,
     * register at <a href="http://platform.fatsecret.com/api/">Fatsecret API</a> to obtain the api key and secret, create fatsecret_credentials.txt in the project root,
     * and paste them there, that is, the api key in the 1st line and the api secret in the 2nd line.</p>
     */
    private void loadCredentials()
    {
        final String explanation = "There should be a file name " + credentialFile + " in the project " +
                "folder containing only the api key in the 1st line and the api secret in the 2nd line. Register at " +
                "http://platform.fatsecret.com/api/ to obtain these strings.";
        try
        {
            InputStream in = new FileInputStream(new File(credentialFile));
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            int count = 0;
            while ((line = reader.readLine()) != null)
            {
                if (count == 0)
                    apiKey = line;
                else if (count == 1)
                    apiSecret = line;
                else
                    throw new IllegalArgumentException(explanation);

                count++;
            }

            if (count != 2)
                throw new IllegalArgumentException(explanation);

        } catch (IOException e)
        {
            throw new IllegalArgumentException(explanation);
        }
    }

    @Override
    public List<SearchResultItem> search(String searchStr)
    {
        try
        {
            FatSecretAPIHelper apiHelper = new FatSecretAPIHelper(apiKey, apiSecret);
            String response;
            response = apiHelper.search(searchStr);
            return parseSearchResults(response);
        } catch (Exception e)
        {
            return new ArrayList<>();
        }
    }

    private List<SearchResultItem> parseSearchResults(String s)
    {
        List<SearchResultItem> searchResultItems = new ArrayList<>();

        JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();
        JsonObject foods = jsonObject.get("foods").getAsJsonObject();
        String maxResults = foods.get("max_results").getAsString();
        int totalResults = Integer.parseInt(foods.get("total_results").getAsString());
        String pageNumber = foods.get("page_number").getAsString();

        if (totalResults == 0)
            return searchResultItems;

        JsonArray foodArray;
        JsonElement foodElement = foods.get("food");
        // if there is only one serving it's not an array
        if (foodElement.isJsonArray())
        {
            foodArray = foodElement.getAsJsonArray();
        } else
        {
            foodArray = new JsonArray();
            foodArray.add(foodElement);
        }

        for (int i = 0; i < foodArray.size(); i++)
        {
            JsonObject entry = foodArray.get(i).getAsJsonObject();

            String brandName = "";
            if (entry.has("brand_name"))
                brandName = entry.get("brand_name").getAsString();
            String description = entry.get("food_description").getAsString();
            String id = entry.get("food_id").getAsString();
            String name = entry.get("food_name").getAsString();
            String type = entry.get("food_type").getAsString();
            String url = entry.get("food_url").getAsString();

            String completeName = name;
            if (!brandName.isEmpty())
                completeName += " (" + brandName + ")";

            searchResultItems.add(new SearchResultItem(id, completeName, description, 0, 1));
        }

        return searchResultItems;
    }

    @Override
    public FatSecretFoodItem retrieve(String id)
    {
        try
        {
            FatSecretAPIHelper apiHelper = new FatSecretAPIHelper(apiKey, apiSecret);
            String response;
            response = apiHelper.retrieve(id);
            return parseFoodItem(response);
        } catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public FoodItem get(String id)
    {
        // implementation same as retrieve, since there is no search history in this case
        return retrieve(id);
    }

    private FatSecretFoodItem parseFoodItem(String s)
    {
        JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();

        JsonObject food = jsonObject.getAsJsonObject("food");
        String id = food.get("food_id").getAsString();
        String name = food.get("food_name").getAsString();
        String brandName = null;
        if (food.has("brand_name"))
            brandName = food.get("brand_name").getAsString();
        String type = food.get("food_type").getAsString();
        // String url = food.get("food_url").getAsString();

        List<FatSecretFoodItem.Serving> servings = new ArrayList<>();

        JsonArray servingArray;
        JsonElement servingElement = food.get("servings").getAsJsonObject().get("serving");
        // if there is only one serving it's not an array
        if (servingElement.isJsonArray())
        {
            servingArray = servingElement.getAsJsonArray();
        } else
        {
            servingArray = new JsonArray();
            servingArray.add(servingElement);
        }
        for (int i = 0; i < servingArray.size(); i++)
        {
            JsonObject serving = servingArray.get(i).getAsJsonObject();

            Map<Specification.NutrientType, NutrientQuantity> nutrients = new HashMap<>();

            // String servingID = serving.get("serving_id").getAsString();
            // String servingDescription = serving.get("serving_description").getAsString();
            // String servingUrl = serving.get("serving_url").getAsString();
            float metricServingAmount = Float.parseFloat(serving.get("metric_serving_amount").getAsString());
            String metricServingUnit = serving.get("metric_serving_unit").getAsString();
            float numberOfUnits = Float.parseFloat(serving.get("number_of_units").getAsString());
            String measurementDescription = serving.get("measurement_description").getAsString();
            float calories = Float.parseFloat(serving.get("calories").getAsString());
            float carbs = Float.parseFloat(serving.get("carbohydrate").getAsString());
            float protein = Float.parseFloat(serving.get("protein").getAsString());
            float fat = Float.parseFloat(serving.get("fat").getAsString());

            nutrients.put(Specification.NutrientType.Carbohydrates, new NutrientQuantity(carbs, NutrientQuantity.Unit.g));
            nutrients.put(Specification.NutrientType.Protein, new NutrientQuantity(protein, NutrientQuantity.Unit.g));
            nutrients.put(Specification.NutrientType.Fat, new NutrientQuantity(fat, NutrientQuantity.Unit.g));

            if (serving.has("saturated_fat"))
            {
                float saturatedFat = Float.parseFloat(serving.get("saturated_fat").getAsString());
                nutrients.put(Specification.NutrientType.FatSaturated, new NutrientQuantity(saturatedFat, NutrientQuantity.Unit.g));
            }
            if (serving.has("polyunsaturated_fat"))
            {
                float polyunsaturatedFat = Float.parseFloat(serving.get("polyunsaturated_fat").getAsString());
                nutrients.put(Specification.NutrientType.FatPolyunsaturated, new NutrientQuantity(polyunsaturatedFat, NutrientQuantity.Unit.g));
            }
            if (serving.has("monounsaturated_fat"))
            {
                float monounsaturatedFat = Float.parseFloat(serving.get("monounsaturated_fat").getAsString());
                nutrients.put(Specification.NutrientType.FatMonounsaturated, new NutrientQuantity(monounsaturatedFat, NutrientQuantity.Unit.g));
            }
            if (serving.has("trans_fat"))
            {
                float transFat = Float.parseFloat(serving.get("trans_fat").getAsString());
                nutrients.put(Specification.NutrientType.FatTrans, new NutrientQuantity(transFat, NutrientQuantity.Unit.g));
            }
            if (serving.has("cholesterol"))
            {
                float cholesterol = Float.parseFloat(serving.get("cholesterol").getAsString());
                nutrients.put(Specification.NutrientType.Cholesterol, new NutrientQuantity(cholesterol, NutrientQuantity.Unit.Mg));
            }
            if (serving.has("sodium"))
            {
                float sodium = Float.parseFloat(serving.get("sodium").getAsString());
                nutrients.put(Specification.NutrientType.Sodium, new NutrientQuantity(sodium, NutrientQuantity.Unit.Mg));
            }
            if (serving.has("potassium"))
            {
                float potassium = Float.parseFloat(serving.get("potassium").getAsString());
                nutrients.put(Specification.NutrientType.Potassium, new NutrientQuantity(potassium, NutrientQuantity.Unit.Mg));
            }
            if (serving.has("fiber"))
            {
                float fiber = Float.parseFloat(serving.get("fiber").getAsString());
                nutrients.put(Specification.NutrientType.Fiber, new NutrientQuantity(fiber, NutrientQuantity.Unit.g));
            }
            if (serving.has("sugar"))
            {
                float sugar = Float.parseFloat(serving.get("sugar").getAsString());
                nutrients.put(Specification.NutrientType.Sugar, new NutrientQuantity(sugar, NutrientQuantity.Unit.g));
            }
            if (serving.has("vitamin_a"))
            {
                float vitaminA = Float.parseFloat(serving.get("vitamin_a").getAsString());
                nutrients.put(Specification.NutrientType.VitaminA, new NutrientQuantity(vitaminA, NutrientQuantity.Unit.Percent));
            }
            if (serving.has("vitamin_c"))
            {
                float vitaminC = Float.parseFloat(serving.get("vitamin_c").getAsString());
                nutrients.put(Specification.NutrientType.VitaminC, new NutrientQuantity(vitaminC, NutrientQuantity.Unit.Percent));
            }
            if (serving.has("calcium"))
            {
                float calcium = Float.parseFloat(serving.get("calcium").getAsString());
                nutrients.put(Specification.NutrientType.Calcium, new NutrientQuantity(calcium, NutrientQuantity.Unit.Percent));
            }
            if (serving.has("iron"))
            {
                float iron = Float.parseFloat(serving.get("iron").getAsString());
                nutrients.put(Specification.NutrientType.Iron, new NutrientQuantity(iron, NutrientQuantity.Unit.Percent));
            }

            servings.add(new FatSecretFoodItem.Serving(numberOfUnits, measurementDescription, metricServingAmount, metricServingUnit, calories, nutrients));
        }

        return new FatSecretFoodItem(id, name, brandName, type, servings.toArray(new FatSecretFoodItem.Serving[servings.size()]));
    }
}
