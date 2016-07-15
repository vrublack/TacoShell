package com.vrublack.nutrition.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

public class HTTPRequest
{
    /**
     * Timeout in miliseconds
     */
    private static int TIMEOUT = 10000;

    public static String executeGet(String baseUrl, String path, NameValuePair[] parameters) throws URISyntaxException, IOException
    {
        String requestStr =  baseUrl + path + encodeQuery(parameters);
        URL url = new URL(requestStr);
        HttpURLConnection connection;
        BufferedReader bufferedReader;
        String line;
        StringBuilder result = new StringBuilder();
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while ((line = bufferedReader.readLine()) != null)
        {
            result.append(line);
        }
        bufferedReader.close();

        return result.toString();
    }

    private static String encodeQuery(NameValuePair[] parameters)
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


    public static class NameValuePair
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
