package smartrek.util;

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
    public static InputStream openHttpConnection(String urlString) throws IOException {
        InputStream in = null;
        int response = -1;
               
        URL url = new URL(urlString); 
        URLConnection conn = url.openConnection();
                 
        if (!(conn instanceof HttpURLConnection)) {
            throw new IOException("Not an HTTP connection");
        }
        
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();                 
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();                                 
            }                     
        }
        catch (Exception ex)
        {
            throw new IOException("Error connecting");            
        }
        return in;     
    }
    
    public static String downloadText(String URL) {
        int BUFFER_SIZE = 4096;
        InputStream in = null;
        try {
            in = openHttpConnection(URL);
        } catch (IOException e1) {
            e1.printStackTrace();
            return "";
        }
        
        if(in == null) {
        	return "";
        }

        InputStreamReader isr = new InputStreamReader(in);

        StringBuffer strBuf = new StringBuffer();
        char[] buf = new char[BUFFER_SIZE];
        
        try {
            while (isr.read(buf) > 0) {
                strBuf.append(buf);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }    
        return new String(strBuf);
    }
}
