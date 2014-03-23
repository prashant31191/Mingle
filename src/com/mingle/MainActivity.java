package com.mingle;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

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

		Session session = ParseFacebookUtils.getSession();
		if(session != null && session.isOpened()) {
			makeMeRequest();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		if(currentUser != null) {
			
		} else {
			startLoginActivity();
		}
	}
	
	private void makeMeRequest() {
		Request request = Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser user, Response response) {
				if (user != null) {
					JSONObject userProfile = new JSONObject();
					
					try {
						userProfile.put("facebookId", user.getId());
						userProfile.put("name", user.getName());
						if (user.getLocation().getProperty("name") != null) {
							userProfile.put("location", (String) user.getLocation().getProperty("name"));
						}
						if (user.getProperty("gender") != null) {
							userProfile.put("gender",
									(String) user.getProperty("gender"));
						}
						if (user.getBirthday() != null) {
							userProfile.put("birthday",
									user.getBirthday());
						}
						if (user.getProperty("relationship_status") != null) {
							userProfile
									.put("relationship_status",
											(String) user
													.getProperty("relationship_status"));
						}

						// Save the user profile info in a user property
						ParseUser currentUser = ParseUser
								.getCurrentUser();
						currentUser.put("profile", userProfile);
						currentUser.saveInBackground();
						
						
					} catch (JSONException e) {
						Log.d(MingleApplication.TAG,
								"Error parsing returned user data.");
					}
					
				} else if(response.getError() != null) {
					
				}
			}
		});
		request.executeAsync();
	}
	
	private void startLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}