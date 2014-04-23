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
		
		Runnable run = new Runnable() {
			@Override
			public void run() {
				if (mParseUsers.size() < 1) {
					findUser();
					handler.postDelayed(this, 5000);
				}
			}
		};
		
		handler.post(run);
	}
	
	private void findUser() {
		ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("Group");
		ArrayList<String> id = new ArrayList<String>();
		id.add(mUser.getObjectId());
		groupQuery.whereContainsAll("users", id);
		
		groupQuery.countInBackground(new CountCallback() {
			public void done(int count, ParseException e) {
				if (e == null) {
					Log.d("count = " + count, "count = " + count);
				} else {
					
				}
			}
		});
	}
	
	
}
