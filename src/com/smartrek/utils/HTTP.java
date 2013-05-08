package com.smartrek.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Handles HTTP connections
 *
 */
public final class HTTP {
	
	private static final int BUF_SIZE = 4096;
	
	/**
	 * Maximum content size that getResponseBody() can handle.
	 */
	public static final int MAX_CONTENT_SIZE = 8*1024*1024;
	
	private HttpURLConnection httpConn;
	
	public HTTP(String urlString) throws IOException {
		httpConn = openHttpConnection(urlString);
	}
	
	/**
	 * Establishes an HTTP connection to a server.
	 * 
	 * @throws IOException
	 */
	public void connect() throws IOException {
		if(httpConn != null) {
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.setConnectTimeout(15000);
			httpConn.setReadTimeout(15000);
			httpConn.connect();
		}
	}
	
	/**
	 * Retrieves an HTTP header. This must be called after connect() is called.
	 * @param key
	 * @return
	 */
	public String getHeaderField(String key) {
		if (httpConn != null) {
			return httpConn.getHeaderField(key);
		}
		return null;
	}
	
	/**
	 * Returns an HTTP response code
	 * 
	 * @return An HTTP response code
	 */
	public int getResponseCode() {
		if(httpConn != null) {
			try {
				return httpConn.getResponseCode();
			}
			catch (IOException e) {
				return -1;
			}
		}
		else {
			return -1;
		}
	}
	
	/**
	 * If Content-Length is larger than MAX_CONTENT_SIZE, use getInputStream().
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getResponseBody() throws IOException {
		if(httpConn != null) {
			InputStream in = null;
			if(getResponseCode() >= 400) {
				in = httpConn.getErrorStream();
			}
			else {
				in = httpConn.getInputStream();
			}
			InputStreamReader isr = new InputStreamReader(in);
			
			StringBuffer strBuf = new StringBuffer();
			char[] buf = new char[BUF_SIZE];
			
        	int read = 0;
            while ((read = isr.read(buf)) > 0) {
                strBuf.append(buf,  0, read);
            }
            in.close();
            
	        return new String(strBuf);
		}
		else {
			return null;
		}
	}
	
	public InputStream getInputStream() throws IOException {
		return httpConn.getInputStream();
	}
	
	public static HttpURLConnection openHttpConnection(String urlString) throws IOException {
        URL url = new URL(urlString); 
        URLConnection conn = url.openConnection();
                 
        if (!(conn instanceof HttpURLConnection)) {
            throw new IOException("Not an HTTP connection");
        }
        
        return (HttpURLConnection) conn;     
    }
	
//    public static InputStream openHttpConnection(String urlString) throws IOException {
//        InputStream in = null;
//        int response = -1;
//               
//        URL url = new URL(urlString); 
//        URLConnection conn = url.openConnection();
//                 
//        if (!(conn instanceof HttpURLConnection)) {
//            throw new IOException("Not an HTTP connection");
//        }
//        
//        try{
//            HttpURLConnection httpConn = (HttpURLConnection) conn;
//            httpConn.setAllowUserInteraction(false);
//            httpConn.setInstanceFollowRedirects(true);
//            httpConn.setRequestMethod("GET");
//            httpConn.connect();
//            response = httpConn.getResponseCode();                 
//            if (response == HttpURLConnection.HTTP_OK) {
//                in = httpConn.getInputStream();                                 
//            }                     
//        }
//        catch (Exception ex)
//        {
//            throw new IOException("Error connecting");            
//        }
//        return in;     
//    }
}
