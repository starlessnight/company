package com.smartrek.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import android.util.Base64;

/**
 * Handles HTTP connections
 *
 */
public final class HTTP {
	
    public enum Method { GET, POST, PUT, DELETE }
    
	private static final int BUF_SIZE = 4096;
	
	/**
	 * Maximum content size that getResponseBody() can handle.
	 */
	public static final int MAX_CONTENT_SIZE = 8*1024*1024;
	
	private HttpURLConnection httpConn;
	
	private String username;
	
	private String password;
	
	private Method method = Method.GET;
	
	private Map<String, String> formData = Collections.emptyMap();
	
	public HTTP(String urlString) throws IOException {
		httpConn = openHttpConnection(urlString);
	}
	
	public HTTP setAuthorization(String username, String password){
	    this.username = username;
	    this.password = password;
	    return this;
	}
	
	public HTTP setMethod(Method m){
	    method = m;
	    return this;
	}
	
	public HTTP setFormData(Map<String, String> formData){
	    this.formData = formData;
	    return this;
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
			httpConn.setRequestMethod(method.name());
			httpConn.setConnectTimeout(15000);
			httpConn.setReadTimeout(15000);
			if(username != null && password != null){
    			String encoding = "iso-8859-1";
    			String authHeader = new String(Base64.encode((username + ':' + password).getBytes(encoding), 
    		        Base64.DEFAULT), encoding);
    			httpConn.setRequestProperty("Authorization", "Basic " + authHeader);
			}
			if(method != Method.GET){
			    httpConn.setDoInput (true);
			    httpConn.setDoOutput (true);
			    httpConn.setUseCaches (false);
			}
			if(formData != null && !formData.isEmpty()){
			    boolean first = true;
			    String content = "";
                for (Entry<String, String> data : formData.entrySet()) {
                    String key = data.getKey();
                    String val = data.getValue();
                    content += (first?"":"&") + key + "=" + URLEncoder.encode(val, "UTF-8");  
                    first = false;
                }
                httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                DataOutputStream output = null;
                try{
                    output = new DataOutputStream(httpConn.getOutputStream());
                    output.writeBytes(content);
                    output.flush();
                }finally{
                    IOUtils.closeQuietly(output);
                }
            }
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
