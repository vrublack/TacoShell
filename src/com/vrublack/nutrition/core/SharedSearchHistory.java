package com.vrublack.nutrition.core;

import com.vrublack.nutrition.core.util.HTTPRequest;

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
        HTTPRequest.NameValuePair[] nameValuePairs = new HTTPRequest.NameValuePair[1];
        nameValuePairs[0] = new HTTPRequest.NameValuePair("search_str", searchString);
        String response = null;
        try
        {
            response = HTTPRequest.executeGet(SERVER_BASE_URL, "/get_most_common_result", nameValuePairs);
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
        HTTPRequest.NameValuePair[] nameValuePairs = new HTTPRequest.NameValuePair[2];
        nameValuePairs[0] = new HTTPRequest.NameValuePair("search_str", searchString);
        nameValuePairs[1] = new HTTPRequest.NameValuePair("ndb_no", selectedNDBNumber);
        try
        {
            HTTPRequest.executeGet(SERVER_BASE_URL, "/result_feedback", nameValuePairs);
        } catch (URISyntaxException | IOException e)
        {
            // do nothing
        }
    }


}
