package com.mingle;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
							ParseUser currentUser = ParseUser.getCurrentUser();
							currentUser.put("facebookId", user.getId());
							currentUser.put("firstName", user.getFirstName());
							if(user.getProperty("gender") != null) {
								currentUser.put("gender", (String) user.getProperty("gender"));
							}
							if(user.getLocation().getProperty("name") != null) {
								currentUser.put("location", (String) user.getLocation().getProperty("name"));
							}
							if(user.getBirthday() != null) {
								currentUser.put("birthday", user.getBirthday());
							}
							currentUser.put("mingling", false);
							currentUser.saveInBackground();
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