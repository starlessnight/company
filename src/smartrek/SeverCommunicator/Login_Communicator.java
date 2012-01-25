package smartrek.SeverCommunicator;

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
public class Login_Communicator extends ServerCommunicator {
	
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
		String login_response = downloadText(loginurl);
		Log.d("Login_Communicator", "Got Response from Server");
		Log.d("Login_Communicator", login_response);

		User user = null;
		try {
			user = Parser.parse_User(name, login_response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// FIXME: This is for bypassing the server side error
		user = new User(123456, "bypass");
		
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
