package com.vrublack.nutrition.core.fatsecret;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Class that is provided on the FatSecret API website with own modifications
 */
public class FatSecretAPIHelper
{
    private static final String URL_BASE = "http://platform.fatsecret.com/rest/server.api?";

    /* Private Data */
    private String mConsumerKey;
    private String mConsumerSecret;

    public FatSecretAPIHelper(String consumerKey, String consumerSecret)
    {
        mConsumerKey = consumerKey;
        mConsumerSecret = consumerSecret;
    }

    /* Getters and Setters */
    public String getConsumerKey()
    {
        return mConsumerKey;
    }

    public void setConsumerKey(String _consumerKey)
    {
        this.mConsumerKey = _consumerKey;
    }

    public String getConsumerSecret()
    {
        return mConsumerSecret;
    }

    public void setConsumerSecret(String _consumerSecret)
    {
        this.mConsumerSecret = _consumerSecret;
    }

    /* Public Methods */
    public FatSecretAuth ProfileCreate() throws FatSecretException
    {
        return ProfileCreate(null);
    }

    public FatSecretAuth ProfileCreate(String userID) throws FatSecretException
    {
        String urlBase = URL_BASE + "method=profile.create";

        if (userID != null)
            urlBase += "&user_id=" + userID;

        try
        {
            OAuthBase oAuth = new OAuthBase();

            URL url = new URL(urlBase);

            Result result = new Result();

            oAuth.generateSignature(url, mConsumerKey, mConsumerSecret, null, null, result, null, null);

            String response = doHttpMethodReq(result.getNormalizedUrl(), "POST", result.getNormalizedRequestParameters() + "&" + OAuthBase.OAUTH_SIGNATURE + "=" + URLEncoder.encode(result.getSignature(), "utf-8"), null);
            Document doc = loadXmlDocument(response);

            errorCheck(doc);

            return new FatSecretAuth(doc.getElementsByTagName("auth_token").item(0).getTextContent(), doc.getElementsByTagName("auth_secret").item(0).getTextContent());
        } catch (FatSecretException ex)
        {
            throw ex;
        } catch (Exception ex)
        {
            throw new FatSecretException(1, "An unknown error occurred: 'please try again later'");
        }
    }

    public FatSecretAuth ProfileGetAuth(String userID) throws FatSecretException
    {
        String urlBase = URL_BASE + "method=profile.get_auth&user_id=" + userID;

        try
        {
            OAuthBase oAuth = new OAuthBase();

            URL url = new URL(urlBase);

            Result result = new Result();

            oAuth.generateSignature(url, mConsumerKey, mConsumerSecret, null, null, result, null, null);

            String response = doHttpMethodReq(result.getNormalizedUrl(), "POST", result.getNormalizedRequestParameters() + "&" + OAuthBase.OAUTH_SIGNATURE + "=" + URLEncoder.encode(result.getSignature(), "utf-8"), null);
            Document doc = loadXmlDocument(response);

            errorCheck(doc);

            return new FatSecretAuth(doc.getElementsByTagName("auth_token").item(0).getTextContent(), doc.getElementsByTagName("auth_secret").item(0).getTextContent());
        } catch (FatSecretException ex)
        {
            throw ex;
        } catch (Exception ex)
        {
            throw new FatSecretException(1, "An unknown error occurred: 'please try again later'");
        }
    }

    public String ProfileRequestScriptSessionKey(String token, String secret, int expires, int consumeWithin, String permittedReferrerRegex, boolean cookie) throws FatSecretException
    {
        String urlBase = URL_BASE + "method=profile.request_script_session_key";

        if (expires > -1)
            urlBase += "&expires=" + expires;

        if (consumeWithin > -1)
            urlBase += "&consume_within=" + consumeWithin;

        if (permittedReferrerRegex != null)
            urlBase += "&permitted_referrer_regex=" + permittedReferrerRegex;

        if (cookie)
            urlBase += "&cookie=true";

        try
        {
            OAuthBase oAuth = new OAuthBase();

            URL url = new URL(urlBase);

            Result result = new Result();

            oAuth.generateSignature(url, mConsumerKey, mConsumerSecret, token, secret, result, null, null);

            String response = doHttpMethodReq(result.getNormalizedUrl(), "POST", result.getNormalizedRequestParameters() + "&" + OAuthBase.OAUTH_SIGNATURE + "=" + URLEncoder.encode(result.getSignature(), "utf-8"), null);
            Document doc = loadXmlDocument(response);

            errorCheck(doc);

            return doc.getElementsByTagName("session_key").item(0).getTextContent();
        } catch (FatSecretException ex)
        {
            throw ex;
        } catch (Exception ex)
        {
            throw new FatSecretException(1, "An unknown error occurred: 'please try again later'");
        }
    }

