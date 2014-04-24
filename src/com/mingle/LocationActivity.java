package com.mingle;

import java.util.ArrayList;
import java.util.List;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class LocationActivity extends Activity {

	private ArrayList<String> mParseUsers = new ArrayList<String>();
	private ParseUser mUser;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		
		mUser = ParseUser.getCurrentUser();
		pollUser();
	}
	
	private void pollUser() {
		final Handler handler = new Handler();
		
		/* Runs in the background until we find a match */
		Runnable run = new Runnable() {
			@Override
			public void run() {
				if (mParseUsers.size() < 1) {
					findUser();
					handler.postDelayed(this, 5000);
				} else {
					mUser.put("mingling", false);
				}
			}
		};
		
		handler.post(run);
	}
	
	private void findUser() {
		/* We are querying the parse data base for other users.
		 * We add a constraint so you won't match with yourself.
		 * It finds other users in the background of the app */
		ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("_User");
		groupQuery.whereNotEqualTo("objectId", mUser.getObjectId());
		groupQuery.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> list, ParseException e) {
				if(e == null) {
					/* This is just debugging stuff stuff, checking to see that shit works */
					Log.d("list size = "+list.size(), "list size = "+list.size());
					if(list.size() != 0) {						
						Log.d("" + list.get(0).getObjectId(), "" + list.get(0).getObjectId());
					}	
				} else {
					e.printStackTrace();
				}
			}	
		});
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
