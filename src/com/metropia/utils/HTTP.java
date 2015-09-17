package com.metropia.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import android.util.Base64;

/**
 * Handles HTTP connections
 *
 */
public final class HTTP {
	
    public enum Method { GET, POST, PUT, DELETE, HEAD }
    
	private static final int BUF_SIZE = 4096;
	
	public static final int defaultTimeout = 60000;
	
	/**
	 * Maximum content size that getResponseBody() can handle.
	 */
	public static final int MAX_CONTENT_SIZE = 8*1024*1024;
	
	private HttpURLConnection httpConn;
	
	private String username;
	
	private String password;
	
	private Method method = Method.GET;
	
	private Map<String, String> formData = Collections.emptyMap();
	
	private JSONObject json;
	
	private byte[] jsonCompressed;
	
	private String ifNoneMatch;
	
	private int timeout = defaultTimeout;
	
	private String referer;
	
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
	
	public HTTP set(JSONObject json, boolean compress){
		if (!compress) this.json = json;
		else {
			String jsonString = json.toString();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
		    try {
		    	GZIPOutputStream gzos = new GZIPOutputStream(baos);
			    gzos.write(jsonString.getBytes("UTF-8"));
			    gzos.close();
			} catch (IOException e) {}
		    jsonCompressed = baos.toByteArray();
		}
	    return this;
	}
	
	public HTTP setIfNoneMatch(String ifNoneMatch){
	    this.ifNoneMatch = ifNoneMatch;
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
			httpConn.setConnectTimeout(timeout);
			httpConn.setReadTimeout(timeout);
			if(username != null && password != null){
    			String encoding = "iso-8859-1";
    			String authHeader = new String(Base64.encode((username + ':' + password).getBytes(encoding), 
    		        Base64.NO_WRAP), encoding);
    			httpConn.setRequestProperty("Authorization", "Basic " + authHeader);
			}
			if(method != Method.GET && method != Method.DELETE){
			    httpConn.setDoInput (true);
			    httpConn.setDoOutput (true);
			    httpConn.setUseCaches (false);
			}
			boolean hasJSON = json != null;
			if(hasJSON || jsonCompressed!=null || formData != null && !formData.isEmpty()){
			    String content = null;
			    String contentType;
			    if(hasJSON){
			        contentType = "application/json";
			        content = json.toString();
			    }
			    else if (jsonCompressed!=null) {
			    	contentType = "application/json";
			    	httpConn.addRequestProperty("Accept-Encoding", "gz");
			    	
			    }else{
			        contentType = "application/x-www-form-urlencoded";
			        content = "";
			        boolean first = true;
	                for (Entry<String, String> data : formData.entrySet()) {
	                    String key = data.getKey();
	                    String val = data.getValue();
	                    content += (first?"":"&") + key + "=" + URLEncoder.encode(val, "UTF-8");  
	                    first = false;
	                }
			    }
			    httpConn.setRequestProperty("Content-Type", contentType);
                DataOutputStream output = null;
                try{
                    output = new DataOutputStream(httpConn.getOutputStream());
                    if (content!=null) output.writeBytes(content);
                    else {
                    	output.write(jsonCompressed);
                    }
                    output.flush();
                }finally{
                    IOUtils.closeQuietly(output);
                }
            }
			if(StringUtils.isNotEmpty(ifNoneMatch)){
			    httpConn.setRequestProperty("If-None-Match", ifNoneMatch);
			}
			if(StringUtils.isNotEmpty(referer)){
			    httpConn.setRequestProperty("Referer", referer);
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
	
	public String getETag(){
	    return getHeaderField("ETag");
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
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
        
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }
        catch (NoSuchAlgorithmException e) {
        }
        catch (KeyManagementException e) {
        }

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    
        URL url = new URL(urlString); 
        URLConnection conn = url.openConnection();
                 
        if (!(conn instanceof HttpURLConnection)) {
            throw new IOException("Not an HTTP connection");
        }
        
        return (HttpURLConnection) conn;     
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
        if(httpConn != null) {
            httpConn.setConnectTimeout(timeout);
            httpConn.setReadTimeout(timeout);
        }
    }

    public void setReferer(String referer) {
        this.referer = referer;
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
