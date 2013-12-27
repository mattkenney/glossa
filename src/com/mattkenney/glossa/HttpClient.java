package com.mattkenney.glossa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import android.util.Log;

public class HttpClient
{
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 20000;
    private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private final StringBuilder data = new StringBuilder();
    private Charset encoding = Charset.forName("UTF-8");

    public HttpClient()
    {
    }

    public HttpClient addData(String part)
    {
        data.append(part);
        return this;
    }

    public HttpClient addParameter(String key, String value)
        throws IOException
    {
        if (data.length() > 0)
        {
            data.append('&');
        }
        data.append(URLEncoder.encode(key, "utf-8"));
        data.append('=');
        data.append(URLEncoder.encode(value, "utf-8"));
        return this;
    }

    public HttpClient clearData()
    {
        data.setLength(0);
        return this;
    }

    public static HttpURLConnection createConnection(String urlString)
        throws IOException
    {
        URL connUrl = new URL(urlString);
        URLConnection connection = connUrl.openConnection();
        if (!(connection instanceof HttpURLConnection))
        {
            throw new IOException("Unsupported protocol: " + urlString);
        }
        HttpURLConnection result = (HttpURLConnection) connection;
        result.setAllowUserInteraction(false);
        result.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
        result.setDoInput(true);
        result.setInstanceFollowRedirects(true);
        result.setReadTimeout(DEFAULT_READ_TIMEOUT);
        result.setUseCaches(true);
        return result;
    }

    public Charset getEncoding()
    {
        return encoding;
    }

    public String getRequestData()
    {
        return data.toString();
    }

    public String getResponseText(HttpURLConnection connection)
        throws IOException
    {
        InputStream connIn = null;
        OutputStream connOut = null;
        StringBuilder buffer = new StringBuilder();

        try
        {
            if (data.length() > 0)
            {
                if (connection.getRequestProperty("Content-Type") == null)
                {
                    connection.setRequestProperty("Content-Type", DEFAULT_CONTENT_TYPE);
                }

                connection.setDoOutput(true);

                connOut = connection.getOutputStream();
                connOut.write(data.toString().getBytes(encoding));
                connOut.flush();
                connOut.close();
            }

            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK)
            {
                Log.d("Glossa", buffer.toString());
                throw new IOException("Server response: " + code);
            }

            connIn = connection.getInputStream();
            Reader rawReader = new InputStreamReader(connIn, encoding);
            BufferedReader bufReader = new BufferedReader(rawReader);

            for (String line = bufReader.readLine(); line != null; line = bufReader.readLine())
            {
                buffer.append(line);
                buffer.append('\n');
            }
        }
        finally
        {
            if (connOut != null)
            {
                try
                {
                    connOut.close();
                }
                catch (Exception e)
                {
                    Log.w("Glossa", "Exception closing OutputStream", e);
                }
            }
            if (connIn != null)
            {
                try
                {
                    connIn.close();
                }
                catch (Exception e)
                {
                    Log.w("Glossa", "Exception closing InputStream", e);
                }
            }
            if (connection != null)
            {
                connection.disconnect();
            }
        }

        return buffer.toString();
    }

    public HttpClient setEncoding(Charset encoding)
    {
        this.encoding = encoding;
        return this;
    }
}