    public String search(String expression) throws FatSecretException
    {
        expression = expression.trim();

        try
        {
            OAuthBase oAuth = new OAuthBase();

            URL url = new URL(URL_BASE);

            Result result = new Result();


            List<String[]> furtherParams = new ArrayList<>();
            furtherParams.add(new String[]{"method", "foods.search"});
            furtherParams.add(new String[]{"search_expression", expression});
            furtherParams.add(new String[]{"page_number", "0"});
            furtherParams.add(new String[]{"max_results", "50"});

            oAuth.generateSignature(url, mConsumerKey, mConsumerSecret, null, null, result, "json", furtherParams);

            return doHttpMethodReq(result.getNormalizedUrl(), "POST", result.getNormalizedRequestParameters() + "&" + OAuthBase.OAUTH_SIGNATURE + "=" + URLEncoder.encode(result.getSignature(), "utf-8"), null);
        } catch (FatSecretException ex)
        {
            throw ex;
        } catch (Exception ex)
        {
            throw new FatSecretException(1, "An unknown error occurred: 'please try again later'");
        }
    }

    public String retrieve(String id) throws FatSecretException
    {
        try
        {
            OAuthBase oAuth = new OAuthBase();

            URL url = new URL(URL_BASE);

            Result result = new Result();


            List<String[]> furtherParams = new ArrayList<>();
            furtherParams.add(new String[]{"method", "food.get"});
            furtherParams.add(new String[]{"food_id", id});
            oAuth.generateSignature(url, mConsumerKey, mConsumerSecret, null, null, result, "json", furtherParams);

            return doHttpMethodReq(result.getNormalizedUrl(), "POST", result.getNormalizedRequestParameters() + "&" + OAuthBase.OAUTH_SIGNATURE + "=" + URLEncoder.encode(result.getSignature(), "utf-8"), null);
        } catch (FatSecretException ex)
        {
            throw ex;
        } catch (Exception ex)
        {
            throw new FatSecretException(1, "An unknown error occurred: 'please try again later'");
        }
    }

    public String ProfileRequestScriptSessionKey(String userID, int expires, int consumeWithin, String permittedReferrerRegex, boolean cookie) throws FatSecretException
    {
        String urlBase = URL_BASE + "method=profile.request_script_session_key&user_id=" + userID;

        if (expires > -1)
            urlBase += "&expires=" + expires;

        if (consumeWithin > -1)
            urlBase += "&consume_within=" + consumeWithin;

        if (permittedReferrerRegex != null)
            urlBase += "&permitted_referrer_regex=" + permittedReferrerRegex;

        if (cookie)
            urlBase += "&cookie=true";

        try
        {
            OAuthBase oAuth = new OAuthBase();

            URL url = new URL(urlBase);

            Result result = new Result();

            oAuth.generateSignature(url, mConsumerKey, mConsumerSecret, null, null, result, null, null);

            String response = doHttpMethodReq(result.getNormalizedUrl(), "POST", result.getNormalizedRequestParameters() + "&" + OAuthBase.OAUTH_SIGNATURE + "=" + URLEncoder.encode(result.getSignature(), "utf-8"), null);
            Document doc = loadXmlDocument(response);

            errorCheck(doc);

            return doc.getElementsByTagName("session_key").item(0).getTextContent();
        } catch (FatSecretException ex)
        {
            throw ex;
        } catch (Exception ex)
        {
            throw new FatSecretException(1, "An unknown error occurred: 'please try again later'");
        }
    }

    /* Private Methods */
    private String doHttpMethodReq(String urlStr, String requestMethod, String paramStr, Map<String, String> header) throws FatSecretException
    {
        StringBuffer sb = new StringBuffer();
        try
        {
            // Construct data
            // Send data
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            if (requestMethod != null)
                conn.setRequestMethod(requestMethod);

            if (header != null)
            {
                for (String key : header.keySet())
                {
                    conn.setRequestProperty(key, header.get(key));
                }
            }

            // If use POST, must use this
            OutputStreamWriter wr = null;
            if (requestMethod != null && !requestMethod.equals("GET") && !requestMethod.equals("DELETE"))
            {
                wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(paramStr);
                wr.flush();
            }

            // Get the response
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            do
            {
                String line = null;
                try
                {
                    line = br.readLine();
                } catch (IOException e)
                {
                    throw new FatSecretException(1, "An unknown error occurred: 'please try again later'");
                }

                if (line == null)
                    break;

                sb.append(line).append("\n");
            } while (true);

            if (wr != null)
                wr.close();

            if (br != null)
                br.close();

        } catch (Exception e)
        {
            throw new FatSecretException(1, "An unknown error occurred: 'please try again later'");
        }
        String response = sb.toString();
        return response;
    }

