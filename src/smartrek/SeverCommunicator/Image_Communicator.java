package smartrek.SeverCommunicator;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Image_Communicator extends Server_Communicator {

	
	public Image_Communicator(){
		super();
	}
	
	@Override
	protected String appendToUrl() {
		// TODO Auto-generated method stub
		return null;
	}

    public Bitmap DownloadImage(String URL)
    {        
        Bitmap bitmap = null;
        InputStream in = null;        
        try {
            in = OpenHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return bitmap;                
    }
}
