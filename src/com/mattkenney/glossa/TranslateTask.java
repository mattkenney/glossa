package com.mattkenney.glossa;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

class TranslateTask extends AsyncTask<String, Integer, Collection<String>>
{
    private static final String SCOPE = "http://api.microsofttranslator.com";
    private static final String GRANT_TYPE = "client_credentials";
    private static final String AUTH_URL = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";
    private static final String TRANSLATE_URL = "http://api.microsofttranslator.com/V2/Http.svc/GetTranslations";
    private static final int AUTH_LIFE = 600;
    private static String authorization = null;
    private static long authorizationExpires = 0L;
    private final String client_id;
    private final String client_secret;

    private final ArrayAdapter<String> adapter;

    TranslateTask(ArrayAdapter<String> adapter)
    {
        this.adapter = adapter;
        client_id = adapter.getContext().getString(R.string.client_id);
        client_secret = adapter.getContext().getString(R.string.client_secret);
    }

    private String getAuthorization()
        throws IOException, JSONException
    {
        synchronized (getClass())
        {
            long now = System.currentTimeMillis();
            Log.v("Glossa", "authorizationExpires: " + authorizationExpires + " (" + now + ")");
            if (authorization == null || authorizationExpires < now)
            {
                HttpClient client = new HttpClient();
                client.addParameter("client_id", client_id);
                client.addParameter("client_secret", client_secret);
                client.addParameter("scope", SCOPE);
                client.addParameter("grant_type", GRANT_TYPE);
                HttpURLConnection connection = HttpClient.createConnection(AUTH_URL);
                connection.setRequestMethod("POST");
                String text = client.getResponseText(connection);
                JSONObject json = new JSONObject(text);
                authorization = "Bearer " + json.optString("access_token");
                int expires = json.optInt("expires_in", AUTH_LIFE);
                authorizationExpires = now + (expires - 60) * 1000L;
                Log.v("Glossa", "new Authorization: " + authorization);
            }
            else
            {
                Log.v("Glossa", "pre Authorization: " + authorization);
            }
            return authorization;
        }
    }

    private String getTranslationXml(String text, String from, String to)
        throws IOException, JSONException
    {
        HttpClient client = new HttpClient();
        //client.addParameter("appId", "");
        client.addParameter("text", text);
        client.addParameter("from", from);
        client.addParameter("to", to);
        client.addParameter("maxTranslations", "5");
        String urlString = TRANSLATE_URL + "?" + client.getRequestData();
        Log.v("Glossa", "GetTranslations URL: " + urlString);
        client.clearData();
        HttpURLConnection connection = HttpClient.createConnection(urlString);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", getAuthorization());
        return client.getResponseText(connection);
    }

    @Override
    protected Collection<String> doInBackground(String... params)
    {
        Collection<String> result = null;
        try
        {
            String xml = getTranslationXml(params[0], params[1], params[2]);
            result = parseTranslationXml(xml, params[2]);
        }
        catch (IOException e)
        {
            Log.e("Glossa", "Exception", e);
        }
        catch (JSONException e)
        {
            Log.e("Glossa", "Exception", e);
        }
        catch (XmlPullParserException e)
        {
            Log.e("Glossa", "Exception", e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(Collection<String> result)
    {
        adapter.clear();
        if (result != null)
        {
            adapter.addAll(result);
        }
        adapter.notifyDataSetChanged();
    }

    private Collection<String> parseTranslationXml(String xml, String to)
        throws IOException, XmlPullParserException
    {
        Locale toLocale = new Locale(to);
        Map<String, String> result = new LinkedHashMap<String, String>();
        XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
        parserFactory.setNamespaceAware(false);
        parserFactory.setValidating(false);
        XmlPullParser parser = parserFactory.newPullParser();
        parser.setInput(new StringReader(xml));
        String text = null;
        for (int event = parser.next(); event != XmlPullParser.END_DOCUMENT; event = parser.next())
        {
            switch (event)
            {
            case XmlPullParser.END_TAG:
                if ("TranslatedText".equals(parser.getName()))
                {
                    String key = text.replace(":", "").toLowerCase(toLocale);
                    result.put(key, text);
                }
                break;
            case XmlPullParser.TEXT:
                text = parser.getText();
                break;
            }
        }
        return result.values();
    }
}
