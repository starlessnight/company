package SmarTrek.AndroidPlatform.DataBaseFunctions;

import java.io.ByteArrayOutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

/****************************************************************************************************************
 * 
 *
 *
 ****************************************************************************************************************/ 
public class PicuteDataBase {

	private final static String TABLE_NAME = "COUPON_PICTURES";
	private final static String DB_NAME = "pics";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_NAME = "picture_url";
	public static final String KEY_DETAILS = "content";
	private static final int DATABASE_VERSION = 1;
	
	
	private static final String DATABASE_CREATE  = "create table " + TABLE_NAME + " ("
	+ KEY_ROWID + " integer primary key autoincrement, "
	+ KEY_NAME + " text not null, "
	+ KEY_DETAILS + " BLOB);";
	
//	private Context context;
	private DatabaseHelper DBHelper;
    private SQLiteDatabase db;
	
	/****************************************************************************************************************
	 * 
	 *
	 *
	 ****************************************************************************************************************/ 
	public PicuteDataBase(Context context) {
    //	this.context = context;
    	DBHelper = new DatabaseHelper(context);
    	
	}
	
	/*==============================================================================================================*/
	/****************************************************************************************************************
	 * 
	 *
	 *
	 ****************************************************************************************************************/ 
	private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
        	try {
        		db.execSQL(DATABASE_CREATE);
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        	
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            // Not used 
        }
		
	}
	/*========================================================================================================*/
	
    //---insert a Picture into the database---
    public long insertPicture(String url, Bitmap image){
    	
        ContentValues initialValues = new ContentValues();
      //  url = removeAllSpecialChars(url);
        initialValues.put(KEY_NAME, url);
        if(image != null) {
        	ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
        	image.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos); 
        	byte[] bitmapdata = bos.toByteArray();
        	initialValues.put(KEY_DETAILS, bitmapdata);
        }

        long spot = db.insert(TABLE_NAME, null, initialValues);
        Log.d("PictureDataBase","Inserted Picture into spot " + spot);
        return spot;
    }
    
    
	
	/****************************************************************************************************************
	 * 
	 *opens the database
	 *
	 ****************************************************************************************************************/ 
    public PicuteDataBase open() throws SQLException{
        db = DBHelper.getWritableDatabase();
       // db.delete(TABLE_NAME, null, null);
        return this;
    }

	/****************************************************************************************************************
	 * 
	 *closes the database
	 *
	 ****************************************************************************************************************/
    public void close() {
        DBHelper.close();
    }
    
    public Cursor getPictue(String url) throws SQLException 
    {
    	Log.d("PictureDataBase",url);
    
    	 Cursor mCursor = null;

    	//url = removeAllSpecialChars(url);
        
    	mCursor = db.query(TABLE_NAME, null, null, null, null, null, null);

    	Log.d("PictureDataBase","Got cursor with " + mCursor.getCount() + " rows");
    	
        if (mCursor != null) {
        	mCursor.moveToFirst();
        	while(!mCursor.isAfterLast()){
        		Log.d("PictureDataBase",mCursor.getString(1));
        		if(mCursor.getString(1).equals(url)){
        			return mCursor;
        		}
        		mCursor.moveToNext();
        	}
        }
        return null;
    }
}
