package com.mingle;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private Button startButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
						
		Session session = ParseFacebookUtils.getSession();
		if (session != null && session.isOpened()) {
			/* get the info we need */
			makeMeRequest();
		} else {
			// error. do something about it
		}
		
		/* setting up the start button with associated onClick */
		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onStartButtonClicked();
			}
		});
	}
	
	private void makeMeRequest() {
		Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if(user != null) {
							JSONObject userProfile = new JSONObject();
							try {
								/* Gets all the information that we need from Facebook and stores
								 * it into a ParseUser that we can access later. */
								userProfile.put("facebookId", user.getId());
								userProfile.put("firstName", user.getFirstName());
								if(user.getLocation().getProperty("name") != null) {
									userProfile.put("location", (String) user.getLocation().getProperty("name"));
								}
								if(user.getProperty("gender") != null) {
									userProfile.put("gender", (String) user.getProperty("gender"));
								}
								if(user.getBirthday() != null) {
									userProfile.put("birthday", user.getBirthday());
								}
								
								ParseUser currentUser = ParseUser.getCurrentUser();
								currentUser.put("facebookId", user.getId());
								currentUser.put("name", user.getName());
								currentUser.put("profile", userProfile);
								currentUser.saveInBackground();
								
							} catch (JSONException e) {
								Log.d(MingleApplication.TAG, "Error pasing returned user data.");
							}
						}
					}
				});
		request.executeAsync();
	}
		
	private void onStartButtonClicked() {
		/* Lets get mingling !! set the mingle flag to true to let all those 
		 * singles out there that we are in fact ready to mingle */
		ParseUser currentUser = ParseUser.getCurrentUser();
		if(currentUser != null) {
			currentUser.put("mingling", true);
			currentUser.saveInBackground();
		} else {
			// error 
		}
		startLocationActivity();
	}
	
	/* Start the LocationActivity. this is where we will try to match the users */
	private void startLocationActivity() {
		Intent intent = new Intent(this, LocationActivity.class);
		startActivity(intent);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			currentUser.put("mingling", false);
			currentUser.saveInBackground();
		}
	}
}