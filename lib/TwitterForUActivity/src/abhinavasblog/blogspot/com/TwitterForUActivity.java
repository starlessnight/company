package abhinavasblog.blogspot.com;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.twitter.android.TwitterApp;
import com.twitter.android.TwitterApp.TwDialogListener;

/*
 Copyright [2010] [Abhinava Srivastava]

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class TwitterForUActivity extends Activity {

	private TwitterApp mTwitter;

	/** Called when the activity is first created. */
	private static final String CONSUMER_KEY = "GPjUBdt9yn3FYTO9RcW2w";// "GPjUBdt9yn3FYTO9RcW2w";
	private static final String CONSUMER_SECRET = "M0uxQG8r4f5ogfMDH0phU0FQvaYBOo2Bv6hP98xiag"; //"M0uxQG8r4f5ogfMDH0phU0FQvaYBOo2Bv6hP98xiag";

	private enum FROM {
		TWITTER_POST, TWITTER_LOGIN
	};

	private enum MESSAGE {
		SUCCESS, DUPLICATE, FAILED, CANCELLED
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mTwitter = new TwitterApp(this, CONSUMER_KEY, CONSUMER_SECRET);
	}

	/**
	 * Handling click events of button used to launch
	 * 
	 * @param v
	 */
	public void onClick(View v) {
		mTwitter.setListener(mTwLoginDialogListener);
		mTwitter.resetAccessToken();
		if (mTwitter.hasAccessToken() == true) {
			try {
				mTwitter.updateStatus(TwitterApp.MESSAGE);
				postAsToast(FROM.TWITTER_POST, MESSAGE.SUCCESS);
			} catch (Exception e) {
				if (e.getMessage().toString().contains("duplicate")) {
					postAsToast(FROM.TWITTER_POST, MESSAGE.DUPLICATE);
				}
				e.printStackTrace();
			}
			mTwitter.resetAccessToken();
		} else {
			mTwitter.authorize();
		}
	}

	/**
	 * Show toast messages
	 * 
	 * @param twitterPost
	 * @param success
	 */
	private void postAsToast(FROM twitterPost, MESSAGE success) {
		switch (twitterPost) {
		case TWITTER_LOGIN:
			switch (success) {
			case SUCCESS:
				Toast.makeText(this, "Login Successful", Toast.LENGTH_LONG)
						.show();
				break;
			case FAILED:
				Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show();
			default:
				break;
			}
			break;
		case TWITTER_POST:
			switch (success) {
			case SUCCESS:
				Toast.makeText(this, "Posted Successfully", Toast.LENGTH_LONG)
						.show();
				break;
			case FAILED:
				Toast.makeText(this, "Posting Failed", Toast.LENGTH_LONG)
						.show();
				break;
			case DUPLICATE:
				Toast.makeText(this,
						"Posting Failed because of duplicate message...",
						Toast.LENGTH_LONG).show();
			default:
				break;
			}
			break;
		}
	}

	/**
	 * Twitter Dialog Listner.
	 */
	private TwDialogListener mTwLoginDialogListener = new TwDialogListener() {

		@Override
		public void onError(String value) {
			postAsToast(FROM.TWITTER_LOGIN, MESSAGE.FAILED);
			Log.e("TWITTER", value);
			mTwitter.resetAccessToken();
		}

		@Override
		public void onComplete(String value) {
			try {
				mTwitter.updateStatus(TwitterApp.MESSAGE);
				postAsToast(FROM.TWITTER_POST, MESSAGE.SUCCESS);
			} catch (Exception e) {
				if (e.getMessage().toString().contains("duplicate")) {
					postAsToast(FROM.TWITTER_POST, MESSAGE.DUPLICATE);
				}
				e.printStackTrace();
			}
			mTwitter.resetAccessToken();
		}
	};
}