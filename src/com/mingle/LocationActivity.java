package com.mingle;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class LocationActivity extends Activity {

	private ParseUser mUser;
	private ParseObject mConference;
	private ParseQuery<ParseObject> matchQuery;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
	
		mUser = ParseUser.getCurrentUser();
		if(mUser != null) {
			findConference();
		}
	}

	private void findConference() {		
		/* We are querying the parse data base for other users.
		 * We add a constraint so you won't match with yourself.
		 * It finds other users in the background of the app */
		matchQuery = ParseQuery.getQuery("Conference");
		matchQuery.whereEqualTo("status", "waiting");
		matchQuery.whereNotEqualTo("user1", mUser);
		matchQuery.countInBackground(new CountCallback() {
			@Override
			public void done(int count, ParseException e) {
				if (count > 0) {
					int row = (int) Math.floor(Math.random()*count);
					matchQuery.setLimit(1);
					matchQuery.setSkip(row);
					matchQuery.findInBackground(new FindCallback<ParseObject>() {
						@Override
						public void done(List<ParseObject> confs, ParseException e) {
							if (confs.size() > 0) {
								lockConference();
							} else {
								createConference();
							}
						}
					});
				} else {
					createConference();
				}
			}
			
		});
	}
	
	private void lockConference() {
		mConference.increment("lock");
		mConference.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if(mConference.getInt("lock") <= 2) {
					mConference.put("user2", mUser);
					mConference.put("status", "mingling");
					mConference.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e) {
							startConference();
						}
					});
				} else {
					// could not join, we're just going to create a conference instead
					createConference();
				}
			}
		});
	}
	
	private void createConference() {
		mConference = new ParseObject("Conference");
		mConference.put("lock", 1);
		mConference.put("user1", mUser);
		mConference.put("status", "waiting");
		mConference.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				startConference();
			}
		});
	}
	
	private void startConference() {
		// Start the video conference
		String conferenceID = mConference.getObjectId();
		String firstName;
		JSONObject profile  = mUser.getJSONObject("profile");
		try {
			if (profile.getString("firstName") != null) {
				firstName = profile.getString("firstName");
			}
			/* Start the video conference using the object ID and first name 
			 * this is where Steven would take over I guess*/
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
