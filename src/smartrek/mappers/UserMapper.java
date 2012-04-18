package smartrek.mappers;

import org.json.JSONException;

import smartrek.models.User;
import smartrek.parsers.Parser;
import android.util.Log;


/****************************************************************************************************
 * 
 * 
 * @author timothyolivas
 *
 ****************************************************************************************************/
public class UserMapper extends Mapper {
	
	// values are database field names
	public static final String UID = "UID";
	public static final String USERNAME = "USERNAME";
	public static final String FIRSTNAME = "FIRSTNAME";
	public static final String LASTNAME = "LASTNAME";
	public static final String EMAIL = "EMAIL";
	
	private String name;
	private String pwd;
	
	public UserMapper() {
		super();
	}
	
	/**
	 * @throws JSONException **************************************************************************************************
	 * 
	 *
	 ****************************************************************************************************/	
	public User login(String name, String pwd) {
		this.name = name;
		this.pwd = pwd;
		String loginurl = sturl + appendToUrl();

		Log.d("Login_Communicator", "Querrying Sever with");
		Log.d("Login_Communicator", loginurl);
		String login_response = downloadText(loginurl);
		Log.d("Login_Communicator", "Got Response from Server");
		Log.d("Login_Communicator", "response = " + login_response);

		User user = null;
		try {
			user = Parser.parse_User(name, login_response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return user;
	}
	
	/****************************************************************************************************
	 * 
	 *
	 ****************************************************************************************************/
	protected String appendToUrl() {
		 //return "/account/verify?username=" + name + "&password=" + pwd;
		return String.format("/account/%s%%20%s", name, pwd);
	}
}
