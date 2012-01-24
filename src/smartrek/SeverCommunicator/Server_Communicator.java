package smartrek.SeverCommunicator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;

/******************************************************************************************************************
 * 
 *
 ******************************************************************************************************************/
public abstract class Server_Communicator {

	protected String sturl;
	
	protected abstract String appendToUrl();
	
	/******************************************************************************************************************
	 * 
	 *
	 ******************************************************************************************************************/
	public Server_Communicator(){
		this.sturl = "http://www.api.smartrekmobile.com";
	}
	
	/******************************************************************************************************************
	 * 
	 *
	 ******************************************************************************************************************/
    protected InputStream OpenHttpConnection(String urlString) throws IOException {
        InputStream in = null;
        int response = -1;
               
        URL url = new URL(urlString); 
        URLConnection conn = url.openConnection();
                 
        if (!(conn instanceof HttpURLConnection))                     
            throw new IOException("Not an HTTP connection");        
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
	
	/******************************************************************************************************************
	 * 
	 *
	 ******************************************************************************************************************/
    public String DownloadText(String URL)
    {
        int BUFFER_SIZE = 2000;
        InputStream in = null;
        try {
            in = OpenHttpConnection(URL);
        } catch (IOException e1) {
            e1.printStackTrace();
            return "";
        }
        Log.d("ServerCommunicator","Attempting to read response");
        InputStreamReader isr = new InputStreamReader(in);
        int charRead;
        String str = "";
        char[] inputBuffer = new char[BUFFER_SIZE];          
        try {
            while ((charRead = isr.read(inputBuffer))>0)
            {                    
                //---convert the chars to a String---
                String readString = 
                    String.copyValueOf(inputBuffer, 0, charRead);                    
                str += readString;
                inputBuffer = new char[BUFFER_SIZE];
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }    
        return str;        
    }
}
//http://www.api.smartrekmobile.com/route.json?
//origin=920%20East%20Playa%20Del%20Norte%20Drive%20%20,Tempe,%20AZ,%2085281%20
//&destination=920%20East%20Playa%20Del%20Norte%20Drive%20%20,Tempe,%20AZ,%2085281%20
//&time_slot=%2018:35
