package com.vrublack.nutrition.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Sends/receives search feedback data from a remote server, so that the feedback data is shared across all users of this application
 */
public class SharedSearchHistory implements SearchHistory
{
    private static final String SERVER_BASE_URL = "https://nutrition-tracker.appspot.com/";

    @Override
    public String getNDBNumberForSearchResult(String searchString)
    {
        NameValuePair[] nameValuePairs = new NameValuePair[1];
        nameValuePairs[0] = new NameValuePair("search_str", searchString);
        String response = null;
        try
        {
            response = executeGet(SERVER_BASE_URL, "/get_most_common_result", nameValuePairs);
            if (response.equals("(no result)"))
                response = null;
        } catch (URISyntaxException | IOException e)
        {
            // do nothing
        }

        return response;
    }

    @Override
    public void putNDBNumberForSearchResult(String searchString, String selectedNDBNumber)
    {
        NameValuePair[] nameValuePairs = new NameValuePair[2];
        nameValuePairs[0] = new NameValuePair("search_str", searchString);
        nameValuePairs[1] = new NameValuePair("ndb_no", selectedNDBNumber);
        try
        {
            executeGet(SERVER_BASE_URL, "/result_feedback", nameValuePairs);
        } catch (URISyntaxException | IOException e)
        {
            // do nothing
        }
    }

    private String executeGet(String baseUrl, String path, NameValuePair[] parameters) throws URISyntaxException, IOException
    {
        String requestStr =  baseUrl + path + encodeQuery(parameters);
        URL url = new URL(requestStr);
        HttpURLConnection connection;
        BufferedReader bufferedReader;
        String line;
        StringBuilder result = new StringBuilder();
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(2000);
        bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while ((line = bufferedReader.readLine()) != null)
        {
            result.append(line);
        }
        bufferedReader.close();

        return result.toString();
    }

    private String encodeQuery(NameValuePair[] parameters)
    {
        if (parameters.length == 0)
            return "";

        String query = "?";
        for (int i = 0; i < parameters.length; i++)
        {
            try
            {
                query += URLEncoder.encode(parameters[i].name, "UTF-8") + "=" + URLEncoder.encode(parameters[i].value, "UTF-8");
            } catch (UnsupportedEncodingException ignored)
            {

            }
            if (i < parameters.length - 1)
                query += "&";
        }

        return query;
    }


    private static class NameValuePair
    {
        public String name;
        public String value;

        public NameValuePair(String name, String value)
        {
            this.name = name;
            this.value = value;
        }
    }

}
