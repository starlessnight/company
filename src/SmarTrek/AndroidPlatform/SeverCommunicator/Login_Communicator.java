package SmarTrek.AndroidPlatform.SeverCommunicator;

import org.json.JSONException;

import android.util.Log;

import SmarTrek.AndroidPlatform.Parsers.Parser;
import SmarTrek.AndroidPlatform.Utilities.User;

/****************************************************************************************************
 * 
 * 
 * @author timothyolivas
 *
 ****************************************************************************************************/
public class Login_Communicator extends Server_Communicator {
	
	private String name;
	private String pwd;
	
	public Login_Communicator(){
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
		String login_response = DownloadText(loginurl);
		Log.d("Login_Communicator", "Got Response from Server");
		Log.d("Login_Communicator", login_response);

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
		 return "/account/verify?username=" + name + "&password=" + pwd;
	}
}