    private Document loadXmlDocument(String xml) throws FatSecretException
    {
        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            return docBuilder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception ex)
        {
            throw new FatSecretException(1, "An unknown error occurred: 'please try again later'");
        }
    }

    private void errorCheck(Document doc) throws FatSecretException
    {
        if (doc.getElementsByTagName("error").getLength() > 0)
        {
            throw new FatSecretException(Integer.parseInt(doc.getElementsByTagName("code").item(0).getTextContent()), doc.getElementsByTagName("message").item(0).getTextContent());
        }
    }

    /* OAuth */
    private static class OAuthBase
    {

        /* OAuth Parameters */
        public static final String OAUTH_VERSION_NUMBER = "1.0";
        public static final String OAUTH_PARAMETER_PREFIX = "oauth_";
        public static final String XOAUTH_PARAMETER_PREFIX = "xoauth_";
        public static final String OPEN_SOCIAL_PARAMETER_PREFIX = "opensocial_";

        public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
        public static final String OAUTH_CALLBACK = "oauth_callback";
        public static final String OAUTH_VERSION = "oauth_version";
        public static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
        public static final String OAUTH_SIGNATURE = "oauth_signature";
        public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
        public static final String OAUTH_NONCE = "oauth_nonce";
        public static final String OAUTH_TOKEN = "oauth_token";
        public static final String OAUTH_FORMAT = "format";
        public static final String OAUTH_METHOD = "method";
        public static final String OAUTH_FOOD_ID = "food_id";
        public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";

        protected String unreservedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.~";

        public void generateSignature(URL url, String consumerKey, String consumerSecret, String token, String tokenSecret, Result result, String format, List<String[]> furtherParams)
        {
            GenerateSignatureBase(url, consumerKey, token, "POST", generateTimeStamp(), generateNonce(), "HMAC-SHA1", result, format, furtherParams);

            String secret = consumerSecret + "&";
            if (tokenSecret != null)
            {
                secret += tokenSecret;
            }

            result.setSignature(getHMACSHA1(secret, result.getSignatureBase()));
        }

        private void GenerateSignatureBase(URL url, String consumerKey, String token, String httpMethod, String timeStamp,
                                           String nonce, String signatureType, Result result, String format, List<String[]> furtherParams)
        {
            Map<String, String> parameters = new HashMap<String, String>();

            parameters = getQueryParameters(url.getQuery(), parameters);

            parameters.put(OAUTH_VERSION, OAUTH_VERSION_NUMBER);
            parameters.put(OAUTH_NONCE, nonce);
            parameters.put(OAUTH_TIMESTAMP, timeStamp);
            parameters.put(OAUTH_SIGNATURE_METHOD, signatureType);
            parameters.put(OAUTH_CONSUMER_KEY, consumerKey);
            if (format != null)
                parameters.put(OAUTH_FORMAT, format);
            if (furtherParams != null)
                for (String[] pair : furtherParams)
                    parameters.put(pair[0], pair[1]);

            if (!IsNullOrEmpty(token))
            {
                parameters.put(OAUTH_TOKEN, token);
            }

            String normalizedUrl = url.getProtocol() + "://" + url.getHost();
            if (url.getPort() != -1 && !((url.getProtocol() == "http" && url.getPort() == 80) || (url.getProtocol() == "https" && url.getPort() == 443)))
            {
                normalizedUrl += ":" + url.getPort();
            }
            normalizedUrl += url.getPath();

            String normalizedRequestParameters = normalizeRequestParameters(parameters);

            result.setNormalizedUrl(normalizedUrl);
            result.setNormalizedRequestParameters(normalizedRequestParameters);
            result.setSignatureBase(httpMethod + "&" + encode(normalizedUrl) + "&" + encode(normalizedRequestParameters));
        }

        private boolean IsNullOrEmpty(String str)
        {
            return (str == null || str.length() == 0);
        }

        private Map<String, String> getQueryParameters(String parameters, Map<String, String> result)
        {
            if (parameters.startsWith("?"))
            {
                parameters = parameters.substring(1);
            }

            if (!IsNullOrEmpty(parameters))
            {
                String[] p = parameters.split("&");
                for (String s : p)
                {
                    if (!IsNullOrEmpty(s) && !s.startsWith(OAUTH_PARAMETER_PREFIX) && !s.startsWith(XOAUTH_PARAMETER_PREFIX) && !s.startsWith(OPEN_SOCIAL_PARAMETER_PREFIX))
                    {
                        if (s.indexOf('=') > -1)
                        {
                            String[] temp = s.split("=");
                            result.put(temp[0], temp[1]);
                        } else
                        {
                            result.put(s, "");
                        }
                    }
                }
            }

            return result;
        }

        public static String encode(String value)
        {
            if (value == null)
                return "";

            try
            {
                return URLEncoder.encode(value, "utf-8")
                        .replace("+", "%20")
                        .replace("!", "%21")
                        .replace("*", "%2A")
                        .replace("\\", "%27")
                        .replace("(", "%28")
                        .replace(")", "%29");
            } catch (UnsupportedEncodingException wow)
            {
                throw new RuntimeException(wow.getMessage(), wow);
            }
        }

        private String normalizeRequestParameters(Map<String, String> parameters)
        {
            List<String> parameterList = new ArrayList<String>();

            for (String key : parameters.keySet())
            {
                String parameter = key + "=" + encode(parameters.get(key));
                parameterList.add(parameter);
            }

            Collections.sort(parameterList);

            StringBuilder s = new StringBuilder();
            for (int i = 0; i < parameterList.size(); i++)
            {
                s.append(parameterList.get(i));
                if (i != parameterList.size() - 1)
                {
                    s.append("&");
                }
            }

            return s.toString();
        }

        private String getHMACSHA1(String key, String data)
        {
            try
            {
                SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF-8"), "HMAC-SHA1");

                Mac mac = Mac.getInstance("HmacSHA1");
                mac.init(signingKey);

                byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));

                return Base64Util.encodeBytes(rawHmac);
            } catch (Exception e)
            {
                throw new RuntimeException("Unable to generate HMAC-SHA1", e);
            }
        }

        private String generateTimeStamp()
        {
            // normally this should be System.currentTimeMillis() / 1000 but for some reason (maybe timezone-related) the server denies such requests
            long timestamp = System.currentTimeMillis() * 2;
            return Long.toString(timestamp);
        }

        private String generateNonce()
        {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    /* Result */
    private static class Result
    {
        private String _signature, _signatureBase, _normalizedUrl, _normalizedRequestParameters;

        public Result()
        {
        }

        public void setSignature(String _signature)
        {
            this._signature = _signature;
        }

        public String getSignature()
        {
            return _signature;
        }

        public void setSignatureBase(String _signatureBase)
        {
            this._signatureBase = _signatureBase;
        }

        public String getSignatureBase()
        {
            return _signatureBase;
        }

        public void setNormalizedUrl(String _normalizedUrl)
        {
            this._normalizedUrl = _normalizedUrl;
        }

        public String getNormalizedUrl()
        {
            return _normalizedUrl;
        }

        public void setNormalizedRequestParameters(String _normalizedRequestParameters)
        {
            this._normalizedRequestParameters = _normalizedRequestParameters;
        }

        public String getNormalizedRequestParameters()
        {
            return _normalizedRequestParameters;
        }
    }

    private static class Base64Util
    {
        public final static int NO_OPTIONS = 0;
        public final static int ENCODE = 1;
        public final static int DECODE = 0;
        public final static int GZIP = 2;
        public final static int DONT_BREAK_LINES = 8;

        private final static int MAX_LINE_LENGTH = 76;
        private final static byte EQUALS_SIGN = (byte) '=';
        private final static byte NEW_LINE = (byte) '\n';
        private final static String PREFERRED_ENCODING = "UTF-8";
        private final static byte[] ALPHABET;
        private final static byte[] _NATIVE_ALPHABET = {
                (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G',
                (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N',
                (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U',
                (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z',
                (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g',
                (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n',
                (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u',
                (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z',
                (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5',
                (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '+', (byte) '/'
        };

        static
        {
            byte[] __bytes;
            try
            {
                __bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(PREFERRED_ENCODING);
            } catch (UnsupportedEncodingException use)
            {
                __bytes = _NATIVE_ALPHABET;
            }
            ALPHABET = __bytes;
        }

        private final static byte[] DECODABET = {
                -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal  0 -  8
                -5, -5, // Whitespace: Tab and Linefeed
                -9, -9, // Decimal 11 - 12
                -5, // Whitespace: Carriage Return
                -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 - 26
                -9, -9, -9, -9, -9, // Decimal 27 - 31
                -5, // Whitespace: Space
                -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
                62, // Plus sign at decimal 43
                -9, -9, -9, // Decimal 44 - 46
                63, // Slash at decimal 47
                52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // Numbers zero through nine
                -9, -9, -9, // Decimal 58 - 60
                -1, // Equals sign at decimal 61
                -9, -9, -9, // Decimal 62 - 64
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, // Letters 'A' through 'N'
                14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // Letters 'O' through 'Z'
                -9, -9, -9, -9, -9, -9, // Decimal 91 - 96
                26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // Letters 'a' through 'm'
                39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, // Letters 'n' through 'z'
                -9, -9, -9, -9                                 // Decimal 123 - 126
        };

        private final static byte WHITE_SPACE_ENC = -5;

        private Base64Util()
        {
        }

        public static String encodeBytes(byte[] source)
        {
            return encodeBytes(source, 0, source.length, NO_OPTIONS);
        }

        public static String encodeBytes(byte[] source, int off, int len, int options)
        {
            int dontBreakLines = (options & DONT_BREAK_LINES);
            int gzip = (options & GZIP);

            if (gzip == GZIP)
            {
                ByteArrayOutputStream baos = null;
                java.util.zip.GZIPOutputStream gzos = null;
                OutputStream b64os = null;

                try
                {
                    baos = new ByteArrayOutputStream();
                    b64os = new OutputStream(baos, ENCODE | dontBreakLines);
                    gzos = new java.util.zip.GZIPOutputStream(b64os);

                    gzos.write(source, off, len);
                    gzos.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                    return null;
                } finally
                {
                    try
                    {
                        gzos.close();
                    } catch (Exception e)
                    {
                    }
                    try
                    {
                        b64os.close();
                    } catch (Exception e)
                    {
                    }
                    try
                    {
                        baos.close();
                    } catch (Exception e)
                    {
                    }
                }

                try
                {
                    return new String(baos.toByteArray(), PREFERRED_ENCODING);
                } catch (UnsupportedEncodingException uue)
                {
                    return new String(baos.toByteArray());
                }
            } else
            {
                boolean breakLines = dontBreakLines == 0;

                int len43 = len * 4 / 3;
                byte[] outBuff = new byte[(len43)
                        + ((len % 3) > 0 ? 4 : 0)
                        + (breakLines ? (len43 / MAX_LINE_LENGTH) : 0)];
                int d = 0;
                int e = 0;
                int len2 = len - 2;
                int lineLength = 0;
                for (; d < len2; d += 3, e += 4)
                {
                    encode3to4(source, d + off, 3, outBuff, e);

                    lineLength += 4;
                    if (breakLines && lineLength == MAX_LINE_LENGTH)
                    {
                        outBuff[e + 4] = NEW_LINE;
                        e++;
                        lineLength = 0;
                    }
                }

                if (d < len)
                {
                    encode3to4(source, d + off, len - d, outBuff, e);
                    e += 4;
                }

                try
                {
                    return new String(outBuff, 0, e, PREFERRED_ENCODING);
                } catch (UnsupportedEncodingException uue)
                {
                    return new String(outBuff, 0, e);
                }
            }
        }

        private static byte[] encode3to4(byte[] b4, byte[] threeBytes, int numSigBytes)
        {
            encode3to4(threeBytes, 0, numSigBytes, b4, 0);
            return b4;
        }

        private static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset)
        {
            int inBuff = (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0)
                    | (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0)
                    | (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);

            switch (numSigBytes)
            {
                case 3:
                    destination[destOffset] = ALPHABET[(inBuff >>> 18)];
                    destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
                    destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
                    destination[destOffset + 3] = ALPHABET[(inBuff) & 0x3f];
                    return destination;

                case 2:
                    destination[destOffset] = ALPHABET[(inBuff >>> 18)];
                    destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
                    destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
                    destination[destOffset + 3] = EQUALS_SIGN;
                    return destination;

                case 1:
                    destination[destOffset] = ALPHABET[(inBuff >>> 18)];
                    destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
                    destination[destOffset + 2] = EQUALS_SIGN;
                    destination[destOffset + 3] = EQUALS_SIGN;
                    return destination;

                default:
                    return destination;
            }
        }

        private static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset)
        {
            if (source[srcOffset + 2] == EQUALS_SIGN)
            {
                int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
                        | ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12);

                destination[destOffset] = (byte) (outBuff >>> 16);
                return 1;
            } else if (source[srcOffset + 3] == EQUALS_SIGN)
            {
                int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
                        | ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12)
                        | ((DECODABET[source[srcOffset + 2]] & 0xFF) << 6);

                destination[destOffset] = (byte) (outBuff >>> 16);
                destination[destOffset + 1] = (byte) (outBuff >>> 8);
                return 2;
            } else
            {
                try
                {
                    int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
                            | ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12)
                            | ((DECODABET[source[srcOffset + 2]] & 0xFF) << 6)
                            | ((DECODABET[source[srcOffset + 3]] & 0xFF));

                    destination[destOffset] = (byte) (outBuff >> 16);
                    destination[destOffset + 1] = (byte) (outBuff >> 8);
                    destination[destOffset + 2] = (byte) (outBuff);

                    return 3;
                } catch (Exception e)
                {
                    System.out.println("" + source[srcOffset] + ": " + (DECODABET[source[srcOffset]]));
                    System.out.println("" + source[srcOffset + 1] + ": " + (DECODABET[source[srcOffset + 1]]));
                    System.out.println("" + source[srcOffset + 2] + ": " + (DECODABET[source[srcOffset + 2]]));
                    System.out.println("" + source[srcOffset + 3] + ": " + (DECODABET[source[srcOffset + 3]]));
                    return -1;
                }
            }
        }

        public static class OutputStream extends FilterOutputStream
        {
            private boolean encode;
            private int position;
            private byte[] buffer;
            private int bufferLength;
            private int lineLength;
            private boolean breakLines;
            private byte[] b4;
            private boolean suspendEncoding;

            public OutputStream(java.io.OutputStream out)
            {
                this(out, ENCODE);
            }

            public OutputStream(java.io.OutputStream out, int options)
            {
                super(out);
                this.breakLines = (options & DONT_BREAK_LINES) != DONT_BREAK_LINES;
                this.encode = (options & ENCODE) == ENCODE;
                this.bufferLength = encode ? 3 : 4;
                this.buffer = new byte[bufferLength];
                this.position = 0;
                this.lineLength = 0;
                this.suspendEncoding = false;
                this.b4 = new byte[4];
            }

            public void write(int theByte) throws IOException
            {
                if (suspendEncoding)
                {
                    super.out.write(theByte);
                    return;
                }

                if (encode)
                {
                    buffer[position++] = (byte) theByte;
                    if (position >= bufferLength)  // Enough to encode.
                    {
                        out.write(encode3to4(b4, buffer, bufferLength));

                        lineLength += 4;
                        if (breakLines && lineLength >= MAX_LINE_LENGTH)
                        {
                            out.write(NEW_LINE);
                            lineLength = 0;
                        }

                        position = 0;
                    }
                } else
                {
                    if (DECODABET[theByte & 0x7f] > WHITE_SPACE_ENC)
                    {
                        buffer[position++] = (byte) theByte;
                        if (position >= bufferLength)
                        {
                            int len = Base64Util.decode4to3(buffer, 0, b4, 0);
                            out.write(b4, 0, len);
                            position = 0;
                        }
                    } else if (DECODABET[theByte & 0x7f] != WHITE_SPACE_ENC)
                    {
                        throw new IOException("Invalid character in Base64Util data.");
                    }
                }
            }

            public void write(byte[] theBytes, int off, int len) throws IOException
            {
                if (suspendEncoding)
                {
                    super.out.write(theBytes, off, len);
                    return;
                }

                for (int i = 0; i < len; i++)
                {
                    write(theBytes[off + i]);
                }
            }

            public void flushBase64() throws IOException
            {
                if (position > 0)
                {
                    if (encode)
                    {
                        out.write(encode3to4(b4, buffer, position));
                        position = 0;
                    } else
                    {
                        throw new IOException("Base64Util input not properly padded.");
                    }
                }
            }

            public void close() throws IOException
            {
                flushBase64();
                super.close();

                buffer = null;
                out = null;
            }

            public void suspendEncoding() throws IOException
            {
                flushBase64();
                this.suspendEncoding = true;
            }

            public void resumeEncoding()
            {
                this.suspendEncoding = false;
            }
        }
    }
}
