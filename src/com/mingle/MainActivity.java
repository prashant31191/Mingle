package com.mingle;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
								
		/* setting up the start button with associated onClick */
		((Button) findViewById(R.id.startButton))
			.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onStartButtonClicked();
			}
		});
		
		Session session = ParseFacebookUtils.getSession();
		if (session != null && session.isOpened()) {
			/* get the info we need */
			makeMeRequest();
		}
	}
	
	public void onResume() {
		super.onResume();
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			
		} else {
			/* User is not logged in, so show them the login screen */
			startLoginActivity();
		}
	}
	
	private void makeMeRequest() {
		Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if(user != null) {
							JSONObject userProfile = new JSONObject();
							try {
								userProfile.put("facebookId", user.getId());
								userProfile.put("firstName", user.getFirstName());
								if(user.getProperty("gender") != null) {
									userProfile.put("gender", (String) user.getProperty("gender"));
								}
								if(user.getLocation().getProperty("name") != null) {
									userProfile.put("location", (String) user.getLocation().getProperty("name"));
								}
								if(user.getBirthday() != null) {
									userProfile.put("birthday", user.getBirthday());
								}
								ParseUser currentUser = ParseUser.getCurrentUser();
								currentUser.put("profile", userProfile);
								currentUser.saveInBackground();		
							} catch (JSONException e) {
								// Error
							}
						} else if (response.getError() != null) {
							if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
									|| (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
								Log.d(MingleApplication.TAG, "Facebook session was invalidated.");
								onLogoutButtonClicked();
							} else {
								Log.d(MingleApplication.TAG, "" + response.getError().getErrorMessage());
							}
						}
					}
				});
		request.executeAsync();
	}
		
	private void onStartButtonClicked() {
		startConferenceActivity();
	}
	
	private void onLogoutButtonClicked() {
		ParseUser.logOut();
		startLoginActivity();	
	}
	
	/* Start the LocationActivity. this is where we will try to match the users */
	private void startConferenceActivity() {
		startActivity(new Intent(this, ConferenceActivity.class));
	}
	
	private void startLoginActivity() {
		startActivity(new Intent(this, LoginActivity.class));
	}
}